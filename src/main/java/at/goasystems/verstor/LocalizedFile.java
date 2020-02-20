package at.goasystems.verstor;

public class LocalizedFile {

	private String isocode;
	private String file;

	public LocalizedFile(String isocode, String file) {
		this.isocode = isocode;
		this.file = file;
	}

	public String getIsocode() {
		return isocode;
	}

	public void setIsocode(String isocode) {
		this.isocode = isocode;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
}
