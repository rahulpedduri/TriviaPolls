package uncc.midterm.polls;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParserException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class HomeActivity extends Activity {

	private static String uid;
	private Resources resources;
	private final String LOGTAG = "demo";
	public static final String POLLS = "polls in serializable";
	private static final String GETPOLLS = "get polls";
	private static final String CLEARPOLLS = "clear polls";
	private Intent i;
	private String getPollsAPIUrl;
	private String clearAllPollsAPIUrl;
	private ArrayList<Poll> polls, answered, unAnswered;
	private ProgressDialog loading;
	private Button answerPolls;
	private Button clearPolls;
	private Button reviewPolls;
	private Button exit;
	private Error error;
	private String recentUsed = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		uid = Config.getUid();
		polls = null;
		answered = new ArrayList<Poll>();
		unAnswered = new ArrayList<Poll>();
		resources = getResources();
		error = null;

		loading = new ProgressDialog(this);
		loading.setCancelable(false);
		getPollsAPIUrl = "http://cci-webdev.uncc.edu/~mshehab/polls/getPolls.php";
		clearAllPollsAPIUrl = "http://cci-webdev.uncc.edu/~mshehab/polls/deleteAllPollAnswers.php";

		refresh();

		answerPolls = (Button) findViewById(R.id.button1);
		reviewPolls = (Button) findViewById(R.id.button2);
		clearPolls = (Button) findViewById(R.id.button3);
		exit = (Button) findViewById(R.id.button4);

		answerPolls.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				i = new Intent(getBaseContext(), PollActivity.class);
				i.putExtra(POLLS, unAnswered);
				startActivityForResult(i, 1);

			}
		});
		reviewPolls.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				i = new Intent(getBaseContext(), ReviewActivity.class);
				i.putExtra(POLLS, answered);
				startActivityForResult(i, 1);

			}
		});
		clearPolls.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				clear();
			}
		});
		exit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				HomeActivity.super.finish();

			}
		});
	}

	private void refresh() {
		recentUsed = GETPOLLS;
		new UseApi().execute(getPollsAPIUrl, GETPOLLS);
	}

	private void clear() {
		recentUsed = CLEARPOLLS;
		new UseApi().execute(clearAllPollsAPIUrl, CLEARPOLLS);
		refresh();
	}

	public class UseApi extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			if (recentUsed.equalsIgnoreCase(GETPOLLS)) {
				loading.setMessage(resources.getString(R.string.loading_polls));
			} else {
				loading.setMessage(resources
						.getString(R.string.clear_polls_message));
			}
			loading.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			if (recentUsed.equalsIgnoreCase(GETPOLLS)) {
				loading.setMessage(resources.getString(R.string.loaded_polls));
			} else {
				loading.setMessage(resources
						.getString(R.string.cleared_message));
			}
			reRenderButtons();
			new Handler().postDelayed(new Runnable() {
				public void run() {
					loading.dismiss();
					if (error != null && error.getCode() < 0) {
						String text = "Error-" + error.getCode() + ": "
								+ error.getMessage();
						Toast.makeText(getBaseContext(), text,
								Toast.LENGTH_LONG).show();
					}
				}
			}, 1000);

			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(String... params) {
			HttpClient client = new DefaultHttpClient();
			URI uri = null;
			try {
				uri = new URI(params[0]);
			} catch (URISyntaxException e1) {

				e1.printStackTrace();
			}
			HttpPost request = new HttpPost(uri);
			List<NameValuePair> vals = new ArrayList<NameValuePair>();
			vals.add(new BasicNameValuePair("uid", uid));

			UrlEncodedFormEntity formParams;
			try {
				formParams = new UrlEncodedFormEntity(vals);
				request.setEntity(formParams);
				HttpResponse response = client.execute(request);
				InputStream is = null;
				
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					is = response.getEntity().getContent();
					recentUsed = params[1];
					if (params[1].equalsIgnoreCase(GETPOLLS)) {
						if (polls != null)
							polls.clear();
						polls = ParseUtil.parsePolls(is);
					} else {
						error = ParseUtil.parseError(is);
					}
				} else {			
					String text = "Resopnse Status: "
							+ response.getStatusLine().getStatusCode();
					Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG)
							.show();
				}
				is.close();
			} catch (UnsupportedEncodingException e) {
				Log.d(LOGTAG, "Exception: " + e);
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				Log.d(LOGTAG, "Exception: " + e);
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(LOGTAG, "Exception: " + e);
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				Log.d(LOGTAG, "Exception: " + e);
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}

	public void reRenderButtons() {
		splitPolls(polls);
		if (answered.isEmpty()) {
			clearPolls.setEnabled(false);
			reviewPolls.setEnabled(false);
		}
		else{
			clearPolls.setEnabled(true);
			reviewPolls.setEnabled(true);
		}
		if (unAnswered.isEmpty()) {
			answerPolls.setEnabled(false);
		}else{
			answerPolls.setEnabled(true);
		}
		Log.d(LOGTAG, "unans: " + unAnswered);
		Log.d(LOGTAG, "ans: " + answered);
	}

	public void splitPolls(ArrayList<Poll> polls2) {
		if (polls2 != null) {
			Iterator<Poll> itr = polls.iterator();
			answered.clear();
			unAnswered.clear();

			while (itr.hasNext()) {
				Poll poll = (Poll) itr.next();
				if (poll.isCompleted()) {
					answered.add(poll);
				} else {
					unAnswered.add(poll);
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			refresh();
		}
	}

}
