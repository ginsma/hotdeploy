package com.polopoly.pcmd.tool.parameters;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.NotProvidedException;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.argument.Parameters;
import com.polopoly.pcmd.parser.ExistingFileParser;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.ImportOrderOrDirectoryFileDiscoverer;
import example.deploy.hotdeploy.discovery.NotApplicableException;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentFile;

public class FilesToDeployParameters implements Parameters {
    private File directoryOrFile;
    private List<DeploymentFile> cachedDiscoveredFiles;

    public void getHelp(ParameterHelp help) {
        help.setArguments(new ExistingFileParser(),
                "Root directory of content (possibly containing " +
                ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME + ") or single content XML file");
    }

    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        try {
            setFileOrDirectory(args.getArgument(0, new ExistingFileParser()));
        }
        catch (NotProvidedException e) {
            // optional.
        }
    }

    public void setFileOrDirectory(File directoryOrFile) {
        this.directoryOrFile = directoryOrFile;
        cachedDiscoveredFiles = null;
    }

    public File getFileOrDirectory() {
        return directoryOrFile;
    }

    public List<DeploymentFile> discoverFiles() {
        if (cachedDiscoveredFiles != null) {
            return cachedDiscoveredFiles;
        }

        File fileOrDirectory = getFileOrDirectory();

        List<DeploymentFile> result;

        if (fileOrDirectory == null) {
            result = Collections.emptyList();
        }
        else if (fileOrDirectory.isDirectory()) {
            result = discoverFilesInDirectory(fileOrDirectory);
        }
        else {
            result = discoverSingleFile(fileOrDirectory);
        }

        cachedDiscoveredFiles = result;

        return result;
    }

    private List<DeploymentFile> discoverSingleFile(File file) {
        return Collections.singletonList((DeploymentFile) new FileDeploymentFile(file));
    }

    public static List<DeploymentFile> discoverFilesInDirectory(File directory) {
        FileDiscoverer discoverer =
            new ImportOrderOrDirectoryFileDiscoverer(directory);

        try {
            return discoverer.getFilesToImport();
        } catch (NotApplicableException e) {
            System.err.println("Could not find any content in directory " +
                directory.getAbsolutePath() + ": " + e.getMessage());
            System.exit(1);

            throw new RuntimeException();
        }
    }

    public File getDirectory() {
        if (!directoryOrFile.isDirectory()) {
            directoryOrFile = directoryOrFile.getParentFile();
        }

        return directoryOrFile;
    }

}
