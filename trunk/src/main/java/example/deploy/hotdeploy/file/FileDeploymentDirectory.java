package example.deploy.hotdeploy.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDeploymentDirectory extends AbstractDeploymentObject implements DeploymentDirectory {
    private static final Logger logger =
        Logger.getLogger(FileDeploymentDirectory.class.getName());
    private File file;

    public FileDeploymentDirectory(File file) {
        this.file = file;
   }

    public boolean exists() {
        return file.exists() && file.canRead();
    }

    public DeploymentObject getFile(String fileName) throws FileNotFoundException {
        File newFile = new File(file, fileName);

        if (!newFile.exists()) {
            throw new FileNotFoundException("File " + newFile.getAbsolutePath() + " does not exist.");
        }

        if (newFile.isDirectory()) {
            return new FileDeploymentDirectory(newFile);
        }
        else {
            return new FileDeploymentFile(newFile);
        }

    }

    public DeploymentObject[] listFiles() {
        File[] files = file.listFiles();
        List<DeploymentObject> result = new ArrayList<DeploymentObject>(files.length);

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().startsWith(".")) {
                continue;
            }

            try {
                result.add(getFile(files[i].getName()));
            } catch (FileNotFoundException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }

        return result.toArray(new DeploymentObject[result.size()]);
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return file.getAbsolutePath();
    }
}
