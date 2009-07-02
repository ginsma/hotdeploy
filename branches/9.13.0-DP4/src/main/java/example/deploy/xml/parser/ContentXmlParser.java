package example.deploy.xml.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import example.deploy.hotdeploy.file.DeploymentFile;

public class ContentXmlParser implements DeploymentFileParser {
    private static final Logger logger =
        Logger.getLogger(ContentXmlParser.class.getName());

    public ContentXmlParser() {
    }

    private void handleException(DeploymentFile file, Exception e) {
        logger.log(Level.WARNING, "While parsing " + file + ": " + e.getMessage(), e);
    }

    public void parse(DeploymentFile file, ParseCallback callback) {
        InputStream inputStream = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            inputStream = file.getInputStream();
            Document document = builder.parse(inputStream);

            Element root = document.getDocumentElement();

            String rootName = root.getNodeName();

            if (rootName.equals("template-definition")) {
                new TemplateDefinitionParser(file, root, callback);
            }
            else if (rootName.equals("batch")) {
                new XmlIoParser(file, root, callback);
            }
            else {
                logger.log(Level.WARNING, "File " + file + " was of unknown type.");
            }
        } catch (FileNotFoundException e) {
            handleException(file, e);
        } catch (ParserConfigurationException e) {
            handleException(file, e);
        } catch (SAXException e) {
            handleException(file, e);
        } catch (IOException e) {
            handleException(file, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }
}
