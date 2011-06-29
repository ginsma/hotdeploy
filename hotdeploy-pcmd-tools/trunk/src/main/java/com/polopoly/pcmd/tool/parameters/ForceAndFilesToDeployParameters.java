package com.polopoly.pcmd.tool.parameters;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;

public class ForceAndFilesToDeployParameters extends FilesToDeployParameters {
    private boolean force = false;
    private boolean ignorePresent = false;

    public static final String BOOTSTRAP_NON_CREATED_PARAMETER = "includenotcreated";
    public static final String IGNORE_PRESENT = "ignorepresent";
    public static final String FORCE_PARAMETER = "force";

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);
        help.addOption(FORCE_PARAMETER, new BooleanParser(), "Whether to overwrite any existing bootstrap file.");
        help.addOption(IGNORE_PRESENT, new BooleanParser(), "Whether to not consider any content to already be present (this is only useful when analyzing Polopoly initxml content).");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);
        setForce(args.getFlag(FORCE_PARAMETER, force));
        setIgnorePresent(args.getFlag(IGNORE_PRESENT, ignorePresent));
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isForce() {
        return force;
    }

    public boolean isIgnorePresent() {
        return ignorePresent;
    }

    public void setIgnorePresent(boolean ignorePresent) {
        this.ignorePresent = ignorePresent;
    }
}
