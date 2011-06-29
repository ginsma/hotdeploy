package com.polopoly.pcmd.tool.parameters;

import static com.polopoly.pcmd.tool.parameters.ForceAndFilesToDeployParameters.IGNORE_PRESENT;

import java.io.File;

import com.polopoly.ps.pcmd.argument.ArgumentException;
import com.polopoly.ps.pcmd.argument.Arguments;
import com.polopoly.ps.pcmd.argument.NotProvidedException;
import com.polopoly.ps.pcmd.argument.ParameterHelp;
import com.polopoly.ps.pcmd.parser.BooleanParser;
import com.polopoly.ps.pcmd.parser.ExistingDirectoryParser;
import com.polopoly.util.client.PolopolyContext;

public class HotdeployValidateParameters extends FilesToDeployParameters {
	private static final String VALIDATE_CLASSES_PARAMETER = "validateclasses";
	private static final String CLASS_DIRECTORY_PARAMETER = "classpath";
	private static final String DATABASE_IS_PRESENT_PARAMETER = "databaseispresent";
	private static final String SEARCH_RESOURCES_PARAMETER = "searchclasspath";
	private static final String ONLY_JAR_RESOURCES_PARAMETER = "onlyjarresources";

	private File classDirectory;
	private boolean ignorePresent = false;
	private boolean validateClasses;
	private boolean databaseIsPresent = false;
	private boolean searchResources = true;
	private boolean onlyJarResources = true;

	public void setValidateClasses(boolean validateClasses) {
		this.validateClasses = validateClasses;
	}

	public boolean isValidateClasses() {
		return validateClasses;
	}

	@Override
	public void getHelp(ParameterHelp help) {
		super.getHelp(help);
		help.addOption(
				VALIDATE_CLASSES_PARAMETER,
				new BooleanParser(),
				"Whether to check that the referenced class names exists in the specified class directory or the current classpath.");

		help.addOption(
				IGNORE_PRESENT,
				new BooleanParser(),
				"Whether to not consider any content to already be present (this is only useful when analyzing Polopoly initxml content).");

		help.addOption(CLASS_DIRECTORY_PARAMETER,
				new ExistingDirectoryParser(),
				"A class directory for checking whether referenced class names exists.");

		help.addOption(
				DATABASE_IS_PRESENT_PARAMETER,
				new BooleanParser(),
				"Whether to consider all data already present (useful to validate if a given XML can be imported.");

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
	}

	@Override
	public void parseParameters(Arguments args, PolopolyContext context)
			throws ArgumentException {
		super.parseParameters(args, context);
		setIgnorePresent(args.getFlag(IGNORE_PRESENT, ignorePresent));
		setValidateClasses(args.getFlag(VALIDATE_CLASSES_PARAMETER, false));
		setDatabaseIsPresent(args.getFlag(DATABASE_IS_PRESENT_PARAMETER, false));
		setSearchResources(args.getFlag(SEARCH_RESOURCES_PARAMETER,
				searchResources));
		setOnlyJarResources(args.getFlag(ONLY_JAR_RESOURCES_PARAMETER,
				onlyJarResources));

		try {
			setClassDirectory(args.getOption(CLASS_DIRECTORY_PARAMETER,
					new ExistingDirectoryParser()));
		} catch (NotProvidedException e) {
			// fine.
		}
	}

	public void setClassDirectory(File classDirectory) {
		this.classDirectory = classDirectory;
	}

	public File getClassDirectory() {
		return classDirectory;
	}

	public void setIgnorePresent(boolean ignorePresent) {
		this.ignorePresent = ignorePresent;
	}

	public boolean isIgnorePresent() {
		return ignorePresent;
	}

	public void setDatabaseIsPresent(boolean databaseIsPresent) {
		this.databaseIsPresent = databaseIsPresent;
	}

	public boolean isDatabaseIsPresent() {
		return databaseIsPresent;
	}

	public void setOnlyJarResources(boolean onlyJarResources) {
		this.onlyJarResources = onlyJarResources;
	}

	public boolean isOnlyJarResources() {
		return onlyJarResources;
	}

	public void setSearchResources(boolean searchResources) {
		this.searchResources = searchResources;
	}

	public boolean isSearchResources() {
		return searchResources;
	}
}
