package com.polopoly.ps.hotdeploy.xml.bootstrap;

import static com.polopoly.ps.hotdeploy.client.Major.ARTICLE;
import static com.polopoly.ps.hotdeploy.client.Major.DEPARTMENT;
import static com.polopoly.ps.hotdeploy.client.Major.INPUT_TEMPLATE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.state.DummyDeploymentFile;
import com.polopoly.ps.hotdeploy.text.TextContentSet;
import com.polopoly.ps.hotdeploy.xml.parser.DeploymentFileParser;
import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;
import com.polopoly.ps.hotdeploy.xml.parser.cache.FileParseCallbackMemento;

public class TestBootstrapGenerator extends TestCase {
    private static final String CONTENT1 = "content1";
    private static final String CONTENT2 = "content2";
    private static final String CONTENT3 = "content3";
    private static final String INPUT_TEMPLATE1 = "template1";

    private Map<DeploymentFile, FileParseCallbackMemento> mementosByFile;
    private Bootstrap bootstrap;
    private Iterator<BootstrapContent> bootstrapIterator;
    private int fileCounter;

    @Override
    public void setUp() {
        mementosByFile = new HashMap<DeploymentFile, FileParseCallbackMemento>();
        fileCounter = 0;
    }

    public void testSimplestCase() {
        DeploymentFile file1 = createFile();

        register(file1).contentReferenceFound(Major.ARTICLE, CONTENT1);
        register(file1).contentFound(CONTENT1, ARTICLE, INPUT_TEMPLATE1);

        generateBootstrap(file1);

        assertBootstrap(new BootstrapContent(ARTICLE, CONTENT1));
        assertNoMoreBootstrap();
    }

    public void testSingleFile() {
        DeploymentFile file1 = createFile();

        register(file1).contentFound(CONTENT1, ARTICLE, INPUT_TEMPLATE1);
        register(file1).contentReferenceFound(Major.ARTICLE, CONTENT1);
        register(file1).contentReferenceFound(Major.ARTICLE, CONTENT2);
        register(file1).contentFound(CONTENT2, DEPARTMENT, INPUT_TEMPLATE1);

        generateBootstrap(file1);

        assertBootstrap(new BootstrapContent(DEPARTMENT, CONTENT2));
        assertNoMoreBootstrap();
    }

    public void testTwoFilesWithInputTemplates() {
        DeploymentFile file1 = createFile();
        DeploymentFile file2 = createFile();

        register(file1).templateFound(CONTENT1);
        register(file1).templateReferenceFound(CONTENT1);
        register(file2).templateReferenceFound(CONTENT1);
        register(file2).templateReferenceFound(CONTENT2);
        register(file2).templateFound(CONTENT2);
        register(file2).templateReferenceFound(CONTENT3);

        generateBootstrap(file1, file2);

        assertBootstrap(new BootstrapContent(INPUT_TEMPLATE, CONTENT2));
        assertNoMoreBootstrap();
    }

    private void assertNoMoreBootstrap() {
        assertFalse(bootstrapIterator.hasNext());
    }

    private void assertBootstrap(BootstrapContent bootstrapContent) {
        BootstrapContent next = bootstrapIterator.next();
        assertEquals(bootstrapContent, next);
        // major is not part of bootstrapcontent's equals
        assertEquals(bootstrapContent.getMajor(), next.getMajor());
    }

    private void generateBootstrap(DeploymentFile... files) {
        DeploymentFileParser parser = new DeploymentFileParser() {
            public TextContentSet parse(DeploymentFile file, ParseCallback callback) {
                return mementosByFile.get(file).replay(callback);
            }};

        bootstrap = new BootstrapGenerator(parser).generateBootstrap(Arrays.asList(files));

        bootstrapIterator = bootstrap.iterator();
    }

    private ParseCallbackAddedContext register(DeploymentFile file) {
        return new ParseCallbackAddedContext(file, mementosByFile.get(file));
    }

    private DeploymentFile createFile() {
        DummyDeploymentFile result = new DummyDeploymentFile(Integer.toString(fileCounter++));

        mementosByFile.put(result, new FileParseCallbackMemento(result));

        return result;
    }

}
