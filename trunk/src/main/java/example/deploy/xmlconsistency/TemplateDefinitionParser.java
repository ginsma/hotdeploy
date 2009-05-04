package example.deploy.xmlconsistency;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import example.deploy.hotdeploy.file.DeploymentFile;

class TemplateDefinitionParser extends AbstractParser {
    TemplateDefinitionParser(DeploymentFile file, Element root, ParseCallback callback) {
        super(file, callback);

        NodeList childList = root.getChildNodes();

        for (int i = 0; i < childList.getLength(); i++) {
            try {
                Element inputTemplate = CheckedCast.cast(childList.item(i), Element.class);

                if (inputTemplate.getNodeName().equals("input-template")) {
                    parseTemplate(inputTemplate);
                }
                else if (inputTemplate.getNodeName().equals("output-template")) {
                    callback.templateFound(file, inputTemplate.getAttribute("name"));
                }
            } catch (CheckedClassCastException e) {
            }
        }
    }

    private void parseTemplate(Element inputTemplate) {
        String name = inputTemplate.getAttribute("name");

        callback.templateFound(file, name);

        parseTemplate(inputTemplate, name);
    }

    private void parseTemplate(Element inputTemplate, String name) {
        NodeList fields = inputTemplate.getChildNodes();

        for (int i = 0; i < fields.getLength(); i++) {
            try {
                Element field = CheckedCast.cast(fields.item(i), Element.class);

                String nodeName = field.getNodeName();

                if (nodeName.equals("content-list")) {
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
                else if (nodeName.equals("policy") || nodeName.equals("viewer") || nodeName.equals("editor")) {
                    callback.classReferenceFound(file, field.getTextContent().trim());
                }
                else if (nodeName.equals("layout") || nodeName.equals("field")) {
                    String fieldTemplate = field.getAttribute("input-template");
                    callback.templateReferenceFound(file,
                            fieldTemplate);

                    NodeList params = field.getChildNodes();

                    for (int j = 0; j < params.getLength(); j++) {
                        try {
                            Element param = CheckedCast.cast(params.item(j), Element.class);

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

                                        parseContentId(id);
                                    }
                                    catch (CheckedClassCastException e) {
                                    }
                                }
                            }
                            else if (param.getNodeName().equals("idparam")) {
                                parseContentId(param);
                            }
                        }
                        catch (CheckedClassCastException e) {
                        }
                    }

                    parseTemplate(field, name);
                }
            } catch (CheckedClassCastException e) {
            }
        }
    }

    private void parseContentId(Element param) {
        NodeList ids = param.getChildNodes();

        for (int k = 0; k < ids.getLength(); k++) {
            try {
                Element id = CheckedCast.cast(ids.item(k), Element.class);

                if (id.getNodeName().equals("externalid")) {
                    callback.contentReferenceFound(file, id.getTextContent());
                }
            }
            catch (CheckedClassCastException e) {
            }
        }
    }
}
