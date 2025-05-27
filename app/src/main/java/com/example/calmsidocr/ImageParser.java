package com.example.calmsidocr;

import android.content.Context;
import android.media.Image;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;

public class ImageParser {
    TextRecognizer recognizer;
    InputImage image;

    public ImageParser(Context context, File fileCapture) throws IOException {
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Uri uriCapture = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                fileCapture
        );
        image = InputImage.fromFilePath(context, uriCapture);
    }

    public Task<Text> parseImageText(){
        return recognizer.process(image);
    }
}
