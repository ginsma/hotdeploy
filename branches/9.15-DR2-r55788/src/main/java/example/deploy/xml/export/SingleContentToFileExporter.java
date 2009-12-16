package example.deploy.xml.export;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import com.polopoly.cm.client.ContentRead;

public interface SingleContentToFileExporter {
    void exportSingleContentToFile(ContentRead content, File file)
            throws ParserConfigurationException,
            TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException,
            FileNotFoundException, ExportException;
}
