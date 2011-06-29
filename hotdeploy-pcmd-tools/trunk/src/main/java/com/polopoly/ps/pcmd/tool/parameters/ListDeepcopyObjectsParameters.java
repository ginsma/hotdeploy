package com.polopoly.ps.pcmd.tool.parameters;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ContentIdListParameters;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.util.client.PolopolyContext;

public class ListDeepcopyObjectsParameters extends ContentIdListParameters {
    private static final String RESOLVE_IDS_OPTION = "resolve";
    private boolean resolve;

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);
        help.addOption(RESOLVE_IDS_OPTION, new BooleanParser(), "Whether to resolve the IDs to external IDs (true by default; this slows down printing results).");

    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);

        setResolve(args.getFlag(RESOLVE_IDS_OPTION, true));
    }

    public void setResolve(boolean resolve) {
        this.resolve = resolve;
    }

    public boolean isResolve() {
        return resolve;
    }

}
