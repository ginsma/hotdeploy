package example.deploy.xml.ordergenerator;

import java.io.File;
import java.util.Collections;
import java.util.List;

import example.deploy.hotdeploy.discovery.FileDiscoverer;
import example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer;
import example.deploy.hotdeploy.file.DeploymentFile;

public class TestGenerateOrderForRealProject {

    public void testDiscoverMtvuFiles() {
        DiscovereredFilesAggregator filesInDirectory =
            new DiscovereredFilesAggregator(Collections.singleton((FileDiscoverer)
                    new ImportOrderFileDiscoverer(new File("/projects/mtvu-trunk/src/resources"))));

        ImportOrderGenerator generator = new ImportOrderGenerator();
        List<DeploymentFile> result = generator.generate(filesInDirectory.getFiles());

        for (DeploymentFile deploymentFile : result) {
            System.out.println(deploymentFile);
        }
    }

}
