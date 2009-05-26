package example.deploy.xml.export.filteredcontent;

import java.io.File;

import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.util.Plural;
import example.deploy.xml.present.PresentFileReader;

public class ProjectContentFilterFactory {
    private PolopolyContext context;

    public ProjectContentFilterFactory(PolopolyContext context) {
        this.context = context;
    }

    public ContentIdFilter getExistingObjectsFilter(File projectContentDirectory) {
        PresentContentFilter projectContentFilter =
            createProjectContentFilter(context, projectContentDirectory);

        System.err.println(Plural.count(projectContentFilter.getPresentIds(), "object") +
                " were product or project content.");

        return new OrContentIdFilter(
                new HotdeployStatusFilter(context),
                new MajorFilter(Major.MAJOR_CONFIG),
                new SecurityRootDepartmentFilter(),
                new InputTemplateFilter(context),
                projectContentFilter);
    }

    protected PresentContentFilter createProjectContentFilter(
            PolopolyContext context, File projectContentDirectory) {
        PresentContentFilter presentContentFilter = new PresentContentFilter(context);

        PresentFileReader reader = new PresentFileReader(projectContentDirectory, presentContentFilter);

        reader.readAndScanContent();

        return presentContentFilter;
    }
}
