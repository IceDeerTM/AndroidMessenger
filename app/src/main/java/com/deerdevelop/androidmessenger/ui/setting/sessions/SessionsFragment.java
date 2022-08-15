package com.deerdevelop.androidmessenger.ui.setting.sessions;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.MainActivity;
import com.deerdevelop.androidmessenger.adapter.sessions.SessionAdapter;
import com.deerdevelop.androidmessenger.databinding.FragmentSessionsBinding;
import com.deerdevelop.androidmessenger.guiupdate.GUIFragmentUpdateListener;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.model.Session;
import com.deerdevelop.androidmessenger.model.StorageToken;
import com.deerdevelop.androidmessenger.model.UserData;
import com.deerdevelop.androidmessenger.ui.informationdialog.InfoDialogFragment;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SessionsFragment extends Fragment implements GUIFragmentUpdateListener {

    private ArrayList<Session> sessions;
    private RecyclerView recyclerViewSessions;
    private SessionAdapter sessionAdapter;
    private CardView cardViewCloseSessions;
    private TextView textViewCurrentDevice;
    private final String URL_GET_SESSIONS = "http://192.168.0.5:5000/api/user/getsessions/";
    private final String URL_CLOSE_SESSIONS = "http://192.168.0.5:5000/api/user/closesessions/";
    private boolean destroyView = false;
    private InfoDialogFragment infoDialogFragment = new InfoDialogFragment();
    private FragmentManager fragmentManager = null;
    private String messageForInfoDialog = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentSessionsBinding binding = FragmentSessionsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fragmentManager = getActivity().getSupportFragmentManager();
        ((MainActivity)getContext()).setGUIFragmentUpdateListener(this);

        sessions = new ArrayList<>();

        recyclerViewSessions = binding.recyclerViewSessions;
        textViewCurrentDevice = binding.textViewCurrentDevice;
        cardViewCloseSessions = binding.cardViewCloseSessions;

        cardViewCloseSessions.setOnClickListener(cardViewCloseSessionsClick);
        getSessions(false);

        return root;
    }

    View.OnClickListener cardViewCloseSessionsClick = v -> {
        sendRequestCloseSessions();
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyView = true;
    }

    @Override
    public void updateInterface(JSONObject json) {

    }

    private void getSessions(boolean flag) {
        new Thread(() -> {

            String tokenJWT = StorageToken.getTokenJWT();

            while (tokenJWT.equals("") && destroyView == false) {
                tokenJWT = StorageToken.getTokenJWT();
            }

            String finalUrl = URL_GET_SESSIONS + UserData.getUid();

            try {
                Gson gson = new Gson();
                JSONObject jsonAnswer = new JSONObject(HttpRequester.getRequest(finalUrl, tokenJWT));

                if (jsonAnswer.getString("answerServer").equals("Successful")) {
                    JSONArray jsonSessions = jsonAnswer.getJSONArray("sessions");
                    sessions.clear();

                    for (int i = 0; i < jsonSessions.length(); i++) {
                        Session session = gson.fromJson(jsonSessions.getJSONObject(i).toString(), Session.class);

                        if (session.getTokenOfDevice().equals(StorageToken.getTokenDevice())) {
                            getActivity().runOnUiThread(() -> textViewCurrentDevice.setText(session.getDeviceInfo()));
                        }
                        else {
                            sessions.add(session);
                        }
                    }
                    if (!flag) getActivity().runOnUiThread(() -> setAdapter());
                    else getActivity().runOnUiThread(() -> sessionAdapter.notifyDataSetChanged());

                }
                else Toaster.show(getContext(), "Произошла ошибка при отправке запроса", false);
            }
            catch (Exception e) {
                messageForInfoDialog = e.getMessage();
                infoDialogFragment.setTextMessage(messageForInfoDialog);
                infoDialogFragment.show(fragmentManager, "dialogFragment");
            }

        }).start();
    }

    private void setAdapter() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false);

        sessionAdapter = new SessionAdapter(sessions);

        recyclerViewSessions.setLayoutManager(layoutManager);
        recyclerViewSessions.setAdapter(sessionAdapter);
    }

    private void sendRequestCloseSessions() {
        if (sessions.size() > 0) {
            new Thread(() -> {

                try {
                    String finalUrl = URL_CLOSE_SESSIONS + UserData.getUid() + "/" + StorageToken.getTokenDevice();
                    JSONObject jsonAnswer = new JSONObject(HttpRequester.deleteRequest(finalUrl, "",
                            StorageToken.getTokenJWT()));
                    if (jsonAnswer.getString("answerServer").equals("Successful")) {
                        Toaster.show(getContext(), "Успешно",false);
                        getSessions(true);
                    }
                    else Toaster.show(getContext(), "Произошла ошибка на сервере",false);
                } catch (Exception e) {
                    messageForInfoDialog = e.getMessage();
                    infoDialogFragment.setTextMessage(messageForInfoDialog);
                    infoDialogFragment.show(fragmentManager, "dialogFragment");
                    e.printStackTrace();
                }
            }).start();
        }
        else Toaster.show(getContext(), "Нет других активных сеансов.", false);
    }
}