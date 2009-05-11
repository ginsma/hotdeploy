package example.deploy.xml.parser;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.polopoly.util.collection.FetchingIterator;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.hotdeploy.util.CheckedCast;
import example.deploy.hotdeploy.util.CheckedClassCastException;

class AbstractParser {
    private static final Logger logger =
        Logger.getLogger(AbstractParser.class.getName());

    protected ParseCallback callback;
    protected DeploymentFile file;

    protected AbstractParser(DeploymentFile file, ParseCallback callback) {
        this.callback = callback;
        this.file = file;
    }

    protected ParsedContentId parseContentId(Element contentIdElement) {
        Major major = null;
        String majorName = null;
        String externalId = null;

        for (Element externalIdElement : children(contentIdElement)) {
            if (externalIdElement.getNodeName().equals("major")) {
                majorName = externalIdElement.getTextContent();

                major = Major.getMajor(majorName);
            }
            else if (externalIdElement.getNodeName().equals("externalid")) {
                externalId = externalIdElement.getTextContent().trim();
            }
        }

        if (externalId != null) {
            if (major == Major.UNKNOWN) {
                logger.log(Level.WARNING, "The major \"" + majorName +
                        "\" used to reference the object with external ID \"" + externalId + "\" in file " + file + " was unknown.");
            }

            return new ParsedContentId(major, externalId);
        }
        else {
            return null;
        }
    }

    protected void findContentReferences(Element content) {
        for (Element child : children(content)) {
            findContentReferences(child);
        }

        ParsedContentId contentReference = parseContentId(content);

        if (contentReference != null) {
            callback.contentReferenceFound(file, contentReference.getMajor(), contentReference.getExternalId());
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
