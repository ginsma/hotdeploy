package com.polopoly.pcmd.tool;

import java.io.File;

import com.polopoly.pcmd.tool.HotdeployGenerateBootstrapTool;
import com.polopoly.pcmd.tool.parameters.HotdeployBootstrapParameters;

/**
 * Makes the {@link HotdeployGenerateBootstrapTool} available from the command line. Makes it possible to 
 * generate bootstraps from ANT etc.
 * @author joel
 *
 */
public class HotdeployBoostrapGenerator {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        if(args.length != 1) {
            System.err.println("Usage:" + '\n' + "java HotdeployBootstrapGenerator [file/directory path]");
            System.exit(1);
        }
        
        File file = new File(args[0]);
        if(!file.exists()) {
            System.err.println("The file or directory: " + args[1] + " does not exist!");
            System.exit(1);
        }
        
        HotdeployGenerateBootstrapTool generateBootstrapTool = new HotdeployGenerateBootstrapTool();
        HotdeployBootstrapParameters parameters = generateBootstrapTool.createParameters();
        parameters.setFileOrDirectory(file);
        parameters.setForce(true);
        
        generateBootstrapTool.execute(null, parameters);
    }

}
