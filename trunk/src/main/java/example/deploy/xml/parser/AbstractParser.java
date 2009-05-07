package example.deploy.xml.parser;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.polopoly.util.collection.FetchingIterator;

import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.CheckedCast;
import example.deploy.hotdeploy.util.CheckedClassCastException;

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

    protected Iterable<Element> children(Element element) {
        final NodeList metadataChildren = element.getChildNodes();

        return new Iterable<Element>() {
            public Iterator<Element> iterator() {
                return new FetchingIterator<Element>() {
                    int j = 0;

                    @Override
                    protected Element fetch() {
                        if (j < metadataChildren.getLength()) {
                            try {
                                return CheckedCast.cast(metadataChildren.item(j++), Element.class);
                            } catch (CheckedClassCastException e) {
                                return fetch();
                            }
                        }
                        else {
                            return null;
                        }
                    }};
            }
        };
    }

    protected boolean isMajorInputTemplate(String major) {
        return major.equalsIgnoreCase("inputtemplate") ||
                major.equalsIgnoreCase("14");
    }
}
