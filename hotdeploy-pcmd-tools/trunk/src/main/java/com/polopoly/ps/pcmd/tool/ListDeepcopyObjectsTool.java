package com.polopoly.ps.pcmd.tool;

import java.util.Iterator;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.deepcopy.CopyTreeNode;
import com.polopoly.cm.app.deepcopy.DeepCopySettings;
import com.polopoly.cm.app.deepcopy.filter.DefaultExportFilter;
import com.polopoly.cm.app.deepcopy.impl.CopyTree;
import com.polopoly.cm.app.deepcopy.impl.CopyTreeBuilder;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.ps.pcmd.field.content.AbstractContentIdField;
import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.pcmd.tool.parameters.ListDeepcopyObjectsParameters;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.collection.ContentIdToContentIterator;

public class ListDeepcopyObjectsTool implements Tool<ListDeepcopyObjectsParameters> {

    public ListDeepcopyObjectsParameters createParameters() {
        return new ListDeepcopyObjectsParameters();
    }

    @SuppressWarnings("unchecked")
    public void execute(PolopolyContext context,
            ListDeepcopyObjectsParameters parameters) {
        ContentIdToContentIterator it =
            new ContentIdToContentIterator(context, parameters.getContentIds(), parameters.isStopOnException());

        while (it.hasNext()) {
            ContentRead rootContent = it.next();

            System.out.println(AbstractContentIdField.get(
                rootContent.getContentId().getContentId(), context));

            try {
                DeepCopySettings settings = new DeepCopySettings(new DefaultExportFilter());

                CopyTree copyTree = buildCopyTree(rootContent, settings, context);

                Iterator<CopyTreeNode> nodes = copyTree.getReferenceMap().values().iterator();

                while (nodes.hasNext()) {
                    CopyTreeNode node = nodes.next();
                    if (node.isDeepCopyable()) {
                        printResultContentId(node, parameters, context);
                    }
                }
            } catch (CMException e) {
                System.err.println("Error building tree for " +
                    AbstractContentIdField.get(rootContent.getContentId(), context) + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        it.printInfo(System.err);
    }

    private void printResultContentId(CopyTreeNode node,
            ListDeepcopyObjectsParameters parameters, PolopolyContext context) {
        ContentId contentId = node.getVersionedContentId().getContentId();

        if (parameters.isResolve()) {
            System.out.println(AbstractContentIdField.get(contentId, context));
        }
        else {
            System.out.println(contentId.getContentIdString());
        }
    }

    public String getHelp() {
        return "Lists the objects the deepcopy algorithm will find for the specified site(s).";
    }

    CopyTree buildCopyTree(ContentRead rootContent, DeepCopySettings settings,
            PolopolyContext context) throws CMException {
        CopyTreeBuilder builder = new CopyTreeBuilder();
        CopyTree tree = builder.buildTree(context.getPolicyCMServer(),
                                          rootContent.getContentId(),
                                          settings);
        builder.filterTree(context.getPolicyCMServer(), tree, settings);

        return tree;
    }
}
