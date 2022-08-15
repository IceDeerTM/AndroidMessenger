package com.deerdevelop.androidmessenger;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.deerdevelop.androidmessenger.service.BackgroundSignInService;
import com.deerdevelop.androidmessenger.service.FileManager;
import com.deerdevelop.androidmessenger.model.StorageToken;
import com.deerdevelop.androidmessenger.model.UserData;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class LaunchActivity extends AppCompatActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "Main Activity";
    private String fileNameToken = "token_data.json", fileNameUser = "userdata.json";
    private boolean nextActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        if (checkPlayServices()) {

            JSONObject jsonObject = FileManager.readJsonFromFile(LaunchActivity.this, fileNameToken);
            try {
                if (jsonObject != null) {
                    String tokenDevice = jsonObject.getString("tokenDevice");
                    if (tokenDevice.equals("")) {
                        getToken();
                    }
                    else StorageToken.setTokenDevice(tokenDevice);
                }
                else getToken();
            } catch (Exception e) {
                e.printStackTrace();
                getToken();
            }
        }

        launchNextActivity();

    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    StorageToken.setTokenDevice(token);
                    // Log and toast
                    String msg = String.format(String.valueOf(R.string.msg_token_fmt), token);

                    JSONObject jsonObject = new JSONObject();

                    try {
                        jsonObject.put("tokenDevice", token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    FileManager.writeJsonInFile(LaunchActivity.this, jsonObject.toString(), fileNameToken);

                    Log.d(TAG, msg);
                    Toast.makeText(LaunchActivity.this, msg, Toast.LENGTH_SHORT).show();
                });
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void launchNextActivity() {
        new Thread(() -> {
            Intent intent = null;
            boolean flag = FileManager.isFilePresent(LaunchActivity.this, fileNameUser);

            if (flag) { // Если вход произведен до этого то запуск MainActivity

                try {
                    JSONObject jsonFromFile = FileManager.readJsonFromFile(LaunchActivity.this, fileNameUser);
                    UserData.setNickName(jsonFromFile.getString("nickName"));
                    UserData.setDisplayName(jsonFromFile.getString("displayName"));
                    UserData.setPassword(jsonFromFile.getString("password"));
                    UserData.setUid(jsonFromFile.getString("uid"));
                    UserData.setEmail(jsonFromFile.getString("email"));

                    BackgroundSignInService.signIn();

                    String path = LaunchActivity.this.getFilesDir().getAbsolutePath() + "/" +
                            UserData.getUid() + "/" + "photo.jpg";

                    File file = new File(path);

                    Bitmap bitmap = FileManager.LoadBitmapFromFile(file);

                    if (bitmap != null) UserData.setPhoto(bitmap);


                    intent = new Intent(LaunchActivity.this, MainActivity.class);
                }
                catch (Exception exc) {
                    Toaster.show(LaunchActivity.this, exc.getMessage(), true);
                }

            }
            else { // Данные о входе отсутствуют, открываем окно регистрации/входа
                intent = new Intent(LaunchActivity.this, RegistrationLoginActivity.class);

            }
            if (intent != null) {
                nextActivity = true;
                startActivity(intent);
            }

        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nextActivity) finish();
    }
}