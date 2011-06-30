package com.polopoly.ps.hotdeploy.xml.bootstrap;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.util.SingleObjectHolder;
import com.polopoly.ps.hotdeploy.util.Tuple;

public class BootstrapContent extends SingleObjectHolder<Tuple<Major, String>> implements Comparable<BootstrapContent> {
    private String externalId;
    private Major major;
    private String inputTemplate;

    public BootstrapContent(Major major, String externalId) {
        super(new Tuple<Major, String>(major, externalId));

        this.externalId = externalId;
        setMajor(major);
    }

    public String getExternalId() {
        return externalId;
    }

    public void setMajor(Major major) {
        setHeldObject(new Tuple<Major, String>(major, externalId));
        this.major = (major == null ? Major.UNKNOWN : major);
    }

    public Major getMajor() {
        return major;
    }

    @Override
    public String toString() {
        return getExternalId() + " (major " + major + ")";
    }

    @Override
    public int hashCode() {
        return getExternalId().hashCode();
    }

    public int compareTo(BootstrapContent o) {
        return getExternalId().compareTo(o.getExternalId());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BootstrapContent &&
            ((BootstrapContent) o).getExternalId().equals(getExternalId());
    }

    public void setInputTemplate(String inputTemplate) {
        this.inputTemplate = inputTemplate;
    }

    public String getInputTemplate() {
        return inputTemplate;
    }
}
