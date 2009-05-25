package example.deploy.xml.export.contentlistentry;

import com.polopoly.cm.VersionedContentId;

import example.deploy.hotdeploy.manualtest.ManualTestCase;
import example.deploy.xml.export.filteredcontent.PresentContentFilter;

public class TestPresentContentFilter extends ManualTestCase {
    private static final String P_ROOT_DEPARTMENT = "p.RootDepartment";
    private static final String P_DEFAULT_DEPARTMENT = "p.DefaultDepartment";
    private PresentContentFilter filter;

    @Override
    public void setUp() {
        filter = new PresentContentFilter(context);
    }

    public void testAddPresentContent() throws Exception {
        VersionedContentId rootDeptContentId =
            context.resolveExternalId(P_ROOT_DEPARTMENT);

        assertFalse(filter.accept(rootDeptContentId));
        filter.presentContent(P_ROOT_DEPARTMENT);
        assertTrue(filter.accept(rootDeptContentId));
    }

    public void testAddPresentTemplate() throws Exception {
        VersionedContentId defaultDeptContentId =
            context.resolveExternalId(P_DEFAULT_DEPARTMENT);

        assertFalse(filter.accept(defaultDeptContentId));
        filter.presentContent(P_DEFAULT_DEPARTMENT);
        assertTrue(filter.accept(defaultDeptContentId));
    }
}
