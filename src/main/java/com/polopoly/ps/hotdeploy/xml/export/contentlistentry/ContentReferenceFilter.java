package com.polopoly.ps.hotdeploy.xml.export.contentlistentry;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.ContentRead;

public interface ContentReferenceFilter {

    boolean isAllowed(ContentRead inContent, ContentId referredContent);

}
