package zorio.s3grabber;

import java.io.IOException;
import java.io.InputStream;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

public class Uploader {

	private Context ctx;
	private SharedPreferences prefs;

	public Uploader(Context ctx) {
		this.ctx = ctx;
		
		prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);

	}
	
	private AmazonS3Client createClient() {
		AmazonS3Client client = new AmazonS3Client(new BasicAWSCredentials(
				prefs.getString("settings_accesskey", ""), 
				prefs.getString("settings_secretkey", "")));
		client.setEndpoint(prefs.getString("settings_region", "s3.amazonaws.com"));
		return client;
	}
	
	private String getBucketName() {
		return prefs.getString("settings_bucketname", null);
	}
	
	private String getObjectPrefix() {
		return prefs.getString("settings_objectprefix", null);
	}

	public boolean checkSettings() {
		String accessKey = prefs.getString("settings_accesskey", "");
		String secretKey = prefs.getString("settings_secretkey", "");
		
		if(accessKey.length() == 0 || secretKey.length() == 0)
			return false; 		
		
		if(getBucketName().length() == 0) 
			return false;
		
		return true;
	}
	
	public void beginUpload(Uri u)
	{
		try {
			if(!checkSettings()) {
				Toast t = Toast.makeText(ctx, "Settings incomplete", Toast.LENGTH_LONG);
				t.show();
				return;
			}
			new UploadImageTask().execute(u);
			
			ImageButton btn = (ImageButton) ((FragmentActivity)ctx).findViewById(R.id.imageButtonSelect);
			btn.setEnabled(false);
			
			AlphaAnimation  anim = new AlphaAnimation(1, 0);
			anim.setDuration(300);
			anim.setInterpolator(new LinearInterpolator()); 
			anim.setRepeatCount(AlphaAnimation.INFINITE); 
			anim.setRepeatMode(Animation.REVERSE);
			btn.startAnimation(anim);		
			((TextView)((MainActivity)ctx).findViewById(R.id.textView1)).setText(R.string.uploading);	
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void onUploadComplete(String fileName) {
		String uri = makeDestinationUri(fileName);
		
		TextView t = ((TextView)((MainActivity)ctx).findViewById(R.id.textView1));
		t.setText(uri);
		t.setAutoLinkMask(Linkify.ALL);
		t.setMovementMethod(LinkMovementMethod.getInstance());
		
		((MainActivity)ctx).updateShareIntent(uri);
	}
	
	private String makeDestinationUri(String fileName) {
		StringBuilder sb = new StringBuilder("http://");
		String cfDomain = prefs.getString("settings_cloudfront", "");
		if(cfDomain.length() > 0) {
			sb.append(cfDomain);
		} else {
			sb.append(prefs.getString("settings_region", ""));
			sb.append("/");
			sb.append(getBucketName());
		}
		sb.append("/");
		sb.append(fileName);
		return sb.toString();
	}
	
	private class UploadImageTask extends AsyncTask<Uri, Void, PutObjectResult> {

		private Uri u;
		private String fileName;
		
		@Override
		protected PutObjectResult doInBackground(Uri... params) {
			u = params[0];
			InputStream is = null;
			ParcelFileDescriptor fd = null;
			try {
				AmazonS3Client client = createClient();
				fileName = getObjectPrefix() + (System.currentTimeMillis() / 1000) + ".jpg";
								
				ContentResolver resolver = ctx.getContentResolver();
				fd = resolver.openFileDescriptor(u, "r");
				
				ObjectMetadata data = new ObjectMetadata();
				data.setContentType("image/jpg");
				data.setContentLength(fd.getStatSize());
								
				is = resolver.openInputStream(u);

				PutObjectRequest req = new PutObjectRequest(getBucketName(), fileName, is, data);
				req.setCannedAcl(CannedAccessControlList.PublicRead);
				
				PutObjectResult res = client.putObject(req);			
				client.shutdown();
				
				return res;
			} catch(Throwable t) {
				t.printStackTrace();
				return null;
			} finally {
				try {
					fd.close();
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		@Override
		protected void onPostExecute(PutObjectResult res) {

			ImageButton btn = (ImageButton) ((FragmentActivity)ctx).findViewById(R.id.imageButtonSelect);
			btn.setEnabled(true);
			if(btn.getAnimation() != null) {
				btn.getAnimation().setRepeatCount(0);
			}
			((TextView)((MainActivity)ctx).findViewById(R.id.textView1)).setText(R.string.select_a_file);
			if(res == null || res.getETag() == null) {
				Toast t = Toast.makeText(ctx, "Failed to upload :(", Toast.LENGTH_SHORT);
				t.show();
			} else {
				onUploadComplete(fileName);
			}
		}

	}

}
