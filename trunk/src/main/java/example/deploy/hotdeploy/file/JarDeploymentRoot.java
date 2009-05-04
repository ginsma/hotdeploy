package example.deploy.hotdeploy.file;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarDeploymentRoot extends JarDeploymentDirectory {
    public JarDeploymentRoot(JarFile file) {
        super(file, null);
    }

    @Override
    protected boolean isInThisDir(JarEntry resultEntry) {
        String name = resultEntry.getName();
        int i = name.indexOf('/', 1);

        // (directories end with a slash)
        return i == -1 || i == name.length()-1;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public String getName() {
        return jarFile.getName();
    }
}
