package com.polopoly.ps.deploy.hotdeploy;

import com.polopoly.ps.deploy.hotdeploy.client.ArgumentConsumer;

public abstract class DiscovererParameterParser implements ArgumentConsumer {
    private DiscovererMainClass discoverer;

    protected DiscovererParameterParser(DiscovererMainClass discoverer) {
        this.discoverer = discoverer;
    }

    public boolean argumentFound(String parameter, String value) {
        if (parameter.equals("discoverresources")) {
            discoverer.setDiscoverResources((value == null ? true : Boolean.parseBoolean(value)));
        }
        else if (parameter.equals("onlyjarresources")) {
            discoverer.setOnlyJarResources((value == null ? true : Boolean.parseBoolean(value)));
        }
        else if (parameter.equals("dir")) {
            if (value == null) {
                valueRequired(parameter);
            }

            discoverer.addDirectoryName(value);
        }
        else {
            return false;
        }

        return true;
    }

    protected void noValueAccepted(String parameter, String value) {
        System.err.println(parameter + " does not take value (value \"" + value + "\" was provided).");
        printParameterHelp();
        System.exit(1);
    }

    protected void valueRequired(String parameter) {
        System.err.println("Parameter " + parameter + " required a value. Provide it using --" + parameter + "=<value>.");
        printParameterHelp();
        System.exit(1);
    }

    protected abstract void printParameterHelp();
}
