package example.deploy.hotdeploy.manualtest;

import junit.framework.TestCase;

import com.polopoly.util.client.ConnectException;
import com.polopoly.util.client.PolopolyClient;
import com.polopoly.util.client.PolopolyContext;

public abstract class ManualTestCase extends TestCase {
    protected static PolopolyContext context;

    static {
        PolopolyClient client = new PolopolyClient();

        client.setAttachSearchService(false);
        client.setAttachStatisticsService(false);

        try {
            context = client.connect();
        }
        catch (ConnectException e) {
            throw new RuntimeException(e);
        }
    }
}
