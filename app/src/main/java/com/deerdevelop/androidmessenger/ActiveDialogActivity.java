package com.deerdevelop.androidmessenger;

import android.Manifest;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.deerdevelop.androidmessenger.guiupdate.GUIActivityUpdateListener;
import com.deerdevelop.androidmessenger.guiupdate.GUIFragmentUpdateListener;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.model.UserData;
import com.deerdevelop.androidmessenger.service.ActivityManager;
import com.deerdevelop.androidmessenger.service.BackgroundWorker;
import com.deerdevelop.androidmessenger.ui.calendar.CalendarFragment;
import com.deerdevelop.androidmessenger.ui.chat.ChatFragment;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ActiveDialogActivity extends AppCompatActivity implements GUIActivityUpdateListener {

    private Toolbar toolbarDialogActivity;
    private GUIFragmentUpdateListener guiFragmentUpdateListener;
    private boolean backAction = false;
    private final String URL_SET_ONLINE = "http://192.168.0.5:5000/api/user/setonlinestatus/";
    private boolean isBackgroundWork = false;
    private final int callbackId = 42;
    private int idMenu = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_dialog_activity);

        toolbarDialogActivity = (Toolbar) findViewById(R.id.toolbarDialogActivity);
        setSupportActionBar(toolbarDialogActivity);

        Bundle extras = getIntent().getExtras();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerDialog, ChatFragment.newInstance())
                    .commitNow();
        }
    }

    public void setToolbarDialogActivity(int id) {
        idMenu = id;
        setSupportActionBar(toolbarDialogActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (idMenu == 1) getMenuInflater().inflate(R.menu.chat, menu);
        else if (idMenu == 2) getMenuInflater().inflate(R.menu.chat_inter, menu);
        else getMenuInflater().inflate(R.menu.active_dialog, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_edit: {
                ((ChatFragment)getSupportFragmentManager().findFragmentById(R.id.containerDialog)).changeMessageOption();
                break;
            }
            case R.id.app_bar_copy: {
                ((ChatFragment)getSupportFragmentManager().findFragmentById(R.id.containerDialog)).copyMessageOption();
                break;
            }
            case R.id.app_bar_delete: {
                ((ChatFragment)getSupportFragmentManager().findFragmentById(R.id.containerDialog)).deleteMessageOption();
                break;
            }
            case R.id.app_bar_add_event: {
                boolean permissions = checkPermissions(callbackId, Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR);
                if (permissions) {
                    openCalendarFragment();
                }
                break;
            }
        }

        return true;
    }

    public void setSubTitle(String subTitle) {
        getSupportActionBar().setSubtitle(subTitle);
    }

    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void updateInterface(JSONObject json) {
        try {
            String typeData = json.getString("typeData");
            if (typeData.equals("closeSessions")) runOnUiThread(() ->
                    ActivityManager.Logout(ActiveDialogActivity.this));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (guiFragmentUpdateListener != null) {
            guiFragmentUpdateListener.updateInterface(json);
        }
    }

    public void setGUIFragmentUpdateListener(GUIFragmentUpdateListener guiFragmentUpdateListener) {
        this.guiFragmentUpdateListener = guiFragmentUpdateListener;
    }


    @Override
    protected void onPause() {
        super.onPause();
        BackgroundWorker.setUpdatedListener(this);
        try {
            if (backAction) Log.d("MyThread", "back Dialog");
            else {
                isBackgroundWork = true;
                sendOnlineStatus("offline");
            }
        }
        catch (Exception e) {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        BackgroundWorker.setUpdatedListener(this);
        try {
            if (isBackgroundWork) {
                sendOnlineStatus("online");
                isBackgroundWork = false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backAction = true;
    }

    private void sendOnlineStatus(String status) {
        new Thread(() -> {
            try {
                String url = URL_SET_ONLINE + UserData.getUid() + "/" + status;
                HttpRequester.getRequest(url);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }).start();
    }

    private boolean checkPermissions(int callbackId, String... permissionsId) {
        boolean permissions = true;

        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED;
        }

        if (!permissions) {
            ActivityCompat.requestPermissions(this, permissionsId, callbackId);
        }
        return permissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case callbackId:
                if (grantResults.length > 0
                        && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
                    openCalendarFragment();

                } else {
                    Toaster.show(this, "Необходимы разрешения на работу с календарем", false);
                }
                return;
        }
    }

    private void openCalendarFragment() {
        CalendarFragment calendarFragment = new CalendarFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerDialog, calendarFragment, "CalendarFragment")
                .addToBackStack(null)
                .commit();
    }
}