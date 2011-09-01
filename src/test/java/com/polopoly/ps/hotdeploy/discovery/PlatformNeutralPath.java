package com.polopoly.ps.hotdeploy.discovery;

import java.io.File;

public class PlatformNeutralPath {

    public static String unixToPlatformSpecificPath(String unixPath) {
        if (File.separatorChar != '/') {
            return unixPath.replace('/', File.separatorChar);
        }
        else {
            return unixPath;
        }
    }
}
