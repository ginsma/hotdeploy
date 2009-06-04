package com.polopoly.pcmd.tool;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.cm.xml.util.export.DefaultContentContentsExporter;
import com.polopoly.pcmd.tool.export.AcceptanceCollectingContentIdFilter;
import com.polopoly.pcmd.tool.export.RejectionCollectingContentIdFilter;
import com.polopoly.pcmd.tool.parameters.ExportParameters;
import com.polopoly.pcmd.tool.parameters.ListExportableParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.util.Plural;
import example.deploy.xml.export.ContentsExporterFactory;
import example.deploy.xml.export.NormalizedFileExporter;
import example.deploy.xml.export.contentlistentry.ContentIdFilterToContentReferenceFilterWrapper;
import example.deploy.xml.export.contentlistentry.ContentReferenceFilter;
import example.deploy.xml.export.filteredcontent.AcceptNoneContentIdFilter;
import example.deploy.xml.export.filteredcontent.OrContentIdFilter;
import example.deploy.xml.export.filteredcontent.PresentContentFilter;
import example.deploy.xml.export.filteredcontent.ProjectContentFilterFactory;
import example.deploy.xml.normalize.DefaultNormalizationNamingStrategy;
import example.deploy.xml.present.PresentFileReader;

public class ExportTool extends ListExportableTool {
    private PolopolyContext context;
    private ExportParameters parameters;

    @Override
    public ExportParameters createParameters() {
        return new ExportParameters();
    }

    @Override
    public void execute(PolopolyContext context, ListExportableParameters listParameters) {
        parameters = (ExportParameters) listParameters;
        this.context = context;

        File outputDirectory = parameters.getOutputDirectory();

        System.err.println("Scanning project content...");

        ContentIdFilter existingObjectsFilter =
            createExistingObjectsFilter(listParameters);

        PresentContentFilter alreadyExportedObjectsFilter =
            createAlreadyExportedObjectsFilter(outputDirectory);

        ContentIdFilter existingOrExportedObjectsFilter =
            or(existingObjectsFilter, alreadyExportedObjectsFilter);

        Set<ContentId> contentIdsToExport = getIdsToExport(existingObjectsFilter);

        RejectionCollectingContentIdFilter collectingExistingOrExportedObjectsFilter =
            new RejectionCollectingContentIdFilter(existingOrExportedObjectsFilter);

        NormalizedFileExporter normalizedFileExporter =
            createExporter(
                outputDirectory, contentIdsToExport,
                new ContentIdFilterToContentReferenceFilterWrapper(
                    collectingExistingOrExportedObjectsFilter));

        normalizedFileExporter.export(contentIdsToExport);

        logRejected(collectingExistingOrExportedObjectsFilter);
    }

    private NormalizedFileExporter createExporter(File outputDirectory,
            Set<ContentId> contentIdsToExport,
            ContentReferenceFilter contentReferenceFilter) {
        ContentsExporterFactory contentsExporterFactory =
            new ContentsExporterFactory(
                context.getPolicyCMServer(),
                context.getUserServer(),
                contentReferenceFilter);

        DefaultContentContentsExporter exporter =
            contentsExporterFactory.createContentsExporter(contentIdsToExport);

        DefaultNormalizationNamingStrategy namingStrategy =
            new DefaultNormalizationNamingStrategy(outputDirectory);

        NormalizedFileExporter normalizedFileExporter =
            new NormalizedFileExporter(context.getPolicyCMServer(), exporter, namingStrategy);

        normalizedFileExporter.setExternalIdGenerator(
            contentsExporterFactory.getExternalIdGenerator());

        return normalizedFileExporter;
    }

    private ContentIdFilter createExistingObjectsFilter(
            ListExportableParameters listParameters) {
        return new ProjectContentFilterFactory(context.getPolicyCMServer()).
            getExistingObjectsFilter(listParameters.getProjectContentDirectory());
    }

    private PresentContentFilter createAlreadyExportedObjectsFilter(File outputDirectory) {
        PresentContentFilter alreadyExportedObjectsFilter =
            new PresentContentFilter(context.getPolicyCMServer());

        System.err.println("Scanning output directory for existing content...");
        new PresentFileReader(outputDirectory, alreadyExportedObjectsFilter).readAndScanContent();

        return alreadyExportedObjectsFilter;
    }

    private void logRejected(RejectionCollectingContentIdFilter collectingFilter) {
        Set<ContentId> rejected = collectingFilter.getCollectedIds();

        if (rejected.isEmpty()) {
            return;
        }

        System.out.println("The exported content had references to " + Plural.count(rejected, "content object") +
            " not part either of the exported data or of project content. These references were excluded. ");

        collectingFilter.printCollectedObjects(context);
    }

    @Override
    protected void logNotExportedBecausePresent(PolopolyContext context,
            AcceptanceCollectingContentIdFilter collectingFilter) {
        Set<ContentId> notExportedBecausePresent = collectingFilter.getCollectedIds();

        if (notExportedBecausePresent.isEmpty()) {
            return;
        }

        System.err.println(Plural.count(notExportedBecausePresent, "objects") + " were not exported because they are " +
                "part of the project data or Polopoly content. Use --" + ExportParameters.EXPORT_PRESENT_OPTION + " to export these too.");
        collectingFilter.printCollectedObjects(context);
    }

    private ContentIdFilter or(ContentIdFilter filter,
            PresentContentFilter presentContentFilter) {
        if (presentContentFilter.getPresentIds().isEmpty()) {
            return filter;
        }
        else {
            return new OrContentIdFilter(
                    filter,
                    presentContentFilter);
        }
    }

    private Set<ContentId> getIdsToExport(ContentIdFilter presentContentFilter) {
        int since = parameters.getSince();
        Iterable<ContentId> idIterable;

        ContentIdFilter excludeFilter =
            parameters.isExportPresent() ? new AcceptNoneContentIdFilter() : presentContentFilter;

        if (parameters.isIdsArguments()) {
            idIterable = parameters.getIdArgumentsIterator(excludeFilter, context);
        }
        else {
            idIterable = super.getIdsToExport(context, since, excludeFilter);
        }

        Set<ContentId> contentIdsToExport = new HashSet<ContentId>(250);

        for (ContentId idToExport : idIterable) {
            contentIdsToExport.add(idToExport);
        }

        return contentIdsToExport;
    }

    @Override
    public String getHelp() {
        return "Exports content as XML";
    }

}
