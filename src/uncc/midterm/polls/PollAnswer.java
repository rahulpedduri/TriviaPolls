package uncc.midterm.polls;

import java.io.Serializable;

public class PollAnswer  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String answer;
	private int aid;
	private double percent;
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public int getAid() {
		return aid;
	}
	public void setAid(int aid) {
		this.aid = aid;
	}
	public double getPercent() {
		return percent;
	}
	public void setPercent(double percent) {
		this.percent = percent;
	}
	@Override
	public String toString() {
		return "PollAnswer [answer=" + answer + ", aid=" + aid + ", percent="
				+ percent + "]";
	}
	
	
}
