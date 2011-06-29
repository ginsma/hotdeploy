package com.polopoly.ps.deploy.xml.export.filteredcontent;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.ps.deploy.hotdeploy.client.Major;


public class MajorFilter implements ContentIdFilter {
    private Major majorToInclude;

    public MajorFilter(Major majorToInclude) {
        this.majorToInclude = majorToInclude;
    }

    public boolean accept(ContentId contentId) {
        return contentId.getMajor() == majorToInclude.getIntegerMajor();
    }

}
