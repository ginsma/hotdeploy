package com.polopoly.ps.hotdeploy.xml.export;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.xml.util.export.DefaultContentContentsExporter;

import com.polopoly.ps.hotdeploy.discovery.PlatformNeutralPath;
import com.polopoly.ps.hotdeploy.manualtest.ManualTestCase;
import com.polopoly.ps.hotdeploy.xml.export.contentlistentry.ContentIdFilterToContentReferenceFilterWrapper;
import com.polopoly.ps.hotdeploy.xml.export.filteredcontent.PresentContentFilter;
import com.polopoly.ps.hotdeploy.xml.normalize.DefaultNormalizationNamingStrategy;

public class TestExporter extends ManualTestCase {
    private static final Logger logger =
        Logger.getLogger(TestExporter.class.getName());

    private Set<ExportedContent> exportedContentSet = new HashSet<ExportedContent>();
    private Set<ExportedContent> cleanUpContents = new HashSet<ExportedContent>();

    private HashSet<ExportedContent> failures;
    private HashSet<ExportedArticle> presentArticles;
    private int articleCounter;
    private DefaultNormalizationNamingStrategy namingStrategy;

    private static File directory;

    public void testSingleContentNoReferences() throws Exception {
        ExportedArticle article = article();

        exportToFile(article);

        cleanUpAll();
        importFromFile(article);

        assertNoFailures();
    }

    public void testExportBothOfTwoContentsWithContentList() throws Exception {
        ExportedArticle exported1 = article();
        ExportedArticle exported2 = article();

        contentListReference(exported1, exported2);

        exportToFile(exported1, exported2);

        cleanUpAll();
        importFromFile(exported1, exported2);

        assertNoFailures();
    }

    public void testExportBothOfTwoContentsWithReference() throws Exception {
        ExportedArticle exported1 = article();
        ExportedArticle exported2 = article();

        contentReference(exported1, exported2);

        exportToFile(exported1, exported2);

        cleanUpAll();
        importFromFile(exported1, exported2);

        assertNoFailures();
    }

    public void testExportOneOfTwoContentsWithContentList() throws Exception {
        ExportedArticle exportedReferringArticle = article();
        ExportedArticle exportedReferredArticle = article();

        ExportedContent exportedReference =
            contentListReference(exportedReferringArticle, exportedReferredArticle);

        exportToFile(exportedReferringArticle);

        cleanUpAll();

        importFromFile(exportedReferringArticle);

        validate();

        assertTrue(!failures.contains(exportedReferringArticle));
        assertTrue(failures.contains(exportedReferredArticle));
        assertTrue(failures.contains(exportedReference));
    }

    public void testExportOneOfTwoContentsWithReference() throws Exception {
        ExportedArticle exportedReferringArticle = article();
        ExportedArticle exportedReferredArticle = article();

        ExportedContent exportedReference =
            contentReference(exportedReferringArticle, exportedReferredArticle);

        exportToFile(exportedReferringArticle);

        cleanUpAll();

        importFromFile(exportedReferringArticle);

        validate();

        assertTrue(!failures.contains(exportedReferringArticle));
        assertTrue(failures.contains(exportedReferredArticle));
        assertTrue(failures.contains(exportedReference));
    }

    public void testExportOneOfTwoContentsWithReferenceToPresent() throws Exception {
        ExportedArticle exportedReferringArticle = article();
        ExportedArticle exportedReferredArticle = article();

        contentReference(exportedReferringArticle, exportedReferredArticle);

        present(exportedReferredArticle);

        exportToFile(exportedReferringArticle);

        cleanUpAllExceptPresent();

        importFromFile(exportedReferringArticle);

        assertNoFailures();
    }

    public void testExportCircularReferences() throws Exception {
        ExportedArticle exportedArticle1 = article();
        ExportedArticle exportedArticle2 = article();

        contentListReference(exportedArticle1, exportedArticle2);
        contentListReference(exportedArticle2, exportedArticle1);

        exportToFile(exportedArticle1, exportedArticle2);

        cleanUpAll();

        importFromFile(exportedArticle1, exportedArticle2);

        assertNoFailures();
    }

    public void testExportSecurityParent() throws Exception {
        ExportedArticle exportedArticle1 = article();
        ExportedArticle exportedArticle2 = article();
        securityParent(exportedArticle1, exportedArticle2);

        exportToFile(exportedArticle1, exportedArticle2);

        cleanUpAll();

        importFromFile(exportedArticle1, exportedArticle2);

        assertNoFailures();
    }

    public void testDontExportSecurityParent() throws Exception {
        ExportedArticle exportedArticle1 = article();
        ExportedArticle exportedArticle2 = article();
        securityParent(exportedArticle1, exportedArticle2);

        exportToFile(exportedArticle1);

        cleanUpAll();

        importFromFile(exportedArticle1);

        validate();

        assertTrue(!failures.contains(exportedArticle1));
        assertTrue(failures.contains(exportedArticle2));

        ContentId defaultParent =
            context.getContent(FilteringSecurityParentIdExporter.DEFAULT_SECURITY_PARENT).
                getContentId().unversioned();

        assertEquals(defaultParent, context.getPolicyUtil(exportedArticle1.getExternalIdString()).getContent().getSecurityParentId());
    }

    private void present(ExportedArticle article) {
        presentArticles.add(article);
    }

    private ExportedContent securityParent(ExportedArticle exportedArticle,
            ExportedArticle securityParent) throws Exception {
        return new ExportedSecurityParent(exportedArticle, securityParent);
    }

    private ExportedContentListReference contentListReference(ExportedArticle fromArticle,
            ExportedArticle toArticle) throws Exception {
        ExportedContentListReference reference = new ExportedContentListReference(fromArticle, toArticle);

        exportedContentSet.add(reference);
        cleanUpContents.add(reference);

        return reference;
    }

    private ExportedContentReference contentReference(ExportedArticle fromArticle,
            ExportedArticle toArticle) throws Exception {
        ExportedContentReference reference = new ExportedContentReference(fromArticle, toArticle);

        exportedContentSet.add(reference);
        cleanUpContents.add(reference);

        return reference;
    }

    private void importFromFile(ExportedContent... contentToImport) throws Exception {
        for (ExportedContent exportedContent : contentToImport) {
            exportedContent.prepareImport();
        }

        for (ExportedContent exportedContent : contentToImport) {
            exportedContent.importFromFile(namingStrategy);
            cleanUpContents.add(exportedContent);
        }
    }

    private void cleanUpAll() throws Exception {
        Iterator<ExportedContent> it = cleanUpContents.iterator();

        while (it.hasNext()) {
            ExportedContent contentToCleanup = it.next();

            contentToCleanup.cleanUp(context);

            it.remove();
        }
    }

    private void cleanUpAllExceptPresent() throws Exception {
        Iterator<ExportedContent> it = cleanUpContents.iterator();

        while (it.hasNext()) {
            ExportedContent contentToCleanup = it.next();

            if (!presentArticles.contains(contentToCleanup)) {
                contentToCleanup.cleanUp(context);
            }

            it.remove();
        }
    }

    private void validate() throws Exception {
        failures = new HashSet<ExportedContent>();

        for (ExportedContent exportedContent : exportedContentSet) {
            if (!exportedContent.validate(context)) {
                failures.add(exportedContent);
            }
        }
    }

    private ExportedArticle article() throws Exception {
        ExportedArticle result = new ExportedArticle(articleCounter++, context);

        result.create();

        exportedContentSet.add(result);
        cleanUpContents.add(result);

        return result;
    }

    private void assertNoFailures() throws Exception {
        validate();
        assertTrue(failures.toString(), failures.isEmpty());
    }

    void exportToFile(ExportedArticle... articlesToExport) {
        PresentContentFilter presentContentFilter = new PresentContentFilter(context.getPolicyCMServer());

        for (ExportedArticle presentArticle : presentArticles) {
            presentContentFilter.presentContent(presentArticle.getExternalIdString());
        }

        ContentsExporterFactory contentsExporterFactory =
            new ContentsExporterFactory(context.getPolicyCMServer(), context.getUserServer(),
                new ContentIdFilterToContentReferenceFilterWrapper(presentContentFilter));

        Set<ContentId> contentIdsToExportSet = new HashSet<ContentId>();

        for (ExportedArticle article : articlesToExport) {
            contentIdsToExportSet.add(article.getContentId());
        }

        DefaultContentContentsExporter exporter = contentsExporterFactory.
            createContentsExporter(contentIdsToExportSet);

        NormalizedFileExporter normalizedFileExporter =
            new NormalizedFileExporter(context.getPolicyCMServer(), exporter, namingStrategy);

        normalizedFileExporter.setExternalIdGenerator(contentsExporterFactory.getExternalIdGenerator());

        normalizedFileExporter.export(contentIdsToExportSet);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        presentArticles = new HashSet<ExportedArticle>();

        articleCounter = 1;
        exportedContentSet.clear();

        directory = new File(PlatformNeutralPath.unixToPlatformSpecificPath("target/exporttarget"));

        if (!directory.exists()) {
            directory.mkdir();
        }

        namingStrategy = new DefaultNormalizationNamingStrategy(directory);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        cleanUpAll();

        deleteDirectory(directory);
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isDirectory() && !file.getName().startsWith(".")) {
                deleteDirectory(file);
            }
            else {
                if (!file.delete()) {
                    System.err.println("Could not delete " + file + ".");
                }
            }
        }

        if (directory.delete()) {
            logger.log(Level.INFO, "Cleaning up. Deleted " + directory + ".");
        }
        else {
            logger.log(Level.WARNING, "Could not delete " + directory + ".");
        }
    }
}
