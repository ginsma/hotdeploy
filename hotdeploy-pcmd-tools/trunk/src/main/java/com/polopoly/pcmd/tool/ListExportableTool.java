package com.polopoly.pcmd.tool;

import java.text.DateFormat;
import java.util.Date;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.search.db.Version;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.pcmd.field.content.AbstractContentIdField;
import com.polopoly.pcmd.tool.parameters.ListExportableParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.client.Major;
import example.deploy.xml.export.filteredcontent.ExcludeMetadataVersionFilter;
import example.deploy.xml.export.filteredcontent.FilteredAllContentFinder;
import example.deploy.xml.export.filteredcontent.HotdeployStatusFilter;
import example.deploy.xml.export.filteredcontent.InputTemplateFilter;
import example.deploy.xml.export.filteredcontent.MajorFilter;
import example.deploy.xml.export.filteredcontent.NegatingContentIdFilter;
import example.deploy.xml.export.filteredcontent.OrContentIdFilter;
import example.deploy.xml.export.filteredcontent.ProjectContentFilterFactory;
import example.deploy.xml.export.filteredcontent.SecurityRootDepartmentFilter;

public class ListExportableTool implements Tool<ListExportableParameters> {
    public ListExportableParameters createParameters() {
        return new ListExportableParameters();
    }

    public void execute(PolopolyContext context,
            ListExportableParameters parameters) {
        System.err.println("Scanning project content...");

        ContentIdFilter existingObjectsFilter =
            new ProjectContentFilterFactory(context).getExistingObjectsFilter(parameters.getProjectContentDirectory());

        int since = parameters.getSince();

        for (ContentId contentId : getContentIds(context, since, existingObjectsFilter)) {
            printResultContentId(contentId, parameters, context);
        }
    }

    private void printResultContentId(ContentId contentId,
            ListExportableParameters parameters, PolopolyContext context) {
        if (parameters.isResolve()) {
            System.out.println(AbstractContentIdField.get(contentId, context));
        }
        else {
            System.out.println(contentId.getContentIdString());
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

    public String getHelp() {
        return "Lists all objects in the system that can be exported.";
    }

}
