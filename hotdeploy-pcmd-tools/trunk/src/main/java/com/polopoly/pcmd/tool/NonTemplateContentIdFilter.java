package com.polopoly.pcmd.tool;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.ps.deploy.hotdeploy.client.Major;


public class NonTemplateContentIdFilter implements ContentIdFilter {

    public boolean accept(ContentId id) {
        return id.getMajor() != Major.INPUT_TEMPLATE.getIntegerMajor() &&
            id.getMajor() != Major.OUTPUT_TEMPLATE.getIntegerMajor();
    }

}
