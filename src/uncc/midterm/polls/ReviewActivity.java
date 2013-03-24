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
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.TableRow;
import android.widget.TextView;

public class ReviewActivity extends Activity {

	public static final String CLEAR_API = "http://cci-webdev.uncc.edu/~mshehab/polls/deletePollAnswer.php";
	private ArrayList<Poll> polls;
	private Resources resources;
	private final String LOGTAG = "demo";
	Iterator<Poll> pollIterator;
	TextView question;
	TableLayout answers;
	Button next;
	Button clear;
	int currPid;
	private ProgressDialog loading;
	private static String uid;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review);
		
		resources=getResources();
		question = (TextView)findViewById(R.id.question);
		answers = (TableLayout)findViewById(R.id.answer_table);
	((TextView)findViewById(R.id.bottom)).setTextColor(Color.BLUE);
		
		if (getIntent().getExtras() != null) {
			if (getIntent().getExtras().containsKey(HomeActivity.POLLS)) {
				polls = (ArrayList<Poll>) getIntent().getSerializableExtra(
						HomeActivity.POLLS);
				Log.d(LOGTAG, "POLLS: " + polls + "POLLS SIZE: " + polls.size());
			}
		}
		pollIterator = polls.iterator();
		renderNext();
		
		((Button) findViewById(R.id.home))
		.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_OK, null);
				ReviewActivity.super.finish();

			}
		});
		clear=((Button) findViewById(R.id.clear));
		clear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new ClearPoll().execute(currPid);

			}
		});
		next = ((Button) findViewById(R.id.next));
		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(!pollIterator.hasNext()){
					next.setEnabled(false);
				}
				renderNext();

			}
		});
		
		uid = Config.getUid();
		loading = new ProgressDialog(this);
		loading.setCancelable(false);
	}

	private void renderNext() {
		if (pollIterator.hasNext()) {
			Poll poll = pollIterator.next();
			
			question.setText(poll.getQuestion());
			currPid = poll.getPid();
			Iterator<PollAnswer> ans = poll.getAnswers().iterator();
			answers.removeAllViews();
			TableRow answer;
			while (ans.hasNext()) {
				answer = new TableRow(getBaseContext());
				
				PollAnswer inst = ans.next();
				String text = inst.getAnswer();
				double percent = inst.getPercent();
				TextView a = new TextView(getBaseContext());
				a.setText(text+"");
				TextView p = new TextView(getBaseContext());
				p.setText(percent+"");
				if(poll.getYourAnswer() == inst.getAid()){
					a.setTextColor(Color.BLUE);
					p.setTextColor(Color.BLUE);
				}else{
					a.setTextColor(Color.BLACK);					
					p.setTextColor(Color.BLACK);
				}
				a.setPadding(4, 4, 20, 4);
				p.setPadding(20, 4, 4, 4);
				answer.addView(a);
				answer.addView(p);
				
				Log.d(LOGTAG,"ADDED ON SCREEN: "+poll);
				answers.addView(answer);
			}
			
		}	else{
			setResult(RESULT_OK, null);
			ReviewActivity.super.finish();
		}
	}
	
	public class ClearPoll extends AsyncTask<Integer, Void, Void>{

		@Override
		protected void onPreExecute() {
			String message="Clearing Poll Answer . . ";
			loading.setMessage(message);
			loading.show();
			super.onPreExecute();
		}
		@Override
		protected void onPostExecute(Void result) {
			loading.setMessage("Poll Cleared. ");
			new Handler().postDelayed(new Runnable() {
				public void run() {
					loading.dismiss();
				}
			}, 1000);
			renderNext();
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Integer... params) {
			int pid = params[0];
			HttpClient client = new DefaultHttpClient();
			URI uri = null;
			try {
				uri = new URI(CLEAR_API);
			} catch (URISyntaxException e1) {

				e1.printStackTrace();
			}
			HttpPost request = new HttpPost(uri);
			List<NameValuePair> vals = new ArrayList<NameValuePair>();
			vals.add(new BasicNameValuePair("uid", uid));
			vals.add(new BasicNameValuePair("pid", pid + ""));
			

			UrlEncodedFormEntity formParams;
			try {
				formParams = new UrlEncodedFormEntity(vals);
				request.setEntity(formParams);
				HttpResponse response = client.execute(request);
				InputStream is = null;
				Log.d(LOGTAG, "Cleared: " + pid+" - ");
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
					// TODO toast..
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
		getMenuInflater().inflate(R.menu.activity_review, menu);
		return true;
	}

}
