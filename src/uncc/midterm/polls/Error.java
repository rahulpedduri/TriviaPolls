package uncc.midterm.polls;

public class Error {
	private String message;
	private int code;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	@Override
	public String toString() {
		return "Error [message=" + message + ", code=" + code + "]";
	}
	
	
}
