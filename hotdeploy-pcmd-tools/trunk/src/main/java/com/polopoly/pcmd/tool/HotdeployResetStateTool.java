package com.polopoly.pcmd.tool;

import com.polopoly.pcmd.tool.parameters.EmptyParameters;
import com.polopoly.ps.deploy.hotdeploy.state.CouldNotFetchChecksumsException;
import com.polopoly.ps.deploy.hotdeploy.state.DefaultFileChecksums;
import com.polopoly.ps.pcmd.tool.Tool;
import com.polopoly.util.client.PolopolyContext;


public class HotdeployResetStateTool implements Tool<EmptyParameters>{

    public EmptyParameters createParameters() {
        return new EmptyParameters();
    }

    public void execute(PolopolyContext context, EmptyParameters parameters) {
        try {
            new DefaultFileChecksums(context.getPolicyCMServer()).clear();

            System.out.println("Successfully reset hotdeploy state.");
        } catch (CouldNotFetchChecksumsException e) {
            System.err.println("Could not fetch hotdeploy state: " + e.getMessage());
        }
    }

    public String getHelp() {
        return "Clears the hotdeploy information about what files have been imported.";
    }

}
