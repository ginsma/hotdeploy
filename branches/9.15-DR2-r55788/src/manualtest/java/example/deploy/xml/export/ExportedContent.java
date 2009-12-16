package example.deploy.xml.export;

import com.polopoly.cm.client.CMException;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.xml.normalize.NormalizationNamingStrategy;

public abstract class ExportedContent {
    void cleanUp(PolopolyContext context) throws Exception {}

    void importFromFile(NormalizationNamingStrategy namingStrategy) throws Exception {}

    abstract boolean validate(PolopolyContext context) throws Exception;

    public void prepareImport() throws CMException {}
}
