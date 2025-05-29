package com.example.calmsidocr;

import static com.example.calmsidocr.Constants.KEY_FILE;
import static com.example.calmsidocr.Constants.MSG_NA;
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

import com.google.mlkit.vision.face.Face;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultActivity extends AppCompatActivity {

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
        TextView txtResult = findViewById(R.id.txtResultContent);
        TextView txtDocID = findViewById(R.id.txtDocIDContent);
        TextView txtPhotoContent = findViewById(R.id.txtPhotoContent);
        ImageView imagePhotoContent = findViewById(R.id.imagePhotoContent);

        String incoming = getIntent().getStringExtra(KEY_FILE);
        Log.d(TAG_BUNDLE, "ResultActivity: " + (incoming != null ? incoming : "null"));

        if (incoming != null) {
            fileCapture = new File(incoming);

            try {
                ImageParser imageParser = new ImageParser(this, fileCapture);

                imageParser.parseImageText()
                        .addOnSuccessListener(text -> {
                            String result = text.getText();
                            String documentID = getDocumentID(result);
                            txtResult.setText(result);
                            txtDocID.setText(!Objects.equals(documentID, "") ? documentID : MSG_NA);
                            Log.d(TAG_EVENT, "CALLBACK: parseImageText SuccessListener");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG_EVENT, "ERROR: parseImageText FailureListener\n" + e);
                        });

                imageParser.extractFace()
                        .addOnSuccessListener(faces -> {
                            if (!faces.isEmpty()) {
                                Rect maxBBox = null;
                                int maxArea = 0;
                                for (Face face : faces) {
                                    Rect bBox = face.getBoundingBox();
                                    int area = getBoundingBoxArea(bBox);
                                    if (area > maxArea) {maxBBox = bBox; maxArea = area;}
                                }
                                assert maxBBox != null;
                                try {
                                    imagePhotoContent.setImageBitmap(getFaceBitmap(fileCapture, maxBBox));
                                    txtPhotoContent.setVisibility(View.INVISIBLE);
                                } catch (IOException e) {
                                    txtPhotoContent.setText(MSG_NA);
                                }
                            } else {
                                txtPhotoContent.setText(MSG_NA);
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
                Log.d(TAG_EVENT, "CALLBACK: ResultActivity onBackPressed");
                launchMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(launchMainActivity);
                finish();
            }
        });

        Log.d(TAG_EVENT, "CALLBACK: ResultActivity onCreate");
    }

    private String getDocumentID(String inputStr) {
        Pattern patNRIC = Pattern.compile(PAT_NRIC);
        Pattern patPassport = Pattern.compile(PAT_PASSPORT);
        Matcher matNRIC = patNRIC.matcher(inputStr);
        Matcher matPassport = patPassport.matcher(inputStr);
        if (matNRIC.find()){
            return matNRIC.group();
        } else if (matPassport.find()) {
            if (matPassport.group().length() <= 9) {return matPassport.group();}
        }
        return "";
    }

    private int getBoundingBoxArea(Rect bBox) {
        return bBox.width() * bBox.height();
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