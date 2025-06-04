package com.example.calmsidocr;

public final class Constants {
    private Constants() {}
    public static final String KEY_FILE = "KEY_F";
    public static final String PAT_NRIC = "\b[0-9]{6}-[0-9]{2}-[0-9]{4}\b";
    public static final String PAT_PASSPORT = "\b[A-Z]*[0-9]+[A-Z]*\b";
    public static final String PAT_LICENSE = "\b[0-9]{12}\b";
    public static final String MSG_NA = "Not found.";
    public static final String TAG_EVENT = "TAG_E";
    public static final String TAG_BUNDLE = "TAG_B";
}
