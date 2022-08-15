package com.deerdevelop.androidmessenger.ui.registration;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import com.deerdevelop.androidmessenger.R;
import com.deerdevelop.androidmessenger.RegistrationLoginActivity;
import com.deerdevelop.androidmessenger.databinding.FragmentRegistrationBinding;
import com.deerdevelop.androidmessenger.service.FileManager;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.ui.informationdialog.InfoDialogFragment;
import org.json.JSONObject;

import java.io.File;

public class RegistrationFragment extends Fragment {

    private FragmentRegistrationBinding binding;
    private String URL = "http://192.168.0.5:5000/api/user/createuser";
    private String URL_SET_START_PHOTO = "http://192.168.0.5:5000/api/user/setstartphoto/";
    private ProgressBar progressBarRegistration;
    private String displayName = "", email = "", password = "", repeatPassword = "", nickName = "";
    private InfoDialogFragment infoDialogFragment = new InfoDialogFragment();
    FragmentManager manager = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Activity act = getActivity ();
        if (act instanceof RegistrationLoginActivity) {
            ((RegistrationLoginActivity) act).setToolbarState (true);
            ((RegistrationLoginActivity)act).setTitleToolBar("Регистрация");
        }

        manager = getActivity().getSupportFragmentManager();
        Button buttonSendRegistration = binding.buttonSendRegistration;
        EditText editTextTextEmailAddress = binding.editTextEmailAddress;
        EditText editTextDisplayName = binding.editTextDisplayName;
        EditText editTextPassword = binding.editTextPassword;
        EditText editTextPasswordRepeat = binding.editTextPasswordRepeat;
        EditText editTextNickName = binding.editTextNickName;
        progressBarRegistration = binding.progressBarRegistration;
        progressBarRegistration.setVisibility(View.INVISIBLE);

        buttonSendRegistration.setOnClickListener(view -> {
            displayName = editTextDisplayName.getText().toString();
            email = editTextTextEmailAddress.getText().toString();
            password = editTextPassword.getText().toString();
            repeatPassword = editTextPasswordRepeat.getText().toString();
            nickName = editTextNickName.getText().toString();

            String message = "";

            if (password.equals(repeatPassword) && !email.equals("") && password.length() > 5 &&
                    password.length() < 21 && nickName.length() > 0 && !nickName.contains(" ")) {
                sendRegistration();
            }
            else if (password.equals("") && repeatPassword.equals("") && email.equals("") && displayName.equals("")) {
                message = "Вы не заполнили ни одно поле, заполните и повторите попытку.";
                infoDialogFragment.setTextMessage(message);
                infoDialogFragment.show(manager, "dialog" );
            }
            else if (password.length() < 5)  {
                message = "Пароль слишком короткий, минимальнная длина пароля - 6 символов.";
                infoDialogFragment.setTextMessage(message);
                infoDialogFragment.show(manager, "dialog" );
            }
            else if (password.length() > 20)  {
                message = "Пароль слишком длинный, максимальная длина пароля - 20 символов.";
                infoDialogFragment.setTextMessage(message);
                infoDialogFragment.show(manager, "dialog" );
            }
            else if (!password.equals(repeatPassword)) {
                message = "Пароли не совпадают.";
                infoDialogFragment.setTextMessage(message);
                infoDialogFragment.show(manager, "dialog" );
            }
            else if (email.equals("")) {
                message = "Вы не ввели адрес электронной почты.";
                infoDialogFragment.setTextMessage(message);
                infoDialogFragment.show(manager, "dialog" );
            }
            else if (displayName.equals("")) {
                message = "Вы не ввели отображаемое имя пользователя.";
                infoDialogFragment.setTextMessage(message);
                infoDialogFragment.show(manager, "dialog" );
            }
            else if (nickName.length() < 1) {
                message = "Вы не ввели никнейм.";
                infoDialogFragment.setTextMessage(message);
                infoDialogFragment.show(manager, "dialog" );
            }
            else if (nickName.contains(" ")) {
                message = "Никнейм не может содержать пробелы.";
                infoDialogFragment.setTextMessage(message);
                infoDialogFragment.show(manager, "dialog" );
            }


        });
        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void sendRegistration() {
        new Thread(() -> {
            JSONObject jsonObject = new JSONObject();
            FragmentManager manager = getActivity().getSupportFragmentManager();
            String messageDialogFragment = "";

            getActivity().runOnUiThread(() -> progressBarRegistration.setVisibility(View.VISIBLE));
            try {
                jsonObject.put("email", email);
                jsonObject.put("displayName", displayName);
                jsonObject.put("password", password);
                jsonObject.put("nickName", nickName);

                JSONObject jsonObjectAnswer = new JSONObject(HttpRequester.postRequest(URL, jsonObject.toString()));


                if (jsonObjectAnswer.getString("answerServer").equals("Successful"))
                {

                    getActivity().runOnUiThread(() -> progressBarRegistration.setVisibility(View.INVISIBLE));
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_start_j);

                    FileManager.SaveBitmapInFile(bitmap, getContext().getFilesDir().getAbsolutePath() + "/", "temp.jpg");
                    File file = new File(getContext().getFilesDir().getAbsolutePath() + "/temp.jpg");
                    HttpRequester.sendFile(URL_SET_START_PHOTO + email, "", file);

                    file.delete();
                    messageDialogFragment = "Успешная регистрация. После закрытия данного окна, вы вернетесь к предыдущему.";
                    infoDialogFragment.setTextMessage(messageDialogFragment);
                    infoDialogFragment.show(manager, "dialog" );

                    getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                }
                else {
                    String descError = jsonObjectAnswer.getString("descError");
                    if (descError.contains("Invalid email address")) {
                        messageDialogFragment = "Некорректный адрес электронной почты";
                    }
                    else if (descError.equals("nickName taken")) messageDialogFragment = "Никнейм занят.";
                    else messageDialogFragment = "Произошла ошибка при обработке запроса. Повторите попытку позже.";

                    infoDialogFragment.setTextMessage(messageDialogFragment);
                    infoDialogFragment.show(manager, "dialog" );
                    getActivity().runOnUiThread(() -> progressBarRegistration.setVisibility(View.INVISIBLE));
                }
            } catch (Exception e) {
                messageDialogFragment = e.getMessage();
                infoDialogFragment.setTextMessage(messageDialogFragment);
                infoDialogFragment.show(manager, "dialog" );
                e.printStackTrace();
            }
        }).start();
    }
}