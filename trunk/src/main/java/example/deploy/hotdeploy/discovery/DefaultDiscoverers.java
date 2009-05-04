package example.deploy.hotdeploy.discovery;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultDiscoverers {
    private static ArrayList<FileDiscoverer> discoverers;

    public static Collection<FileDiscoverer> getDiscoverers() {
        return discoverers;
    }

    public void addDiscoverer(FileDiscoverer discoverer) {
        discoverers.add(discoverer);
    }

    static {
        discoverers = new ArrayList<FileDiscoverer>(3);

        discoverers.add(new ResourceFileDiscoverer());
        discoverers.add(new FallbackDiscoverer(
                new ImportOrderFileDiscoverer(),
                new DirectoryFileDiscoverer()));
    }
}
