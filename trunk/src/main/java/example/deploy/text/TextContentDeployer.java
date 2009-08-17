package example.deploy.text;

import static com.polopoly.cm.VersionedContentId.LATEST_VERSION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.LockInfo;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.client.impl.exceptions.EJBFinderException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

import example.deploy.hotdeploy.util.CheckedCast;
import example.deploy.hotdeploy.util.CheckedClassCastException;

public class TextContentDeployer {
    private static final Logger logger =
        Logger.getLogger(TextContentDeployer.class.getName());

    private TextContentSet contentSet;
    private PolicyCMServer server;

    public TextContentDeployer(TextContentSet contentSet, PolicyCMServer server) {
        this.contentSet = contentSet;
        this.server = server;
    }

    public Collection<Policy> deploy() throws DeployException {
        boolean success = false;
        Map<String, Policy> newVersionById = new HashMap<String, Policy>();

        try {
            for (TextContent textContent : contentSet) {
                try {
                    Policy newVersionPolicy = createNewVersion(textContent);

                    newVersionById.put(textContent.getId(), newVersionPolicy);

                    createPublishInVersion(textContent, newVersionById);
                }
                catch (CMException e) {
                    throw new DeployException("While creating " + textContent.getId() + ": " + e, e);
                }
            }

            for (TextContent textContent : contentSet) {
                try {
                    Policy newVersion = newVersionById.get(textContent.getId());

                    deploy(textContent, newVersion);
                } catch (CMException e) {
                    throw new DeployException("While importing " + textContent.getId() + ": " + e, e);
                }
            }

            // do this in a separate step since the deploy step clears content lists and we might be deploying
            // to another object in the same batch.
            for (TextContent textContent : contentSet) {
                try {
                    if (textContent.getPublishIn() != null) {
                        String publishInExternalId = ((ExternalIdReference) textContent.getPublishIn()).getExternalId();

                        Policy newVersion = newVersionById.get(textContent.getId());
                        Policy publishInVersion = newVersionById.get(publishInExternalId);

                        publish(newVersion, publishInVersion, textContent.getPublishInGroup());
                    }
                } catch (CMException e) {
                    throw new DeployException("While publishing " + textContent.getId() + ": " + e, e);
                }
            }

            Iterator<Policy> newVersionIterator = newVersionById.values().iterator();
            Collection<Policy> result = new ArrayList<Policy>();

            while (newVersionIterator.hasNext()) {
                Policy newVersion = newVersionIterator.next();

                try {
                    newVersion.getContent().commit();
                } catch (CMException e) {
                    String externalId;
                    try {
                        externalId = newVersion.getContent().getExternalId().getExternalId();
                    } catch (CMException e1) {
                        externalId = newVersion.getContentId().getContentId().getContentIdString();
                    }

                    throw new DeployException("While committing " + externalId + ": " + e, e);
                }

                newVersionIterator.remove();
                result.add(newVersion);
            }

            success = true;

            return result;
        }
        finally {
            if (!success) {
                for (Entry<String, Policy> newVersionEntry : newVersionById.entrySet()) {
                    Policy newVersion = newVersionEntry.getValue();
                    String id = newVersionEntry.getKey();

                    try {
                        server.abortContent(newVersion, true);
                    } catch (CMException e) {
                        logger.log(Level.WARNING, "While aborting " + id + " after failure: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void publish(Policy publish, Policy publishIn,
            String publishInGroup) throws CMException {
        Content publishInContent = publishIn.getContent();
        ContentList contentList;

        if (publishInGroup != null) {
            contentList = publishInContent.getContentList(publishInGroup);
        }
        else {
            contentList = publishInContent.getContentList();
        }

        if (!contains(contentList, publish.getContentId())) {
            contentList.add(contentList.size(), new ContentReference(publish.getContentId().getContentId(), null));
        }
    }

    private boolean contains(ContentList contentList, ContentId contentId) throws CMException {
        for (int i = contentList.size()-1; i >= 0; i--) {
            if (contentList.getEntry(i).getReferredContentId().equalsIgnoreVersion(contentId)) {
                return true;
            }
        }

        return false;
    }

    private void createPublishInVersion(TextContent textContent,
            Map<String, Policy> newVersionById) throws CMException {
        Reference publishIn = textContent.getPublishIn();
        String publishInExternalId = ((ExternalIdReference) publishIn).getExternalId();

        if (publishIn != null && !newVersionById.containsKey(publishInExternalId)) {
            Policy newPublishInVersion =
                createNewVersionOfExistingContent(publishIn.resolve(server));

            newVersionById.put(publishInExternalId, newPublishInVersion);
        }
    }

    private void deploy(TextContent textContent, Policy policy) throws CMException, DeployException {
        Content content = policy.getContent();

        if (textContent.getTemplateId() != null) {
            TextContent template = contentSet.get(textContent.getTemplateId());

            if (template == null) {
                throw new DeployException("The object \"" + textContent.getTemplateId() +
                    "\" specified as template of " + textContent + " must be defined in the same file.");
            }

            deploy(template, policy);
        }

        setReferences(textContent, content);

        setComponents(textContent, content);

        setLists(textContent, content);

        setSecurityParent(textContent, content);

        setInputTemplate(textContent, content);
    }

    private void setInputTemplate(TextContent textContent, Content content)
            throws CMException {
        Reference inputTemplate = textContent.getInputTemplate();

        if (inputTemplate != null) {
            content.setInputTemplateId(inputTemplate.resolve(server));
        }
    }

    private void setSecurityParent(TextContent textContent, Content content)
            throws CMException {
        Reference securityParent = textContent.getSecurityParent();

        if (securityParent != null) {
            content.setSecurityParentId(securityParent.resolve(server));
        }
    }

    private void setLists(TextContent textContent, Content content)
            throws CMException {
        for (Entry<String, List<Reference>> groupEntry : textContent.getLists().entrySet()) {
            String group = groupEntry.getKey();

            ContentList contentList = content.getContentList(group);

            for (int i = contentList.size()-1; i >= 0; i--) {
                contentList.remove(0);
            }

            int index = 0;

            for (Reference reference : groupEntry.getValue()) {
                contentList.add(index++, new ContentReference(reference.resolve(server), null));
            }
        }
    }

    private void setComponents(TextContent textContent, Content content)
            throws CMException {
        for (Entry<String, Map<String, String>> groupEntry : textContent.getComponents().entrySet()) {
            String group = groupEntry.getKey();

            for (Entry<String, String> componentEntry : groupEntry.getValue().entrySet()) {
                String component = componentEntry.getKey();
                String value = componentEntry.getValue();

                content.setComponent(group, component, value);
            }
        }
    }

    private void setReferences(TextContent textContent, Content content)
            throws CMException {
        for (Entry<String, Map<String, Reference>> groupEntry : textContent.getReferences().entrySet()) {
            String group = groupEntry.getKey();

            for (Entry<String, Reference> componentEntry : groupEntry.getValue().entrySet()) {
                String component = componentEntry.getKey();
                Reference reference = componentEntry.getValue();

                content.setContentReference(group, component, reference.resolve(server));
            }
        }
    }

    private Policy createNewVersion(TextContent textContent)
            throws CMException, DeployException {
        VersionedContentId contentId = null;

        try {
            contentId = server.findContentIdByExternalId(new ExternalContentId(textContent.getId()));
        } catch (EJBFinderException e) {
        }

        Policy newVersionPolicy;

        if (contentId == null) {
            InputTemplate inputTemplate = getInputTemplate(textContent);

            newVersionPolicy = server.createContent(getMajor(server, inputTemplate), inputTemplate.getContentId());
            newVersionPolicy.getContent().setExternalId(textContent.getId());
        }
        else {
            newVersionPolicy = createNewVersionOfExistingContent(contentId);
        }

        return newVersionPolicy;
    }

    private Policy createNewVersionOfExistingContent(ContentId contentId) throws CMException {
        VersionedContentId latestVersion = new VersionedContentId(contentId, LATEST_VERSION);

        latestVersion = server.translateSymbolicContentId(latestVersion);

        Policy newVersionPolicy;
        LockInfo lockInfo = server.getContent(latestVersion).getLockInfo();

        if (lockInfo != null && !lockInfo.getLocker().equals(server.getCurrentCaller())) {
            ((Content) server.getContent(contentId)).forcedUnlock();
        }

        newVersionPolicy = server.createContentVersion(latestVersion);

        return newVersionPolicy;
    }

    private InputTemplate getInputTemplate(TextContent textContent)
            throws DeployException, CMException {
        Reference inputTemplateReference = textContent.getInputTemplate();

        TextContent atTemplate = textContent;

        while (inputTemplateReference == null && atTemplate.getTemplateId() != null) {
            atTemplate = contentSet.get(textContent.getTemplateId());

            if (atTemplate != null) {
                inputTemplateReference = atTemplate.getInputTemplate();
            }
        }

        if (inputTemplateReference == null) {
            throw new DeployException(textContent.getId() + " needs to specify an input " +
        	"template since the object did not already exist.");
        }

        ContentId inputTemplateId = inputTemplateReference.resolve(server);

        InputTemplate inputTemplate;

        try {
            inputTemplate = CheckedCast.cast(server.getContent(inputTemplateId), InputTemplate.class);
        } catch (CheckedClassCastException e) {
            throw new DeployException("The input template " + inputTemplateReference + " did not have InputTemplate as policy.");
        }
        return inputTemplate;
    }

    private int getMajor(PolicyCMServer server,
            InputTemplate inputTemplate) throws CMException, DeployException {
        String majorString = inputTemplate.getComponent("polopoly.Client", "major");

        if (majorString == null) {
            throw new DeployException("The input template " + inputTemplate.getName() + " did not specify its major (type). Assuming \"Article\".");
        }

        try {
            return server.getMajorByName(majorString);
        }
        catch (EJBFinderException e) {
            throw new DeployException("The input template " + inputTemplate.getName() + " specified the unknown major \"" + majorString + "\".");
        }
    }
}
