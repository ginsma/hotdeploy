package com.polopoly.ps.hotdeploy.xml.parser;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.util.SingleObjectHolder;
import com.polopoly.ps.hotdeploy.util.Tuple;

public class ParsedContentId extends SingleObjectHolder<Tuple<Major, String>> {
    private Major major;
    private String externalId;

    public ParsedContentId(Major major, String externalId) {
        super(new Tuple<Major, String>(major, externalId));
        this.major = (major == null ? Major.UNKNOWN : major);
        this.externalId = externalId;
    }

    public Major getMajor() {
        return major;
    }

    public String getExternalId() {
        return externalId;
    }

}
