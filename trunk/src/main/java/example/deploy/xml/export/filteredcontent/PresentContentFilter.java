package example.deploy.xml.export.filteredcontent;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.exception.NoSuchExternalIdException;

import example.deploy.xml.present.PresentFilesAware;

public class PresentContentFilter implements PresentFilesAware, ContentIdFilter {
    private static final Logger logger =
        Logger.getLogger(PresentContentFilter.class.getName());

    private Set<ContentId> presentIds = new HashSet<ContentId>(100);
    private PolopolyContext context;

    public PresentContentFilter(PolopolyContext context) {
        this.context = context;
    }

    public void presentContent(String externalId) {
        present(externalId);
    }

    public void presentTemplate(String inputTemplate) {
        present(inputTemplate);
    }

    private void present(String externalId) {
        try {
            VersionedContentId contentId =
                context.resolveExternalId(externalId);

            presentIds.add(contentId.getContentId());
        } catch (NoSuchExternalIdException e) {
            logger.log(Level.FINE, "While looking up purportedly present content " + externalId + ": " + e.getMessage());
        }
    }

    public Set<ContentId> getPresentIds() {
        return presentIds;
    }

    public boolean accept(ContentId contentId) {
        return presentIds.contains(contentId.getContentId());
    }
}
