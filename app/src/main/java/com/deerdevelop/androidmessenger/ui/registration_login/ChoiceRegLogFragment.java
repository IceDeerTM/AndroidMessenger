package com.deerdevelop.androidmessenger.ui.registration_login;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Button;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import com.deerdevelop.androidmessenger.R;
import com.deerdevelop.androidmessenger.RegistrationLoginActivity;
import com.deerdevelop.androidmessenger.databinding.FragmentChoiceRegLogBinding;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.ui.informationdialog.InfoDialogFragment;
import com.deerdevelop.androidmessenger.ui.login.LoginFragment;
import com.deerdevelop.androidmessenger.ui.registration.RegistrationFragment;
import org.json.JSONObject;

public class ChoiceRegLogFragment extends Fragment {

    private FragmentChoiceRegLogBinding binding;
    private TextView textViewResetPassword;
    private final String URL_RESET_PASSWORD = "http://192.168.0.5:5000/api/user/resetpassword/";
    private InfoDialogFragment infoDialogFragment = new InfoDialogFragment();

    public static ChoiceRegLogFragment newInstance() {
        return new ChoiceRegLogFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChoiceRegLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Activity act = getActivity();
        if (act instanceof RegistrationLoginActivity) {
            ((RegistrationLoginActivity) act).setToolbarState (false);
            act.setTitle("Меню");
        }

        Button buttonChoiceLogin = binding.buttonChoiceLogin;
        Button buttonChoiceRegistration = binding.buttonChoiceRegistration;
        textViewResetPassword = binding.textViewResetPassword;
        textViewResetPassword.setOnClickListener(textViewResetPasswordClicked);

        buttonChoiceLogin.setOnClickListener(view -> {
            LoginFragment loginFragment = new LoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerRL, loginFragment, "LoginFragment")
                    .addToBackStack(null)
                    .commit();

        });

        buttonChoiceRegistration.setOnClickListener(view -> {
            RegistrationFragment registrationFragment = new RegistrationFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerRL, registrationFragment, "RegistrationFragment")
                    .addToBackStack(null)
                    .commit();

        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    View.OnClickListener textViewResetPasswordClicked = v -> {
        try {

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View promptsView = layoutInflater.inflate(R.layout.fragment_input_dialog, null);
            alert.setView(promptsView);

            final AlertDialog alertDialog = alert.show();

            TextView textViewTitle = promptsView.findViewById(R.id.textViewTitle);
            TextView textViewMessage = promptsView.findViewById(R.id.textViewMessage);
            textViewTitle.setText("Сброс пароля");
            textViewMessage.setText("Введите в поле ввода адрес вашей электронной почты:");
            EditText editTextEmail = promptsView.findViewById(R.id.editTextEmail);
            Button buttonOk = promptsView.findViewById(R.id.buttonOk);
            Button buttonCancel = promptsView.findViewById(R.id.buttonCancel);

            buttonOk.setOnClickListener(view -> {
                String email = editTextEmail.getText().toString();

                if (!email.equals("")) {
                    sendResetPassword(email);
                }
                else {

                }

                alertDialog.cancel();
            });

            buttonCancel.setOnClickListener(view -> {
                alertDialog.cancel();
            });

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    };

    private void sendResetPassword(String email) {
        new Thread(() -> {

            FragmentManager manager = getActivity().getSupportFragmentManager();
            String messageInfoDialog;

            try {
                String finalUrl = URL_RESET_PASSWORD + email;
                JSONObject jsonAnswer = new JSONObject(HttpRequester.getRequest(finalUrl));

                if (jsonAnswer.getString("answerServer").equals("Successful")) {
                    messageInfoDialog = "На указанную почту отправлено письмо. Перейди в почтовый сервис" +
                            " и следуйте указаниям";
                }
                else {
                    messageInfoDialog = jsonAnswer.getString("exceptionMessage");
                    if (messageInfoDialog.contains("INVALID_EMAIL"))
                        messageInfoDialog = "Введен некорректный адрес электронной почты.";
                    else if (messageInfoDialog.contains("EMAIL_NOT_FOUND"))
                        messageInfoDialog = "Пользователя с таким email не существует.";
                }

            } catch (Exception e) {
                messageInfoDialog = e.getMessage();
                e.printStackTrace();
            }

            infoDialogFragment.setTextMessage(messageInfoDialog);
            infoDialogFragment.show(manager, "dialog");
        }).start();
    }
}