package aaditya.myruns5;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileActivity extends Activity
{

	public static final int REQUEST_CODE_CROP_PHOTO = 2;
	public static final int REQUEST_CODE_SELECT_FROM_GALLERY = 1;
	public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;

	private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
	private static final String IMAGE_UNSPECIFIED = "image/*";

	private boolean isTakenFromCamera;
	private Uri mImageCaptureUri;
	private ImageView mImageView;

	@Override
	public void onCreate(Bundle paramBundle)
	{
		super.onCreate(paramBundle);
		setContentView(R.layout.profile);
		this.mImageView = ((ImageView)findViewById(R.id.image));
		if (paramBundle != null)
			this.mImageCaptureUri = ((Uri)paramBundle.getParcelable(URI_INSTANCE_STATE_KEY));

		//initialize Spinner for list of Class Years: 2013,2014,2015,2016 - defined under class-array in res/values/strings.xml
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.class_array, android.R.layout.simple_spinner_item);
		//Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//Apply the adapter to the spinner
		spinner.setAdapter(adapter);


		//initialize AutoComplete for list of Majors - defined under major-array in res/values/strings.xml
		ArrayAdapter<String> major_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.major_array));
		AutoCompleteTextView major_textView = (AutoCompleteTextView)
				findViewById(R.id.autoCompleteTextView1);
		major_textView.setAdapter(major_adapter);

		//existing profile is loaded from SharedPreferences on creation
		loadProfile();
	}

	//when instance state is saved, stores mImageCaptureUri in URI_INSTANCE_STATE KEY

	@Override
	protected void onSaveInstanceState(Bundle paramBundle)
	{
		super.onSaveInstanceState(paramBundle);
		paramBundle.putParcelable(URI_INSTANCE_STATE_KEY, this.mImageCaptureUri);
	}

	// ****************** button click callbacks ***************************//
	//called on button click 'Cancel', shows cancel toast and closes profile activity
	public void onCancelClicked(View paramView)
	{
		Toast.makeText(getApplicationContext(), getString(R.string.ui_profile_toast_cancel_text), 0).show();
		finish();
	}

	// changing the profile image, show the dialog asking the user
	// to choose between taking a picture and picking from gallery
	public void onChangePhotoClicked(View paramView)
	{
		displayDialog(1);
	}

	//called on button click 'Save', shows save toast and closes profile activity
	public void onSaveClicked(View paramView)
	{
		saveProfile();
		Toast.makeText(getApplicationContext(), getString(R.string.ui_profile_toast_save_text), 0).show();
		finish();
	}


	// Handle data after activity returns. based on request code
	protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
	{
		//System.out.println ("Args: " + paramInt1 + " " + paramInt2);
		if (paramInt2 == -1) {
			switch (paramInt1)
			{
			default:
				return;
			case 0:
				cropImage();
				return;
			case 1:
				this.mImageCaptureUri = paramIntent.getData();
				cropImage();
				return;
			case 2:
				Bundle localBundle = paramIntent.getExtras();
				//System.out.println("got local bundle");
				if (localBundle != null) {
					this.mImageView.setImageBitmap((Bitmap)localBundle.getParcelable("data"));
				}
				return;
			}
		}

		return;
	}

	// ******* Photo picker dialog related functions ************//

	//display photo picker dialog
	public void displayDialog(int paramInt)
	{
		MyRunsDialogFragment.newInstance(paramInt).show(getFragmentManager(), getString(R.string.dialog_fragment_tag_photo_picker));
	}

	//Handle photo picking based on request code, 
	public void onPhotoPickerItemSelected(int paramInt)
	{
		switch (paramInt)
		{
		default:
			return;
		case 0:
			Intent localIntent2 = new Intent("android.media.action.IMAGE_CAPTURE");
			this.mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
			localIntent2.putExtra("output", this.mImageCaptureUri);
			localIntent2.putExtra("return-data", true);
			try
			{
				startActivityForResult(localIntent2, REQUEST_CODE_TAKE_FROM_CAMERA);
				this.isTakenFromCamera = true;
				return;
			}
			catch (ActivityNotFoundException localActivityNotFoundException)
			{
				while (true)
					localActivityNotFoundException.printStackTrace();
			}
		case 1:
		}
		Intent localIntent1 = new Intent();
		localIntent1.setType(IMAGE_UNSPECIFIED);
		localIntent1.setAction("android.intent.action.GET_CONTENT");
		startActivityForResult(localIntent1, REQUEST_CODE_SELECT_FROM_GALLERY);
	}

	// ****************** private helper functions ***************************//

	// Crop and resize the image for profile
	private void cropImage()
	{
		Intent localIntent = new Intent("com.android.camera.action.CROP");
		localIntent.setDataAndType(this.mImageCaptureUri, IMAGE_UNSPECIFIED);
		localIntent.putExtra("outputX", 100);
		localIntent.putExtra("outputY", 100);
		localIntent.putExtra("aspectX", 1);
		localIntent.putExtra("aspectY", 1);
		localIntent.putExtra("scale", true);
		localIntent.putExtra("return-data", true);
		startActivityForResult(localIntent, REQUEST_CODE_CROP_PHOTO);
	}

	//Load existing profile
	private void loadProfile()
	{
		SharedPreferences localSharedPreferences = getSharedPreferences(getString(R.string.preference_name), 0);

		String str1 = localSharedPreferences.getString(getString(R.string.preference_key_profile_name), "");
		((EditText)findViewById(R.id.editText1)).setText(str1);

		String str2 = localSharedPreferences.getString(getString(R.string.preference_key_profile_email), "");
		((EditText)findViewById(R.id.editText2)).setText(str2);

		String str3 = localSharedPreferences.getString(getString(R.string.preference_key_profile_phone), "");
		((EditText)findViewById(R.id.editText3)).setText(str3);

		//Use index of RadioButton to indicate choice
		int i = localSharedPreferences.getInt(getString(R.string.preference_key_profile_gender), -1);
		if (i >= 0)
			((RadioButton)((RadioGroup)findViewById(R.id.radioGender)).getChildAt(i)).setChecked(true);

		//load position of spinner
		int spinner_value = localSharedPreferences.getInt(getString(R.string.preference_key_profile_class), -1);
		((Spinner)findViewById(R.id.spinner1)).setSelection(spinner_value);

		String str5 = localSharedPreferences.getString(getString(R.string.preference_key_profile_major), "");
		((TextView)findViewById(R.id.autoCompleteTextView1)).setText(str5);
		try
		{
			FileInputStream localFileInputStream = openFileInput(getString(R.string.profile_photo_file_name));
			Bitmap localBitmap = BitmapFactory.decodeStream(localFileInputStream);
			this.mImageView.setImageBitmap(localBitmap);
			localFileInputStream.close();
			return;
		}
		catch (IOException localIOException)
		{
			this.mImageView.setImageResource(R.drawable.default_profile);
		}
	}

	//saves current profile
	private void saveProfile()
	{
		SharedPreferences.Editor localEditor = getSharedPreferences(getString(R.string.preference_name), 0).edit();
		localEditor.putString(getString(R.string.preference_key_profile_name), ((EditText)findViewById(R.id.editText1)).getText().toString());
		localEditor.putString(getString(R.string.preference_key_profile_email), ((EditText)findViewById(R.id.editText2)).getText().toString());
		localEditor.putString(getString(R.string.preference_key_profile_phone), ((EditText)findViewById(R.id.editText3)).getText().toString());
		String str = getString(R.string.preference_key_profile_gender);
		RadioGroup localRadioGroup = (RadioGroup)findViewById(R.id.radioGender);

		//get index of checked RadioButton
		localEditor.putInt(str, localRadioGroup.indexOfChild(findViewById(localRadioGroup.getCheckedRadioButtonId())));

		//get position of selected Spinner item
		localEditor.putInt(getString(R.string.preference_key_profile_class), ((Spinner)findViewById(R.id.spinner1)).getSelectedItemPosition());
		localEditor.putString(getString(R.string.preference_key_profile_major), ((EditText)findViewById(R.id.autoCompleteTextView1)).getText().toString());
		localEditor.apply();
		this.mImageView.buildDrawingCache();
		Bitmap localBitmap = this.mImageView.getDrawingCache();
		try
		{
			FileOutputStream localFileOutputStream = openFileOutput(getString(R.string.profile_photo_file_name), 0);
			localBitmap.compress(Bitmap.CompressFormat.PNG, 100, localFileOutputStream);
			localFileOutputStream.flush();
			localFileOutputStream.close();
			return;
		}
		catch (IOException localIOException)
		{
			localIOException.printStackTrace();
		}
	}


}

