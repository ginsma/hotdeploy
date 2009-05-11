package com.polopoly.pcmd.tool;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;

public class ForceAndFilesToDeployParameters extends FilesToDeployParameters {
    private boolean force = false;

    static final String FORCE_PARAMETER = "force";

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);
        help.addOption(FORCE_PARAMETER, new BooleanParser(), "Whether to overwrite any existing bootstrap file.");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);
        setForce(args.getFlag(FORCE_PARAMETER, force));
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isForce() {
        return force;
    }
}
