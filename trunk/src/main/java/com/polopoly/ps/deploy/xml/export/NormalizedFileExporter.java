package com.polopoly.ps.deploy.xml.export;

import static com.polopoly.ps.deploy.hotdeploy.util.Plural.count;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerFactoryConfigurationError;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.xml.util.export.ExternalIdGenerator;
import com.polopoly.cm.xml.util.export.PrefixExternalIdGenerator;
import com.polopoly.ps.deploy.hotdeploy.client.Major;
import com.polopoly.ps.deploy.xml.normalize.NormalizationNamingStrategy;


public class NormalizedFileExporter {
    private static final Logger logger = Logger
            .getLogger(NormalizedFileExporter.class.getName());

    private NormalizationNamingStrategy namingStrategy;

    private ExternalIdGenerator externalIdGenerator = new PrefixExternalIdGenerator(
            "");

    private SingleContentToFileExporter singleContentToFileExporter;

    private PolicyCMServer server;

    public NormalizedFileExporter(PolicyCMServer server,
            SingleContentToFileExporter singleContentToFileExporter,
            NormalizationNamingStrategy namingStrategy) {
        this.server = server;
        this.namingStrategy = namingStrategy;
        this.singleContentToFileExporter = singleContentToFileExporter;
    }

    public void export(Set<ContentId> contentIdsToExport) {
        int exportedCount = 0;

        for (ContentId contentIdToExport : contentIdsToExport) {
            logger.log(Level.INFO, "Exporting "
                    + count(contentIdsToExport, "object") + "...");

            try {
                ContentRead content = server.getContent(contentIdToExport);

                exportSingleContent(content);

                if (++exportedCount % 100 == 0) {
                    printStatus(exportedCount);
                }
            } catch (CMException e) {
                logger.log(Level.WARNING, "While fetching content "
                        + contentIdToExport.getContentIdString()
                        + " to export: " + e.getMessage(), e);
            }
        }

        printStatus(exportedCount);
    }

    private void exportSingleContent(ContentRead content)
            throws TransformerFactoryConfigurationError {
        File file = null;

        try {
            String externalId = externalIdGenerator.generateExternalId(content);

            String inputTemplate = server.getContent(
                    content.getInputTemplateId()).getExternalId()
                    .getExternalId();

            file = namingStrategy.getFileName(Major.getMajor(content
                    .getContentId().getMajor()), externalId, inputTemplate);

            singleContentToFileExporter
                    .exportSingleContentToFile(content, file);
        } catch (Exception e) {
            logger.log(Level.WARNING, "While exporting " + content + " to "
                    + file + ": " + e.getMessage(), e);
        }
    }

    private void printStatus(int exportedCount) {
        logger.log(Level.INFO, "Exported " + count(exportedCount, "object")
                + "...");
    }

    public void setExternalIdGenerator(ExternalIdGenerator externalIdGenerator) {
        this.externalIdGenerator = externalIdGenerator;
    }
}
