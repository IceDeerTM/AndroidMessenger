package com.deerdevelop.androidmessenger.ui.setting.mainsetting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import com.deerdevelop.androidmessenger.service.ActivityManager;
import com.deerdevelop.androidmessenger.MainActivity;
import com.deerdevelop.androidmessenger.R;
import com.deerdevelop.androidmessenger.databinding.FragmentMainSettingsBinding;
import com.deerdevelop.androidmessenger.guiupdate.GUIFragmentUpdateListener;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.service.FileManager;
import com.deerdevelop.androidmessenger.model.StorageToken;
import com.deerdevelop.androidmessenger.model.UserData;
import com.deerdevelop.androidmessenger.ui.informationdialog.InfoDialogFragment;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;
import org.json.JSONObject;

import java.io.*;

public class MainSettingsFragment extends Fragment implements GUIFragmentUpdateListener {

    private ImageView imageViewSettingsPhoto;
    private TextView textViewSettingDisplayName;
    private EditText editTextChangeDisplayName;
    private Uri userPhotoUri;
    private final String URL_CHANGE_PHOTO = "http://192.168.0.5:5000/api/user/changephoto/";
    private InfoDialogFragment infoDialogFragment = new InfoDialogFragment();
    private FragmentManager fragmentManager = null;
    private String messageForInfoDialog = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentMainSettingsBinding binding = FragmentMainSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ((MainActivity)getContext()).setGUIFragmentUpdateListener(this);
        fragmentManager = getActivity().getSupportFragmentManager();

        imageViewSettingsPhoto = binding.imageViewSettingsPhoto;
        imageViewSettingsPhoto.setImageBitmap(UserData.getPhoto());
        imageViewSettingsPhoto.setOnClickListener(imageViewSettingsPhotoClick);

        ImageButton imageButtonChangeDisplayName = binding.imageButtonChangeDisplayName;
        Button buttonLogout = binding.buttonLogout;

        textViewSettingDisplayName = binding.textViewSettingDisplayName;
        CardView cardViewSession = binding.cardViewSession;
        editTextChangeDisplayName = binding.editTextChangeDisplayName;
        TextView textViewSettingsNickName = binding.textViewSettingsNickName;

        editTextChangeDisplayName.setText(UserData.getDisplayName());

        textViewSettingDisplayName.setText(UserData.getDisplayName());
        textViewSettingsNickName.setText(UserData.getNickName());
        cardViewSession.setOnClickListener(v -> ((MainActivity)getContext()).navigate(R.id.nav_sessions));
        buttonLogout.setOnClickListener(logoutListener);
        imageButtonChangeDisplayName.setOnClickListener(displayNameChangeClicked);

        return root;
    }

    View.OnClickListener logoutListener = v -> {
        if (StorageToken.getTokenJWT().equals("")) {
            messageForInfoDialog = "Проблемы с соединением с сервером";
            infoDialogFragment.setTextMessage(messageForInfoDialog);
            infoDialogFragment.show(fragmentManager, "dialogFragment");
        }
        sendLogoutRequest();
    };

    View.OnClickListener displayNameChangeClicked = v -> {
        String displayName = editTextChangeDisplayName.getText().toString();
        if (displayName.length() > 0 && !displayName.equals("") && displayName.length() < 20) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("senderNickName", UserData.getNickName());

                String url = "http://192.168.0.5:5000/api/user/changedisplayname/" + UserData.getUid();
                jsonObject.put("displayName", displayName);
                sendRequest(url, jsonObject);

            } catch (Exception e) {
                messageForInfoDialog = e.getMessage();
                infoDialogFragment.setTextMessage(messageForInfoDialog);
                infoDialogFragment.show(fragmentManager, "dialogFragment");
                e.printStackTrace();
            }
        }
    };

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                userPhotoUri = uri;
                sendRequestChangePhoto();
            });

    View.OnClickListener imageViewSettingsPhotoClick = v -> {
        mGetContent.launch("image/*");
    };

    private void sendRequestChangePhoto() {
        new Thread(() -> {

            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(userPhotoUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                String path = getContext().getFilesDir().getAbsolutePath() + "/" +
                        UserData.getUid() + "/";
                File file = new File(path, "photo.jpg");

                FileManager.SaveBitmapInFile(bitmap, path, "photo.jpg");

                String finalUrl = URL_CHANGE_PHOTO + UserData.getUid();
                JSONObject jsonObject = new JSONObject(HttpRequester.sendFile(finalUrl, StorageToken.getTokenJWT(), file));

                if (jsonObject.getString("answerServer").equals("Failed")) {
                    Toaster.show(getContext(), "Произошла ошибка", false);
                }
            } catch (Exception e) {
                messageForInfoDialog = e.getMessage();
                infoDialogFragment.setTextMessage(messageForInfoDialog);
                infoDialogFragment.show(fragmentManager, "dialogFragment");
                e.printStackTrace();
            }

        }).start();
    }

    @Override
    public void updateInterface(JSONObject json) {
        try {
            String typeData = json.getString("typeData");

            if (typeData.equals("changePhoto")) {
                getActivity().runOnUiThread(() -> imageViewSettingsPhoto.setImageBitmap(UserData.getPhoto()));
            }
            else if (typeData.equals("changeDisplayName")) {
                getActivity().runOnUiThread(() -> {
                    textViewSettingDisplayName.setText(UserData.getDisplayName());
                    editTextChangeDisplayName.setText(UserData.getDisplayName());
                });
            }
        } catch (Exception e) {
            messageForInfoDialog = e.getMessage();
            infoDialogFragment.setTextMessage(messageForInfoDialog);
            infoDialogFragment.show(fragmentManager, "dialogFragment");
            e.printStackTrace();
        }
    }

    private void sendLogoutRequest() {
        new Thread(() -> {
            String url = "http://192.168.0.5:5000/api/user/logout/" + UserData.getUid() + "/"
                    + StorageToken.getTokenDevice();
            try {
                JSONObject jsonObject = new JSONObject(HttpRequester.deleteRequest(url, "", StorageToken.getTokenJWT()));
                if (jsonObject.getString("answerServer").equals("Successful")) {
                    String pathToUserData = getContext().getFilesDir().getAbsolutePath() + "/" + "userdata.json";
                    File file = new File(pathToUserData); file.delete();

                    getActivity().runOnUiThread(() -> ActivityManager.Logout(getActivity()));
                }

            } catch (Exception e) {
                messageForInfoDialog = e.getMessage();
                infoDialogFragment.setTextMessage(messageForInfoDialog);
                infoDialogFragment.show(fragmentManager, "dialogFragment");
                e.printStackTrace();
            }
        }).start();
    }

    private void sendRequest(String url, JSONObject jsonObject) {
        new Thread(() -> {
            try {
                JSONObject jsonAnswer = new JSONObject(HttpRequester.patchRequest(url, jsonObject.toString(),
                        StorageToken.getTokenJWT()));
                if (jsonAnswer.getString("answerServer").equals("Exist")) {
                    Toaster.show(getContext(), "Никнейм занят", false);
                }
                else if (jsonAnswer.getString("answerServer").equals("Failed")) {
                    Toaster.show(getContext(), "Произошла ошибка при смене имени", false);
                }
            } catch (Exception e) {
                messageForInfoDialog = e.getMessage();
                infoDialogFragment.setTextMessage(messageForInfoDialog);
                infoDialogFragment.show(fragmentManager, "dialogFragment");
                e.printStackTrace();
            }
        }).start();
    }
}