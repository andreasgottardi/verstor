package at.goasystems.verstor;

public class MetaData {

	private String resourcemimetype;
	private String resourceextension;
	private String originmimetype;
	private String originextension;

	public MetaData() {
		this.resourcemimetype = "";
		this.resourceextension = "";
		this.originmimetype = "";
		this.originextension = "";
	}

	public MetaData(String resourcemimetype, String resourceextension, String originmimetype, String originextension) {
		this.resourcemimetype = resourcemimetype;
		this.resourceextension = resourceextension;
		this.originmimetype = originmimetype;
		this.originextension = originextension;
	}

	public String getResourcemimetype() {
		return resourcemimetype;
	}

	public void setResourcemimetype(String resourcemimetype) {
		this.resourcemimetype = resourcemimetype;
	}

	public String getResourceextension() {
		return resourceextension;
	}

	public void setResourceextension(String resourceextension) {
		this.resourceextension = resourceextension;
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
