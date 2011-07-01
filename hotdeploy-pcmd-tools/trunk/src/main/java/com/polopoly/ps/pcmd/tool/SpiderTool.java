package com.polopoly.ps.pcmd.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.hotdeploy.xml.export.filteredcontent.AndContentIdFilter;
import com.polopoly.ps.hotdeploy.xml.export.filteredcontent.NegatingContentIdFilter;
import com.polopoly.ps.hotdeploy.xml.export.filteredcontent.ProjectContentFilterFactory;
import com.polopoly.ps.pcmd.FatalToolException;
import com.polopoly.ps.pcmd.field.content.AbstractContentIdField;
import com.polopoly.ps.pcmd.tool.parameters.SpiderParameters;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.content.ContentUtil;
import com.polopoly.util.contentid.ContentIdUtil;
import com.polopoly.util.exception.ContentGetException;


public class SpiderTool implements Tool<SpiderParameters> {

    public class QueuedId {

		private ContentId id;
		private int depth;
		private ContentId source;

		public QueuedId(ContentId id, int depth, ContentId source) {
			this.id = id;
			this.depth = depth;
			this.source = source;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof QueuedId && ((QueuedId) obj).id.equalsIgnoreVersion(id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}

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

        final Set<ContentId> skip = parameters.getSkip();

        if (!skip.isEmpty()) {
            filter = new AndContentIdFilter(filter, new ContentIdFilter() {
                public boolean accept(ContentId contentId) {
                    return !skip.contains(contentId.getContentId());
                }});
        }

        Set<ContentId> spideredIds = new HashSet<ContentId>();
        List<QueuedId> spiderQueue = new ArrayList<QueuedId>();

        Iterator<ContentId> startIdIterator = parameters.getContentIds();

        while (startIdIterator.hasNext()) {
            ContentId startId = startIdIterator.next();

            spiderQueue.add(new QueuedId(startId, 0, null));
        }

        while (!spiderQueue.isEmpty()) {
        	QueuedId queuedId = spiderQueue.remove(0);
        	ContentId contentId = queuedId.id;
        	
            if (filter.accept(contentId)) {
                spideredIds.add(contentId);

                System.out.print(toString(contentId, context));

                if (parameters.isVerbose()) {
                    ContentId source = queuedId.source;

                    if (source != null) {
                        System.out.print(" (from " + toString(source, context) + ", depth " + queuedId.depth + ")");
                    }
                    
                    if (spideredIds.size() % 1000 == 0) {
                    	System.err.println("Spidered " + spideredIds.size() + " objects... " + spiderQueue.size() + " remaining in queue...");
                    }
                }

                System.out.println();
            }

            try {
            	ContentUtil content = context.getContent(queuedId.id);
            
            	if (!shouldFollowReferences(queuedId, content, parameters)) {
            		continue;
            	}
            
                Set<ContentId> candidateIds = allReferences(content);

                for (ContentId candidateId : candidateIds) {
                    candidateId = candidateId.getContentId();

                    QueuedId idToQueue = new QueuedId(candidateId, queuedId.depth + 1, contentId);

                    if (!spideredIds.contains(candidateId) && !spiderQueue.contains(idToQueue) && filter.accept(candidateId)) {
						spiderQueue.add(idToQueue);
                    }
                }
            }
            catch (Exception e) {
                System.err.println("While handling " + toString(contentId, context) + ": " + e);

                if (parameters.isStopOnException()) {
                    System.exit(1);
                }
            }
        }
    }

	private boolean shouldFollowReferences(QueuedId queuedId,
			ContentUtil content, SpiderParameters parameters) {
		if (parameters.getDontSpiderTemplates().contains(content.getInputTemplate().getContentId().unversioned())) {
			return false;
		}
		
		return queuedId.depth < parameters.getMaximumDepth();
	}

    private Set<ContentId> allReferences(ContentUtil content) throws ContentGetException {
        Set<ContentId> result = new HashSet<ContentId>();

        ContentId contentId = content.getContentId().unversioned();

        for (String group : content.getContentReferenceGroupNames()) {
            for (String name : content.getContentReferenceNames(group)) {
                ContentIdUtil referredId = content.getContentReference(group, name);

                if (exists(referredId)) {
                	result.add(referredId.unversioned());
                }
                else {
                	System.err.println("The content reference " + group + ":" + name + 
                			" in " + toString(contentId, content.getContext()) + 
                			" could not be resolved. It refers to " + toString(referredId, content.getContext()) + "."); 
                }
            }
        }

        ContentIdUtil securityParentId = content.getSecurityParentId();

        if (securityParentId != null) {
            result.add(securityParentId.unversioned());
        }

        return result;
    }

	private boolean exists(ContentIdUtil referredId) {
		try {
			referredId.asContent();
			
			return true;
		} catch (ContentGetException e) {
			return false;
		}
	}

	private String toString(ContentId contentId, PolopolyContext context) {
		return AbstractContentIdField.get(contentId, context);
	}

    public String getHelp() {
        return "Spiders all non-project and non-system content excluding templates starting from a certain object and following content references.";
    }

}
