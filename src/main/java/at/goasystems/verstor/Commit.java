package at.goasystems.verstor;

import java.util.Date;

public class Commit {

	private String hash;
	private String message;
	private Date date;

	public Commit(String hash, String message, Date date) {

		this.hash = hash;
		this.message = message;
		this.date = date;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
