package com.polopoly.pcmd.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.polopoly.pcmd.tool.parameters.HotdeployListParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.client.Major;
import example.deploy.xml.allcontent.AllContent;
import example.deploy.xml.allcontent.AllContentFinder;

public class HotdeployListTool implements Tool<HotdeployListParameters> {

    public HotdeployListParameters createParameters() {
        return new HotdeployListParameters();
    }

    public void execute(PolopolyContext context, HotdeployListParameters parameters) {
        AllContentFinder finder =
            new AllContentFinder(parameters.discoverFiles());

        AllContent allContent = finder.find();

        if (parameters.getMajor() != null) {
            printMajor(allContent, parameters.getMajor());
        }
        else {
            List<Major> majors = new ArrayList<Major>(allContent.getMajors());
            Collections.sort(majors);

            for (Major major : majors) {
                if (parameters.isVerbose()) {
                    System.out.println();
                    System.out.println("Major " + major.toString().toUpperCase());
                }

                printMajor(allContent, major);
            }
        }
    }

    private void printMajor(AllContent allContent, Major major) {
        List<String> externalIdsOrdered =
            new ArrayList<String>(allContent.getExternalIds(major));

        Collections.sort(externalIdsOrdered);

        for (String externalId :  externalIdsOrdered) {
            System.out.println(externalId);
        }
    }

    public String getHelp() {
        return "Returns a list of all the content defined in the specified directory.";
    }

}
