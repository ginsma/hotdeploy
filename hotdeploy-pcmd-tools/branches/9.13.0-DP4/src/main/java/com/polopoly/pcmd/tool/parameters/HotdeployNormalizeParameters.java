package com.polopoly.pcmd.tool.parameters;

import java.io.File;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.util.client.PolopolyContext;

public class HotdeployNormalizeParameters extends FilesToDeployParameters {
    private File toDirectory;

    public File getToDirectory() {
        return toDirectory;
    }

    public void setToDirectory(File toDirectory) {
        this.toDirectory = toDirectory;
    }

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);

        help.setArguments(new ExistingDirectoryParser(),
            "The directory to create the normalized XML in.");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);

        toDirectory = args.getArgument(1, new ExistingDirectoryParser());
    }
}
