package com.polopoly.pcmd.tool.parameters;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.ContentIdListParameters;
import com.polopoly.pcmd.argument.NotProvidedException;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.argument.Parameters;
import com.polopoly.pcmd.parser.BooleanParser;
import com.polopoly.pcmd.parser.ContentIdParser;
import com.polopoly.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.util.client.PolopolyContext;

public class SpiderParameters extends ContentIdListParameters implements Parameters {
    private static final String PROJECT_CONTENT_DIR_OPTION = "projectcontent";
    private static final String VERBOSE_OPTION = "verbose";
    private List<File> projectContentDirectories;
    private boolean verbose = false;

    public List<File> getProjectContentDirectories() {
        return projectContentDirectories;
    }

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);
        help.setArguments(new ContentIdParser(), "A series of content IDs to start spidering from.");
        help.addOption(PROJECT_CONTENT_DIR_OPTION, new ExistingDirectoryParser(),
            "A directory containing content already imported that has already been imported and should thus not be exported. This option may be specified multiple times.");
        help.addOption(VERBOSE_OPTION, new BooleanParser(),
                "Whether to print more information on the spidering process.");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);
        try {
            projectContentDirectories =
                args.getOptions(PROJECT_CONTENT_DIR_OPTION, new ExistingDirectoryParser());
        }
        catch (NotProvidedException e) {
            projectContentDirectories = Collections.emptyList();
        }
    }

    public boolean isVerbose() {
        return verbose;
    }
}
