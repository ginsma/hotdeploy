package com.polopoly.pcmd.tool.parameters;

import java.io.File;
import java.util.Iterator;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.ContentIdListParameters;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.argument.Parameters;
import com.polopoly.pcmd.parser.BooleanParser;
import com.polopoly.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.collection.FetchingIterator;

public class ExportParameters extends ListExportableParameters implements Parameters {
    private File outputDirectory;

    private ContentIdListParameters idListParameters = new ContentIdListParameters() {
        @Override
        protected int getFirstContentIdIndex() {
            return 1;
        }
    };

    private boolean idsAreArguments;
    private static final String ALL_OPTION = "all";

    @Override
    public void getHelp(ParameterHelp help) {
        help.setArguments(new ExistingDirectoryParser(),
            "The directory to write the exported files to (any existing files will be overwritten).");

        idListParameters.getHelp(help);

        super.getHelp(help);

        help.addOption(ALL_OPTION, new BooleanParser(),
                "Whether to export all exportable objects (if not specified, the objects to be exported will be expected on standard in or as arguments)");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        outputDirectory = args.getArgument(0, new ExistingDirectoryParser());

        super.parseParameters(args, context);

        idsAreArguments = !args.getFlag(ALL_OPTION, false);

        if (idsAreArguments) {
            try {
                idListParameters.parseParameters(args, context);
            }
            catch (ArgumentException e) {
                throw new ArgumentException(e.getMessage() + " (specify " + ALL_OPTION +
                    "=true to export all objects rather than a list of specific objects)");
            }
        }
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public boolean isIdsArguments() {
        return idsAreArguments;
    }

    public Iterable<ContentId> getIdArgumentsIterator(final ContentIdFilter excludeFilter) {
        return new Iterable<ContentId>() {
            public Iterator<ContentId> iterator() {
                return new FetchingIterator<ContentId>() {
                    private Iterator<ContentId> delegate = idListParameters.iterator();

                    @Override
                    protected ContentId fetch() {
                        while (delegate.hasNext()) {
                            ContentId candidate = delegate.next();

                            if (!excludeFilter.accept(candidate)) {
                                return candidate;
                            }
                        }

                        return null;
                    }};
            }};
    }
}
