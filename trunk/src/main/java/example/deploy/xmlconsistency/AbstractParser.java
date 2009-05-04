package example.deploy.xmlconsistency;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import example.deploy.hotdeploy.file.DeploymentFile;

class AbstractParser {
    protected ParseCallback callback;
    protected DeploymentFile file;

    protected AbstractParser(DeploymentFile file, ParseCallback callback) {
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
