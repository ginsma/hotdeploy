package example.deploy.xml.export;

import com.polopoly.cm.client.CMException;
import com.polopoly.util.client.PolopolyContext;

public abstract class ExportedContent {
    void cleanUp(PolopolyContext context) throws Exception {}

    void importFromFile() throws Exception {}

    abstract boolean validate(PolopolyContext context) throws Exception;

    public void prepareImport() throws CMException {}
}
