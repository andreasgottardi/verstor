package at.goasystems.verstor;

import java.net.URI;

public class LocalizedFile {

	private String isocode;
	private URI uri;

	public LocalizedFile(String isocode, URI uri) {
		this.isocode = isocode;
		this.uri = uri;
	}

	public String getIsocode() {
		return isocode;
	}

	public void setIsocode(String isocode) {
		this.isocode = isocode;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}
}
