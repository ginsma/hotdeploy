package com.polopoly.ps.hotdeploy.state;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.polopoly.ps.hotdeploy.file.DeploymentFile;

public class NonPersistedFileChecksums implements FileChecksums {
	private Map<DeploymentFile, Long> quickChecksums = new HashMap<DeploymentFile, Long>();
	private Map<DeploymentFile, Long> slowChecksums = new HashMap<DeploymentFile, Long>();
	private Map<DeploymentFile, String> additionalInfo = new HashMap<DeploymentFile, String>();
	private boolean persisted = true;

	public long getQuickChecksum(DeploymentFile file) {
		Long result = quickChecksums.get(file);

		if (result == null) {
			return -1;
		} else {
			return result;
		}
	}

	public long getSlowChecksum(DeploymentFile file) {
		Long result = slowChecksums.get(file);

		if (result == null) {
			return -1;
		} else {
			return result;
		}
	}

	public void setChecksums(DeploymentFile file, long quickChecksum,
			long slowChecksum) {
		slowChecksums.put(file, slowChecksum);
		quickChecksums.put(file, quickChecksum);
		persisted = false;
	}

	public boolean contains(DeploymentFile file) {
		return slowChecksums.containsKey(file)
				|| quickChecksums.containsKey(file);
	}

	public boolean areAllChangesPersisted() {
		return persisted;
	}

	public void persist() throws CouldNotUpdateStateException {
		persisted = true;
	}

	@Override
	public Iterator<DeploymentFile> iterator() {
		return slowChecksums.keySet().iterator();
	}

	@Override
	public void deleteChecksums(DeploymentFile file) {
		slowChecksums.remove(file);
		quickChecksums.remove(file);
	}

	@Override
	public void setAdditionalInformation(DeploymentFile file,
			String additionalInformation) {
		additionalInfo.put(file, additionalInformation);
	}

	@Override
	public String getAdditionalInformation(DeploymentFile file)
			throws NoInformationStoredException {
		String result = additionalInfo.get(file);

		if (result == null) {
			throw new NoInformationStoredException();
		}

		return result;
	}

}
