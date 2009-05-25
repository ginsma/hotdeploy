package example.deploy.xml.export.contentlistentry;

import static com.polopoly.util.policy.Util.util;

import org.w3c.dom.Element;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.xml.util.export.ContentListEntryExporter;
import com.polopoly.util.client.PolopolyContext;

public class FilteringContentListEntryExporter implements ContentListEntryExporter {
    private ContentListEntryExporter delegate;
    private ContentReferenceFilter filter;
    private PolopolyContext context;

    public FilteringContentListEntryExporter(ContentListEntryExporter delegate, ContentReferenceFilter filter, PolopolyContext context) {
        this.delegate = delegate;
        this.filter = filter;
        this.context = context;
    }

    public void exportContentListEntry(Element contentListElement,
            ContentListRead contentList, ContentRead content, int position) {
        ContentReference entry = util(contentList, context).getEntry(position);
        ContentId referredId = entry.getReferredContentId();
        ContentId referenceMetadata = entry.getReferenceMetaDataId();

        if ((referredId == null || filter.isAllowed(content, referredId)) &&
            (referenceMetadata == null || filter.isAllowed(content, referenceMetadata))) {
            delegate.exportContentListEntry(contentListElement, contentList, content, position);
        }
    }
}
