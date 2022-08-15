package com.deerdevelop.androidmessenger;

import android.content.Context;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;
import com.deerdevelop.androidmessenger.ui.registration_login.ChoiceRegLogFragment;

public class RegistrationLoginActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private boolean nextActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_login);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerRL, ChoiceRegLogFragment.newInstance())
                    .commitNow();

            toolbar = (Toolbar) findViewById(R.id.toolbarRL);
            setSupportActionBar(toolbar);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
        }
    }

    public void setTitleToolBar(String title) {
        toolbar.setTitle(title);
    }

    public void setToolbarState(boolean flag) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(flag);
        getSupportActionBar().setDisplayShowHomeEnabled(flag);
    }

    public void setNextActivity(boolean nextActivity) {
        this.nextActivity = nextActivity;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (nextActivity) finish();
    }

}