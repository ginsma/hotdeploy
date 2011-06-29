package com.polopoly.ps.pcmd.parser;

import com.polopoly.ps.deploy.hotdeploy.client.Major;
import com.polopoly.ps.pcmd.parser.ParseException;
import com.polopoly.ps.pcmd.parser.Parser;

public class MajorParser implements Parser<Major> {

    public String getHelp() {
        return "The major either by name or the integer.";
    }

    public Major parse(String majorName) throws ParseException {
        return Major.getMajor(majorName);
    }

}
