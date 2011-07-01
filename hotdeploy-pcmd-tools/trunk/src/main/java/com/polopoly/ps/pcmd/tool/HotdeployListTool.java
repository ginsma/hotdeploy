package com.polopoly.ps.pcmd.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.hotdeploy.client.Major;
import com.polopoly.ps.hotdeploy.xml.allcontent.AllContent;
import com.polopoly.ps.hotdeploy.xml.allcontent.AllContentFinder;
import com.polopoly.ps.pcmd.tool.parameters.HotdeployListParameters;
import com.polopoly.util.client.PolopolyContext;


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
        return "Lists all content defined in content or template XML in the specified directory.";
    }

}
