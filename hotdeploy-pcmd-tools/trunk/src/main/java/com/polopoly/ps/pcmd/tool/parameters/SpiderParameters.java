package com.polopoly.ps.pcmd.tool.parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.polopoly.cm.ContentId;
import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.ContentIdListParameters;
import com.polopoly.ps.pcmd.argument.NotProvidedException;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.argument.Parameters;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.ps.pcmd.parser.ContentIdParser;
import com.polopoly.ps.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.ps.pcmd.parser.IntegerParser;
import com.polopoly.util.client.PolopolyContext;

public class SpiderParameters extends ContentIdListParameters implements Parameters {
    private static final String PROJECT_CONTENT_DIR_OPTION = "projectcontent";
    private static final String VERBOSE_OPTION = "verbose";
    private static final String SKIP_OPTION = "skip";
    private static final String DONT_SPIDER_TEMPLATES = "dontspidertemplates";
    private static final String DEPTH = "depth";
    
    private List<File> projectContentDirectories;
    private boolean verbose = false;
    private int depth = Integer.MAX_VALUE;
    private List<ContentId> dontSpiderTemplates = Collections.emptyList();
    
    private Set<ContentId> skip;

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
        help.addOption(SKIP_OPTION, new ContentIdParser(),
                "Content ID to not traverse during spidering (may be specified multiple times).");
        help.addOption(DEPTH, new IntegerParser(),
        		"Maximum depth to spider to (0 means only start objects, 1 means start objects plus their immediate references).");
        help.addOption(DONT_SPIDER_TEMPLATES, new ContentIdParser(), "Don't spider any references in objects with this input template (may be specified multiple times).");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);

        verbose = args.getFlag(VERBOSE_OPTION, true);

        try {
            projectContentDirectories =
                args.getOptions(PROJECT_CONTENT_DIR_OPTION, new ExistingDirectoryParser());
        }
        catch (NotProvidedException e) {
            projectContentDirectories = Collections.emptyList();
        }

        try {
            skip = new HashSet<ContentId>(args.getOptions(SKIP_OPTION, new ContentIdParser(context)));
        }
        catch (NotProvidedException e) {
            skip = Collections.emptySet();
        }

        try {
        	List<ContentId> possiblyVersionedTemplateIds = args.getOptions(DONT_SPIDER_TEMPLATES, new ContentIdParser(context));

        	setDontSpiderTemplates(toUnversionedIds(possiblyVersionedTemplateIds));
        }
        catch (NotProvidedException e) {
        	// leave list empty.
        }
        
        try {
			setDepth(args.getOption(DEPTH, new IntegerParser()));
		} catch (NotProvidedException e) {
			setDepth(Integer.MAX_VALUE);
		}
    }

	private List<ContentId> toUnversionedIds(List<ContentId> templateIds) {
		List<ContentId> result = new ArrayList<ContentId>(templateIds.size());
		
		for (ContentId versionedId : templateIds) {
			result.add(toUnversionedId(versionedId));
		}
		
		return result;
	}

	private ContentId toUnversionedId(ContentId versionedId) {
		return versionedId.getContentId();
	}

    public boolean isVerbose() {
        return verbose;
    }

    public Set<ContentId> getSkip() {
        return skip;
    }

	private void setDontSpiderTemplates(List<ContentId> dontSpiderTemplates) {
		this.dontSpiderTemplates = dontSpiderTemplates;
	}

	public List<ContentId> getDontSpiderTemplates() {
		return dontSpiderTemplates;
	}

	private void setDepth(int depth) {
		this.depth = depth;
	}

	public int getMaximumDepth() {
		return depth;
	}
}
