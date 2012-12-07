package zorio.s3grabber;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

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
		
		return v;
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
