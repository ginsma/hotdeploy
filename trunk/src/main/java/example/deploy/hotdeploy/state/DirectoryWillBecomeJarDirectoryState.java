package example.deploy.hotdeploy.state;

import java.io.File;
import java.util.Collections;
import java.util.List;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.file.FileDeploymentDirectory;
import example.deploy.hotdeploy.file.FileDeploymentFile;

public class DirectoryWillBecomeJarDirectoryState implements DirectoryState {
    private DirectoryState delegateState;
    private String jarFileName;
    private List<FileDeploymentDirectory> directories;

    public DirectoryWillBecomeJarDirectoryState(DirectoryState delegateState, FileDeploymentDirectory directory, String jarFileName) {
        this(delegateState, Collections.singletonList(directory), jarFileName);
    }

    public DirectoryWillBecomeJarDirectoryState(DirectoryState delegateState, List<FileDeploymentDirectory> directories, String jarFileName) {
        this.delegateState = delegateState;
        this.jarFileName = jarFileName;
        this.directories = directories;
    }

    public boolean hasFileChanged(DeploymentFile file) {
        return delegateState.hasFileChanged(transform(file));
    }

    public void persist() throws CouldNotUpdateStateException {
        delegateState.persist();
    }

    public void reset(DeploymentFile file, boolean failed) {
        delegateState.reset(transform(file), failed);
    }

    private DeploymentFile transform(DeploymentFile file) {
        if (!(file instanceof FileDeploymentFile)) {
            return file;
        }

        for (FileDeploymentDirectory directory : directories) {
            if (directory.imports(file)) {
                String relativeName = directory.getRelativeName(file);

                if (File.separatorChar != '/') {
                    relativeName = relativeName.replace(File.separatorChar, '/');
                }

                if (relativeName.startsWith("/")) {
                    relativeName = relativeName.substring(1);
                }

                final String finalRelativeName = relativeName;

                return new FileDeploymentFile(((FileDeploymentFile) file).getFile()) {
                    @Override
                    public String getName() {
                        return jarFileName + '!' + finalRelativeName;
                    }
                };
            }

        }

        return file;
    }
}
