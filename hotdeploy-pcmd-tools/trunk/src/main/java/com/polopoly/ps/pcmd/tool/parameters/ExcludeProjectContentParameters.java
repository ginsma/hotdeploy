package com.polopoly.ps.pcmd.tool.parameters;

import java.io.File;
import java.util.List;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ContentIdListParameters;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.util.client.PolopolyContext;

public class ExcludeProjectContentParameters extends ContentIdListParameters {
    private ProjectContentParameters projectContentParameters = new ProjectContentParameters();

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);
        projectContentParameters.getHelp(help);
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);
        projectContentParameters.parseParameters(args, context);
    }

    public List<File> getProjectContentDirectories() {
        return projectContentParameters.getProjectContentDirectories();
    }

    public boolean isResolve() {
        return projectContentParameters.isResolve();
    }

}
