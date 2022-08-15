package com.deerdevelop.androidmessenger.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import com.deerdevelop.androidmessenger.MainActivity;
import com.deerdevelop.androidmessenger.R;
import com.deerdevelop.androidmessenger.RegistrationLoginActivity;
import com.deerdevelop.androidmessenger.databinding.FragmentLoginBinding;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.service.FileManager;
import com.deerdevelop.androidmessenger.model.StorageToken;
import com.deerdevelop.androidmessenger.model.UserData;
import com.deerdevelop.androidmessenger.ui.informationdialog.InfoDialogFragment;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;
import com.deerdevelop.androidmessenger.ui.downloader.DownloadDialogsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends Fragment {

    private final String URL = "http://192.168.0.5:5000/api/user/signin/";
    private String fileName = "userdata.json";
    private FragmentLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private String email = "", password = "";;
    private CardView cardViewSendMessage;
    private InfoDialogFragment resultDialogFragment = new InfoDialogFragment();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        firebaseAuth = FirebaseAuth.getInstance();

        Activity act = getActivity ();
        if (act instanceof RegistrationLoginActivity) {
            ((RegistrationLoginActivity) act).setToolbarState (true);
            ((RegistrationLoginActivity)act).setTitleToolBar("Вход");
        }


        Button buttonSignIn = binding.buttonSignIn;
        EditText editTextEmailSignIn = binding.editTextEmailSignIn;
        EditText editTextPasswordSignIn = binding.editTextPasswordSignIn;

        cardViewSendMessage = binding.cardViewSendMessage;
        cardViewSendMessage.setVisibility(View.INVISIBLE);

        buttonSignIn.setOnClickListener(view -> {
            email = editTextEmailSignIn.getText().toString(); password = editTextPasswordSignIn.getText().toString();

            if (email.equals("") || password.equals("")) {
                String message = "Вы не заполнили одно из полей. Заполните и повторите попытку.";
                resultDialogFragment.setTextMessage(message);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                resultDialogFragment.show(manager, "dialog" );
            }
            else signIn(email, password);
        });

        cardViewSendMessage.setOnClickListener(v -> {
            if (email != "") sendEmailVerified();
        });

        return root;
    }

    private void signIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            UserData.setFirebaseUser(user);

                            signInThread(user);


                        } else {
                            String message = task.getException().getMessage();
                            if (message.contains("The email address is badly formatted")) {
                                message = "Некорректный email адрес.";
                            }
                            else if (message.contains("The password is invalid")) {
                                message = "Введен неправильный пароль.";
                            }
                            else if (message.contains("There is no user record")) {
                                message = "Пользователя с такими данными не существует.";
                            }

                            resultDialogFragment.setTextMessage(message);
                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            resultDialogFragment.show(manager, "dialog" );
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void signInThread(FirebaseUser firebaseUser) {
        new Thread(() -> {
            JSONObject jsonObject = new JSONObject();

            try {
                GetTokenResult idToken = firebaseUser.getIdToken(false).getResult();

                String deviceInfo = android.os.Build.BRAND + " " + android.os.Build.MODEL;
                jsonObject.put("deviceInfo", deviceInfo);
                jsonObject.put("idToken", idToken.getToken());
                jsonObject.put("tokenDevice", StorageToken.getTokenDevice());

                String finalURL = URL + firebaseUser.getUid();
                JSONObject jsonAnswer = new JSONObject(HttpRequester.patchRequest(finalURL, jsonObject.toString()));

                if (jsonAnswer.getString("answerServer").equals("Successful"))
                {
                    StorageToken.setTokenJWT(jsonAnswer.getString("jwtToken"));
                    JSONObject jsonUserData = new JSONObject();
                    jsonUserData.put("uid", firebaseUser.getUid());
                    jsonUserData.put("email", email);
                    jsonUserData.put("password", password);
                    jsonUserData.put("displayName", firebaseUser.getDisplayName());
                    jsonUserData.put("nickName", jsonAnswer.getString("nickName"));

                    UserData.setDisplayName(firebaseUser.getDisplayName()); UserData.setEmail(email);
                    UserData.setNickName(jsonAnswer.getString("nickName")); UserData.setPassword(password);
                    UserData.setUid(firebaseUser.getUid());

                    saveUserDataInFile(jsonUserData.toString());

                    ((RegistrationLoginActivity)getActivity()).setNextActivity(true);

                    if (jsonAnswer.getString("updateDialogs").equals("Yes")) {

                        DownloadDialogsFragment downloadDialogsFragment = new DownloadDialogsFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.containerRL, downloadDialogsFragment, "downloadFragment")
                                .addToBackStack(null)
                                .commit();

                    } else {

                        startActivity(new Intent(getActivity(), MainActivity.class));
                    }
                }
                else if (jsonAnswer.getString("answerServer").equals("noEmailVerified")) {
                    String message = "Вы не подтвердили адрес электронную почты. Зайдите на почту, найдите письмо" +
                            " и перейдите по ссылке.";
                    getActivity().runOnUiThread(() -> cardViewSendMessage.setVisibility(View.VISIBLE));
                    resultDialogFragment.setTextMessage(message);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    resultDialogFragment.show(manager, "dialog" );
                }
                else if (jsonAnswer.getString("answerServer").equals("userNotFind"))
                    Toaster.show(getContext(), "Пользователь не существует", false);
                else if (jsonAnswer.getString("answerServer").equals("tokenNotValid"))
                    Toaster.show(getContext(), "Недействительный токен авторизации Firebase", false);
                else if (jsonAnswer.getString("answerServer").equals("Failed"))
                    Toaster.show(getContext(), "Произошла ошибка при обработке запроса", false);

            } catch (Exception e) {
                Toaster.show(getContext(), e.toString(), true);
                e.printStackTrace();
            }

        }).start();
    }

    void saveUserDataInFile(String json) {
        new Thread(() -> {
            FileManager.writeJsonInFile(getContext(), json, fileName);
        }).start();
    }

    private void sendEmailVerified() {
        new Thread(() -> {
            String url = "http://192.168.0.5:5000/api/user/sendemailverified/" + email;
            try {
                JSONObject jsonAnswer = new JSONObject(HttpRequester.getRequest(url));

                if (jsonAnswer.getString("answerServer").equals("Successful")) {
                    Toaster.show(getContext(), "Вам на почту отправлено письмо.", false);
                    cardViewSendMessage.setVisibility(View.INVISIBLE);
                }
                else Toaster.show(getContext(), "Произошла ошибка при обработке запроса.", false);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }).start();
    }
}