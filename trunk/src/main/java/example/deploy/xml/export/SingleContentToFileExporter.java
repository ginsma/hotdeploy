package example.deploy.xml.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.xml.util.export.DefaultContentContentsExporter;

public class SingleContentToFileExporter {
    private static final Logger logger =
        Logger.getLogger(SingleContentToFileExporter.class.getName());
    private DefaultContentContentsExporter contentsExporter;

    public SingleContentToFileExporter(DefaultContentContentsExporter contentsExporter) {
        this.contentsExporter = contentsExporter;
    }

    public void exportSingleContentToFile(
            ContentRead content,
            File file) throws ParserConfigurationException,
            TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException, FileNotFoundException {
        StreamResult streamResult = null;
        FileOutputStream outputStream = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.newDocument();

            Element batchElement = document.createElement("batch");
            batchElement.setAttribute("xmlns", "http://www.polopoly.com/polopoly/cm/xmlio");

            document.appendChild(batchElement);

            Element contentElement = document.createElement("content");
            batchElement.appendChild(contentElement);

            contentsExporter.exportContentContents(contentElement, content);

            outputStream = new FileOutputStream(file);

            DOMSource domSource = new DOMSource(document);
            streamResult = new StreamResult(outputStream);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
            serializer.setOutputProperty(OutputKeys.INDENT,"yes");
            serializer.transform(domSource, streamResult);

            logger.log(Level.INFO, "Wrote " + content + " to file " + file.getAbsolutePath() + ".");
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

}
