package example.deploy.hotdeploy.text;

import static com.polopoly.cm.server.ServerNames.CONTENT_ATTRG_SYSTEM;
import static com.polopoly.cm.server.ServerNames.CONTENT_ATTR_NAME;

import java.io.InputStream;

import example.deploy.text.ExternalIdReference;
import example.deploy.text.Reference;
import example.deploy.text.StandAloneValidationContext;
import example.deploy.text.TextContent;
import example.deploy.text.TextContentParser;
import example.deploy.text.TextContentSet;
import example.deploy.text.ValidationException;

import junit.framework.TestCase;

public class TestTextContentParser extends TestCase {
    private TextContentSet parsed;

    @Override
    protected void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/textcontent/simplecontent.content");

        if (is == null) {
            fail("resource not found.");
        }

        TextContentParser parser = new TextContentParser(is);

        parsed = parser.parse();
    }

    public void testValidate() throws ValidationException {
        parsed.validate(new StandAloneValidationContext());
    }

    public void testSimpleContent() throws Exception {
        TextContent article = parsed.get("textcontent.simplearticle");

        assertEquals("textcontent.simplearticle", article.getId());
        assertEquals("Test Page", article.getComponent(CONTENT_ATTRG_SYSTEM, CONTENT_ATTR_NAME));
        assertEqualsReference("p.siteengine.Page", article.getInputTemplate());

        article = parsed.get("textcontent.simplecontenttest");

        assertEquals("textcontent.simplecontenttest", article.getId());
        assertEquals("Test Site", article.getComponent(CONTENT_ATTRG_SYSTEM, CONTENT_ATTR_NAME));
        assertEqualsReference("p.siteengine.Site", article.getInputTemplate());
        assertEqualsReference("p.siteengine.Sites.d", article.getSecurityParent());
        assertEqualsReference("example.DefaultPageLayout", article.getReference("pageLayout","selected"));
        assertEquals(2, article.getList("polopoly.Department").size());
        assertEqualsReference("textcontent.simplearticle", article.getList("polopoly.Department").get(0));
        assertEqualsReference("textcontent.simplearticle2", article.getList("polopoly.Department").get(1));
        assertEqualsReference("p.siteengine.Sites.d", article.getPublishIn());
    }

    private void assertEqualsReference(String string, Reference reference) {
        assertTrue(reference instanceof ExternalIdReference);
        assertEquals(string, ((ExternalIdReference) reference).getExternalId());
    }
}
