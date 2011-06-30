package com.polopoly.ps.hotdeploy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import com.polopoly.ps.hotdeploy.discovery.DefaultDiscoveryDirectories;
import com.polopoly.ps.hotdeploy.discovery.DeploymentDirectoryDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.FileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.ResourceFileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import com.polopoly.ps.hotdeploy.file.DeploymentDirectory;


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
