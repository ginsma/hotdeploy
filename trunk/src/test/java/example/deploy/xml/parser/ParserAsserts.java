package example.deploy.xml.parser;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;

public class ParserAsserts implements ParseCallback {
    Set<String> foundTemplates = new HashSet<String>();
    Set<String> foundContent = new HashSet<String>();
    Set<String> foundTemplateReferences = new HashSet<String>();
    Set<ParsedContentId> foundContentReferences = new HashSet<ParsedContentId>();
    Set<String> foundClassReferences = new HashSet<String>();

    void assertFoundTemplates(String[] expectTemplates) {
        Assert.assertEquals(asSet(expectTemplates), foundTemplates);
    }

    void assertFoundContent(String[] expectContent) {
        Assert.assertEquals(asSet(expectContent), foundContent);
    }

    void assertFoundClassReferences(String[] expectClassReferences) {
        Assert.assertEquals(asSet(expectClassReferences), foundClassReferences);
    }

    void assertFoundTemplateReferences(String[] expectTemplateReferences) {
        Assert.assertEquals(asSet(expectTemplateReferences), foundTemplateReferences);
    }

    void assertFoundContentReferences(ParsedContentId[] expectContentReferences) {
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

    public void contentFound(DeploymentFile file, String externalId,
            Major major, String inputTemplate) {
        foundContent.add(externalId);
    }

    public void contentReferenceFound(DeploymentFile file, Major major, String externalId) {
        foundContentReferences.add(new ParsedContentId(major, externalId));
    }

    public void templateFound(DeploymentFile file, String inputTemplate) {
        foundTemplates.add(inputTemplate);
    }

    public void templateReferenceFound(DeploymentFile file, String inputTemplate) {
        foundTemplateReferences.add(inputTemplate);
    }
}
