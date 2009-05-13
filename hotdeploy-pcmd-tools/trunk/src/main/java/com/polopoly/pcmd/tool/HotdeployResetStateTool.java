package com.polopoly.pcmd.tool;

import com.polopoly.pcmd.tool.parameters.EmptyParameters;
import com.polopoly.util.client.PolopolyContext;

import example.deploy.hotdeploy.state.CouldNotFetchChecksumsException;
import example.deploy.hotdeploy.state.DefaultFileChecksums;

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
