package com.polopoly.pcmd.tool.export;

import static example.deploy.hotdeploy.util.Plural.count;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerFactoryConfigurationError;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.xml.util.export.DefaultContentContentsExporter;
import com.polopoly.cm.xml.util.export.ExternalIdGenerator;
import com.polopoly.cm.xml.util.export.PrefixExternalIdGenerator;
import com.polopoly.pcmd.field.content.AbstractContentIdField;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.collection.ContentIdToContentUtilIterator;
import com.polopoly.util.content.ContentUtil;

import example.deploy.hotdeploy.client.Major;
import example.deploy.xml.export.SingleContentToFileExporter;
import example.deploy.xml.normalize.NormalizationNamingStrategy;

/**
 * Same as {@link example.deploy.xml.export.NormalizedFileExporter}, but logs in a way adapted to PCMD.
 */
public class NormalizedFileExporter {
    private static final Logger logger =
        Logger.getLogger(NormalizedFileExporter.class.getName());

    private NormalizationNamingStrategy namingStrategy;

    private ExternalIdGenerator externalIdGenerator = new PrefixExternalIdGenerator("");
    private SingleContentToFileExporter singleContentToFileExporter;

    private PolopolyContext context;

    public NormalizedFileExporter(PolopolyContext context, DefaultContentContentsExporter contentsExporter,
            NormalizationNamingStrategy namingStrategy) {
        this.context = context;
        this.namingStrategy = namingStrategy;
        this.singleContentToFileExporter = new SingleContentToFileExporter(contentsExporter);
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

    private void exportSingleContent(ContentUtil content) throws TransformerFactoryConfigurationError {
        File file = null;

        try {
            String externalId = externalIdGenerator.generateExternalId(content);

            file = namingStrategy.getFileName(
                    Major.getMajor(content.getContentId().getMajor()),
                    externalId, content.getInputTemplate().getExternalIdString());

            singleContentToFileExporter.exportSingleContentToFile(content, file);
        } catch (Exception e) {
            logger.log(Level.WARNING, "While exporting " + content + " to " + file + ": " + e.getMessage(), e);
        }
    }

    private void printStatus(int exportedCount) {
        System.err.println("Exported " + count(exportedCount, "object") + "...");
    }


    public void setExternalIdGenerator(ExternalIdGenerator externalIdGenerator) {
        this.externalIdGenerator = externalIdGenerator;
    }
}
