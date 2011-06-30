package com.polopoly.ps.hotdeploy.xml.export;

import java.util.Set;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.util.content.ContentReadUtil;

import com.polopoly.ps.hotdeploy.manualtest.ManualTestCase;
import com.polopoly.ps.hotdeploy.xml.export.filteredcontent.FilteredAllContentFinder;

public class TestAllNonPresentContentFinder extends ManualTestCase {
    private ContentReadUtil rootDepartment;
    private FilteredAllContentFinder finder;
    private ContentId rootDepartmentId;

    @Override
    public void setUp() throws Exception {
        rootDepartment = context.getContent("p.RootDepartment");
        rootDepartmentId = rootDepartment.getContentId().unversioned();

        finder = new FilteredAllContentFinder(context.getPolicyCMServer());

        finder.addFilter(new ContentIdFilter() {
            public boolean accept(ContentId contentId) {
                return contentId.equalsIgnoreVersion(rootDepartmentId);
            }});
    }

    public void test() throws Exception {
        Set<ContentId> nonPresent = finder.findAllNonPresentContent();

        assertFalse(nonPresent.isEmpty());
        assertTrue(nonPresent.contains(rootDepartmentId));
        assertEquals("Expected only " + rootDepartmentId + " but was " + nonPresent, 1, nonPresent.size());
    }
}
