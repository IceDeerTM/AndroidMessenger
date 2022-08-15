package com.deerdevelop.androidmessenger.ui.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.ActiveDialogActivity;
import com.deerdevelop.androidmessenger.guiupdate.GUIFragmentUpdateListener;
import com.deerdevelop.androidmessenger.CallBackLongClickListener;
import com.deerdevelop.androidmessenger.adapter.active_dialog.MessageAdapter;
import com.deerdevelop.androidmessenger.databinding.FragmentChatBinding;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.service.FileManager;
import com.deerdevelop.androidmessenger.model.*;
import com.deerdevelop.androidmessenger.ui.informationdialog.InfoDialogFragment;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatFragment extends Fragment implements GUIFragmentUpdateListener, CallBackLongClickListener {

    private ArrayList<MessageAdapterType> messages;
    private RecyclerView recyclerViewMessage;
    private MessageAdapter messageAdapter;
    private String nickNameInterlocutor, displayNameInterlocutor = "", pathFileChat = "";
    private final String URL_DIALOG = "http://192.168.0.5:5000/api/dialog/";
    private final String URL_SEND_MESSAGE = "http://192.168.0.5:5000/api/message/sendmessage/";
    private final String URL_CHANGE_MESSAGE = "http://192.168.0.5:5000/api/message/changemessage/";
    private final String URL_DELETE_MESSAGE = "http://192.168.0.5:5000/api/message/deletemessage/";
    private boolean changeTextActivated = false, isTapMessage = false;
    private ImageButton buttonSend;
    private EditText inputText;
    private MessageAdapterType clickedMessage;
    private boolean backAction = false;
    private InfoDialogFragment infoDialogFragment = new InfoDialogFragment();
    private String messageForDialogFragment = "";
    private FragmentManager fragmentManager = null;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentChatBinding binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ((ActiveDialogActivity)getContext()).setGUIFragmentUpdateListener(this);

        recyclerViewMessage = binding.recyclerViewMessage;
        fragmentManager = getActivity().getSupportFragmentManager();
        messages = new ArrayList<>();

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            nickNameInterlocutor = extras.getString("nickName");
            pathFileChat = getContext().getFilesDir().getAbsolutePath() + "/" + UserData.getUid() + "/"
                    + nickNameInterlocutor + "/chat";

            checkFirebaseUser();
            File file = new File(getContext().getFilesDir().getAbsolutePath() + "/" + UserData.getUid() + "/"
                    + nickNameInterlocutor + "/displayName.json");
            JSONObject jsonObject = FileManager.readJsonFromFile(getContext(), file);
            if (!nickNameInterlocutor.equals("Selected")) sendGetOnlineStatus();
            try {
                displayNameInterlocutor = jsonObject.getString("displayName");
                ((ActiveDialogActivity)getContext()).setTitle(displayNameInterlocutor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        buttonSend = binding.buttonSend;
        inputText = binding.inputText;

        buttonSend.setOnClickListener(view -> {
            String text = inputText.getText().toString();
            if (!text.equals("") && !text.equals("  ")) {
                if (changeTextActivated) {
                    clickedMessage.setTextMessage(text);
                    sendChangeMessage(clickedMessage);
                    inputText.setText("");
                    changeTextActivated = false;
                    isTapMessage = false;
                    clickedMessage = null;
                    ((ActiveDialogActivity)getActivity()).setToolbarDialogActivity(0);
                }
                else {
                    sendMessage(text);
                    inputText.setText("");
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyThread", "Я вышель");
        backAction = true;
    }

    private void sendChangeMessage(MessageAdapterType messageAdapterType) {
        if (messageAdapterType.getTextMessage().length() > 0 && !messageAdapterType.getTextMessage().equals("")) {
            new Thread(() -> {
                try {
                    Gson gson = new Gson();

                    String jsonMessage = gson.toJson(messageAdapterType);
                    JSONObject newMessageJSON = new JSONObject(jsonMessage);
                    newMessageJSON.put("senderToken", StorageToken.getTokenDevice());

                    JSONObject jsonAnswerServer = new JSONObject(HttpRequester.patchRequest(URL_CHANGE_MESSAGE,
                            newMessageJSON.toString(), StorageToken.getTokenJWT()));

                    if (jsonAnswerServer.getString("answerServer").equals("Successful")) {
                        jsonAnswerServer.remove("answerServer");

                        changeMessage(messageAdapterType);

                        File newFileMessage = new File(pathFileChat + "/" + messageAdapterType.getMessageId() + ".json");
                        FileManager.writeJsonInFile(getContext(), jsonAnswerServer.toString(), newFileMessage);
                    }
                    else {
                        messageForDialogFragment = "Произошла ошибка при обработке запроса";
                        infoDialogFragment.setTextMessage(messageForDialogFragment);
                        infoDialogFragment.show(fragmentManager, "dialogFragment");
                    }

                } catch (Exception e) {
                    messageForDialogFragment = e.getMessage();
                    infoDialogFragment.setTextMessage(messageForDialogFragment);
                    infoDialogFragment.show(fragmentManager, "dialogFragment");
                    e.printStackTrace();
                }

            }).start();
        }
    }

    private void sendMessage(String textMessage) {
        new Thread(() -> {
            try {
                Gson gson = new Gson();
                MessageUser newMessageUser = new MessageUser(textMessage, UserData.getNickName(), nickNameInterlocutor);

                String jsonMessage = gson.toJson(newMessageUser);
                JSONObject newMessageJSON = new JSONObject(jsonMessage);
                newMessageJSON.put("senderToken", StorageToken.getTokenDevice());

                JSONObject jsonAnswerServer = new JSONObject(HttpRequester.postRequest(URL_SEND_MESSAGE,
                        newMessageJSON.toString(), StorageToken.getTokenJWT()));

                if (jsonAnswerServer.getString("answerServer").equals("Successful")) {
                    jsonAnswerServer.remove("answerServer");

                    newMessageUser = gson.fromJson(jsonAnswerServer.toString(), MessageUser.class);

                    insertMessage(newMessageUser);

                    File newFileMessage = new File(pathFileChat + "/" + newMessageUser.getMessageId() + ".json");
                    FileManager.writeJsonInFile(getContext(), jsonAnswerServer.toString(), newFileMessage);
                }
                else {
                    messageForDialogFragment = "Произошла ошибка при обработке запроса";
                    infoDialogFragment.setTextMessage(messageForDialogFragment);
                    infoDialogFragment.show(fragmentManager, "dialogFragment");
                }

            } catch (Exception e) {
                messageForDialogFragment = e.getMessage();
                infoDialogFragment.setTextMessage(messageForDialogFragment);
                infoDialogFragment.show(fragmentManager, "dialogFragment");
                e.printStackTrace();
            }

        }).start();
    }

    private void sendDeleteMessage(MessageAdapterType messageAdapterType) {
        new Thread(() -> {
            try {
                Gson gson = new Gson();

                String jsonMessage = gson.toJson(messageAdapterType);
                JSONObject newMessageJSON = new JSONObject(jsonMessage);
                newMessageJSON.put("senderToken", StorageToken.getTokenDevice());

                JSONObject jsonAnswerServer = new JSONObject(HttpRequester.deleteRequest(URL_DELETE_MESSAGE,
                        newMessageJSON.toString(), StorageToken.getTokenJWT()));

                if (jsonAnswerServer.getString("answerServer").equals("Successful")) {
                    jsonAnswerServer.remove("answerServer");

                    deleteMessage(messageAdapterType);

                    File fileMessage = new File(pathFileChat + "/" + messageAdapterType.getMessageId()
                            + ".json");
                    fileMessage.delete();
                }
                else {
                    messageForDialogFragment = "Произошла ошибка при обработке запроса";
                    infoDialogFragment.setTextMessage(messageForDialogFragment);
                    infoDialogFragment.show(fragmentManager, "dialogFragment");
                }

            } catch (Exception e) {
                messageForDialogFragment = e.getMessage();
                infoDialogFragment.setTextMessage(messageForDialogFragment);
                infoDialogFragment.show(fragmentManager, "dialogFragment");
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * При вызове данный метод загружает из директории диалога весь список сообщений, в конце вызывает setAdapter()
     */
    private void loadMessagesFromDevice() {
        try {
            File chatDirectory = new File(pathFileChat);
            File[] messageFiles = chatDirectory.listFiles();

            for (File messageFile : messageFiles) {
                JSONObject jsonMessage = FileManager.readJsonFromFile(getContext(), messageFile);
                Gson gson = new Gson();
                Message message = gson.fromJson(jsonMessage.toString(), Message.class);
                MessageAdapterType messageType;

                if (message.getSenderNickName().equals(UserData.getNickName())) {
                    messageType = new MessageUser(message);
                } else {
                    messageType = new MessageInterlocutor(message);
                }
                messages.add(messageType);
            }
            getActivity().runOnUiThread( () -> {
                setAdapter();
            });
        }
        catch (Exception e) {
            messageForDialogFragment = e.getMessage();
            infoDialogFragment.setTextMessage(messageForDialogFragment);
            infoDialogFragment.show(fragmentManager, "dialogFragment");
        }
    }

    /**
     * Метод для проверки UserData.getFirebaseUser() на null, в случае если он отсутсвует, то загружаются сообщения
     * из данных приложения и запускается цикл, который не закроется пока не выполнится условие проверки на null,
     * потом же идет проверка сообщений чата на актуальность с бекапом на сервере. ВАЖНО, метод выполняется
     * в другом потоке.
     */
    private void checkFirebaseUser() {
        new Thread(() -> {
            AtomicBoolean isLoadedToRecyclerView = new AtomicBoolean(false);

            if (UserData.getFirebaseUser() == null || StorageToken.getTokenJWT().equals("")) {

                loadMessagesFromDevice();
                isLoadedToRecyclerView.set(true);
                String token = StorageToken.getTokenJWT();
                while ((UserData.getFirebaseUser() == null || token.equals("")) && backAction == false) {
                    token = StorageToken.getTokenJWT();
                }
                if (backAction) {
                    Log.d("MyThread", "Поток закрыт");
                    return;
                }
            }

            checkChatForRelevance(isLoadedToRecyclerView);

        }).start();
    }

    /**
     * Данный метод проверяет файлы данных прилложения, где хранятся сообщения чата, а также при необходимости
     * скачивает их с сервера.
     * @param isLoadedToRecyclerView был ли до этого вызван метол loadMessagesFromDevice
     */
    private void checkChatForRelevance(AtomicBoolean isLoadedToRecyclerView) {

        ArrayList<MessageAdapterType> newMessages = new ArrayList<>();
        try {

            File chatDirectory = new File(pathFileChat);
            File[] messageFiles = chatDirectory.listFiles();

            FileManager.sortFilesByMessageId(messageFiles);

            if (messageFiles.length > 0) {
                if (!messageFiles[0].getName().equals("message1.json")) {
                    newMessages = downloadChatFromServer();
                }
                else {
                    String messageId = messageFiles[messageFiles.length - 1].getName();
                    String endURL = URL_DIALOG + "checkactualmessage/" + UserData.getUid() + "/" + nickNameInterlocutor + "/"
                            + messageId;
                    JSONObject jsonAnswer = new JSONObject(HttpRequester.getRequest(endURL, StorageToken.getTokenJWT()));

                    if (jsonAnswer.getString("updateDialog").equals("Yes")) newMessages = downloadChatFromServer();
                    else if (!isLoadedToRecyclerView.get())
                    {
                        loadMessagesFromDevice();
                        return;
                    }
                }
                messages.clear();
                messages.addAll(newMessages);

                if (isLoadedToRecyclerView.get()) {

                    getActivity().runOnUiThread(() -> {
                        messageAdapter.notifyDataSetChanged();
                        recyclerViewMessage.smoothScrollToPosition(recyclerViewMessage.getAdapter().getItemCount() - 1);
                    });
                }
                else {
                    getActivity().runOnUiThread(() -> setAdapter());
                }
            }
            else getActivity().runOnUiThread(() -> {
                setAdapter();
            });

        } catch (Exception e) {
            messageForDialogFragment = e.getMessage();
            infoDialogFragment.setTextMessage(messageForDialogFragment);
            infoDialogFragment.show(fragmentManager, "dialogFragment");
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private ArrayList<MessageAdapterType> downloadChatFromServer() {
        ArrayList<MessageAdapterType> newMessages = new ArrayList<>();

        try {
            String endURL = URL_DIALOG + "getfulldialog/" + UserData.getUid() + "/" + nickNameInterlocutor;
            JSONObject jsonMessages = new JSONObject(HttpRequester.getRequest(endURL, StorageToken.getTokenJWT()));

            JSONArray jsonArrayMessages = jsonMessages.getJSONArray("messages");
            Gson gson = new Gson();

            for (int i = 0; i < jsonArrayMessages.length(); i++) {
                JSONObject jsonObject = jsonArrayMessages.getJSONObject(i);
                Message message = gson.fromJson(jsonObject.toString(), Message.class);
                MessageAdapterType messageType;

                if (message.getSenderNickName().equals(UserData.getNickName())) {
                    messageType = new MessageUser(message);
                }
                else {
                    messageType = new MessageInterlocutor(message);
                }
                newMessages.add(messageType);
            }
            saveMessages(jsonArrayMessages);
        }
        catch (Exception exc) {
            messageForDialogFragment = exc.getMessage();
            infoDialogFragment.setTextMessage(messageForDialogFragment);
            infoDialogFragment.show(fragmentManager, "dialogFragment");
            exc.printStackTrace();
        }
        return newMessages;
    }

    private void saveMessages(JSONArray jsonArray) {
        new Thread(() -> {

            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonMessage = jsonArray.getJSONObject(i);
                    String path = pathFileChat + "/" + jsonMessage.getString("messageId") + ".json";

                    File fileMessage = new File(path);

                    FileManager.writeJsonInFile(getContext(), jsonMessage.toString(), fileMessage);
                }
            } catch (Exception e) {
                messageForDialogFragment = e.getMessage();
                infoDialogFragment.setTextMessage(messageForDialogFragment);
                infoDialogFragment.show(fragmentManager, "dialogFragment");
                e.printStackTrace();
            }


        }).start();
    }

    private void setAdapter() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false);
        recyclerViewMessage.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(messages, this);

        recyclerViewMessage.setAdapter(messageAdapter);
        if (recyclerViewMessage.getAdapter().getItemCount() > 0)
            recyclerViewMessage.smoothScrollToPosition(recyclerViewMessage.getAdapter().getItemCount() - 1);

    }

    private int searchMessageByMessageId(String clickedMessageId) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageId().equals(clickedMessageId)) {
                return i;
            }

        }
        return -1;
    }

    @Override
    public void updateInterface(JSONObject json) {
        Gson gson = new Gson();
        try {
            String senderNickName = json.getString("senderNickName");
            String typeData = json.getString("typeData");
            json.remove("typeData");

            if (typeData.equals("incomingMessage") || typeData.equals("changeMessage") || typeData.equals("deleteMessage")) {
                MessageAdapterType messageAdapterType = null;

                if (json.getString("senderNickName").equals(nickNameInterlocutor))
                    messageAdapterType = gson.fromJson(json.toString(), MessageInterlocutor.class);
                else if (json.getString("senderNickName").equals(UserData.getNickName())) {
                    messageAdapterType = gson.fromJson(json.toString(), MessageUser.class);
                }

                if (messageAdapterType != null) {
                    if (typeData.equals("incomingMessage")) {
                        insertMessage(messageAdapterType);
                    }
                    else if (typeData.equals("changeMessage")) {
                        changeMessage(messageAdapterType);
                    }
                    else if (typeData.equals("deleteMessage")) {
                        deleteMessage(messageAdapterType);
                    }
                }
            }
            else if (typeData.equals("changeDisplayName")) {
                if (senderNickName.equals(nickNameInterlocutor)) {
                    String displayName = json.getString("displayName");
                    getActivity().runOnUiThread(() -> {
                        ((ActiveDialogActivity)getContext()).setTitle(displayName);
                    });

                }
            }
            else if (typeData.equals("onlineStatus")) {
                if (senderNickName.equals(nickNameInterlocutor)) {
                    String online = json.getString("online");
                    getActivity().runOnUiThread(() -> {
                        if (online.equals("true")) ((ActiveDialogActivity)getContext()).setSubTitle("В сети");
                        else ((ActiveDialogActivity)getContext()).setSubTitle("Не в сети");
                    });

                }
            }

        } catch (Exception e) {
            messageForDialogFragment = e.getMessage();
            infoDialogFragment.setTextMessage(messageForDialogFragment);
            infoDialogFragment.show(fragmentManager, "dialogFragment");
            e.printStackTrace();
        }
    }

    private void insertMessage(MessageAdapterType messageAdapterType) {
        messages.add(messageAdapterType);
        getActivity().runOnUiThread(() -> {
            messageAdapter.notifyItemInserted(messageAdapter.getItemCount() - 1);
            recyclerViewMessage.smoothScrollToPosition(recyclerViewMessage.getAdapter().getItemCount() - 1);
        });

    }

    private void changeMessage(MessageAdapterType messageAdapterType) {
        int index = searchMessageByMessageId(messageAdapterType.getMessageId());
        if (index != -1) {
            messages.get(index).setTextMessage(messageAdapterType.getTextMessage());
            getActivity().runOnUiThread(() -> {
                try {
                    messageAdapter.notifyItemChanged(index);
                    recyclerViewMessage.smoothScrollToPosition(recyclerViewMessage.getAdapter().getItemCount() - 1);
                } catch (Exception e) {
                    messageForDialogFragment = e.getMessage();
                    infoDialogFragment.setTextMessage(messageForDialogFragment);
                    infoDialogFragment.show(fragmentManager, "dialogFragment");
                }
            });
        }
    }

    private void deleteMessage(MessageAdapterType messageAdapterType) {
        int index = searchMessageByMessageId(messageAdapterType.getMessageId());
        if (index != -1) {
            messages.remove(index);
            getActivity().runOnUiThread(() -> {
                try {
                    messageAdapter.notifyItemRemoved(index);
                    if (recyclerViewMessage.getAdapter().getItemCount() > 0)
                        recyclerViewMessage.smoothScrollToPosition(recyclerViewMessage.getAdapter().getItemCount() - 1);

                } catch (Exception e) {
                    messageAdapter.notifyDataSetChanged();
                    if (recyclerViewMessage.getAdapter().getItemCount() > 0)
                        recyclerViewMessage.smoothScrollToPosition(recyclerViewMessage.getAdapter().getItemCount() - 1);
                }
            });
        }
    }

    public void changeMessageOption() {
        if (clickedMessage != null) {
            changeTextActivated = true;
            inputText.setText(clickedMessage.getTextMessage());
        }
    }

    public void deleteMessageOption() {
        if (clickedMessage != null) {
            sendDeleteMessage(clickedMessage);
            isTapMessage = false;
            ((ActiveDialogActivity)getActivity()).setToolbarDialogActivity(0);
            clickedMessage = null;
        }
    }

    public void copyMessageOption() {

        if (clickedMessage != null) {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("copy", clickedMessage.getTextMessage());
            clipboard.setPrimaryClip(clip);
            clickedMessage = null;
            isTapMessage = false;
            ((ActiveDialogActivity)getActivity()).setToolbarDialogActivity(0);
        }
    }

    /**
     * Данный метод является callback-ом из класса ViewHolder, вызывыается при долгом нажатии на сообщение
     * @param messageId id передаваемого сообще
     */
    @Override
    public void onLongClickMessage(String messageId) {
        int displayMode = 0;
        if (isTapMessage) {
            clickedMessage = null;
            changeTextActivated = false;
        }
        else {
            int index = searchMessageByMessageId(messageId);
            if (index != -1) {
                clickedMessage = messages.get(index);
                if (clickedMessage instanceof MessageUser)
                    displayMode = 1;
                else displayMode = 2;
            }
        }

        isTapMessage = !isTapMessage;
        ((ActiveDialogActivity)getActivity()).setToolbarDialogActivity(displayMode);
    }

    private void sendGetOnlineStatus() {
        new Thread(() -> {
            String url = "http://192.168.0.5:5000/api/user/getonlinestatususer/" + nickNameInterlocutor;
            try {
                JSONObject jsonAnswer = new JSONObject(HttpRequester.getRequest(url, StorageToken.getTokenJWT()));
                if (jsonAnswer.getString("answerServer").equals("Successful")) {
                    jsonAnswer.remove("answerServer");
                    String online = jsonAnswer.getString("onlineStatus");
                    if (online.equals("true")) ((ActiveDialogActivity)getContext()).setSubTitle("В сети");
                    else ((ActiveDialogActivity)getContext()).setSubTitle("Не в сети");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}