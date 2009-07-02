package com.polopoly.pcmd.tool.parameters;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.NotProvidedException;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.parser.IntegerParser;
import com.polopoly.util.client.PolopolyContext;

public class ListExportableParameters extends ProjectContentParameters {
    private static final String SINCE_OPTION = "since";
    private int since;

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);

        help.addOption(SINCE_OPTION, new IntegerParser(),
                "Return all content created since this version (optional).");
    }

    public int getSince() {
        return since;
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);

        try {
            since =
                args.getOption(SINCE_OPTION, new IntegerParser());
        }
        catch (NotProvidedException e) {
        }
    }
}
