package uk.ac.wlv.criminalintent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.text.format.DateFormat;
import java.util.Date;
import java.util.UUID;

import android.widget.CompoundButton;
import android.widget.Toast;
import android.app.Activity;;
import android.content.Intent;
import androidx.fragment.app.FragmentManager;
import java.util.Date;
import android.provider.ContactsContract;
import android.net.Uri;
import android.database.Cursor;
import android.widget.ImageButton;
import android.widget.ImageView;
import java.io.File;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.content.pm.ResolveInfo;
import java.util.List;
import android.graphics.Bitmap;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id" ;
    private static final String DIALOG_DATE = "Dialogdate";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 6;
    private static final String CRIME_FRAGMENT = "CrimeFragment";
    private Crime mCrime;
    private EditText mTitleField;
    Button mDateButton;
    CheckBox mSolvedCheckBox;
    private Button mDeleteCrime;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;
    private Callbacks mCallbacks;

    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private File mPhotoFile;
    private Button mViewPhotoButton;

    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public void onPause(){
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mDeleteCrime = (Button) v.findViewById(R.id.delete_button);
        mDeleteCrime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(CRIME_FRAGMENT, mCrime.getId().toString());
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
            }
        });

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String title = s.toString();
                mCrime.setTitle(title);
            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setText(mCrime.getDate().toString());
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        mViewPhotoButton = v.findViewById(R.id.view_photo_button);
        mViewPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoFile != null && mPhotoFile.exists()) {
                    Intent intent = new Intent(getActivity(), PhotoActivity.class);
                    intent.putExtra(PhotoActivity.EXTRA_PHOTO_FILE_PATH, mPhotoFile.getPath());
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "No photo available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        PackageManager packageManager = getActivity().getPackageManager();
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "uk.ac.wlv.criminalintent.fileprovider", mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
                // Start PhotoActivity to display the full-size photo
                Intent intent = new Intent(getActivity(), PhotoActivity.class);
                intent.putExtra(PhotoActivity.EXTRA_PHOTO_FILE_PATH, mPhotoFile.getPath());
                startActivity(intent);
            }
        });


        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        updatePhotoView();


        mCallSuspectButton = (Button) v.findViewById(R.id.crime_call_suspect);
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mCrime.getSuspect() == null) {
                    Toast.makeText(getActivity(), getString(R.string.toast_no_suspect), Toast.LENGTH_SHORT).show();
                    return;
                }
                checkReadContactsPermission();

            }
        });


        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        if (requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mDateButton.setText(mCrime.getDate().toString());
        }

        else if (requestCode == REQUEST_CONTACT && data !=null)
        {
            Uri contactUri = data.getData();
            String[] queryFields = new String[]
                    {
                            ContactsContract.Contacts.DISPLAY_NAME
                    };
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                if (c.getCount() == 0)
                {
                    return;
                }

                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            }finally {
                c.close();
            }
        }
        else if (requestCode == REQUEST_PHOTO)
        {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "uk.ac.wlv.criminalintent.fileprovider", mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }
    }

    private String getCrimeReport(){
        String solvedString = null;
        if (mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else
        {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null)
        {
            suspect = getString(R.string.crime_report_no_suspect);
        }else
        {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report,mCrime.getTitle(),
                dateString,solvedString,suspect);
        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    public void checkReadContactsPermission() {// Here, thisActivity is the current activity
        Log.i(CRIME_FRAGMENT, "checkReadContactsPermission: start");
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            Log.i(CRIME_FRAGMENT, "checkReadContactsPermission: called");
            callSuspect();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                Toast.makeText(getActivity(), "Dial with phone successful", Toast.LENGTH_SHORT).show();
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    callSuspect();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void callSuspect() {
        Cursor c = getActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + "=?",
                new String[]{mCrime.getSuspect()},
                null
        );

        try {
            if (c == null || c.getCount() == 0) {
                Log.i(CRIME_FRAGMENT, "callSuspect: Cursor is null or empty");
                return;
            }

            if (c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (columnIndex != -1) {
                    String phoneNumber = c.getString(columnIndex);
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                } else {
                    Log.i(CRIME_FRAGMENT, "callSuspect: Phone number column not found in the cursor");
                }
            }

        } finally {
            c.close();
        }
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

}