package com.polopoly.pcmd.tool;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.cm.xml.util.export.DefaultContentContentsExporter;
import com.polopoly.pcmd.field.content.AbstractContentIdField;
import com.polopoly.pcmd.tool.parameters.ExportParameters;
import com.polopoly.pcmd.tool.parameters.ListExportableParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.util.Plural;
import example.deploy.xml.export.ContentsExporterFactory;
import example.deploy.xml.export.NormalizedFileExporter;
import example.deploy.xml.export.contentlistentry.ContentIdFilterToContentReferenceFilterWrapper;
import example.deploy.xml.export.filteredcontent.OrContentIdFilter;
import example.deploy.xml.export.filteredcontent.PresentContentFilter;
import example.deploy.xml.export.filteredcontent.ProjectContentFilterFactory;
import example.deploy.xml.export.filteredcontent.RejectionCollectingContentIdFilter;
import example.deploy.xml.normalize.DefaultNormalizationNamingStrategy;
import example.deploy.xml.present.PresentFileReader;

public class ExportTool extends ListExportableTool {

    private ExportParameters parameters;

    @Override
    public ExportParameters createParameters() {
        return new ExportParameters();
    }

    @Override
    public void execute(PolopolyContext context, ListExportableParameters listParameters) {
        parameters = (ExportParameters) listParameters;

        File outputDirectory = parameters.getOutputDirectory();

        System.err.println("Scanning project content...");

        ContentIdFilter existingObjectsFilter =
            new ProjectContentFilterFactory(context).getExistingObjectsFilter(listParameters.getProjectContentDirectory());

        PresentContentFilter alreadyExportedObjectsFilter =
            createAlreadyExportedObjectsFilter(context, outputDirectory);

        ContentIdFilter existingOrExportedObjectsFilter =
            or(existingObjectsFilter, alreadyExportedObjectsFilter);

        RejectionCollectingContentIdFilter collectingExistingOrExportedObjectsFilter =
            new RejectionCollectingContentIdFilter(existingOrExportedObjectsFilter);

        ContentsExporterFactory contentsExporterFactory =
            new ContentsExporterFactory(context,
                new ContentIdFilterToContentReferenceFilterWrapper(
                    collectingExistingOrExportedObjectsFilter));

        Set<ContentId> contentIdsToExport = new HashSet<ContentId>(250);
        int since = parameters.getSince();

        for (ContentId idToExport : getContentIds(context, since, existingObjectsFilter)) {
            contentIdsToExport.add(idToExport);
        }

        DefaultContentContentsExporter exporter =
            contentsExporterFactory.createContentsExporter(contentIdsToExport);

        DefaultNormalizationNamingStrategy namingStrategy =
            new DefaultNormalizationNamingStrategy(outputDirectory);

        NormalizedFileExporter normalizedFileExporter =
            new NormalizedFileExporter(context, exporter, namingStrategy);

        normalizedFileExporter.setExternalIdGenerator(
            contentsExporterFactory.getExternalIdGenerator());

        normalizedFileExporter.export(contentIdsToExport);

        Set<ContentId> rejected = collectingExistingOrExportedObjectsFilter.getRejectedIds();

        if (!rejected.isEmpty()) {
            logRejected(rejected, context);
        }
    }

    private void logRejected(Set<ContentId> rejected, PolopolyContext context) {
        System.out.println("The exported content had references to " + Plural.count(rejected, "content object") +
            " not part either of the exported data or of project content. These references were excluded. ");

        if (rejected.size() > 50) {
            System.out.print("Some of the referenced objects were: ");

            rejected = collect(rejected, 50);
        }
        else {
            System.out.print("The referenced objects were: ");
        }

        for (ContentId contentId : rejected) {
            System.out.println(AbstractContentIdField.get(contentId, context));
        }
    }

    private <T> Set<T> collect(Set<T> set, int count) {
        HashSet<T> result = new HashSet<T>(count);

        for (T object : set) {
            result.add(object);

            if (result.size() >= count) {
                break;
            }
        }

        return result;
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

    private PresentContentFilter createAlreadyExportedObjectsFilter(
            PolopolyContext context, File outputDirectory) {
        PresentContentFilter alreadyExportedObjectsFilter = new PresentContentFilter(context);

        System.err.println("Scanning output directory for existing content...");
        new PresentFileReader(outputDirectory, alreadyExportedObjectsFilter).readAndScanContent();

        return alreadyExportedObjectsFilter;
    }

    @Override
    public Iterable<ContentId> getContentIds(PolopolyContext context, int since, ContentIdFilter excludeFilter) {
        if (parameters.isIdsArguments()) {
            return parameters.getIdArgumentsIterator(excludeFilter);
        }
        else {
            return super.getContentIds(context, since, excludeFilter);
        }
    }

    @Override
    public String getHelp() {
        return "Exports content as XML";
    }

}
