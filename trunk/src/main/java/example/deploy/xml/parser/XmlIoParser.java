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
        ParsedContentId contentId = null;
        String inputTemplate = null;
        ParsedContentId securityParentId = null;

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
                contentId = parseContentId(metadataChild);
            }
            else if (nodeName.equals("security-parent")) {
                securityParentId = parseContentId(metadataChild);
            }
        }

        if (contentId != null) {
            Major major = contentId.getMajor();

            if (major == Major.INPUT_TEMPLATE) {
                callback.templateFound(file, contentId.getExternalId());
            }
            // objects can only be created if the major is specified.
            else if (major == Major.UNKNOWN) {
                callback.contentReferenceFound(file, contentId.getMajor(), contentId.getExternalId());
            }
            else {
                callback.contentFound(file, contentId.getExternalId(), major, inputTemplate);
            }
        }
        else {
            logger.log(Level.WARNING, "There was a metadata definition in " + file + " that did not contain an external ID.");
        }

        if (securityParentId != null) {
            callback.contentReferenceFound(file, securityParentId.getMajor(), securityParentId.getExternalId());
        }

        if (inputTemplate != null) {
            callback.templateReferenceFound(file, inputTemplate);
        }
    }
}
