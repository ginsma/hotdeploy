package example.deploy.xml.export.filteredcontent;

import java.util.HashSet;
import java.util.Set;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.util.ContentIdFilter;

public class RejectionCollectingContentIdFilter implements ContentIdFilter {
    private Set<ContentId> rejected = new HashSet<ContentId>(100);
    private ContentIdFilter delegate;

    public RejectionCollectingContentIdFilter(
            ContentIdFilter delegate) {
        this.delegate = delegate;
    }

    public boolean accept(ContentId contentId) {
        boolean result = delegate.accept(contentId);

        if (!result) {
            rejected.add(contentId);
        }

        return result;
    }

    public Set<ContentId> getRejectedIds() {
        return rejected;
    }
}
