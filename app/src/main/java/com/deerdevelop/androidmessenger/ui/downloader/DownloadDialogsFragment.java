package com.deerdevelop.androidmessenger.ui.downloader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.deerdevelop.androidmessenger.MainActivity;
import com.deerdevelop.androidmessenger.R;
import com.deerdevelop.androidmessenger.RegistrationLoginActivity;
import com.deerdevelop.androidmessenger.databinding.FragmentDownloadDialogsBinding;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.service.FileManager;
import com.deerdevelop.androidmessenger.model.StorageToken;
import com.deerdevelop.androidmessenger.model.UserData;
import com.deerdevelop.androidmessenger.model.Message;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class DownloadDialogsFragment extends Fragment {

    private FragmentDownloadDialogsBinding binding;
    private TextView textViewLoad;
    private String URL_GET_PHOTO = "http://192.168.0.5:5000/api/user/getphoto/";
    private ProgressBar progressBarDownload;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDownloadDialogsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Activity act = getActivity ();
        if (act instanceof RegistrationLoginActivity) {
            ((RegistrationLoginActivity) act).setToolbarState (false);
            ((RegistrationLoginActivity)act).setTitleToolBar("Подготовка к запуску");
        }

        textViewLoad = binding.textViewLoad;
        progressBarDownload = binding.progressBarDownload;
        progressBarDownload.setVisibility(View.INVISIBLE);

        downloadDialogs();

        return root;
    }

    private void updateText(String str) {
        textViewLoad.setText(str);
    }

    private void downloadDialogs() {
        new Thread(() -> {
            getActivity().runOnUiThread(() -> {
                updateText("Получение данных с сервера");
                progressBarDownload.setVisibility(View.VISIBLE);
            });
            String uid = UserData.getFirebaseUser().getUid();
            String URL = "http://192.168.0.5:5000/api/dialog/getdialogpreviews/" + uid;
            String jsonFromServer = HttpRequester.getRequest(URL, StorageToken.getTokenJWT());

            try {
                JSONObject dialogs = new JSONObject(jsonFromServer);
                JSONArray jsonArray = dialogs.getJSONArray("dialogs");

                getActivity().runOnUiThread(() -> updateText("Сохранение файлов"));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Gson gson = new Gson();

                    String nickName = jsonObject.getString("nickName");
                    String displayName = jsonObject.getString("displayName");
                    String path = getContext().getFilesDir().getAbsolutePath() + "/" + uid + "/" + nickName + "/chat";
                    File file = new File(path);
                    file.mkdirs();
                    JSONObject jsonDisplayName = new JSONObject(); jsonDisplayName.put("displayName", displayName);
                    file = new File(getContext().getFilesDir().getAbsolutePath() + "/" + uid + "/" + nickName
                            + "/displayName.json");
                    FileManager.writeJsonInFile(getContext(), jsonDisplayName.toString(), file);
                    Bitmap bitmap;
                    if (nickName.equals("Selected")) {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bookmarks_j);
                    }
                    else {
                        bitmap = HttpRequester.downloadBitmap(URL_GET_PHOTO + nickName, StorageToken.getTokenJWT());
                    }
                    FileManager.SaveBitmapInFile(bitmap, getContext().getFilesDir().getAbsolutePath() + "/" + uid
                            + "/" + nickName + "/", "photo.jpg");

                    try {
                        JSONObject message = jsonObject.getJSONObject("message");

                        Message userMessage = gson.fromJson(message.toString(), Message.class);
                        path += "/" + userMessage.getMessageId() + ".json";
                        file = new File(path);
                        FileManager.writeJsonInFile(getContext(), message.toString(), file);

                    }
                    catch(Exception e) {

                    }

                }
                Bitmap bitmap = HttpRequester.downloadBitmap(URL_GET_PHOTO + UserData.getNickName(), StorageToken.getTokenJWT());

                UserData.setPhoto(bitmap);
                FileManager.SaveBitmapInFile(bitmap, getContext().getFilesDir().getAbsolutePath() + "/" + uid
                        + "/", "photo.jpg");

                getActivity().runOnUiThread(() -> updateText("Выполнено. Идет запуск приложения"));
                startActivity(new Intent(getActivity(), MainActivity.class));
            } catch (Exception e) {

                e.printStackTrace();
            }
        }).start();
    }

}