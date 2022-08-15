package com.deerdevelop.androidmessenger;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import com.deerdevelop.androidmessenger.guiupdate.GUIActivityUpdateListener;
import com.deerdevelop.androidmessenger.guiupdate.GUIFragmentUpdateListener;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.model.UserData;
import com.deerdevelop.androidmessenger.service.ActivityManager;
import com.deerdevelop.androidmessenger.service.BackgroundWorker;
import com.deerdevelop.androidmessenger.ui.setting.mainsetting.MainSettingsFragment;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.deerdevelop.androidmessenger.databinding.ActivityMainBinding;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements GUIActivityUpdateListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavController navController;
    private ImageView imageViewPhotoHeader;
    private TextView textViewDisplayNameHeader, textViewNickNameHeader;
    private GUIFragmentUpdateListener guiFragmentUpdateListener;
    private boolean startNextActivity = false;
    private final String URL_SET_ONLINE = "http://192.168.0.5:5000/api/user/setonlinestatus/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDialogs.toolbar);

        //AppBar
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_search, R.id.nav_main_settings, R.id.nav_sessions)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dialogs);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        imageViewPhotoHeader = navigationView.getHeaderView(0).findViewById(R.id.imageViewPhotoHeader);
        textViewDisplayNameHeader = navigationView.getHeaderView(0).findViewById(R.id.textViewDisplayNameHeader);
        textViewNickNameHeader = navigationView.getHeaderView(0).findViewById(R.id.textViewNickNameHeader);
        imageViewPhotoHeader.setImageBitmap(UserData.getPhoto());
        textViewDisplayNameHeader.setText(UserData.getDisplayName());
        textViewNickNameHeader.setText(UserData.getNickName());

        //receiveNotification();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.dialogs, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dialogs);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void navigate(int id) {
        navController.navigate(id);
    }

    /*
    @Override
    public void onBackPressed() {
        finish();
    }*/

    @Override
    public void updateInterface(JSONObject json) {
        try {
            String typeData = json.getString("typeData");
            String senderNickName = json.getString("senderNickName");
            if (senderNickName.equals(UserData.getNickName())) {

                if (typeData.equals("changePhoto")) {
                    runOnUiThread(() -> imageViewPhotoHeader.setImageBitmap(UserData.getPhoto()));
                }
                else if (typeData.equals("changeDisplayName")) {
                    String displayName = json.getString("displayName");
                    runOnUiThread(() -> textViewDisplayNameHeader.setText(displayName));
                }
                else if (typeData.equals("closeSessions")) {
                    runOnUiThread(() -> ActivityManager.Logout(MainActivity.this));
                }


                if (guiFragmentUpdateListener instanceof MainSettingsFragment) guiFragmentUpdateListener.updateInterface(json);
            }
            else guiFragmentUpdateListener.updateInterface(json);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setGUIFragmentUpdateListener(GUIFragmentUpdateListener guiFragmentUpdateListener) {
        this.guiFragmentUpdateListener = guiFragmentUpdateListener;
    }


    @Override
    protected void onPause() {
        super.onPause();

        try {
            if (!startNextActivity) {
                sendOnlineStatus("offline");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BackgroundWorker.setUpdatedListener(this);
        try {
            if (!startNextActivity) {
                sendOnlineStatus("online");
            }
            startNextActivity = false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void sendOnlineStatus(String status) {
        new Thread(() -> {
            try {
                String url = URL_SET_ONLINE + UserData.getUid() + "/" + status;
                HttpRequester.getRequest(url);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }



    public void setStartNextActivity(boolean startNextActivity) {
        this.startNextActivity = startNextActivity;
    }
}