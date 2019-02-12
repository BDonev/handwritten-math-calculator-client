package bg.tusofia.fdiba.mathcalc;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_FROM_GALLERY = 2;
    private static final String HOST_URL = "http://10.0.2.2:5000";
    private static final String UPLOAD_URL = HOST_URL + "/uploadImage";
    private static final String OUTPUT_URL = HOST_URL + "/getOutput";

    private Button takePhotoButton;
    private Button photoFromGalleryButton;
    private TextView resultTextView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initElements();
        initListeners();
    }

    private void initElements() {
        takePhotoButton = findViewById(R.id.takePhotoButton);
        photoFromGalleryButton = findViewById(R.id.photoFromGalleryButton);
        resultTextView = findViewById(R.id.resultTextView);
        imageView = findViewById(R.id.imageView);
    }

    private void initListeners() {
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
        photoFromGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getPhotoFromGalleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(getPhotoFromGalleryIntent, REQUEST_IMAGE_FROM_GALLERY);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            try {
                File imageFile = saveBitmapToFile(imageBitmap, "currentImage.jpg");
                if (imageFile != null) {
                    uploadImage(imageFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(imageBitmap);
            getOutput();
        } else if (requestCode == REQUEST_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                String imagepath = getPath(imageUri);
                File imageFile = new File(imagepath);
                if (imageFile != null) {
                    uploadImage(imageFile);
                    imageView.setImageBitmap(selectedImage);
                    getOutput();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(projection[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    private File saveBitmapToFile(Bitmap bitmap, String filename) throws IOException {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 0);

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        File file = new File(path, filename);
        try {
            fOut = new FileOutputStream(file);
            // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fOut != null) {
                fOut.flush();
                fOut.close();
            }
        }
        return file;
    }

    private void uploadImage(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        ImageUploadTask imageUploadTask = new ImageUploadTask(filePath, UPLOAD_URL);
        imageUploadTask.execute();
    }

    private void getOutput() {
        GetOutputTask getOutputTask = new GetOutputTask(OUTPUT_URL);
        try {
            String expression = getOutputTask.execute().get();
            ExpressionEvaluator evaluator = new ExpressionEvaluator(expression);
            String result = evaluator.evaluate();
            resultTextView.setText(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            resultTextView.setText("Error");
        }
    }

}