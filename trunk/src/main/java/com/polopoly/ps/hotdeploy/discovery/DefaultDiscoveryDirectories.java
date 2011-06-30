package com.polopoly.ps.hotdeploy.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultDiscoveryDirectories {
    private static List<String> directories = new ArrayList<String>();

    public static Collection<String> getDirectories() {
        return directories;
    }

    public void addDirectory(String directory) {
        directories.add(directory);
    }

    static {
        for (String directoryToAdd : new String[] {
            "/META-INF/content",
            "/WEB-INF/classes/content",
            "/WEB-INF/content",
            "src/main/resources/content",
            "content",
            "."}) {
            directories.add(directoryToAdd);
        }
    };



}
