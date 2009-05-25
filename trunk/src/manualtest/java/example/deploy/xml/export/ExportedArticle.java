package example.deploy.xml.export;

import static example.deploy.hotdeploy.client.Major.getMajor;

import java.io.File;
import java.util.Random;

import junit.framework.Assert;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.impl.exceptions.EJBFinderException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.contentid.ContentIdUtil;
import com.polopoly.util.exception.PolicyGetException;
import com.polopoly.util.policy.PolicyModification;
import com.polopoly.util.policy.PolicyUtil;
import com.polopoly.util.policy.Util;

import example.deploy.hotdeploy.deployer.DefaultSingleFileDeployer;
import example.deploy.hotdeploy.discovery.PlatformNeutralPath;
import example.deploy.hotdeploy.file.FileDeploymentFile;
import example.deploy.xml.normalize.DefaultNormalizationNamingStrategy;

public class ExportedArticle extends ExportedContent {
    private static final String INPUT_TEMPLATE = "p.DefaultArticle";

    private String externalId;
    private ContentIdUtil contentId;

    private PolopolyContext context;

    private int articleCounter;

    private static File directory;

    public ExportedArticle(int articleCounter, PolopolyContext context) {
        this.context = context;
        this.articleCounter = articleCounter;
    }

    public ExportedContent reference(final ExportedArticle referencedArticle) throws Exception {
        return new ExportedContentListReference(this, referencedArticle);
    }

    @Override
    boolean validate(PolopolyContext context) throws Exception {
        try {
            PolicyUtil policy = context.getPolicyUtil(externalId);

            return policy.getInputTemplate().getExternalIdString().equals(INPUT_TEMPLATE);
        } catch (PolicyGetException e) {
            if (!(e.getCause() instanceof EJBFinderException)) {
                throw e;
            }

            return false;
        }
    }

    ContentIdUtil getContentId() {
        return contentId.getLatestCommittedVersion();
    }

    @Override
    void cleanUp(PolopolyContext context) throws CMException {
        context.getPolicyUtil(externalId).delete();
    }

    @Override
    public void prepareImport() throws CMException {
        super.prepareImport();

        Policy policy = context.getPolicyCMServer().createContent(contentId.getMajor(), null);
        policy.getContent().setExternalId(externalId);
        policy.getContent().commit();
    }

    @Override
    public void importFromFile() throws Exception {
        File file =
            getNamingStrategy().getFileName(
                   getMajor(contentId.getMajor()),
                   externalId,
                   INPUT_TEMPLATE);

        DefaultSingleFileDeployer deployer =
            new DefaultSingleFileDeployer(context.getPolicyCMServer());
        deployer.prepare();

        boolean importResult = deployer.importAndHandleException(
            new FileDeploymentFile(file));

        if (!importResult) {
            Assert.fail("Importing " + file + " failed.");
        }
    }

    public void create() throws CMException {
        final String externalId = "article-" + articleCounter + "-" + new Random().nextInt();

        PolicyUtil article =
            Util.util(context.createPolicy(1, INPUT_TEMPLATE, new PolicyModification<Policy>() {
            public void modify(Policy newVersion) throws CMException {
                newVersion.getContent().setExternalId(externalId);
                newVersion.getContent().setName("Test article " + externalId);
            }}));

        this.externalId = article.getContent().getExternalIdString();
        this.contentId = article.getContentId();
    }

    static DefaultNormalizationNamingStrategy getNamingStrategy() {
        DefaultNormalizationNamingStrategy namingStrategy =
            new DefaultNormalizationNamingStrategy(directory);

        return namingStrategy;
    }

    public String getExternalIdString() {
        return externalId;
    }

    static {
        directory =
            new File(PlatformNeutralPath.unixToPlatformSpecificPath("src/test/resources/exporttarget"));
    }
}