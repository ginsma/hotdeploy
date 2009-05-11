package example.deploy.xml.parser;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.CheckedCast;
import example.deploy.hotdeploy.util.CheckedClassCastException;

class TemplateDefinitionParser extends AbstractParser {
    TemplateDefinitionParser(DeploymentFile file, Element root, ParseCallback callback) {
        super(file, callback);

        for (Element inputTemplate : children(root)) {
            if (inputTemplate.getNodeName().equals("input-template")) {
                parseInputTemplate(inputTemplate);
            }
            else if (inputTemplate.getNodeName().equals("output-template")) {
                parseOutputTemplate(file, callback, inputTemplate);
            }
        }
    }

    private void parseOutputTemplate(DeploymentFile file,
            ParseCallback callback, Element outputTemplate) {
        String outputTemplatesInputTemplate =
            outputTemplate.getAttribute("input-template");

        String outputTemplateName = outputTemplate.getAttribute("name");

        callback.contentFound(file,
                outputTemplateName, Major.OUTPUT_TEMPLATE,
                outputTemplatesInputTemplate);
        callback.templateReferenceFound(file, outputTemplatesInputTemplate);
    }

    private void parseInputTemplate(Element inputTemplate) {
        String name = inputTemplate.getAttribute("name");

        callback.templateFound(file, name);

        parseTemplate(inputTemplate, name);
    }

    private void parseTemplate(Element inputTemplate, String name) {
        for (Element field : children(inputTemplate)) {
            String nodeName = field.getNodeName();

            if (nodeName.equals("content-list")) {
                parseContentList(field);
            }
            else if (nodeName.equals("policy") || nodeName.equals("viewer") || nodeName.equals("editor")) {
                callback.classReferenceFound(file, field.getTextContent().trim());
            }
            else if (nodeName.equals("layout") || nodeName.equals("field")) {
                parseFieldOrLayout(name, field);
            }
            else if (nodeName.equals("content-list-wrapper")) {
                String wrapperClass = field.getTextContent().trim();

                callback.classReferenceFound(file, wrapperClass);
            }
            else if (nodeName.equals("output-templates")) {
                for (Element templateId : children(field)) {
                    if (templateId.getNodeName().equals("id")) {
                        callback.contentReferenceFound(file, Major.OUTPUT_TEMPLATE, templateId.getTextContent().trim());
                    }
                }
            }
        }
    }

    private void parseFieldOrLayout(String name, Element field) {
        String fieldTemplate = field.getAttribute("input-template");
        callback.templateReferenceFound(file, fieldTemplate);

        for (Element param : children(field)) {
            if (param.getNodeName().equals("param")) {
                String paramName = param.getAttribute("name");

                if (paramName.equals("inputTemplateId")) {
                    callback.templateReferenceFound(file, param.getTextContent().trim());
                }
                else if (paramName.endsWith(".class")) {
                    callback.classReferenceFound(file, param.getTextContent().trim());
                }
            }
            else if (param.getNodeName().equals("idparam-list")) {
                NodeList ids = param.getChildNodes();

                for (int k = 0; k < ids.getLength(); k++) {
                    try {
                        Element id = CheckedCast.cast(ids.item(k), Element.class);

                        parseContentIdReference(id);
                    }
                    catch (CheckedClassCastException e) {
                    }
                }
            }
            else if (param.getNodeName().equals("idparam")) {
                parseContentIdReference(param);
            }
        }

        parseTemplate(field, name);
    }

    private void parseContentList(Element field) {
        try {
            Node namedItem = field.getAttributes().
                getNamedItem("input-template");

            if (namedItem != null) {
                String templateName = namedItem.getNodeValue();

                if (templateName != null && !templateName.equals("")) {
                    callback.templateReferenceFound(file, templateName);
                }
            }
        } catch (DOMException e) {
        }
    }

    private void parseContentIdReference(Element param) {
        ParsedContentId contentId = parseContentId(param);

        if (contentId == null) {
            return;
        }

        if (contentId.getMajor() == Major.INPUT_TEMPLATE) {
            callback.templateReferenceFound(file, contentId.getExternalId());
        }
        else {
            callback.contentReferenceFound(file, contentId.getMajor(), contentId.getExternalId());
        }
    }
}
