package example.deploy.xml.allcontent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import example.deploy.hotdeploy.client.Major;

public class AllContent {
    private Map<Major, Set<String>> externalIdsByMajor = new HashMap<Major, Set<String>>();

    public void add(Major major, String externalId) {
        getExternalIds(major).add(externalId);
    }

    public Set<Major> getMajors() {
        return externalIdsByMajor.keySet();
    }

    public Set<String> getExternalIds(Major major) {
        Set<String> result = externalIdsByMajor.get(major);

        if (result == null) {
            result = new HashSet<String>();
            externalIdsByMajor.put(major, result);
        }

        return result;
    }
}
