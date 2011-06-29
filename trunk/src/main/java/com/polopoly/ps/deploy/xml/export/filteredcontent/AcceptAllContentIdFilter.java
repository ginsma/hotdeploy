package com.polopoly.ps.deploy.xml.export.filteredcontent;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;

public class AcceptAllContentIdFilter implements ContentIdFilter {

    public boolean accept(ContentId contentId) {
        return true;
    }

}
