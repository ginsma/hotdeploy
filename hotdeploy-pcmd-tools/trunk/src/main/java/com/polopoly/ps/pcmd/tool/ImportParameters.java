package com.polopoly.ps.pcmd.tool;

import static com.polopoly.ps.deploy.hotdeploy.discovery.importorder.ImportOrderFileDiscoverer.IMPORT_ORDER_FILE_NAME;
import static com.polopoly.ps.pcmd.bootstrap.BootstrapFileGenerator.BOOTSTRAP_FILE_NAME;
import static com.polopoly.ps.pcmd.tool.parameters.ForceAndFilesToDeployParameters.BOOTSTRAP_NON_CREATED_PARAMETER;
import static com.polopoly.ps.pcmd.tool.parameters.ForceAndFilesToDeployParameters.IGNORE_PRESENT;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.NotProvidedException;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.ps.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.ps.pcmd.tool.parameters.FilesToDeployParameters;
import com.polopoly.util.client.PolopolyContext;

public class ImportParameters extends FilesToDeployParameters {
	private boolean bootstrapNonCreated = false;
	private boolean generateImportOrder = false;
	private boolean generateBootstrap = false;
	private boolean ignorePresent = false;
	private boolean force = false;
	private boolean searchResources = true;
	private boolean onlyJarResources = true;
	private String considerJar;
	private boolean ignoreContentListAddFailures;
	private List<File> directories = null;

	private static final String DIRECTOY_PARAMETER = "dir";
	private static final String GENERATE_IMPORT_ORDER_PARAMETER = "order";
	private static final String GENERATE_BOOTSTRAP_PARAMETER = "bootstrap";
	private static final String FORCE_PARAMETER = "force";
	private static final String SEARCH_RESOURCES_PARAMETER = "searchclasspath";
	private static final String CONSIDER_JAR = "considerjar";
	private static final String ONLY_JAR_RESOURCES_PARAMETER = "onlyjarresources";
	private static final String IGNORE_CONTENT_LIST_ADD_FAILURES = "ignorecontentlistaddfailures";

	@Override
	public void getHelp(ParameterHelp help) {
		super.getHelp(help);
		help.addOption(
				DIRECTOY_PARAMETER,
				new ExistingDirectoryParser(),
				"Directory to search content in. This option may be provided multiple times.");
		help.addOption(
				BOOTSTRAP_NON_CREATED_PARAMETER,
				new BooleanParser(),
				"Whether to bootstrap content objects that are never created in the files "
						+ "(these are likely to be misspellings if you specifying the full set of files to import).");
		help.addOption(
				GENERATE_IMPORT_ORDER_PARAMETER,
				new BooleanParser(),
				"Whether to determine an appropriate ordering of the files and store it as an "
						+ IMPORT_ORDER_FILE_NAME
						+ " file. Will be done automatically if such a file is missing.");
		help.addOption(
				GENERATE_BOOTSTRAP_PARAMETER,
				new BooleanParser(),
				"Whether to determine which objects need bootstrapping and create a "
						+ BOOTSTRAP_FILE_NAME
						+ " file. Will be done automatically when the import order is generated.");
		help.addOption(FORCE_PARAMETER, new BooleanParser(),
				"Whether to import files even if they had not been modified.");
		help.addOption(
				IGNORE_PRESENT,
				new BooleanParser(),
				"Whether to not consider any content to already be present (this is only useful when analyzing Polopoly initxml content).");
		help.addOption(
				SEARCH_RESOURCES_PARAMETER,
				new BooleanParser(),
				"Whether to deploy content present as resources on the class path (e.g. in JAR files). True by default.");
		help.addOption(
				ONLY_JAR_RESOURCES_PARAMETER,
				new BooleanParser(),
				"Whether (if "
						+ SEARCH_RESOURCES_PARAMETER
						+ " is true) to only deploy resources in JARS and not files on the class path. True by default.");
		help.addOption(
				CONSIDER_JAR,
				null,
				"Whether to treat the directory as if it were a JAR file with the specified name. "
						+ "This means that if the files were imported from that file before and are unchanged they will not be reimported.");
		help.addOption(
				IGNORE_CONTENT_LIST_ADD_FAILURES,
				null,
				"Whether to ignore failures to add objects to content lists. This useful if there are content list wrappers acting up e.g. when dealing with bootstrapped objects.");
	}

	@Override
	public void parseParameters(Arguments args, PolopolyContext context)
			throws ArgumentException {
		super.parseParameters(args, context);
		
		try {
			setDirectories(args.getOptions(
					DIRECTOY_PARAMETER, new ExistingDirectoryParser()));
		} catch (NotProvidedException e) {
			setDirectories(Collections.<File> emptyList());
		}

		setBootstrapNonCreated(args.getFlag(BOOTSTRAP_NON_CREATED_PARAMETER,
				bootstrapNonCreated));
		setGenerateImportOrder(args.getFlag(GENERATE_IMPORT_ORDER_PARAMETER,
				generateImportOrder));
		setGenerateBootstrap(args.getFlag(GENERATE_BOOTSTRAP_PARAMETER,
				generateBootstrap));
		setIgnorePresent(args.getFlag(IGNORE_PRESENT, ignorePresent));
		setForce(args.getFlag(FORCE_PARAMETER, force));
		setSearchResources(args.getFlag(SEARCH_RESOURCES_PARAMETER,
				searchResources));
		setOnlyJarResources(args.getFlag(ONLY_JAR_RESOURCES_PARAMETER,
				onlyJarResources));
		setConsiderJar(args.getOptionString(CONSIDER_JAR, null));
		setIgnoreContentListAddFailures(args.getFlag(
				IGNORE_CONTENT_LIST_ADD_FAILURES, false));
	}

	public void setDirectories(List<File> directories) {
		this.directories = directories;
	}

	public List<File> getDirectories() {
		return directories;
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

	public String getConsiderJar() {
		return considerJar;
	}

	public void setConsiderJar(String considerJar) {
		this.considerJar = considerJar;
	}

	public void setOnlyJarResources(boolean onlyJarResources) {
		this.onlyJarResources = onlyJarResources;
	}

	public boolean isOnlyJarResources() {
		return onlyJarResources;
	}

	public void setIgnoreContentListAddFailures(
			boolean ignoreContentListAddFailures) {
		this.ignoreContentListAddFailures = ignoreContentListAddFailures;
	}

	public boolean isIgnoreContentListAddFailures() {
		return ignoreContentListAddFailures;
	}
}