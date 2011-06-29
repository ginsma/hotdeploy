package com.polopoly.ps.pcmd.tool.parameters;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.util.client.PolopolyContext;

public class HotdeployFindParameters extends FilesToDeployParameters {
    private String externalId;

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);

        help.setArguments(null, "The external ID of the declaration to find.");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);

        setExternalId(args.getArgument(1));
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalId() {
        return externalId;
    }

}
