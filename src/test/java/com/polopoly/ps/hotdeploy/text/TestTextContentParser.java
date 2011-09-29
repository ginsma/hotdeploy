package com.polopoly.ps.hotdeploy.text;

import static com.polopoly.cm.server.ServerNames.CONTENT_ATTRG_SYSTEM;
import static com.polopoly.cm.server.ServerNames.CONTENT_ATTR_NAME;

import java.io.InputStream;

import com.polopoly.ps.hotdeploy.validation.StandAloneValidationContext;
import com.polopoly.ps.hotdeploy.validation.ValidationException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestTextContentParser extends TestCase {
	private TextContentSet parsed;

	@Override
	protected void setUp() throws Exception {
		String resourceName = "/textcontent/simplecontent.content";
		InputStream is = getClass().getResourceAsStream(resourceName);

		if (is == null) {
			fail("resource not found.");
		}

		TextContentParser parser = new TextContentParser(is, getClass()
				.getResource(resourceName), resourceName);

		Assert.assertEquals("simplecontent", parser.getFileName());

		parsed = parser.parse();
	}

	public void testValidate() throws ValidationException {
		parsed.validate(new StandAloneValidationContext());
	}

	public void testSimpleContent() throws Exception {
		TextContent article = parsed.get("textcontent.simplearticle");

		assertEquals("textcontent.simplearticle", article.getId());
		assertEquals("Test Page: name",
				article.getComponent(CONTENT_ATTRG_SYSTEM, CONTENT_ATTR_NAME));
		assertEqualsReference("p.siteengine.Page", article.getInputTemplate());

		assertEquals("line1\nline2", article.getComponent("group", "name"));

		article = parsed.get("textcontent.simplecontenttest");

		assertEquals("textcontent.simplecontenttest", article.getId());
		assertEquals("Test Site",
				article.getComponent(CONTENT_ATTRG_SYSTEM, CONTENT_ATTR_NAME));
		assertEqualsReference("p.siteengine.Site", article.getInputTemplate());
		assertEqualsReference("p.siteengine.Sites.d",
				article.getSecurityParent());
		assertEqualsReference("com.polopoly.ps.DefaultPageLayout",
				article.getReference("pageLayout", "selected"));
		assertEquals(2, article.getList("polopoly.Department").size());
		assertEqualsReference("textcontent.simplearticle",
				article.getList("polopoly.Department").get(0));
		assertEqualsReference("textcontent.simplearticle2",
				article.getList("polopoly.Department").get(1));
		assertEqualsReference("p.siteengine.Sites.d", article.getPublishings()
				.get(0).getPublishIn());
	}

	private void assertEqualsReference(String string, Reference reference) {
		assertTrue(reference instanceof ExternalIdReference);
		assertEquals(string, ((ExternalIdReference) reference).getExternalId());
	}
}
