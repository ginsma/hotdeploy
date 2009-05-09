package com.polopoly.pcmd.tool;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.NotProvidedException;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.argument.Parameters;
import com.polopoly.pcmd.parser.BooleanParser;
import com.polopoly.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.pcmd.parser.ExistingFileParser;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.discovery.DirectoryFileDiscoverer;
import example.deploy.hotdeploy.discovery.FallbackDiscoverer;
import example.deploy.hotdeploy.discovery.NotApplicableException;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;

public class FilesToDeployParameters implements Parameters {
    private static final String VALIDATE_CLASSES_PARAMETER = "validateclasses";
    private static final String CLASS_DIRECTORY_PARAMETER = "classpath";
    private File directoryOrFile;
    private boolean validateClasses;
    private File classDirectory;

    public boolean isValidateClasses() {
        return validateClasses;
    }

    public void setValidateClasses(boolean validateClasses) {
        this.validateClasses = validateClasses;
    }

    public void getHelp(ParameterHelp help) {
        help.setArguments(new ExistingFileParser(),
                "Root directory of content (possibly containing " +
                ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME + ") or single content XML file");

        help.addOption(VALIDATE_CLASSES_PARAMETER, new BooleanParser(),
                "Whether to check that the referenced class names exists in the specified class directory or the current classpath.");

        help.addOption(CLASS_DIRECTORY_PARAMETER, new ExistingDirectoryParser(),
                "A class directory for checking whether referenced class names exists.");
    }

    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        setFile(args.getArgument(0, new ExistingFileParser()));
        setValidateClasses(args.getFlag(VALIDATE_CLASSES_PARAMETER, false));

        try {
            setClassDirectory(args.getOption(CLASS_DIRECTORY_PARAMETER, new ExistingDirectoryParser()));
        }
        catch (NotProvidedException e) {
            // fine.
        }
    }

    private void setFile(File directoryOrFile) {
        this.directoryOrFile = directoryOrFile;
    }

    public File getFileOrDirectory() {
        return directoryOrFile;
    }

    public List<DeploymentFile> discoverFiles() {
        File fileOrDirectory = getFileOrDirectory();

        if (fileOrDirectory.isDirectory()) {
            return discoverFilesInDirectory(fileOrDirectory);
        }
        else {
            return discoverSingleFile(fileOrDirectory);
        }
    }

    private List<DeploymentFile> discoverSingleFile(File file) {
        return Collections.singletonList((DeploymentFile) new FileDeploymentFile(file));
    }

    private List<DeploymentFile> discoverFilesInDirectory(File directory) {
        FallbackDiscoverer discoverer =
            new FallbackDiscoverer(
                new ImportOrderFileDiscoverer(),
                new DirectoryFileDiscoverer());

        try {
            return discoverer.getFilesToImport(directory);
        } catch (NotApplicableException e) {
            System.err.println("Could not find any content in directory " +
                directory.getAbsolutePath() + ": " + e.getMessage());
            System.exit(1);

            throw new RuntimeException();
        }
    }

    public void setClassDirectory(File classDirectory) {
        this.classDirectory = classDirectory;
    }

    public File getClassDirectory() {
        return classDirectory;
    }

    public File getDirectory() {
        if (!directoryOrFile.isDirectory()) {
            directoryOrFile = directoryOrFile.getParentFile();
        }

        return directoryOrFile;
    }

}
