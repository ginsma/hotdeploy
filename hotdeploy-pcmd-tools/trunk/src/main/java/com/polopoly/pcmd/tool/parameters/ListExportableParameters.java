package com.polopoly.pcmd.tool.parameters;

import java.io.File;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.NotProvidedException;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.argument.Parameters;
import com.polopoly.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.pcmd.parser.IntegerParser;
import com.polopoly.util.client.PolopolyContext;

public class ListExportableParameters implements Parameters {
    private static final String PROJECT_CONTENT_DIR_OPTION = "projectcontent";
    private static final String SINCE_OPTION = "since";
    private File projectContentDirectory;
    private int since;

    public File getProjectContentDirectory() {
        return projectContentDirectory;
    }

    public void getHelp(ParameterHelp help) {
        help.addOption(PROJECT_CONTENT_DIR_OPTION, new ExistingDirectoryParser(),
            "A directory containing content already imported that has already been imported and should thus not be exported.");
        help.addOption(SINCE_OPTION, new IntegerParser(),
                "Return all content created since this version (optional).");
    }

    public int getSince() {
        return since;
    }

    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        try {
            projectContentDirectory =
                args.getOption(PROJECT_CONTENT_DIR_OPTION, new ExistingDirectoryParser());
        }
        catch (NotProvidedException e) {
        }

        try {
            since =
                args.getOption(SINCE_OPTION, new IntegerParser());
        }
        catch (NotProvidedException e) {
        }
    }
}
