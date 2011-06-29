package com.polopoly.pcmd.tool;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.pcmd.tool.parameters.ExcludeProjectContentParameters;
import com.polopoly.ps.deploy.xml.export.filteredcontent.ProjectContentFilterFactory;
import com.polopoly.ps.pcmd.field.content.AbstractContentIdField;
import com.polopoly.ps.pcmd.tool.Tool;
import com.polopoly.util.client.PolopolyContext;


public class ExcludeProjectContentTool implements Tool<ExcludeProjectContentParameters> {

    public ExcludeProjectContentParameters createParameters() {
        return new ExcludeProjectContentParameters();
    }

    public void execute(PolopolyContext context, ExcludeProjectContentParameters parameters) {
        List<File> projectContentDirectories = parameters.getProjectContentDirectories();

        if (!projectContentDirectories.isEmpty()) {
            System.err.println("Scanning project content...");
        }

        ContentIdFilter filter =
            new ProjectContentFilterFactory(context.getPolicyCMServer()).
                getExistingObjectsFilter(projectContentDirectories);

        Iterator<ContentId> it = parameters.getContentIds();

        while (it.hasNext()) {
            ContentId contentId = it.next();

            if (filter.accept(contentId)) {
                continue;
            }

            printResultContentId(contentId, parameters, context);
        }
    }

    private void printResultContentId(ContentId contentId,
            ExcludeProjectContentParameters parameters, PolopolyContext context) {
        if (parameters.isResolve()) {
            System.out.println(AbstractContentIdField.get(contentId, context));
        }
        else {
            System.out.println(contentId.getContentId().getContentIdString());
        }
    }

    public String getHelp() {
        return "Returns the content IDs passed to it (piped or as arguments) with any existing content defined in the specified content directory removed.";
    }

}
