package zorio.s3grabber;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.content.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class UploadFragment extends Fragment {
	
	private Uploader uploader;
	
	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setHasOptionsMenu(true);
		uploader = new Uploader(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = LayoutInflater.from(getActivity()).inflate(
				R.layout.fragment_upload, null);

		View startButton = v.findViewById(R.id.imageButtonSelect);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				beginSelectPicture();
			}
		});
		
		EditText t = (EditText) v.findViewById(R.id.finalUrl);
		t.setKeyListener(null);
		
		v.findViewById(R.id.viewUploadResult).setVisibility(View.INVISIBLE);
		
		v.findViewById(R.id.imageButtonCopyUrl).setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				copyUri();
			}
		});
		
		v.findViewById(R.id.imageButtonOpenUrl).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				openUri();		
			}
		});
		
		return v;
	}
	
	private void copyUri() {
		View v = getView();
		Context ctx = v.getContext();	
		EditText t = (EditText) v.findViewById(R.id.finalUrl);
		String currentText = t.getText().toString();
		
		if(currentText != null && currentText.length() > 0) {
			ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newUri(ctx.getContentResolver(), "URI", Uri.parse(currentText));
			clipboard.setPrimaryClip(clip);									
			Toast.makeText(v.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void openUri() {
		View v = getView();
		EditText t = (EditText) v.findViewById(R.id.finalUrl);
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(t.getText().toString()));
		startActivity(browserIntent);		
	}

	private void beginSelectPicture() {			
		ArrayList<Intent> intents = new ArrayList<Intent>();

		PackageManager pm = getActivity().getPackageManager();
		
		Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		List<ResolveInfo> l = pm.queryIntentActivities(camIntent, 0);
		for (ResolveInfo ri : l) {
			final Intent i = new Intent(camIntent);
			i.setComponent(new ComponentName(ri.activityInfo.packageName,
					ri.activityInfo.name));
			i.putExtra(MediaStore.EXTRA_OUTPUT, CameraContentProvider.CONTENT_URI);
			intents.add(i);
		}
		
		Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
		gallIntent.setType("image/*");

		final Intent chooserIntent = Intent.createChooser(gallIntent,
				"Select Source");
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
				intents.toArray(new Parcelable[] {}));

		startActivityForResult(
				Intent.createChooser(chooserIntent, "Select Picture"), 1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				Uri u = null;
				if(data == null || data.getData() == null) {
					// Camera intent
					u = CameraContentProvider.CONTENT_URI;
				} else {
					// Uri intent
					u = data.getData();
				}
				if(u != null) {
					uploader.beginUpload(u);
				}
			}
		}
	}

}
