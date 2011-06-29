package com.polopoly.ps.deploy.xml.export;

import com.polopoly.cm.client.CMException;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.policy.PolicyModification;
import com.polopoly.util.policy.PolicyUtil;

public class ExportedSecurityParent extends ExportedContent {
    private String articleExternalId;
    private String parentExternalId;

    public ExportedSecurityParent(ExportedArticle exportedArticle, final ExportedArticle securityParent) throws Exception {
        articleExternalId = exportedArticle.getExternalIdString();
        parentExternalId = securityParent.getExternalIdString();

        exportedArticle.getContentId().asPolicyUtil().modifyUtil(new PolicyModification<PolicyUtil>() {
            public void modify(PolicyUtil newVersion) throws CMException {
                newVersion.getContent().setSecurityParentId(securityParent.getContentId());
            }});
    }

    @Override
    boolean validate(PolopolyContext context) throws Exception {
        return parentExternalId.equals(
                context.getPolicyUtil(articleExternalId).getContent().
                    getSecurityParentId().asContent().getExternalIdString());
    }

}
