package example.deploy.xml.parser;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;

class XmlIoParser extends AbstractParser {
    private static final Logger logger =
        Logger.getLogger(XmlIoParser.class.getName());

    XmlIoParser(DeploymentFile file, Element root, ParseCallback callback) {
        super(file, callback);

        for (Element content : children(root)) {
            if (content.getNodeName().equals("content")) {
                parseContent(content);
            }
        }
    }

    private void parseContent(Element content) {
        for (Element element : children(content)) {
            if (element.getNodeName().equals("metadata")) {
                parseMetadata(element);
            }
            else {
                findContentReferences(element);
            }
        }
    }

    private void parseMetadata(Element metadata) {
        String externalId = null;
        String inputTemplate = null;

        Major major = null;

        for (Element metadataChild : children(metadata)) {
            String nodeName = metadataChild.getNodeName();

            if (nodeName.equals("input-template")) {
                for (Element inputTemplateElement : children(metadataChild)) {
                    if (inputTemplateElement.getNodeName().equals("externalid")) {
                        inputTemplate = inputTemplateElement.getTextContent().trim();
                    }
                }
            }
            else if (nodeName.equals("contentid")) {
                for (Element externalIdElement : children(metadataChild)) {
                    if (externalIdElement.getNodeName().equals("major")) {
                        String majorName = externalIdElement.getTextContent();

                        major = Major.getMajor(majorName);
                    }
                    else if (externalIdElement.getNodeName().equals("externalid")) {
                        externalId = externalIdElement.getTextContent().trim();
                    }
                }

                if (major == Major.UNKNOWN) {
                    logger.log(Level.WARNING, "The major used to specify the object with external ID \"" + externalId + "\" in file " + file + " was unknown.");
                }
            }
        }

        // objects can only be created if the major is specified.
        if (major != null) {
            if (major == Major.INPUT_TEMPLATE) {
                callback.templateFound(file, externalId);
            }
            else {
                callback.contentFound(file, externalId, major, inputTemplate);
            }
        }
        else {
            callback.contentReferenceFound(file, externalId);
        }

        if (inputTemplate != null) {
            callback.templateReferenceFound(file, inputTemplate);
        }
    }
}
