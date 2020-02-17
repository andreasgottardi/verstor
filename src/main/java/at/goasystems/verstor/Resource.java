package at.goasystems.verstor;

import java.util.ArrayList;
import java.util.List;

public class Resource {

	private String resourceid;
	private List<LocalizedFile> files;
	private MetaData metadata;

	public Resource() {
		this.metadata = new MetaData();
		files = new ArrayList<>();
	}

	public String getResourceid() {
		return resourceid;
	}

	public void setResourceid(String resourceid) {
		this.resourceid = resourceid;
	}

	public List<LocalizedFile> getFiles() {
		return files;
	}

	public void addFile(LocalizedFile file) {
		this.files.add(file);
	}

	public void addFiles(List<LocalizedFile> files) {
		this.files.addAll(files);
	}

	public MetaData getMetadata() {
		return metadata;
	}

	public void setMetadata(MetaData metadata) {
		this.metadata = metadata;
	}
}
