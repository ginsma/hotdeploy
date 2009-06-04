package example.deploy.xml.export.filteredcontent;

import java.io.File;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.util.ContentIdFilter;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.util.Plural;
import example.deploy.xml.present.PresentFileReader;

public class ProjectContentFilterFactory {
    private PolicyCMServer server;

    public ProjectContentFilterFactory(PolicyCMServer server) {
        this.server = server;
    }

    public ContentIdFilter getExistingObjectsFilter(File projectContentDirectory) {
        PresentContentFilter projectContentFilter =
            createProjectContentFilter(server, projectContentDirectory);

        System.err.println(Plural.count(projectContentFilter.getPresentIds(), "object") +
                " were product or project content.");

        return new OrContentIdFilter(
                new HotdeployStatusFilter(server),
                new MajorFilter(Major.MAJOR_CONFIG),
                new SecurityRootDepartmentFilter(),
                new InputTemplateFilter(server),
                projectContentFilter);
    }

    protected PresentContentFilter createProjectContentFilter(
            PolicyCMServer server, File projectContentDirectory) {
        PresentContentFilter presentContentFilter = new PresentContentFilter(server);

        PresentFileReader reader = new PresentFileReader(projectContentDirectory, presentContentFilter);

        reader.readAndScanContent();

        return presentContentFilter;
    }
}
