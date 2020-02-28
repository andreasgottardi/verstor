package at.goasystems.verstor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Resource {

	private String resourceid;
	private MetaData metadata;
	private URI origin;
	private List<LocalizedFile> localizedfiles;

	public Resource() {
		this.resourceid = "";
		this.metadata = new MetaData();
		this.origin = null;
		localizedfiles = new ArrayList<>();
	}

	public Resource(String resourceid) {
		this.resourceid = resourceid;
		this.metadata = new MetaData();
		localizedfiles = new ArrayList<>();
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

	public URI getOrigin() {
		return origin;
	}

	public void setOrigin(URI origin) {
		this.origin = origin;
	}

	public List<LocalizedFile> getLocalizedfiles() {
		return localizedfiles;
	}

	public void setLocalizedfiles(List<LocalizedFile> localizedfiles) {
		this.localizedfiles = localizedfiles;
	}

	public void addFile(LocalizedFile file) {
		this.localizedfiles.add(file);
	}

	public void addFiles(List<LocalizedFile> files) {
		this.localizedfiles.addAll(files);
	}
}
