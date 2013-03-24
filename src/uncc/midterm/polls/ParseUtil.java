package uncc.midterm.polls;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class ParseUtil {
	private final static String LOGTAG = "demo";

	public static ArrayList<Poll> parsePolls(InputStream in)
			throws XmlPullParserException, IOException {
		XmlPullParser parser = XmlPullParserFactory.newInstance()
				.newPullParser();
		parser.setInput(in, "UTF-8");
		ArrayList<Poll> polls = new ArrayList<Poll>();

		Poll poll = null;
		String question = null;
		int pid = -1;
		boolean completed = false;
		ArrayList<PollAnswer> answers = null;
		int yourAnswer = -1;

		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				if (parser.getName().equals("poll")) {
					poll = new Poll();
					answers = new ArrayList<PollAnswer>();
					pid = Integer.parseInt(parser
							.getAttributeValue(null, "pid").trim());
					int i = Integer.parseInt(parser.getAttributeValue(null,
							"completed").trim());
					if (i > 0) {
						yourAnswer = Integer.parseInt(parser.getAttributeValue(
								null, "youranswer").trim());
						completed = true;
					} else
						completed = false;

				} else if (parser.getName().equals("question")) {
					question = parser.nextText().trim();

				} else if (parser.getName().equals("answer")) {
					PollAnswer answer = new PollAnswer();
					int aid = Integer.parseInt(parser.getAttributeValue(null,
							"aid").trim());
					if (completed) {
						double percent = Double.parseDouble(parser
								.getAttributeValue(null, "percent").trim());
						answer.setPercent(percent);
					}
					String text = parser.nextText().trim();
					answer.setAid(aid);
					answer.setAnswer(text);
					

					answers.add(answer);

				}
				break;

			case XmlPullParser.END_TAG:
				if (parser.getName().equals("poll")) {
					poll.setAnswers(answers);
					poll.setQuestion(question);
					poll.setCompleted(completed);
					poll.setPid(pid);
					poll.setYourAnswer(yourAnswer);

					Log.d(LOGTAG, "Poll: " + poll.toString());

					polls.add(poll);

				}
			default:
				break;
			}
			event = parser.next();

		}
		return polls;
	}

	public static Error parseError(InputStream in)
			throws XmlPullParserException, IOException {
		XmlPullParser parser = XmlPullParserFactory.newInstance()
				.newPullParser();
		parser.setInput(in, "UTF-8");
		Error error = new Error();
		int code = 0;
		String message = "";
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				if (parser.getName().equals("error")) {

					code = Integer.parseInt(parser.getAttributeValue(null,
							"code").trim());
					if(!parser.isEmptyElementTag())
					message = parser.nextText().trim();

				}
				
				break;

			case XmlPullParser.END_TAG:
				if (parser.getName().equals("error")) {
					error.setCode(code);
					error.setMessage(message);

				}
			default:
				break;
			}
			event = parser.next();

		}

		return error;

	}
}
