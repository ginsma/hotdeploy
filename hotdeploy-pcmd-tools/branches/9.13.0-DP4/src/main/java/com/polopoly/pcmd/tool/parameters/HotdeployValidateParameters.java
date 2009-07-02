package com.polopoly.pcmd.tool.parameters;

import static com.polopoly.pcmd.tool.parameters.ForceAndFilesToDeployParameters.IGNORE_PRESENT;

import java.io.File;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.NotProvidedException;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.parser.BooleanParser;
import com.polopoly.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.util.client.PolopolyContext;

public class HotdeployValidateParameters extends FilesToDeployParameters {
    private static final String VALIDATE_CLASSES_PARAMETER = "validateclasses";
    private static final String CLASS_DIRECTORY_PARAMETER = "classpath";

    private File classDirectory;
    private boolean ignorePresent = false;
    private boolean validateClasses;

    public void setValidateClasses(boolean validateClasses) {
        this.validateClasses = validateClasses;
    }

    public boolean isValidateClasses() {
        return validateClasses;
    }

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);
        help.addOption(VALIDATE_CLASSES_PARAMETER, new BooleanParser(),
        "Whether to check that the referenced class names exists in the specified class directory or the current classpath.");

        help.addOption(IGNORE_PRESENT, new BooleanParser(),
                "Whether to not consider any content to already be present (this is only useful when analyzing Polopoly initxml content).");

        help.addOption(CLASS_DIRECTORY_PARAMETER, new ExistingDirectoryParser(),
                "A class directory for checking whether referenced class names exists.");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);
        setIgnorePresent(args.getFlag(IGNORE_PRESENT, ignorePresent));
        setValidateClasses(args.getFlag(VALIDATE_CLASSES_PARAMETER, false));

        try {
            setClassDirectory(args.getOption(CLASS_DIRECTORY_PARAMETER, new ExistingDirectoryParser()));
        }
        catch (NotProvidedException e) {
            // fine.
        }
    }

    public void setClassDirectory(File classDirectory) {
        this.classDirectory = classDirectory;
    }

    public File getClassDirectory() {
        return classDirectory;
    }

    public void setIgnorePresent(boolean ignorePresent) {
        this.ignorePresent = ignorePresent;
    }

    public boolean isIgnorePresent() {
        return ignorePresent;
    }

}
