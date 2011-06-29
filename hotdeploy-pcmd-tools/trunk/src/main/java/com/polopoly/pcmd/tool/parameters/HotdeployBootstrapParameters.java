package com.polopoly.pcmd.tool.parameters;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;

public class HotdeployBootstrapParameters extends ForceAndFilesToDeployParameters {
    private boolean force = false;
    private boolean bootstrapNonCreated = false;

    public static final String FORCE_PARAMETER = "force";

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);
        help.addOption(FORCE_PARAMETER, new BooleanParser(), "Whether to overwrite any existing bootstrap file.");
        help.addOption(BOOTSTRAP_NON_CREATED_PARAMETER, new BooleanParser(), "Whether to bootstrap content objects that are never created in the files " +
    		"(these are likely to be misspellings if you specifying the full set of files to import).");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);
        setForce(args.getFlag(FORCE_PARAMETER, force));
        setBootstrapNonCreated(args.getFlag(BOOTSTRAP_NON_CREATED_PARAMETER, bootstrapNonCreated));
    }

    @Override
    public void setForce(boolean force) {
        this.force = force;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    public void setBootstrapNonCreated(boolean bootstrapNonCreated) {
        this.bootstrapNonCreated = bootstrapNonCreated;
    }

    public boolean isBootstrapNonCreated() {
        return bootstrapNonCreated;
    }
}
