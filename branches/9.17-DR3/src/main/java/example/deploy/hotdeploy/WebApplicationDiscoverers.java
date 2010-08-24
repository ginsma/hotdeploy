package example.deploy.hotdeploy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import example.deploy.hotdeploy.discovery.DefaultDiscoveryDirectories;
import example.deploy.hotdeploy.discovery.DeploymentDirectoryDiscoverer;
import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.ResourceFileDiscoverer;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import example.deploy.hotdeploy.file.DeploymentDirectory;

public class WebApplicationDiscoverers {
    public static List<FileDiscoverer> getWebAppDiscoverers(ServletContext servletContext) {
        File rootDirectory = new File(servletContext.getRealPath("/"));

        Collection<DeploymentDirectory> directories =
            new DeploymentDirectoryDiscoverer(rootDirectory,
                DefaultDiscoveryDirectories.getDirectories()).getDiscoveredDirectories();

        List<FileDiscoverer> discoverers = new ArrayList<FileDiscoverer>();
        discoverers.add(new ResourceFileDiscoverer(false));

        for (DeploymentDirectory directory : directories) {
            discoverers.add(new ImportOrderFileDiscoverer(directory));
        }
        return discoverers;
    }
}
