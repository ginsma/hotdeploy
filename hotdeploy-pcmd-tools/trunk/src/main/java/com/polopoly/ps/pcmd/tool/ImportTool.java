package com.polopoly.ps.pcmd.tool;

import static com.polopoly.ps.hotdeploy.util.Plural.plural;
import static com.polopoly.ps.pcmd.tool.HotdeployGenerateImportOrderTool.generateImportOrder;
import static com.polopoly.ps.pcmd.tool.HotdeployGenerateImportOrderTool.writeFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.polopoly.cm.ContentId;
import com.polopoly.ps.hotdeploy.deployer.DefaultSingleFileDeployer;
import com.polopoly.ps.hotdeploy.deployer.FatalDeployException;
import com.polopoly.ps.hotdeploy.deployer.MultipleFileDeployer;
import com.polopoly.ps.hotdeploy.discovery.FileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.ImportOrderOrDirectoryFileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.NotApplicableException;
import com.polopoly.ps.hotdeploy.discovery.PluginFileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.ResourceFileDiscoverer;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrder;
import com.polopoly.ps.hotdeploy.discovery.importorder.ImportOrderFile;
import com.polopoly.ps.hotdeploy.file.DeploymentFile;
import com.polopoly.ps.hotdeploy.file.FileDeploymentDirectory;
import com.polopoly.ps.hotdeploy.state.CouldNotUpdateStateException;
import com.polopoly.ps.hotdeploy.state.DirectoryState;
import com.polopoly.ps.hotdeploy.state.DirectoryStateFetcher;
import com.polopoly.ps.hotdeploy.state.DirectoryWillBecomeJarDirectoryState;
import com.polopoly.ps.hotdeploy.state.NoFilesImportedDirectoryState;
import com.polopoly.ps.hotdeploy.xml.parser.ContentXmlParser;
import com.polopoly.ps.hotdeploy.xml.parser.cache.CachingDeploymentFileParser;
import com.polopoly.ps.pcmd.bootstrap.BootstrapFileGenerator;
import com.polopoly.ps.pcmd.field.content.AbstractContentIdField;
import com.polopoly.pcmd.tool.Tool;
import com.polopoly.ps.pcmd.tool.parameters.FilesToDeployParameters;
import com.polopoly.util.client.PolopolyContext;


public class ImportTool implements Tool<ImportParameters> {
	private static final class LoggingSingleFileDeployer extends
			DefaultSingleFileDeployer {
		private int fileCount;
		private int imported;
		private PolopolyContext context;

		private LoggingSingleFileDeployer(PolopolyContext context, int fileCount) {
			super(context.getPolicyCMServer());

			this.context = context;
			this.fileCount = fileCount;
		}

		@Override
		public boolean importAndHandleException(DeploymentFile fileToImport)
				throws FatalDeployException {
			StringBuffer message = new StringBuffer(100);

			message.append("Importing ");
			message.append(fileToImport);

			if (fileCount > 10) {
				message.append(" (" + getPercentage(imported++) + "%)");
			}

			message.append("...");

			System.out.println(message);

			return super.importAndHandleException(fileToImport);
		}

		@Override
		protected void contentCommitted(ContentId createdId) {
			System.out.println(AbstractContentIdField.get(createdId, context));
		}

		private String getPercentage(int i) {
			return Integer.toString(100 * imported / fileCount);
		}
	}

	private CachingDeploymentFileParser cachingParser;
	private DirectoryState directoryState;
	private DirectoryStateFetcher directoryStateFetcher;

	public ImportTool() {
		cachingParser = new CachingDeploymentFileParser(new ContentXmlParser());
	}

	public ImportParameters createParameters() {
		return new ImportParameters();
	}

	private boolean importOrderAvailable(FilesToDeployParameters parameters) {
		try {
			return new ImportOrderFile(parameters.getDirectory()).getFile()
					.exists();
		} catch (IOException e) {
			System.err
					.println("Could not determine where to place import order: "
							+ e.getMessage()
							+ ". Not writing import order file.");

			return false;
		}
	}

	private boolean shouldCreateImportOrder(ImportParameters parameters,
			List<DeploymentFile> files) {
		return files.size() > 1
				&& (!importOrderAvailable(parameters) || parameters
						.isGenerateImportOrder());
	}

	private List<DeploymentFile> createImportOrder(ImportParameters parameters) {
		ImportOrder importOrder = generateImportOrder(cachingParser,
				parameters, parameters.isIgnorePresent());

		writeFile(importOrder);

		return importOrder;
	}

	private boolean shouldCreateBootstrap(ImportParameters parameters,
			boolean createImportOrder) {
		return createImportOrder || parameters.isGenerateBootstrap();
	}

	private void createBootstrap(ImportParameters parameters,
			List<DeploymentFile> files) {
		BootstrapFileGenerator generator = new BootstrapFileGenerator();

		generator.setForce(true);
		generator.setBootstrapNonCreated(parameters.isBootstrapNonCreated());
		generator.setIgnorePresent(parameters.isIgnorePresent());
		generator.setParser(cachingParser);

		generator.generateBootstrap(parameters.getDirectory(), files);
	}

	private DirectoryState getDirectoryState(PolopolyContext context,
			ImportParameters parameters) {
		if (parameters.isForce()) {
			return new NoFilesImportedDirectoryState();
		} else {
			directoryStateFetcher = new DirectoryStateFetcher(
					context.getPolicyCMServer());

			DirectoryState directoryState = directoryStateFetcher
					.getDirectoryState();

			String considerDirectoryJar = parameters.getConsiderJar();

			if (considerDirectoryJar != null) {
				directoryState = new DirectoryWillBecomeJarDirectoryState(
						directoryState, new FileDeploymentDirectory(
								parameters.getDirectory()),
						considerDirectoryJar);
			}

			return directoryState;
		}
	}

	public void execute(PolopolyContext context, ImportParameters parameters) {
		directoryState = getDirectoryState(context, parameters);

		try {
			if (parameters.isSearchResources()) {
				deploy(context, parameters);
			}

			List<DeploymentFile> files = parameters.discoverFiles();

			boolean createImportOrder = shouldCreateImportOrder(parameters,
					files);

			if (createImportOrder) {
				files = createImportOrder(parameters);
			}

			boolean createBootstrap = shouldCreateBootstrap(parameters,
					createImportOrder);

			if (createBootstrap) {
				createBootstrap(parameters, files);
			}

			if (parameters.getFileOrDirectory() != null) {
				System.out.println(files.size() + " content file"
						+ plural(files.size()) + " found in "
						+ parameters.getFileOrDirectory().getAbsolutePath()
						+ "...");

				MultipleFileDeployer deployer = createDeployer(context,
						parameters, files.size());

				deployer.deploy(files);

				System.out.println(deployer.getResultMessage(files));
			}
		} catch (FatalDeployException e) {
			System.err.println("Deployment could not be performed: "
					+ e.getMessage());
		} finally {
			persistState(directoryState);
		}
	}

	private MultipleFileDeployer createDeployer(PolopolyContext context,
			ImportParameters parameters, int fileCount) {
		LoggingSingleFileDeployer singleFileDeployer = new LoggingSingleFileDeployer(
				context, fileCount);

		singleFileDeployer.setIgnoreContentListAddFailures(parameters
				.isIgnoreContentListAddFailures());

		return new MultipleFileDeployer(singleFileDeployer, directoryState);
	}

	private void deploy(PolopolyContext context,
			ImportParameters parameters) throws FatalDeployException {
		List<FileDiscoverer> discoverers = new ArrayList<FileDiscoverer>();

		if (parameters.isSearchResources()) {
			discoverers.add(new ResourceFileDiscoverer(parameters
					.isOnlyJarResources()));
			discoverers.add(new PluginFileDiscoverer());
		}

		for (File directory : parameters.getDirectories()) {
			discoverers
			.add(new ImportOrderOrDirectoryFileDiscoverer(directory));
		}

		List<DeploymentFile> filesToDeploy = new ArrayList<DeploymentFile>();

		for (FileDiscoverer discoverer : discoverers) {
			try {
				filesToDeploy.addAll(discoverer.getFilesToImport());
			} catch (NotApplicableException e) {
				System.err.println("Cannot apply discovery strategy "
						+ discoverer + ": " + e.getMessage());
			}
		}

		if (filesToDeploy.isEmpty()) {
			return;
		}

		System.out
		.println(filesToDeploy.size() + " content file"
				+ plural(filesToDeploy.size())
				+ " found in the classpath.");

		MultipleFileDeployer deployer = createDeployer(context, parameters,
				filesToDeploy.size());

		deployer.deploy(filesToDeploy);

		if (!deployer.isAllFilesUnchanged()) {
			System.out.println("Content in the classpath: "
					+ deployer.getResultMessage(filesToDeploy));
		}

		// we might have deployed the hotdeploy templates here. if that is
		// the case we can now (and only now) fetch the directory state.
		if (directoryStateFetcher != null) {
			directoryState = directoryStateFetcher
			.refreshAfterFailingToFetch();
		}
	}

	private void persistState(DirectoryState directoryState) {
		try {
			directoryState.persist();
		} catch (CouldNotUpdateStateException e) {
			System.err.println("Error recording deployment state: "
					+ e.getMessage());
		}
	}

	public String getHelp() {
		return "Imports content files that have been modified since last time they were imported (or were never imported).";
	}
}
