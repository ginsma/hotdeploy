package example.deploy.xml.export;

import static example.deploy.hotdeploy.util.Plural.count;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
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

import com.polopoly.cm.ContentId;
import com.polopoly.cm.xml.util.export.DefaultContentContentsExporter;
import com.polopoly.cm.xml.util.export.ExternalIdGenerator;
import com.polopoly.cm.xml.util.export.PrefixExternalIdGenerator;
import com.polopoly.pcmd.field.content.AbstractContentIdField;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.collection.ContentIdToContentUtilIterator;
import com.polopoly.util.content.ContentUtil;

import example.deploy.hotdeploy.client.Major;
import example.deploy.xml.normalize.NormalizationNamingStrategy;

public class NormalizedFileExporter {
    private static final Logger logger =
        Logger.getLogger(NormalizedFileExporter.class.getName());

    private PolopolyContext context;
    private NormalizationNamingStrategy namingStrategy;

    private DefaultContentContentsExporter contentsExporter;
    private ExternalIdGenerator externalIdGenerator = new PrefixExternalIdGenerator("");

    public NormalizedFileExporter(PolopolyContext context, DefaultContentContentsExporter contentsExporter,
            NormalizationNamingStrategy namingStrategy) {
        this.context = context;
        this.contentsExporter = contentsExporter;
        this.namingStrategy = namingStrategy;
    }

    public void export(Set<ContentId> contentIdsToExport) {
        ContentIdToContentUtilIterator contentToExportIterator =
            new ContentIdToContentUtilIterator(context, contentIdsToExport.iterator(), false);

        System.err.println("Exporting " + count(contentIdsToExport, "object") + "...");

        int exportedCount = 0;

        while (contentToExportIterator.hasNext()) {
            ContentUtil content = contentToExportIterator.next();

            exportSingleContent(content);

            System.out.println(AbstractContentIdField.get(content.getContentId().getContentId(), context));

            if (++exportedCount % 100 == 0) {
                printStatus(exportedCount);
            }
        }

        printStatus(exportedCount);
    }

    private void printStatus(int exportedCount) {
        System.err.println("Exported " + count(exportedCount, "object") + "...");
    }

    private void exportSingleContent(ContentUtil content)
            throws TransformerFactoryConfigurationError {
        File file = null;

        try {
            String externalId = externalIdGenerator.generateExternalId(content);

            file = namingStrategy.getFileName(
                    Major.getMajor(content.getContentId().getMajor()),
                    externalId, content.getInputTemplate().getExternalIdString());

            exportSingleContentToFile(content, file);
        } catch (Exception e) {
            logger.log(Level.WARNING, "While exporting " + content + " to " + file + ": " + e.getMessage(), e);
        }
    }

    private void exportSingleContentToFile(
            ContentUtil content,
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

    public void setExternalIdGenerator(ExternalIdGenerator externalIdGenerator) {
        this.externalIdGenerator = externalIdGenerator;
    }
}
