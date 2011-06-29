package com.polopoly.pcmd.tool.parameters;

import com.polopoly.pcmd.parser.MajorParser;
import com.polopoly.ps.deploy.hotdeploy.client.Major;
import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.NotProvidedException;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;


public class HotdeployListParameters extends FilesToDeployParameters {
    private static final String MAJOR_OPTION = "major";
    private static final String VERBOSE_OPTION = "verbose";
    private Major major;
    private boolean verbose;

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);

        help.addOption(MAJOR_OPTION, new MajorParser(),
            "If specified, limits the list to content of a single major.");

        help.addOption(VERBOSE_OPTION, new BooleanParser(),
            "Whether to print major names of objects.");
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

        setVerbose(args.getFlag(VERBOSE_OPTION, false));
    }

    public void setMajor(Major major) {
        this.major = major;
    }

    public Major getMajor() {
        return major;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

}
