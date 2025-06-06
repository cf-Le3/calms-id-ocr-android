package com.example.calmsidocr;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.lifecycle.ViewModel;

import java.io.File;

public class ResultViewModel extends ViewModel {
    private String documentID;
    private String name;
    private String result;
    private String photoMsg;
    private Drawable face;
    private File fileCapture;

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPhotoMsg() {
        return photoMsg;
    }

    public void setPhotoMsg(String photoMsg) {
        this.photoMsg = photoMsg;
    }

    public Drawable getFace() {
        return face;
    }

    public void setFace(Drawable face) {
        this.face = face;
    }

    public File getFileCapture() {
        return fileCapture;
    }

    public void setFileCapture(File fileCapture) {
        this.fileCapture = fileCapture;
    }
}
