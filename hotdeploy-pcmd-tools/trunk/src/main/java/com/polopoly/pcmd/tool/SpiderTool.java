package com.polopoly.pcmd.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.pcmd.FatalToolException;
import com.polopoly.pcmd.field.content.AbstractContentIdField;
import com.polopoly.pcmd.tool.parameters.SpiderParameters;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.content.ContentUtil;
import com.polopoly.util.contentid.ContentIdUtil;
import com.polopoly.util.exception.ContentGetException;

import example.deploy.xml.export.filteredcontent.AndContentIdFilter;
import example.deploy.xml.export.filteredcontent.NegatingContentIdFilter;
import example.deploy.xml.export.filteredcontent.ProjectContentFilterFactory;

public class SpiderTool implements Tool<SpiderParameters> {

    public SpiderParameters createParameters() {
        return new SpiderParameters();
    }

    public void execute(PolopolyContext context, SpiderParameters parameters)
    throws FatalToolException {
        List<File> projectContentDirectories = parameters.getProjectContentDirectories();

        if (!projectContentDirectories.isEmpty()) {
            System.err.println("Scanning project content...");
        }

        ContentIdFilter filter =
            new NegatingContentIdFilter(
                    new ProjectContentFilterFactory(context.getPolicyCMServer()).
                    getExistingObjectsFilter(projectContentDirectories));

        filter = new AndContentIdFilter(filter, new NonTemplateContentIdFilter());

        Set<ContentId> spideredIds = new HashSet<ContentId>();
        List<ContentId> spiderQueue = new ArrayList<ContentId>();

        Iterator<ContentId> it = parameters.getContentIds();

        while (it.hasNext()) {
            ContentId contentId = it.next();

            spiderQueue.add(contentId);
        }

        while (!spiderQueue.isEmpty()) {
            ContentId contentId = spiderQueue.remove(0);

            try {
                Set<ContentId> candidateIds = allReferences(contentId, context);

                for (ContentId candidateId : candidateIds) {
                    candidateId = candidateId.getContentId();

                    if (!spideredIds.contains(candidateId) &&
                            filter.accept(candidateId)) {
                        spideredIds.add(candidateId);
                        spiderQueue.add(candidateId);

                        System.out.print(AbstractContentIdField.get(candidateId, context));

                        if (parameters.isVerbose()) {
                            System.out.print(" ");
                            System.out.println(" (from " + AbstractContentIdField.get(contentId, context) + ")");
                        }
                    }
                }
            }
            catch (Exception e) {
                System.err.println("While handling " + AbstractContentIdField.get(contentId, context) + ": " + e);

                if (parameters.isStopOnException()) {
                    System.exit(1);
                }
            }
        }
    }

    private Set<ContentId> allReferences(ContentId contentId, PolopolyContext context) throws ContentGetException {
        Set<ContentId> result = new HashSet<ContentId>();

        ContentUtil content = context.getContent(contentId);

        for (String group : content.getContentReferenceGroupNames()) {
            for (String name : content.getContentReferenceNames(group)) {
                result.add(content.getContentReference(group, name).unversioned());
            }
        }

        ContentIdUtil securityParentId = content.getSecurityParentId();

        if (securityParentId != null) {
            result.add(securityParentId.unversioned());
        }

        return result;
    }

    public String getHelp() {
        return "Spiders all non-project and non-system content excluding templates starting from a certain object and following content references.";
    }

}
