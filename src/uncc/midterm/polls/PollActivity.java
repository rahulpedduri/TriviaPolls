package uncc.midterm.polls;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class PollActivity extends Activity {
	
	private ArrayList<Poll> polls;
	private Resources resources;
	private final String LOGTAG = "demo";
	Iterator<Poll> pollIterator;
	boolean working;
	TextView question;
	RadioGroup answers;
	Button next;
	int currIndex;
	HashMap<Integer, Integer> saves;
	int pid;
	boolean radioSelected;
	private static String uid;
	private static final String SUBMIT_API = "http://cci-webdev.uncc.edu/~mshehab/polls/submitPollAnswer.php";
	ProgressDialog progressBar;
	int progress;
	boolean pollCompleted;
	boolean nextDisable;
	@SuppressLint("UseSparseArrays")
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poll);
		nextDisable=false;
		working = true;
		resources = getResources();
		polls = null;
		pollCompleted=false;
		saves = new HashMap<Integer, Integer>();
		if (getIntent().getExtras() != null) {
			if (getIntent().getExtras().containsKey(HomeActivity.POLLS)) {
				polls = (ArrayList<Poll>) getIntent().getSerializableExtra(
						HomeActivity.POLLS);
				Log.d(LOGTAG, "POLLS: " + polls + "POLLS SIZE: " + polls.size());
			}
		}
		question = (TextView) findViewById(R.id.question);
		answers = (RadioGroup) findViewById(R.id.answers);
		pollIterator = polls.iterator();

		next();

		((Button) findViewById(R.id.button1))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						setResult(RESULT_OK, null);
						PollActivity.super.finish();

					}
				});

		next = ((Button) findViewById(R.id.button2));
		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			
				if(!nextDisable){
				if (!radioSelected) {
					String text = resources
							.getString(R.string.error_poll_unanswered);
					Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT)
							.show();
				}else
				next();
		}else{
			submitAnswers();
			
			next.setEnabled(false);
		}

			}
		});
		uid = Config.getUid();
		progressBar = new ProgressDialog(this);
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setCancelable(false);
		progressBar.setMessage("Submitting Answers");
		

	}

	private void next() {
		if (pollIterator.hasNext()) {
			Poll poll = pollIterator.next();
			radioSelected = false;
			question.setText(poll.getQuestion());
			pid = poll.getPid();
			Iterator<PollAnswer> ans = poll.getAnswers().iterator();
			answers.removeAllViews();
			RadioButton answer;
			while (ans.hasNext()) {
				answer = new RadioButton(getBaseContext());
				PollAnswer inst = ans.next();
				String text = inst.getAnswer();
				answer.setText(text);
				answer.setTag(R.id.aid, inst.getAid());
				answer.setTextColor(Color.BLACK);
				answer.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						saves.put(pid, (Integer) v.getTag(R.id.aid));
						radioSelected = true;
						if (!pollIterator.hasNext()) {
							
						}
						Log.d(LOGTAG, "HashMap: " + saves);

					}
				});

				answers.addView(answer);
			}
			if (!pollIterator.hasNext()) {
				nextDisable=true;
			}
		}
	}

	protected void submitAnswers() {
		
		progress=currIndex=0;
		progressBar.setProgress(progress);
		progressBar.setMax(saves.size());
		progressBar.show();
		Iterator<Integer> itr;
		if (saves != null) {
			itr = saves.keySet().iterator();
			while(itr.hasNext()){
				Integer p = (Integer) itr.next();
				Integer a = saves.get(p);
			new SubmitAnswers().execute(p,a);
			
			}		
			new Handler().postDelayed(new Runnable() {
				public void run() {
					progressBar.dismiss();
					Log.d(LOGTAG,"Finished PollActivity");
					setResult(RESULT_OK, null);
					PollActivity.super.finish();
				}
			}, 1000);
		}

	}

	public void setProgressBarInc() {
		progressBar.setProgress(++progress);
	}

	public class SubmitAnswers extends AsyncTask<Integer, Integer, Void> {

		
		@Override
		protected void onPostExecute(Void result) {
			setProgressBarInc();			
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Integer... params) {

			int pid = params[0];
			int aid = params[1];
			HttpClient client = new DefaultHttpClient();
			URI uri = null;
			try {
				uri = new URI(SUBMIT_API);
			} catch (URISyntaxException e1) {

				e1.printStackTrace();
			}
			HttpPost request = new HttpPost(uri);
			List<NameValuePair> vals = new ArrayList<NameValuePair>();
			vals.add(new BasicNameValuePair("uid", uid));
			vals.add(new BasicNameValuePair("pid", pid + ""));
			vals.add(new BasicNameValuePair("aid", aid + ""));

			UrlEncodedFormEntity formParams;
			try {
				formParams = new UrlEncodedFormEntity(vals);
				request.setEntity(formParams);
				HttpResponse response = client.execute(request);
				InputStream is = null;
				Log.d(LOGTAG, "Submitted: " + pid+" - "+ aid);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					
					is = response.getEntity().getContent();
					Error error = ParseUtil.parseError(is);
					Log.d(LOGTAG, "Object Error: " + error+"");
					if (error.getCode() < 0) {
						String text = "Error-" + error.getCode() + ": "
								+ error.getMessage();
						Toast.makeText(getBaseContext(), text,
								Toast.LENGTH_LONG).show();
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
		getMenuInflater().inflate(R.menu.activity_poll, menu);
		return true;
	}

}
