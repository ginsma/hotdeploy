package example.deploy.xmlconsistency;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class XmlIoParser extends AbstractParser {
    XmlIoParser(File file, Element root, ParseCallback callback) {
        super(file, callback);

        NodeList childList = root.getChildNodes();
        
        for (int i = 0; i < childList.getLength(); i++) {
            try {
                Element content = CheckedCast.cast(childList.item(i), Element.class);
                
                if (content.getNodeName().equals("content")) {
                    parseContent(content);
                }
            } catch (CheckedClassCastException e) {
            }
        }
    }

    private void parseContent(Element content) {
        NodeList contentChildren = content.getChildNodes();

        for (int i = 0; i < contentChildren.getLength(); i++) {
            try {
                Element metadata = 
                    CheckedCast.cast(contentChildren.item(i), Element.class);
                
                if (metadata.getNodeName().equals("metadata")) {
                    NodeList metadataChildren = metadata.getChildNodes();
                    
                    String externalId = null;
                    String inputTemplate = null;
                    boolean template = false;
                    
                    for (int j = 0; j < metadataChildren.getLength(); j++) {
                        try {
                            Element metadataChild = 
                                CheckedCast.cast(metadataChildren.item(j), Element.class);
                            
                            String nodeName = metadataChild.getNodeName();
                            
                            if (nodeName.equals("input-template")) {
                                NodeList inputTemplateChildren = metadataChild.getChildNodes();
                                
                                for (int k = 0; k < inputTemplateChildren.getLength(); k++) {
                                    Node inputTemplateElement = 
                                        inputTemplateChildren.item(k);
                                    
                                    if (inputTemplateElement.getNodeName().equals("externalid")) {
                                        inputTemplate = inputTemplateElement.getTextContent().trim();
                                    }
                                }
                            }
                            else if (nodeName.equals("contentid")) {
                                NodeList contentIdChildren = metadataChild.getChildNodes();
                                
                                for (int k = 0; k < contentIdChildren.getLength(); k++) {
                                    Node externalIdElement = contentIdChildren.item(k);
                                    
                                    if (externalIdElement.getNodeName().equals("major")) {
                                        String major = externalIdElement.getTextContent();
                                        
                                        if (major.equalsIgnoreCase("inputtemplate") ||
                                                major.equalsIgnoreCase("14")) {
                                            template = true;
                                        }
                                    }
                                    else if (externalIdElement.getNodeName().equals("externalid")) {
                                        externalId = externalIdElement.getTextContent().trim();
                                    }
                                }
                            }
                        } catch (CheckedClassCastException e) {
                        }
                    }
                    
                    if (template) {
                        callback.templateFound(file, externalId);
                    }
                    else {
                        callback.contentFound(file, externalId, inputTemplate);
                    }
                }
            } catch (CheckedClassCastException e) {
            }
        }
        
        findContentReferences(content);
    }
}
