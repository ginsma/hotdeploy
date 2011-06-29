package com.polopoly.ps.deploy.xml.ordergenerator;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.polopoly.ps.deploy.hotdeploy.discovery.PlatformNeutralPath;
import com.polopoly.ps.deploy.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.deploy.hotdeploy.file.FileDeploymentFile;
import com.polopoly.ps.deploy.xml.ordergenerator.ImportOrderGenerator;

import junit.framework.TestCase;

public class TestOrderGenerator extends TestCase {
    private static final String DIRECTORY = PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources/ordertest/");

    private static final String FIELDS_FILE = DIRECTORY + "fields.xml";

    private static final String ARTICLE_FILE = DIRECTORY + "article.xml";

    private static final String SITE_FILE = DIRECTORY + "site.xml";

    HashSet<DeploymentFile> files = new HashSet<DeploymentFile>();

    public void testThreeFiles() {
        createFile(SITE_FILE);
        createFile(ARTICLE_FILE);
        createFile(FIELDS_FILE);

        ImportOrderGenerator generator = new ImportOrderGenerator();

        List<DeploymentFile> order = generator.generate(files);

        Iterator<DeploymentFile> iterator = order.iterator();

        assertIsNext(FIELDS_FILE, iterator);
        assertIsNext(ARTICLE_FILE, iterator);
        assertIsNext(SITE_FILE, iterator);
    }

    private void assertIsNext(String relativeExpectedFileName,
            Iterator<DeploymentFile> iterator) {
        String expectedFileName = new File(relativeExpectedFileName).getAbsolutePath();
        String actualFileName = iterator.next().getName();
        assertEquals(expectedFileName, actualFileName);
    }

    private void createFile(String fileName) {
        files.add(new FileDeploymentFile(new File(fileName)));
    }
}
