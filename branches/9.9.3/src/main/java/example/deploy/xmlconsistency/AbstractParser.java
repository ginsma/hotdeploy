package example.deploy.xmlconsistency;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class AbstractParser {
    protected ParseCallback callback;
    protected File file;

    protected AbstractParser(File file, ParseCallback callback) {
        this.callback = callback;
        this.file = file;
    }
    
    protected void findContentReferences(Element content) {
        NodeList children = content.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            try {
                Element child = 
                    CheckedCast.cast(children.item(i), Element.class);
      
                if (child.getNodeName().equals("externalid")) {
                    callback.contentReferenceFound(file, child.getTextContent());
                }
                
                findContentReferences(child);
            } catch (CheckedClassCastException e) {
            }
        }
    }
}
