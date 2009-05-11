package example.deploy.xml.parser;

import static example.deploy.hotdeploy.client.Major.ARTICLE;
import static example.deploy.hotdeploy.client.Major.DEPARTMENT;
import static example.deploy.hotdeploy.client.Major.OUTPUT_TEMPLATE;
import static example.deploy.hotdeploy.client.Major.UNKNOWN;

import java.io.File;

import junit.framework.TestCase;
import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.discovery.PlatformNeutralPath;
import example.deploy.hotdeploy.file.FileDeploymentFile;

public class TestParser extends TestCase {
    private static final String DIRECTORY = "src/test/resources/parsertest/";
    private static final String BOOTSTRAP_TEMPLATE_FILE = DIRECTORY +
    		"bootstrapTemplate.xml";
    private static final String BOOTSTRAP_CONTENT_FILE = DIRECTORY +
    		"bootstrapContent.xml";
    private static final String MODIFY_CONTENT_FILE = DIRECTORY +
    		"modifyContent.xml";
    private static final String CIRCULAR_REFERENCE_CONTENT_FILE = DIRECTORY +
    		"circularReferenceContent.xml";
    private static final String COMPLEX_CONTENT_FILE = DIRECTORY +
    		"complexContent.xml";
    private static final String SIMPLE_TEMPLATE_FILE = DIRECTORY +
                "simpleTemplate.xml";
    private static final String TEMPLATE_WITH_LAYOUTS_FILE = DIRECTORY +
                "templateWithLayouts.xml";
    private static final String TRICKY_TEMPLATES_FILE = DIRECTORY +
                "trickyTemplates.xml";
    private static final String OUTPUT_TEMPLATES_FILE = DIRECTORY +
                "outputTemplates.xml";

    private ParserAsserts parserAsserts;

    public void testBootstrapTemplate() {
        parse(BOOTSTRAP_TEMPLATE_FILE);

        parserAsserts.assertFoundTemplates(new String[] {"example.Template"});
        parserAsserts.assertFoundContent(new String[] {});
        parserAsserts.assertFoundClassReferences(new String[] {});
        parserAsserts.assertFoundContentReferences(new ParsedContentId[] {});
        parserAsserts.assertFoundTemplateReferences(new String[] {});
    }

    public void testBootstrapContent() {
        parse(BOOTSTRAP_CONTENT_FILE);

        parserAsserts.assertFoundTemplates(new String[] {});
        parserAsserts.assertFoundContent(new String[] { "example.Content" });
        parserAsserts.assertFoundClassReferences(new String[] {});
        parserAsserts.assertFoundContentReferences(new ParsedContentId[] {});
        parserAsserts.assertFoundTemplateReferences(new String[] {});
    }

    public void testModifyContent() {
        parse(MODIFY_CONTENT_FILE);

        parserAsserts.assertFoundTemplates(new String[] {});
        parserAsserts.assertFoundContent(new String[] { });
        parserAsserts.assertFoundClassReferences(new String[] {});
        parserAsserts.assertFoundContentReferences(new ParsedContentId[] {
                contentId(UNKNOWN, "example.Content"),
                contentId(ARTICLE, "example.ReferredContent") });
        parserAsserts.assertFoundTemplateReferences(new String[] {});
    }

    public void testCircularReferenceContent() {
        parse(CIRCULAR_REFERENCE_CONTENT_FILE);

        parserAsserts.assertFoundTemplates(new String[] {});
        parserAsserts.assertFoundContent(new String[] { "example.Content" });
        parserAsserts.assertFoundClassReferences(new String[] {});
        parserAsserts.assertFoundContentReferences(new ParsedContentId[] {
                contentId(ARTICLE, "example.Content") });
        parserAsserts.assertFoundTemplateReferences(new String[] {});
    }

    public void testComplexContent() {
        parse(COMPLEX_CONTENT_FILE);

        parserAsserts.assertFoundTemplates(new String[] {});
        parserAsserts.assertFoundContent(new String[] {
                "p.siteengine.LocalizedStrings.d", "localizedstrings.swedish", "localizedstrings.english", "article.communityconfiguration" });
        parserAsserts.assertFoundClassReferences(new String[] {});
        parserAsserts.assertFoundContentReferences(new ParsedContentId[] {
                contentId(UNKNOWN, "article.communityconfiguration"),
                contentId(UNKNOWN, "p.siteengine.Configuration.d"),
                contentId(UNKNOWN, "p.siteengine.Configuration.d"),
                contentId(UNKNOWN, "localizedstrings.english"),
                contentId(DEPARTMENT, "securityparent"),
                contentId(UNKNOWN, "localizedstrings.swedish") });
        parserAsserts.assertFoundTemplateReferences(new String[] {"p.siteengine.CommunityConfiguration", "p.siteengine.LocalizedStringsDepartment", "p.siteengine.LocalizedStrings"});
    }

    public void testSimpleTemplate() {
        parse(SIMPLE_TEMPLATE_FILE);

        parserAsserts.assertFoundTemplates(new String[] { "example.Monitor"});
        parserAsserts.assertFoundContent(new String[] {});
        parserAsserts.assertFoundClassReferences(new String[] { "my.Policy", "com.polopoly.cm.app.widget.OTopPolicyWidget" });
        parserAsserts.assertFoundContentReferences(new ParsedContentId[] {});
        parserAsserts.assertFoundTemplateReferences(new String[] { "p.ContentVersionLimiter" });
    }

    public void testTemplateWithLayouts() {
        parse(TEMPLATE_WITH_LAYOUTS_FILE);

        parserAsserts.assertFoundTemplates(new String[] { "p.UserSessionFrameTemplate"});
        parserAsserts.assertFoundContent(new String[] {});
        parserAsserts.assertFoundClassReferences(new String[] { "com.polopoly.cm.app.widget.impl.OFramePagePolicyWidget", "ViewerWidget", "com.polopoly.cm.app.widget.OTopPolicyWidget" });
        parserAsserts.assertFoundContentReferences(new ParsedContentId[] {});
        parserAsserts.assertFoundTemplateReferences(new String[] { "p.Clipboard", "p.PreviewControl", "p.Page", "p.InlinePageMenu" });
    }

    public void testTrickyTemplates() {
        parse(TRICKY_TEMPLATES_FILE);

        parserAsserts.assertFoundTemplates(new String[] { "example.Article", "example.NewsletterContentListWrapper"});
        parserAsserts.assertFoundContent(new String[] {});
        parserAsserts.assertFoundClassReferences(new String[] { "com.polopoly.cm.app.policy.ConfigurableContentListWrapper" });
        parserAsserts.assertFoundContentReferences(new ParsedContentId[] {
                contentId(UNKNOWN, "example.Image") });
        parserAsserts.assertFoundTemplateReferences(new String[] { "example.Image", "it.wid.ContentCreator" });
    }

    public void testOutputTemplates() {
        parse(OUTPUT_TEMPLATES_FILE);

        parserAsserts.assertFoundTemplates(new String[] { "example.FlashElement" });
        parserAsserts.assertFoundContent(new String[] { "example.FlashElement.ot" });
        parserAsserts.assertFoundContentReferences(new ParsedContentId[] {
                contentId(OUTPUT_TEMPLATE, "example.FlashElement.ot") });
        parserAsserts.assertFoundTemplateReferences(new String[] { "p.siteengine.ElementOutputTemplate" });
    }

    private ParsedContentId contentId(Major major, String externalId) {
        return new ParsedContentId(major, externalId);
    }

    @Override
    public void setUp() {
        parserAsserts = new ParserAsserts();
    }

    private void parse(String fileName) {
        new XmlParser().parse(new FileDeploymentFile(new File(
            PlatformNeutralPath.unixToPlatformSpecificPath(fileName))), parserAsserts);
    }

}
