package com.polopoly.pcmd.tool.parameters;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.argument.Parameters;
import com.polopoly.util.client.PolopolyContext;

public class EmptyParameters implements Parameters {

    public void getHelp(ParameterHelp help) {
    }

    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
    }

}
