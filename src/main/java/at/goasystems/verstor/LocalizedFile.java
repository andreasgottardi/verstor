package at.goasystems.verstor;

import java.io.File;

public class LocalizedFile {

	private String isocode;
	private File file;

	public LocalizedFile(String isocode, File file) {
		this.isocode = isocode;
		this.file = file;
	}

	public String getIsocode() {
		return isocode;
	}

	public void setIsocode(String isocode) {
		this.isocode = isocode;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
