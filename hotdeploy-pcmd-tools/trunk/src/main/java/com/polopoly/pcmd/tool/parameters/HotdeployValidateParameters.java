package com.polopoly.pcmd.tool.parameters;

import static com.polopoly.pcmd.tool.parameters.ForceAndFilesToDeployParameters.IGNORE_PRESENT;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;

public class HotdeployValidateParameters extends FilesToDeployParameters {
    private boolean ignorePresent = false;

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);
        help.addOption(IGNORE_PRESENT, new BooleanParser(),
                "Whether to not consider any content to already be present (this is only useful when analyzing Polopoly initxml content).");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);
        setIgnorePresent(args.getFlag(IGNORE_PRESENT, ignorePresent));
    }

    public void setIgnorePresent(boolean ignorePresent) {
        this.ignorePresent = ignorePresent;
    }

    public boolean isIgnorePresent() {
        return ignorePresent;
    }

}
