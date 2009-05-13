package com.polopoly.pcmd.tool.parameters;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.NotProvidedException;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.parser.MajorParser;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.client.Major;

public class HotdeployListParameters extends FilesToDeployParameters {
    private static final String MAJOR_OPTION = "major";
    private Major major;

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);

        help.addOption(MAJOR_OPTION, new MajorParser(),
            "If specified, limits the list to content of a single major.");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);

        try {
            setMajor(args.getOption(MAJOR_OPTION, new MajorParser()));
        }
        catch (NotProvidedException e) {
            // ignore.
        }
    }

    public void setMajor(Major major) {
        this.major = major;
    }

    public Major getMajor() {
        return major;
    }
}
