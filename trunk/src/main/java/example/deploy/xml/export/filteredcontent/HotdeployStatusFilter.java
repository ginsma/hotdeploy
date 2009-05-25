package example.deploy.xml.export.filteredcontent;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.util.ContentIdFilter;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.exception.NoSuchExternalIdException;

import example.deploy.hotdeploy.state.DefaultFileChecksums;

public class HotdeployStatusFilter implements ContentIdFilter {
    private static final String OLD_HOTDEPLOY_STATE_EXTERNAL_ID =
        "p.HotDeployDirectoryState";

    private VersionedContentId hotdeployStatusContentId;

    private VersionedContentId oldHotdeployStatusContentId;

    public HotdeployStatusFilter(PolopolyContext context) {
        try {
            hotdeployStatusContentId = context.resolveExternalId(
                DefaultFileChecksums.CHECKSUMS_SINGLETON_EXTERNAL_ID_NAME);
        } catch (NoSuchExternalIdException e) {
            // current version of hotdeploy not used.
        }

        try {
            oldHotdeployStatusContentId = context.resolveExternalId(
                    OLD_HOTDEPLOY_STATE_EXTERNAL_ID);
        } catch (NoSuchExternalIdException e) {
            // old version of hotdeploy not used.
        }
    }

    public boolean accept(ContentId contentId) {
        if (contentId == null) {
            return false;
        }

        if (contentId.equalsIgnoreVersion(hotdeployStatusContentId)) {
            return true;
        }

        if (contentId.equalsIgnoreVersion(oldHotdeployStatusContentId)) {
            return true;
        }

        return false;
    }

}
