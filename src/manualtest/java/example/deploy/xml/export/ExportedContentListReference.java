package com.polopoly.ps.hotdeploy.xml.export;

import com.polopoly.cm.client.CMException;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.contentid.ContentIdUtil;
import com.polopoly.util.contentlist.ContentListUtilImpl.ContentListContentIds;
import com.polopoly.util.policy.PolicyModification;
import com.polopoly.util.policy.PolicyUtil;

public class ExportedContentListReference extends ExportedContent {
    private int index;
    private String fromExternalId;
    private String toExternalId;

    ExportedContentListReference(ExportedArticle fromArticle, final ExportedArticle referencedArticle) throws Exception {
        PolicyUtil fromPolicyUtil = fromArticle.getContentId().asPolicyUtil();

        fromExternalId = fromPolicyUtil.getContent().getExternalIdString();
        toExternalId = referencedArticle.getContentId().asContent().getExternalIdString();

        fromPolicyUtil.modifyUtil(new PolicyModification<PolicyUtil>() {
            public void modify(PolicyUtil newVersion) throws CMException {
                ContentListContentIds contentIds = newVersion.getContent().getContentList().contentIds();

                index = contentIds.size();
                contentIds.add(index, referencedArticle.getContentId());
            }});
    }

    @Override
    boolean validate(PolopolyContext context) throws Exception {
        ContentListContentIds contentIds =
            context.getContent(fromExternalId).getContentList().contentIds();

        if (contentIds.size() <= index) {
            return false;
        }

        ContentIdUtil referredId =
            contentIds.get(index);

        return toExternalId.equals(referredId.asContent().getExternalIdString());
    }

    @Override
    public String toString() {
        return "content list reference to " + toExternalId + " in " + fromExternalId;
    }
}
