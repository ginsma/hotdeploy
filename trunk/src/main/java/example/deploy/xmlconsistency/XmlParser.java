package example.deploy.xmlconsistency;

import java.io.File;
import java.io.FileInputStream;
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

class XmlParser {
    private static final Logger logger = 
        Logger.getLogger(XmlParser.class.getName());
    
    XmlParser(File file, ParseCallback callback) {
        try {
            InputStream is = new FileInputStream(file);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);

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
        }
    }

    private void handleException(File file, Exception e) {
        logger.log(Level.WARNING, "While parsing " + file + ": " + e.getMessage(), e);
    }
}
