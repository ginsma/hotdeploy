package example.deploy.xml.normalize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import example.deploy.hotdeploy.client.Major;
import example.deploy.hotdeploy.file.DeploymentFile;
import example.deploy.xml.parser.ParseCallback;
import example.deploy.xml.parser.ParseContext;

public class NormalizeElementGatherer implements ParseCallback {

    private static final String TEMPLATE_ROOT_NAME = "template-definition";

    private static final String CONTENT_ROOT_NAME = "batch";

    private Set<File> writtenFiles = new HashSet<File>();

    private NormalizationNamingStrategy namingStrategy;

    public NormalizeElementGatherer(File directory) {
        this(new DefaultNormalizationNamingStrategy(directory, "xml"));
    }

    public NormalizeElementGatherer(NormalizationNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    public void classReferenceFound(DeploymentFile file, String string) {
        // ignore
    }

    public void contentReferenceFound(ParseContext context, Major major,
            String externalId) {
        // ignore
    }

    public void contentFound(ParseContext context, String externalId,
            Major major, String inputTemplate) {
        File outputFile = namingStrategy.getFileName(major, externalId,
                inputTemplate);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Element rootElement;
            Document document;

            if (writtenFiles.contains(outputFile)) {
                FileInputStream inputStream = new FileInputStream(outputFile);
                Document existingDocument = builder.parse(inputStream);

                document = builder.newDocument();

                rootElement = (Element) document.adoptNode(existingDocument
                        .getDocumentElement());
            } else {
                document = builder.newDocument();

                if (isTemplate(major)) {
                    rootElement = document.createElement(TEMPLATE_ROOT_NAME);

                    rootElement.setAttribute("version", "1.0");
                    rootElement.setAttribute("xmlns",
                            "http://www.polopoly.com/polopoly/cm/app/xml");
                    rootElement.setAttribute("xmlns:xsi",
                            "http://www.w3.org/2001/XMLSchema-instance");
                    rootElement
                            .setAttribute("xsi:schemaLocation",
                                    "http://www.polopoly.com/polopoly/cm/app/xml ../../xsd/template.xsd");
                } else {
                    rootElement = document.createElement(CONTENT_ROOT_NAME);

                    rootElement.setAttribute("xmlns",
                            "http://www.polopoly.com/polopoly/cm/xmlio");
                }
            }

            FileOutputStream outputStream = new FileOutputStream(outputFile);

            document.appendChild(rootElement);

            rootElement
                    .appendChild(document.adoptNode(context.getXmlElement()));

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(outputStream);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);

            outputStream.close();
            writtenFiles.add(outputFile);

            System.out.println("Wrote " + outputFile.getAbsolutePath() + ".");
        } catch (Exception e) {
            System.err.println("While writing " + externalId + " to "
                    + outputFile.getAbsolutePath() + ": " + e.toString());
        }
    }

    private boolean isTemplate(Major major) {
        return major == Major.INPUT_TEMPLATE || major == Major.OUTPUT_TEMPLATE;
    }
}
