package com.polopoly.pcmd.parser;

import example.deploy.hotdeploy.client.Major;

public class MajorParser implements Parser<Major> {

    public String getHelp() {
        return "The major either by name or the integer.";
    }

    public Major parse(String majorName) throws ParseException {
        return Major.getMajor(majorName);
    }

}
