package com.polopoly.pcmd.tool.parameters;

import java.io.File;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.NotProvidedException;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.argument.Parameters;
import com.polopoly.pcmd.parser.BooleanParser;
import com.polopoly.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.util.client.PolopolyContext;

public class ProjectContentParameters implements Parameters {
    private static final String PROJECT_CONTENT_DIR_OPTION = "projectcontent";
    private static final String RESOLVE_IDS_OPTION = "resolve";
    private File projectContentDirectory;
    private boolean resolve;

    public File getProjectContentDirectory() {
        return projectContentDirectory;
    }

    public void getHelp(ParameterHelp help) {
        help.addOption(PROJECT_CONTENT_DIR_OPTION, new ExistingDirectoryParser(),
            "A directory containing content already imported that has already been imported and should thus not be exported.");
        help.addOption(RESOLVE_IDS_OPTION, new BooleanParser(), "Whether to resolve the IDs to external IDs (true by default; this slows down printing results).");
    }

    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        try {
            projectContentDirectory =
                args.getOption(PROJECT_CONTENT_DIR_OPTION, new ExistingDirectoryParser());
        }
        catch (NotProvidedException e) {
        }

        setResolve(args.getFlag(RESOLVE_IDS_OPTION, true));
    }

    public void setResolve(boolean resolve) {
        this.resolve = resolve;
    }

    public boolean isResolve() {
        return resolve;
    }
}
