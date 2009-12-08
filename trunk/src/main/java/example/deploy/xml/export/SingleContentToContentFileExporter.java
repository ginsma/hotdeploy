package example.deploy.xml.export;

import static com.polopoly.cm.server.ServerNames.CONTENT_ATTRG_SYSTEM;
import static com.polopoly.cm.server.ServerNames.CONTENT_ATTR_NAME;
import static example.deploy.text.TextContentParser.COMPONENT_PREFIX;
import static example.deploy.text.TextContentParser.FILE_PREFIX;
import static example.deploy.text.TextContentParser.ID_PREFIX;
import static example.deploy.text.TextContentParser.INPUT_TEMPLATE_PREFIX;
import static example.deploy.text.TextContentParser.LIST_PREFIX;
import static example.deploy.text.TextContentParser.NAME_PREFIX;
import static example.deploy.text.TextContentParser.REFERENCE_PREFIX;
import static example.deploy.text.TextContentParser.SECURITY_PARENT_PREFIX;
import static example.deploy.text.TextContentParser.SEPARATOR_CHAR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentFileInfo;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.server.ServerNames;

import example.deploy.xml.export.contentlistentry.ContentReferenceFilter;

public class SingleContentToContentFileExporter implements
        SingleContentToFileExporter {
    private static final Logger LOGGER = Logger
            .getLogger(SingleContentToContentFileExporter.class.getName());

    private PolicyCMServer server;

    private ContentReferenceFilter filter;

    public SingleContentToContentFileExporter(PolicyCMServer server,
            ContentReferenceFilter filter) {
        this.server = server;
        this.filter = filter;
    }

    public void exportSingleContentToFile(ContentRead content, File file)
            throws ExportException {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file),
                    "UTF-8");

            writeId(content, writer);

            writeName(content, writer);

            writeSecurityParent(content, writer);

            writeComponents(content, writer);

            writeContentReferences(content, writer);

            writeFiles(content, file, writer);

            writer.close();
        } catch (CMException e) {
            throw new ExportException(e);
        } catch (IOException e) {
            throw new ExportException(e);
        }
    }

    private void writeId(ContentRead content, Writer writer)
            throws CMException, IOException, ExportException {
        ExternalContentId externalId = content.getExternalId();

        if (externalId != null) {
            writeln(writer, ID_PREFIX + SEPARATOR_CHAR
                    + externalId.getExternalId());
        } else {
            throw new ExportException(
                    toString(content)
                            + " did not have an external ID and therefore cannot be exported to .content format.");
        }
    }

    private void writeFiles(ContentRead content, File file, Writer writer)
            throws CMException, IOException, FileNotFoundException {
        ContentFileInfo[] contentFiles = content.listFiles("/", true);

        for (ContentFileInfo contentFile : contentFiles) {
            if (contentFile.isDirectory()) {
                continue;
            }

            File outputFile = new File(file.getParent(), content
                    .getExternalId().getExternalId()
                    + "." + contentFile.getName());

            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

            content.exportFile(contentFile.getPath(), fileOutputStream);

            fileOutputStream.close();

            writeln(writer, FILE_PREFIX + SEPARATOR_CHAR
                    + contentFile.getPath() + SEPARATOR_CHAR
                    + outputFile.getName());
        }
    }

    private void writeContentReferences(ContentRead content, Writer writer)
            throws CMException, IOException, ExportException {
        String[] groups = content.getContentReferenceGroupNames();

        Arrays.sort(groups);

        for (String group : groups) {
            String[] names = content.getContentReferenceNames(group);

            if (isContentList(names)) {
                ContentList contentList = content.getContentList(group);

                int size = contentList.size();

                for (int i = 0; i < size; i++) {
                    ContentReference entry = contentList.getEntry(i);
                    ContentId rmd = entry.getReferenceMetaDataId();

                    try {
                        if (rmd != null) {
                            writeln(writer, LIST_PREFIX
                                    + SEPARATOR_CHAR
                                    + group
                                    + SEPARATOR_CHAR
                                    + toContentId(content, rmd)
                                    + SEPARATOR_CHAR
                                    + toContentId(content, entry
                                            .getReferredContentId()));
                        } else {
                            writeln(writer, LIST_PREFIX
                                    + SEPARATOR_CHAR
                                    + group
                                    + SEPARATOR_CHAR
                                    + toContentId(content, entry
                                            .getReferredContentId()));
                        }
                    } catch (NotExportableException e) {
                    }

                }
            } else {
                Arrays.sort(names);

                for (String name : names) {
                    if (group.equals(ServerNames.CONTENT_ATTRG_SYSTEM)
                            && name
                                    .equals(ServerNames.CONTENT_ATTR_INPUT_TEMPLATEID)) {
                        continue;
                    }

                    try {
                        writeln(writer, REFERENCE_PREFIX
                                + SEPARATOR_CHAR
                                + group
                                + ':'
                                + name
                                + SEPARATOR_CHAR
                                + toContentId(content, content
                                        .getContentReference(group, name)));
                    } catch (NotExportableException e) {
                    }
                }
            }
        }
    }

    private boolean isContentList(String[] names) {
        for (String name : names) {
            try {
                Integer.parseInt(name);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    private void writeComponents(ContentRead content, Writer writer)
            throws CMException, IOException {
        String[] groups = content.getComponentGroupNames();

        Arrays.sort(groups);

        for (String group : groups) {
            String[] names = content.getComponentNames(group);

            Arrays.sort(names);

            for (String name : names) {
                // skip name.
                if (group.equals(CONTENT_ATTRG_SYSTEM)
                        && name.equals(CONTENT_ATTR_NAME)) {
                    continue;
                }

                String value = content.getComponent(group, name);

                writeln(writer, COMPONENT_PREFIX + SEPARATOR_CHAR + group
                        + SEPARATOR_CHAR + name + SEPARATOR_CHAR
                        + escape(value));
            }
        }
    }

    private void writeSecurityParent(ContentRead content, Writer writer)
            throws IOException, ExportException {
        ContentId securityParentId = content.getSecurityParentId();

        if (securityParentId != null) {
            try {
                writeln(writer, SECURITY_PARENT_PREFIX + SEPARATOR_CHAR
                        + toContentId(content, securityParentId));
            } catch (NotExportableException e) {
            }
        }
    }

    private void writeName(ContentRead content, Writer writer)
            throws IOException, ExportException, CMException {
        writeln(writer, INPUT_TEMPLATE_PREFIX + SEPARATOR_CHAR
                + getInputTemplateName(content));

        String contentName = content.getComponent(CONTENT_ATTRG_SYSTEM,
                CONTENT_ATTR_NAME);

        if (contentName != null) {
            writeln(writer, NAME_PREFIX + SEPARATOR_CHAR + contentName);
        }
    }

    private String escape(String value) {
        return value.replace(":", "\\:").replace("\n", "\\n");
    }

    private void writeln(Writer writer, String string) throws IOException {
        writer.write(string);
        writer.write("\n");
    }

    private String toContentId(ContentRead inContent, ContentId contentId)
            throws ExportException, NotExportableException {
        try {
            if (filter != null && !filter.isAllowed(inContent, contentId)) {
                throw new NotExportableException("Not allowed by filter.");
            }

            ContentRead referredContent = server.getContent(contentId);

            ExternalContentId externalId = referredContent.getExternalId();

            if (externalId == null) {
                String message = "Content "
                        + toString(referredContent)
                        + " had no external ID. Cannot export it to .content format.";

                LOGGER.log(Level.WARNING, "While exporting "
                        + toString(referredContent) + ": " + message);

                throw new NotExportableException(message);
            }

            return externalId.getExternalId();
        } catch (CMException e) {
            throw new ExportException("Getting external ID of "
                    + contentId.getContentIdString() + ":  " + e.getMessage(),
                    e);
        }
    }

    private String toString(ContentRead content) {
        return content.getContentId().getContentId().getContentIdString();
    }

    private String getInputTemplateName(ContentRead content)
            throws ExportException {
        try {
            ContentRead inputTemplate = server.getContent(content
                    .getInputTemplateId());

            ExternalContentId externalId = inputTemplate.getExternalId();

            if (externalId == null) {
                throw new ExportException("Input template "
                        + toString(inputTemplate) + " had no external ID.");
            }

            return externalId.getExternalId();
        } catch (CMException e) {
            throw new ExportException("Getting input template of "
                    + toString(content) + ": " + e.getMessage(), e);
        }
    }
}
