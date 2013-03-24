package uncc.midterm.polls;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;
import android.util.SparseArray;


public class Poll implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 35207301951848005L;
	
	private String question;
	private int pid;
	private boolean completed;
	private ArrayList<PollAnswer> answers;
	private int yourAnswer;
	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public ArrayList<PollAnswer> getAnswers() {
		return answers;
	}

	public void setAnswers(ArrayList<PollAnswer> answers) {
		this.answers = answers;
	}

	public int getYourAnswer() {
		return yourAnswer;
	}

	public void setYourAnswer(int yourAnswer) {
		this.yourAnswer = yourAnswer;
	}

	@Override
	public String toString() {
		return "Poll [question=" + question + ", pid=" + pid + ", completed="
				+ completed + ", answers=" + answers + ", yourAnswer="
				+ yourAnswer + "]";
	}

	
}
