package at.goasystems.verstor;

import com.google.gson.annotations.SerializedName;

public class LocalizedFile {

	@SerializedName("isocode")
	private String isocode;

	@SerializedName("file")
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
