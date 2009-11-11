package example.deploy.text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import example.deploy.hotdeploy.client.Major;

public class TextContent {
    private Major major = Major.UNKNOWN;
    private String id;
    private Reference securityParent;
    private Reference inputTemplate;

    private Map<String, Map<String, String>> components = new HashMap<String, Map<String,String>>();
    private Map<String, Map<String, Reference>> references = new HashMap<String, Map<String,Reference>>();
    private Map<String, List<Reference>> lists = new HashMap<String, List<Reference>>();
    private Map<String, byte[]> files = new HashMap<String, byte[]>();

    private List<Publishing> publishings = new ArrayList<Publishing>();

    private String templateId;

    public Map<String, List<Reference>> getLists() {
        return lists;
    }

    public Map<String, Map<String, String>> getComponents() {
        return components;
    }

    public Map<String, Map<String, Reference>> getReferences() {
        return references;
    }

    public void setComponent(String group, String name, String value) {
        Map<String, String> groupMap = components.get(group);

        if (groupMap == null) {
            groupMap = new HashMap<String, String>();
            components.put(group, groupMap);
        }

        groupMap.put(name, value);
    }

    public String getComponent(String group, String name) {
        Map<String, String> groupMap = components.get(group);

        if (groupMap == null) {
            return null;
        }

        return groupMap.get(name);
    }

    public void setReference(String group, String name, Reference reference) {
        Map<String, Reference> groupMap = references.get(group);

        if (groupMap == null) {
            groupMap = new HashMap<String, Reference>();
            references.put(group, groupMap);
        }

        groupMap.put(name, reference);
    }


    public Reference getReference(String group, String name) {
        Map<String, Reference> groupMap = references.get(group);

        if (groupMap == null) {
            return null;
        }

        return groupMap.get(name);
    }

    public void setSecurityParent(Reference securityParent) {
        this.securityParent = securityParent;
    }

    public Reference getSecurityParent() {
        return securityParent;
    }

    public void setInputTemplate(Reference inputTemplate) {
        this.inputTemplate = inputTemplate;
    }

    public Reference getInputTemplate() {
        return inputTemplate;
    }

    public List<Reference> getList(String group) {
        List<Reference> list = lists.get(group);

        if (list == null) {
            list = new ArrayList<Reference>();
            lists.put(group, list);
        }

        return list;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void validate(ValidationContext context) throws ValidationException {
        if (id == null) {
            throw new ValidationException("Content without ID specified.");
        }

        if (inputTemplate == null && templateId == null) {
            throw new ValidationException(this + " needs have an input template or a content template.");
        }

        if (inputTemplate != null) {
            try {
                inputTemplate.validateTemplate(context);
            }
            catch (ValidationException v) {
                v.setContext("input template of " + this);

                throw v;
            }
        }

        if (templateId != null) {
            context.validateTextContentExistence(templateId);
        }

        for (Entry<String, Map<String, Reference>> groupEntry : references.entrySet()) {
            String group = groupEntry.getKey();

            for (Entry<String, Reference> referenceEntry : groupEntry.getValue().entrySet()) {
                String name = referenceEntry.getKey();
                Reference reference = referenceEntry.getValue();

                try {
                    reference.validate(context);
                }
                catch (ValidationException v) {
                    v.setContext("reference " + group + ":" + name + " in " + this);

                    throw v;
                }
            }
        }

        if (securityParent != null) {
            try {
                securityParent.validate(context);
            }
            catch (ValidationException v) {
                v.setContext("security parent of " + this);

                throw v;
            }
        }

        for (Publishing publishing : publishings) {
            try {
                publishing.getPublishIn().validate(context);
            }
            catch (ValidationException v) {
                v.setContext("publish content of " + this);

                throw v;
            }
        }
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public List<Publishing> getPublishings() {
        return publishings;
    }

    void addFile(String fileName, InputStream fileData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);

        int ch;

        while ((ch = fileData.read()) != -1) {
            baos.write(ch);
        }

        files.put(fileName, baos.toByteArray());
    }

    Map<String, byte[]> getFiles() {
        return files;
    }

    public void addPublishing(Publishing publishing) {
        publishings.add(publishing);
    }

    public Major getMajor() {
        return major;
    }

    public void setMajor(Major major) {
        this.major = major;
    }

    @Override
    public String toString() {
        return id;
    }
}
