package com.polopoly.pcmd.tool.parameters;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.util.client.PolopolyContext;

public class EmptyParameters implements Parameters {

    public void getHelp(ParameterHelp help) {
    }

    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
    }

}
