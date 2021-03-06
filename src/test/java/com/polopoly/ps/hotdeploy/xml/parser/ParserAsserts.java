package com.polopoly.ps.hotdeploy.xml.parser;

import static com.polopoly.ps.hotdeploy.client.Major.INPUT_TEMPLATE;

import java.util.HashSet;
import java.util.Set;

import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.xml.parser.ParseCallback;
import com.polopoly.ps.hotdeploy.xml.parser.ParseContext;
import com.polopoly.ps.hotdeploy.xml.parser.ParsedContentId;

import junit.framework.Assert;

public class ParserAsserts implements ParseCallback {
    Set<String> foundTemplates = new HashSet<String>();
    Set<String> foundContent = new HashSet<String>();
    Set<String> foundTemplateReferences = new HashSet<String>();
    Set<ParsedContentId> foundContentReferences = new HashSet<ParsedContentId>();
    Set<String> foundClassReferences = new HashSet<String>();

    public void assertFoundTemplates(String... expectTemplates) {
        Assert.assertEquals(asSet(expectTemplates), foundTemplates);
    }

    public void assertFoundContent(String... expectContent) {
        Assert.assertEquals(asSet(expectContent), foundContent);
    }

    public void assertFoundClassReferences(String... expectClassReferences) {
        Assert.assertEquals(asSet(expectClassReferences), foundClassReferences);
    }

    public void assertFoundTemplateReferences(String... expectTemplateReferences) {
        Assert.assertEquals(asSet(expectTemplateReferences), foundTemplateReferences);
    }

    public void assertFoundContentReferences(ParsedContentId... expectContentReferences) {
        Assert.assertEquals(asSet(expectContentReferences), foundContentReferences);
    }

    <T> HashSet<T> asSet(T[] array) {
        HashSet<T> result = new HashSet<T>();

        for (T object : array) {
            result.add(object);
        }

        return result;
    }

    public void classReferenceFound(DeploymentFile file, String string) {
        foundClassReferences.add(string);
    }

    public void contentFound(ParseContext context, String externalId,
            Major major, String inputTemplate) {
        if (major == INPUT_TEMPLATE) {
            foundTemplates.add(externalId);
        }
        else {
            foundContent.add(externalId);
        }
    }

    public void contentReferenceFound(ParseContext context, Major major, String externalId) {
        if (major == INPUT_TEMPLATE) {
            foundTemplateReferences.add(externalId);
        }
        else {
            foundContentReferences.add(new ParsedContentId(major, externalId));
        }
    }
}
