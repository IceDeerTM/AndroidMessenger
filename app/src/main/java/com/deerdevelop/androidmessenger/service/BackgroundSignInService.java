package com.deerdevelop.androidmessenger.service;

import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.model.StorageToken;
import com.deerdevelop.androidmessenger.model.UserData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import org.json.JSONException;
import org.json.JSONObject;

public class BackgroundSignInService {
    private static String URL = "http://192.168.0.5:5000/api/user/signin/";

    private static FirebaseUser getFirebaseUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        try {
            Task<AuthResult> authResult = firebaseAuth.signInWithEmailAndPassword(UserData.getEmail(), UserData.getPassword());

            while(!authResult.isComplete()) {

            }

            return authResult.getResult().getUser();
        }
        catch (Exception e) {
            return null;
        }

    }

    public static void signIn() {
        new Thread(() -> {
            while (UserData.getFirebaseUser() == null || StorageToken.getTokenDevice().equals("")) {
                FirebaseUser firebaseUser = getFirebaseUser();
                if (firebaseUser != null) {

                    try {
                        GetTokenResult getTokenResult = firebaseUser.getIdToken(false).getResult();

                        UserData.setFirebaseUser(firebaseUser);

                        JSONObject jsonObject = new JSONObject();
                        String deviceInfo = android.os.Build.BRAND + " " + android.os.Build.MODEL;
                        jsonObject.put("deviceInfo", deviceInfo);
                        jsonObject.put("idToken", getTokenResult.getToken());
                        jsonObject.put("tokenDevice", StorageToken.getTokenDevice());
                        String finalURL = URL + firebaseUser.getUid();
                        JSONObject jsonAnswer = new JSONObject(HttpRequester.patchRequest(finalURL, jsonObject.toString()));

                        if (jsonAnswer.getString("answerServer").equals("Successful")) {
                            StorageToken.setTokenJWT(jsonAnswer.getString("jwtToken"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
