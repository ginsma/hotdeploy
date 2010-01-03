package com.polopoly.ps.hotdeploy.xml.parser;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.polopoly.ps.hotdeploy.file.ResourceFile;
import com.polopoly.ps.hotdeploy.text.ExternalIdReference;
import com.polopoly.ps.hotdeploy.text.Reference;
import com.polopoly.ps.hotdeploy.text.TextContent;
import com.polopoly.ps.hotdeploy.text.TextContentSet;

/**
 * Note that parsing content XML to TextContent is not fully implemented, for
 * example it doesn't support content lists.
 */
public class ContentXmlParserTest extends TestCase {
	private TextContent content;

	public void setUp() throws Exception {
		ResourceFile file = new ResourceFile("/" + getClass().getName() + ".xml");
		TextContentSet allContent = new ContentXmlParser().parse(file, new DefaultParseCallback(), true);

		Assert.assertEquals(1, allContent.size());

		content = allContent.iterator().next();
	}

	public void testExternalId() {
		Assert.assertEquals("externalId", content.getId());
	}

	public void testComponents() {
		Assert.assertEquals("Name", content.getComponent("polopoly.Content", "name"));
		Assert.assertEquals("Category",
				content.getComponent("categorization[department.categorydimension.tag.Tag]", "text"));
	}

	public void testInputTemplate() {
		Assert.assertEquals("inputtemplate", toExternalId(content.getInputTemplate()));
	}

	public void testSecurityParent() {
		Assert.assertEquals("securityparent", toExternalId(content.getSecurityParent()));
	}

	private String toExternalId(Reference reference) {
		return ((ExternalIdReference) reference).getExternalId();
	}

	public void testFile() throws Exception {
		Assert.assertEquals("file content", new String(content.getFileData("image/file.txt"), "UTF-8"));
	}

	public void testContentListsAsReferences() {
		Assert.assertEquals("category.mycategory",
				toExternalId(content.getReference("categorization[department.categorydimension.subject]", "1")));

		Assert.assertEquals("image1", toExternalId(content.getReference("images", "107")));
		Assert.assertEquals("image2", toExternalId(content.getReference("images", "108")));
	}
}
