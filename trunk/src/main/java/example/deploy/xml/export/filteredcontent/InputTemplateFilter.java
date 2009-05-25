package example.deploy.xml.export.filteredcontent;

import static example.deploy.hotdeploy.client.Major.INPUT_TEMPLATE;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.server.ServerNames;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.content.ContentUtil;
import com.polopoly.util.contentid.ContentIdUtil;
import com.polopoly.util.exception.ContentGetException;

import example.deploy.hotdeploy.client.Major;

public class InputTemplateFilter implements ContentIdFilter {
    private static final Logger logger =
        Logger.getLogger(InputTemplateFilter.class.getName());

    private static final String FIELD_INPUT_TEMPLATE =
        "p.IT.PolicyWidgetOutputTemplate";

    private static final String DEFAULT_REFERENCE_METADATA = "p.DefaultReferenceMetaData";

    private PolopolyContext context;

    public InputTemplateFilter(PolopolyContext context) {
        this.context = context;
    }

    public boolean accept(ContentId contentId) {
        if (isInputTemplate(contentId)) {
            return true;
        }

        if (isField(contentId)) {
            return true;
        }

        if (isReferenceMetadataForField(contentId)) {
            return true;
        }

        return false;
    }

    private boolean isReferenceMetadataForField(ContentId contentId) {
        if (contentId.getMajor() != Major.REFERENCE_METADATA.getIntegerMajor()) {
            return false;
        }

        try {
            ContentUtil content = context.getContent(contentId);

            ContentIdUtil referredId = content.getContentReference(
                    ServerNames.REFERENCE_ATTRG_SYSTEM,
                    ServerNames.REFERENCE_ATTR_REFERRED_CONTENT_ID);

            if (referredId.getMajor() != Major.INPUT_TEMPLATE.getIntegerMajor()) {
                return false;
            }

            if (!content.getInputTemplate().getExternalIdString().equals(DEFAULT_REFERENCE_METADATA)) {
                return false;
            }

            return true;
        } catch (ContentGetException e) {
            return false;
        }
    }

    private boolean isField(ContentId contentId) {
        if (contentId.getMajor() != Major.OUTPUT_TEMPLATE.getIntegerMajor()) {
            return false;
        }

        return getInputTemplateString(contentId).equals(FIELD_INPUT_TEMPLATE);
    }

    private String getInputTemplateString(ContentId contentId) {
        try {
            return context.getContent(contentId).getInputTemplate().getExternalIdString();
        } catch (ContentGetException e) {
            logger.log(Level.WARNING, e.getMessage(), e);

            return "";
        } catch (CMRuntimeException e) {
            logger.log(Level.WARNING, e.getMessage(), e);

            return "";
        }
    }

    private boolean isInputTemplate(ContentId contentId) {
        return contentId.getMajor() == INPUT_TEMPLATE.getIntegerMajor();
    }
}
