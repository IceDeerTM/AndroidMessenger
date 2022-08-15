package com.deerdevelop.androidmessenger.model;

import android.graphics.Bitmap;
import android.media.Image;
import com.google.firebase.auth.FirebaseUser;

public class UserData {
    private static String email, displayName, NickName, password;
    private static Bitmap photo;
    private static FirebaseUser firebaseUser;
    private static String uid;

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        UserData.email = email;
    }

    public static String getDisplayName() {
        return displayName;
    }

    public static void setDisplayName(String displayName) {
        UserData.displayName = displayName;
    }

    public static String getNickName() {
        return NickName;
    }

    public static void setNickName(String nickName) {
        NickName = nickName;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        UserData.password = password;
    }

    public static Bitmap getPhoto() {
        return photo;
    }

    public static void setPhoto(Bitmap photo) {
        UserData.photo = photo;
    }

    public static FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public static void setFirebaseUser(FirebaseUser firebaseUser) {
        UserData.firebaseUser = firebaseUser;
    }

    public static String getUid() {
        return uid;
    }

    public static void setUid(String uid) {
        UserData.uid = uid;
    }
}
