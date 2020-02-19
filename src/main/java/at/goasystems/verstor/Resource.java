package at.goasystems.verstor;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Resource {

	@SerializedName("resourceid")
	private String resourceid;

	@SerializedName("metadata")
	private MetaData metadata;

	@SerializedName("files")
	private List<LocalizedFile> files;

	public Resource() {
		this.resourceid = "";
		this.metadata = new MetaData();
		files = new ArrayList<>();
	}

	public Resource(String resourceid) {
		this.resourceid = resourceid;
		this.metadata = new MetaData();
		files = new ArrayList<>();
	}

	public String getResourceid() {
		return resourceid;
	}

	public void setResourceid(String resourceid) {
		this.resourceid = resourceid;
	}

	public MetaData getMetadata() {
		return metadata;
	}

	public void setMetadata(MetaData metadata) {
		this.metadata = metadata;
	}

	public List<LocalizedFile> getFiles() {
		return files;
	}

	public void setFiles(List<LocalizedFile> files) {
		this.files = files;
	}

	public void addFile(LocalizedFile file) {
		this.files.add(file);
	}

	public void addFiles(List<LocalizedFile> files) {
		this.files.addAll(files);
	}
}
