package com.deerdevelop.androidmessenger.ui.search;

import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.*;
import androidx.cardview.widget.CardView;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.deerdevelop.androidmessenger.ActiveDialogActivity;
import com.deerdevelop.androidmessenger.databinding.FragmentSearchUserBinding;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.model.StorageToken;
import com.deerdevelop.androidmessenger.model.UserData;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class SearchUserFragment extends Fragment {

    private FragmentSearchUserBinding binding;
    private ProgressBar progressBar;
    private EditText editTextSearch;
    private ImageButton imageButtonSearch;
    private Button buttonCreateDialog;
    private TextView textViewDisplayNameSearched, textViewNickNameSearched, textViewSearchResult;
    private CardView cardViewSearched;
    private ImageView imageViewPhotoSearched;
    private final String URL_SEARCH_USER = "http://192.168.0.5:5000/api/user/searchuser/";
    private final String URL_CREATE_DIALOG = "http://192.168.0.5:5000/api/dialog/createdialog/";
    private final String URL_GET_PHOTO = "http://192.168.0.5:5000/api/user/getphoto/";
    private String nickName = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editTextSearch = binding.editTextSearch;
        imageButtonSearch = binding.imageButtonSearch;
        buttonCreateDialog = binding.buttonCreateDialog;
        textViewDisplayNameSearched = binding.textViewDisplayNameSearched;
        textViewNickNameSearched = binding.textViewNickNameSearched;
        cardViewSearched = binding.cardViewSearched;
        imageViewPhotoSearched = binding.imageViewPhotoSearched;
        textViewSearchResult = binding.textViewSearchResult;
        textViewSearchResult.setVisibility(View.INVISIBLE);

        cardViewSearched.setVisibility(View.INVISIBLE);

        progressBar = binding.progressBar;
        progressBar.setVisibility(View.INVISIBLE);

        imageButtonSearch.setOnClickListener(v -> {
            String text = editTextSearch.getText().toString();
            nickName = "";
            cardViewSearched.setVisibility(View.INVISIBLE);
            if (!text.equals("")) {
                sendSearchRequest(text);
            }
        });

        buttonCreateDialog.setOnClickListener(v -> {
            sendCreateDialogRequest(nickName);
        });

        return root;
    }

    private void sendSearchRequest(String inputNickName) {
        textViewSearchResult.setVisibility(View.INVISIBLE);
        if (!inputNickName.equals(UserData.getNickName())) {
            progressBar.setVisibility(View.VISIBLE);

            new Thread(() -> {

                String finalUrl = URL_SEARCH_USER + inputNickName;
                try {
                    JSONObject jsonAnswerServer = new JSONObject(HttpRequester.getRequest(finalUrl,
                            StorageToken.getTokenJWT()));

                    if (jsonAnswerServer.getString("answerServer").equals("Exist")) {
                        String displayName = jsonAnswerServer.getString("displayName");
                        nickName = jsonAnswerServer.getString("nickName");

                        String urlGetPhoto = URL_GET_PHOTO + nickName;
                        Bitmap bitmapPhoto = HttpRequester.downloadBitmap(urlGetPhoto, StorageToken.getTokenJWT());

                        getActivity().runOnUiThread(() -> {
                            textViewSearchResult.setText("Резльтат поиска");
                            textViewSearchResult.setVisibility(View.VISIBLE);

                            cardViewSearched.setVisibility(View.VISIBLE);
                            imageViewPhotoSearched.setImageBitmap(bitmapPhoto);

                            textViewNickNameSearched.setText(nickName);
                            textViewDisplayNameSearched.setText(displayName);

                        });

                    }
                    else {
                        Toaster.show(getContext(), "Данного пользователя не существует", false);
                    }

                } catch (JSONException e) {
                    Toaster.show(getContext(), e.getMessage(), false);
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
            }).start();
        }
        else {
            textViewSearchResult.setText("Пользователь не найден");
            textViewSearchResult.setVisibility(View.VISIBLE);
        }

    }

    private void sendCreateDialogRequest(String searchedNickName) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            String finalUrl = URL_CREATE_DIALOG + UserData.getUid() + "/" + searchedNickName;
            try {
                JSONObject jsonAnswerServer = new JSONObject(HttpRequester.getRequest(finalUrl,
                        StorageToken.getTokenJWT()));

                if (jsonAnswerServer.getString("answerServer").equals("Successful")) {
                    String path = getContext().getFilesDir().getAbsolutePath() + "/" + UserData.getUid() + "/" +
                            searchedNickName;
                    File file = new File(path);
                    boolean flag = file.exists();
                    while (!flag) {
                        flag = file.exists();
                    }
                    getActivity().runOnUiThread(() -> {
                        cardViewSearched.setVisibility(View.INVISIBLE);
                    });

                    getActivity().runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));

                    Intent intent = new Intent(getActivity(), ActiveDialogActivity.class);
                    intent.putExtra("nickName", searchedNickName);
                    startActivity(intent);
                }
                else {
                    getActivity().runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
                    Toaster.show(getContext(), "Данного пользователя не существует", false);
                }

            } catch (Exception e) {
                Toaster.show(getContext(), e.getMessage(), false);
                e.printStackTrace();
            }


        }).start();
    }
}