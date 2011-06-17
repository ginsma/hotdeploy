package example.deploy.hotdeploy.discovery.importorder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import example.deploy.hotdeploy.file.DeploymentDirectory;
import example.deploy.hotdeploy.file.DeploymentObject;

public class ImportOrderFileWriter {
	private ImportOrderFile importOrderFile;

	public ImportOrderFileWriter(ImportOrderFile importOrderFile) {
		this.importOrderFile = importOrderFile;
	}

	public void write(Writer writer) {
		PrintWriter printWriter = new PrintWriter(writer);
		DeploymentDirectory directory = importOrderFile.getDirectory();

		for (String dependency : importOrderFile.getDependencies()) {
			printWriter.println(ImportOrderFileParser.DEPENDENCY_PREFIX
					+ dependency);
		}

		for (DeploymentObject deploymentObject : importOrderFile) {
			printWriter.println(directory.getRelativeName(deploymentObject));
		}
	}

	public void write() throws IOException {
		Writer fileWriter = new OutputStreamWriter(new FileOutputStream(
				importOrderFile.getFile()));

		try {
			write(fileWriter);
		} finally {
			fileWriter.close();
		}
	}
}
