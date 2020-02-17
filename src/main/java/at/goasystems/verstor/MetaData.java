package at.goasystems.verstor;

public class MetaData {

	private String resourcemimetype;
	private String originmimetype;
	private String originextension;

	public MetaData() {
	}

	public MetaData(String resourcemimetype, String originmimetype, String originextension) {
		this.resourcemimetype = resourcemimetype;
		this.originmimetype = originmimetype;
		this.originextension = originextension;
	}

	public String getResourcemimetype() {
		return resourcemimetype;
	}

	public void setResourcemimetype(String resourcemimetype) {
		this.resourcemimetype = resourcemimetype;
	}

	public String getOriginmimetype() {
		return originmimetype;
	}

	public void setOriginmimetype(String originmimetype) {
		this.originmimetype = originmimetype;
	}

	public String getOriginextension() {
		return originextension;
	}

	public void setOriginextension(String originextension) {
		this.originextension = originextension;
	}
}
