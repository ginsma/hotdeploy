package com.polopoly.pcmd.tool;

import static com.polopoly.pcmd.bootstrap.BootstrapFileGenerator.BOOTSTRAP_FILE_NAME;
import static com.polopoly.pcmd.tool.parameters.ForceAndFilesToDeployParameters.BOOTSTRAP_NON_CREATED_PARAMETER;
import static com.polopoly.pcmd.tool.parameters.ForceAndFilesToDeployParameters.IGNORE_PRESENT;
import static example.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME;

import com.polopoly.pcmd.argument.ArgumentException;
import com.polopoly.pcmd.argument.Arguments;
import com.polopoly.pcmd.argument.ParameterHelp;
import com.polopoly.pcmd.parser.BooleanParser;
import com.polopoly.pcmd.tool.parameters.FilesToDeployParameters;
import com.polopoly.util.client.PolopolyContext;

public class ImportParameters extends FilesToDeployParameters {
    private boolean bootstrapNonCreated = false;
    private boolean generateImportOrder = false;
    private boolean generateBootstrap = false;
    private boolean ignorePresent = false;
    private boolean force = false;
    private boolean searchResources;

    private static final String GENERATE_IMPORT_ORDER_PARAMETER = "order";
    private static final String GENERATE_BOOTSTRAP_PARAMETER = "bootstrap";
    private static final String FORCE_PARAMETER = "force";
    private static final String SEARCH_RESOURCES_PARAMETER = "searchclasspath";

    @Override
    public void getHelp(ParameterHelp help) {
        super.getHelp(help);
        help.addOption(BOOTSTRAP_NON_CREATED_PARAMETER, new BooleanParser(),
                "Whether to bootstrap content objects that are never created in the files " +
                "(these are likely to be misspellings if you specifying the full set of files to import).");
        help.addOption(GENERATE_IMPORT_ORDER_PARAMETER, new BooleanParser(),
                "Whether to determine an appropriate ordering of the files and store it as an " + IMPORT_ORDER_FILE_NAME +
                " file. Will be done automatically if such a file is missing.");
        help.addOption(GENERATE_BOOTSTRAP_PARAMETER, new BooleanParser(),
                "Whether to determine which objects need bootstrapping and create a " +
                BOOTSTRAP_FILE_NAME +
                " file. Will be done automatically when the import order is generated.");
        help.addOption(FORCE_PARAMETER, new BooleanParser(),
                "Whether to import files even if they had not been modified.");
        help.addOption(IGNORE_PRESENT, new BooleanParser(),
                "Whether to not consider any content to already be present (this is only useful when analyzing Polopoly initxml content).");
        help.addOption(SEARCH_RESOURCES_PARAMETER, new BooleanParser(),
                "Whether to deploy content present as resources on the class path (e.g. in JAR files). True by default.");
    }

    @Override
    public void parseParameters(Arguments args, PolopolyContext context)
            throws ArgumentException {
        super.parseParameters(args, context);
        setBootstrapNonCreated(args.getFlag(BOOTSTRAP_NON_CREATED_PARAMETER, bootstrapNonCreated));
        setGenerateImportOrder(args.getFlag(GENERATE_IMPORT_ORDER_PARAMETER, generateImportOrder));
        setGenerateBootstrap(args.getFlag(GENERATE_BOOTSTRAP_PARAMETER, generateBootstrap));
        setIgnorePresent(args.getFlag(IGNORE_PRESENT, ignorePresent));
        setForce(args.getFlag(FORCE_PARAMETER, force));
        setSearchResources(args.getFlag(SEARCH_RESOURCES_PARAMETER, true));
    }

    public void setBootstrapNonCreated(boolean bootstrapNonCreated) {
        this.bootstrapNonCreated = bootstrapNonCreated;
    }

    public boolean isBootstrapNonCreated() {
        return bootstrapNonCreated;
    }

    public boolean isGenerateImportOrder() {
        return generateImportOrder;
    }

    public void setGenerateImportOrder(boolean generateImportOrder) {
        this.generateImportOrder = generateImportOrder;
    }

    public boolean isGenerateBootstrap() {
        return generateBootstrap;
    }

    public void setGenerateBootstrap(boolean generateBootstrap) {
        this.generateBootstrap = generateBootstrap;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public void setIgnorePresent(boolean ignorePresent) {
        this.ignorePresent = ignorePresent;
    }

    public boolean isIgnorePresent() {
        return ignorePresent;
    }

    public void setSearchResources(boolean searchResources) {
        this.searchResources = searchResources;
    }

    public boolean isSearchResources() {
        return searchResources;
    }
}
