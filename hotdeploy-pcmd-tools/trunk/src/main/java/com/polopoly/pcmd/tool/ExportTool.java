package com.polopoly.pcmd.tool;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.cm.xml.util.export.DefaultContentContentsExporter;
import com.polopoly.pcmd.tool.parameters.ExportParameters;
import com.polopoly.pcmd.tool.parameters.ListExportableParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.xml.export.ContentsExporterFactory;
import example.deploy.xml.export.NormalizedFileExporter;
import example.deploy.xml.export.contentlistentry.ContentIdFilterToContentReferenceFilterWrapper;
import example.deploy.xml.export.filteredcontent.OrContentIdFilter;
import example.deploy.xml.export.filteredcontent.PresentContentFilter;
import example.deploy.xml.normalize.DefaultNormalizationNamingStrategy;

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

        ContentIdFilter existingObjectsFilter =
            getExistingObjectsFilter(context, listParameters);

        PresentContentFilter alreadyExportedObjectsFilter =
            createAlreadyExportedObjectsFilter(context, outputDirectory);

        ContentIdFilter existingOrExportedObjectsFilter =
            or(existingObjectsFilter, alreadyExportedObjectsFilter);

        ContentsExporterFactory contentsExporterFactory =
            new ContentsExporterFactory(context,
                new ContentIdFilterToContentReferenceFilterWrapper(
                    existingOrExportedObjectsFilter));

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

        readPresentFilesFromDirectory(outputDirectory,
            "already exported files", alreadyExportedObjectsFilter);

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
