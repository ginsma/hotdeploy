package com.polopoly.ps.deploy.xml.export;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.contentid.ContentIdUtil;
import com.polopoly.util.exception.ContentGetException;
import com.polopoly.util.policy.PolicyModification;
import com.polopoly.util.policy.PolicyUtil;

public class ExportedContentReference extends ExportedContent {
    protected static final String GROUP = "group";
    protected static final String REFERENCE = "ref";

    private String fromExternalId;
    private String toExternalId;

    ExportedContentReference(ExportedArticle fromArticle, final ExportedArticle referencedArticle) throws Exception {
        PolicyUtil fromPolicyUtil = fromArticle.getContentId().asPolicyUtil();

        fromExternalId = fromPolicyUtil.getContent().getExternalIdString();
        toExternalId = referencedArticle.getContentId().asContent().getExternalIdString();

        fromPolicyUtil.modifyUtil(new PolicyModification<PolicyUtil>() {
            public void modify(PolicyUtil newVersion) throws CMException {
                newVersion.getContent().setContentReference(GROUP, REFERENCE, referencedArticle.getContentId());
            }});
    }

    @Override
    boolean validate(PolopolyContext context) throws Exception {
        ContentId reference =
            context.getContent(fromExternalId).getContentReference(GROUP, REFERENCE);

        ContentIdUtil shouldReferTo;

        try {
            shouldReferTo = context.getContent(toExternalId).getContentId();
        } catch (ContentGetException e) {
            return false;
        }

        return reference != null && reference.equalsIgnoreVersion(shouldReferTo);
    }

    @Override
    public String toString() {
        return "reference to " + toExternalId + " in " + fromExternalId;
    }
}
