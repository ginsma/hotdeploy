package com.polopoly.pcmd.tool;

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
    private static final String SYSTEM_TEMPLATE_PREFIX = "p.";
    private static final String XML_EXTENSION = ".xml";
    private static final String CONTENT_DIRECTORY = "content";

    private static final String TEMPLATE_ROOT_NAME = "template-definition";
    private static final String CONTENT_ROOT_NAME = "batch";

    private File templateDirectory;
    private File contentDirectory;

    private Set<File> writtenFiles = new HashSet<File>();

    public NormalizeElementGatherer(File directory) {
        this.templateDirectory = directory;

        contentDirectory = new File(directory, CONTENT_DIRECTORY);

        mkdir(contentDirectory);
    }

    private static void mkdir(File directory) {
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                System.err.println("Could not create directory " + directory.getAbsolutePath());
                System.exit(1);
            }
        }
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
        File outputFile = getFileName(major, externalId, inputTemplate);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Element rootElement;
            Document document;

            if (writtenFiles.contains(outputFile)) {
                FileInputStream inputStream = new FileInputStream(outputFile);
                Document existingDocument = builder.parse(inputStream);

                document = builder.newDocument();

                rootElement = (Element) document.adoptNode(existingDocument.getDocumentElement());
            }
            else {
                document = builder.newDocument();

                if (isTemplate(major)) {
                    rootElement = document.createElement(TEMPLATE_ROOT_NAME);

                    rootElement.setAttribute("version", "1.0");
                    rootElement.setAttribute("xmlns", "http://www.polopoly.com/polopoly/cm/app/xml");
                    rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                    rootElement.setAttribute("xsi:schemaLocation", "http://www.polopoly.com/polopoly/cm/app/xml ../../xsd/template.xsd");
                }
                else {
                    rootElement = document.createElement(CONTENT_ROOT_NAME);

                    rootElement.setAttribute("xmlns", "http://www.polopoly.com/polopoly/cm/xmlio");
                }
            }

            FileOutputStream outputStream = new FileOutputStream(outputFile);

            document.appendChild(rootElement);

            rootElement.appendChild(
                document.adoptNode(context.getXmlElement()));

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(outputStream);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
            serializer.setOutputProperty(OutputKeys.INDENT,"yes");
            serializer.transform(domSource, streamResult);

            outputStream.close();
            writtenFiles.add(outputFile);

            System.out.println("Wrote " + outputFile.getAbsolutePath() + ".");
        } catch (Exception e) {
            System.err.println("While writing " + externalId + " to " + outputFile.getAbsolutePath() + ": " + e.toString());
        }
    }

    private boolean isTemplate(Major major) {
        return major == Major.INPUT_TEMPLATE || major == Major.OUTPUT_TEMPLATE;
    }

    private File getFileName(Major major, String externalId, String inputTemplate) {
        if (major == Major.INPUT_TEMPLATE) {
            File directory = templateDirectory;

            if (externalId.startsWith(SYSTEM_TEMPLATE_PREFIX)) {
                directory = new File(directory, "system");

                mkdir(directory);
            }

            return new File(directory, externalId + XML_EXTENSION);
        }
        else {
            File directory;

            if (inputTemplate != null && !inputTemplate.equals("")) {
                directory = new File(contentDirectory, inputTemplate);

                mkdir(directory);
            }
            else {
                directory = contentDirectory;
            }

            return new File(directory, externalId + XML_EXTENSION);
        }
    }
}
