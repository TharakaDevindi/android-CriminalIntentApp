package uk.ac.wlv.criminalintent;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class PhotoActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_FILE_PATH = "uk.ac.wlv.criminalintent.photo_file_path";

    private ImageView mPhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mPhotoView = findViewById(R.id.full_size_photo);

        String photoFilePath = getIntent().getStringExtra(EXTRA_PHOTO_FILE_PATH);
        if (photoFilePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath);
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
