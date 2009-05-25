package com.polopoly.pcmd.tool;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.search.db.Version;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.pcmd.field.content.AbstractContentIdField;
import com.polopoly.pcmd.tool.parameters.FilesToDeployParameters;
import com.polopoly.pcmd.tool.parameters.ListExportableParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.Plural;
import example.deploy.xml.export.filteredcontent.ExcludeMetadataVersionFilter;
import example.deploy.xml.export.filteredcontent.FilteredAllContentFinder;
import example.deploy.xml.export.filteredcontent.HotdeployStatusFilter;
import example.deploy.xml.export.filteredcontent.InputTemplateFilter;
import example.deploy.xml.export.filteredcontent.MajorFilter;
import example.deploy.xml.export.filteredcontent.NegatingContentIdFilter;
import example.deploy.xml.export.filteredcontent.OrContentIdFilter;
import example.deploy.xml.export.filteredcontent.PresentContentFilter;
import example.deploy.xml.export.filteredcontent.SecurityRootDepartmentFilter;
import example.deploy.xml.parser.ContentXmlParser;
import example.deploy.xml.present.PresentFileReader;
import example.deploy.xml.present.PresentFilesAware;
import example.deploy.xml.present.PresentFilesAwareToParseCallbackAdapter;

public class ListExportableTool implements Tool<ListExportableParameters> {
    public ListExportableParameters createParameters() {
        return new ListExportableParameters();
    }

    public void execute(PolopolyContext context,
            ListExportableParameters parameters) {
        ContentIdFilter existingObjectsFilter = getExistingObjectsFilter(context, parameters);

        int since = parameters.getSince();

        for (ContentId contentId : getContentIds(context, since, existingObjectsFilter)) {
            System.out.println(AbstractContentIdField.get(contentId, context));
        }
    }

    public Iterable<ContentId> getContentIds(PolopolyContext context,
            int since, ContentIdFilter excludeFilter) {
        FilteredAllContentFinder finder = new FilteredAllContentFinder(context);

        if (since > 0) {
            System.out.println("Scanning content created since version " + since + " (" +
                DateFormat.getDateTimeInstance().format(new Date(since)) + ")");

            finder.addSearchExpression(new Version(since, Version.GREATER_THAN_OR_EQ));
        }

        finder.addFilter(new ExcludeMetadataVersionFilter());
        finder.addFilter(
            new NegatingContentIdFilter(
                new OrContentIdFilter(
                    new MajorFilter(Major.CONTENT),
                    new MajorFilter(Major.MAJOR_CONFIG),
                    new InputTemplateFilter(context),
                    new HotdeployStatusFilter(context),
                    new SecurityRootDepartmentFilter(),
                    excludeFilter)));

        try {
            return finder.findAllNonPresentContent();
        } catch (CMException e) {
            System.err.println(e.toString());

            System.exit(1);
            return null;
        }
    }

    protected ContentIdFilter getExistingObjectsFilter(PolopolyContext context,
            ListExportableParameters parameters) {
        File projectContentDirectory = parameters.getProjectContentDirectory();

        PresentContentFilter projectContentFilter =
            createProjectContentFilter(context, projectContentDirectory, "project content");

        System.out.println(Plural.count(projectContentFilter.getPresentIds(), "object") +
                " were product or project content and will not be exported.");

        return new OrContentIdFilter(
                new HotdeployStatusFilter(context),
                new MajorFilter(Major.MAJOR_CONFIG),
                new SecurityRootDepartmentFilter(),
                new InputTemplateFilter(context),
                projectContentFilter);
    }

    protected PresentContentFilter createProjectContentFilter(
            PolopolyContext context, File presentFilesDirectory, String directoryDescription) {
        PresentContentFilter presentContentFilter = new PresentContentFilter(context);

        if (presentFilesDirectory != null) {
            readPresentFilesFromDirectory(presentFilesDirectory, directoryDescription, presentContentFilter);
        }
        else {
            readPresentFilesPackagedWithHotdeploy(presentContentFilter);
        }

        return presentContentFilter;
    }

    private void readPresentFilesPackagedWithHotdeploy(
            PresentFilesAware presentFilesAware) {
        new PresentFileReader(new File("."), presentFilesAware).read();
    }

    protected void readPresentFilesFromDirectory(
            File presentFilesDirectory,
            String directoryDescription,
            PresentFilesAware presentFilesAware) {
        new PresentFileReader(presentFilesDirectory, presentFilesAware).read();

        System.err.println("Scanning " + directoryDescription + "...");

        List<DeploymentFile> deploymentFiles =
            FilesToDeployParameters.discoverFilesInDirectory(presentFilesDirectory);

        ContentXmlParser parser = new ContentXmlParser();

        PresentFilesAwareToParseCallbackAdapter callback =
            new PresentFilesAwareToParseCallbackAdapter(presentFilesAware);

        for (DeploymentFile deploymentFile : deploymentFiles) {
            parser.parse(deploymentFile, callback);
        }
    }

    public String getHelp() {
        return "Lists all objects in the system that can be exported.";
    }

}
