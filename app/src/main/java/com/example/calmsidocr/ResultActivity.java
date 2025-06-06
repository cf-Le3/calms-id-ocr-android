package com.example.calmsidocr;

import static com.example.calmsidocr.Constants.KEY_FILE;
import static com.example.calmsidocr.Constants.MSG_NA;
import static com.example.calmsidocr.Constants.PAT_LICENSE;
import static com.example.calmsidocr.Constants.PAT_NRIC;
import static com.example.calmsidocr.Constants.PAT_PASSPORT;
import static com.example.calmsidocr.Constants.TAG_BUNDLE;
import static com.example.calmsidocr.Constants.TAG_EVENT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProvider;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultActivity extends AppCompatActivity {

    // View components
    private TextView txtResult;
    private TextView txtDocID;
    private TextView txtName;
    private TextView txtPhoto;
    private ImageView imagePhotoContent;

    // View model
    private ResultViewModel resultViewModel;
    private File fileCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Standard activity setup.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind view components.
        txtDocID = findViewById(R.id.txtDocIDContent);
        txtName = findViewById(R.id.txtNameContent);
        txtResult = findViewById(R.id.txtResultContent);
        txtPhoto = findViewById(R.id.txtPhotoContent);
        imagePhotoContent = findViewById(R.id.imagePhotoContent);

        // Handle view model.
        resultViewModel = new ViewModelProvider(this).get(ResultViewModel.class);
        if (resultViewModel.getDocumentID() != null) { txtDocID.setText(resultViewModel.getDocumentID()); }
        if (resultViewModel.getName() != null) { txtName.setText(resultViewModel.getName()); }
        if (resultViewModel.getResult() != null) { txtResult.setText(resultViewModel.getResult()); }
        if (resultViewModel.getPhotoMsg() != null) { txtPhoto.setText(resultViewModel.getPhotoMsg()); }
        if (resultViewModel.getFace() != null) { imagePhotoContent.setImageDrawable(resultViewModel.getFace()); }
        if (resultViewModel.getFileCapture() != null) { fileCapture = resultViewModel.getFileCapture(); }

        String incoming = getIntent().getStringExtra(KEY_FILE);
        Log.d(TAG_BUNDLE, "ResultActivity: " + (incoming != null ? incoming : "null"));

        if (incoming != null) {
            fileCapture = new File(incoming);

            try {
                ImageParser imageParser = new ImageParser(this, fileCapture);

                imageParser.parseImageText()
                        .addOnSuccessListener(text -> {
                            if (text != null) {
                                handleTextResult(text);
                            } else {
                                txtResult.setText(MSG_NA);
                                txtDocID.setText(MSG_NA);
                                txtName.setText(MSG_NA);
                            }
                            Log.d(TAG_EVENT, "CALLBACK: parseImageText SuccessListener");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG_EVENT, "ERROR: parseImageText FailureListener\n" + e);
                        });

                imageParser.extractFace()
                        .addOnSuccessListener(faces -> {
                            if (!faces.isEmpty()) {
                                Rect faceBoundingBox = getLargestBoundingBox(faces);
                                try {
                                    imagePhotoContent.setImageBitmap(getFaceBitmap(fileCapture, faceBoundingBox));
                                    txtPhoto.setVisibility(View.INVISIBLE);
                                } catch (IOException e) {
                                    txtPhoto.setText(MSG_NA);
                                    Log.e(TAG_EVENT, "EXCEPTION: extractFace SuccessListener\n" + e);
                                }
                            } else {
                                txtPhoto.setText(MSG_NA);
                            }
                            Log.d(TAG_EVENT, "CALLBACK: extractFace SuccessListener");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG_EVENT, "ERROR: extractFace FailureListener\n" + e);
                        });
            } catch (IOException e) {
                Log.e(TAG_EVENT, "EXCEPTION: ImageParser constructor\n" + e);
            }
        }

        Intent launchMainActivity = new Intent(this, MainActivity.class);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG_EVENT, "File deleted: " + fileCapture.delete());
                fileCapture = null;
                Log.d(TAG_EVENT, "CALLBACK: ResultActivity onBackPressed");
                launchMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(launchMainActivity);
                finish();
            }
        });

        Log.d(TAG_EVENT, "CALLBACK: ResultActivity onCreate");
    }

    private void handleTextResult(Text text) {
        String result = text.getText();
        txtResult.setText(result);

        Pattern patNRIC = Pattern.compile(PAT_NRIC);
        Matcher matNRIC = patNRIC.matcher(result);
        if (matNRIC.find()) {
            txtDocID.setText(matNRIC.group());
        } else {
            Pattern patPassport = Pattern.compile(PAT_PASSPORT);
            Matcher matPassport = patPassport.matcher(result);
            if (matPassport.find() && (
                    matPassport.group().length() >= 3 && matPassport.group().length() <= 9)
            ) {
                txtDocID.setText(matPassport.group());
            } else {
                Pattern patLicense = Pattern.compile(PAT_LICENSE);
                Matcher matLicense = patLicense.matcher(result);
                if (matLicense.find()) {
                    txtDocID.setText(matLicense.group());
                } else {
                    txtDocID.setText(MSG_NA);
                }
            }
        }

        txtName.setText(MSG_NA);
    }

    @Override
    protected void onStop() {
        resultViewModel.setDocumentID(txtDocID.getText().toString());
        resultViewModel.setName(txtName.getText().toString());
        resultViewModel.setResult(txtResult.getText().toString());
        resultViewModel.setPhotoMsg(txtResult.getText().toString());
        resultViewModel.setFace(imagePhotoContent.getDrawable());
        resultViewModel.setFileCapture(fileCapture);
        super.onStop();
    }

    private Rect getLargestBoundingBox(List<Face> faces) {
        Rect maxBoundingBox = null;
        int maxArea = 0;
        for (Face face : faces) {
            Rect boundingBox = face.getBoundingBox();
            int area = boundingBox.width() * boundingBox.height();
            if (area > maxArea) {maxBoundingBox = boundingBox; maxArea = area;}
        }
        return maxBoundingBox;
    }

    private Bitmap getFaceBitmap(File fileCapture, Rect bBox) throws IOException {
        ExifInterface exif = new ExifInterface(fileCapture);
        Matrix matrix = new Matrix();
        matrix.postRotate(exif.getRotationDegrees());
        Bitmap bitmap = BitmapFactory.decodeFile(fileCapture.getAbsolutePath());
        Bitmap rotated = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
        );
        return Bitmap.createBitmap(
                rotated,
                bBox.left,
                bBox.top,
                bBox.width(),
                bBox.height()
        );
    }
}