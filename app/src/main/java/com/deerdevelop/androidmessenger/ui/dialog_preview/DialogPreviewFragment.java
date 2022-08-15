package com.deerdevelop.androidmessenger.ui.dialog_preview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.*;
import com.deerdevelop.androidmessenger.CallBackListener;
import com.deerdevelop.androidmessenger.adapter.dialog_preview.DialogPreviewAdapter;
import com.deerdevelop.androidmessenger.databinding.FragmentDialogPreviewBinding;
import com.deerdevelop.androidmessenger.guiupdate.GUIFragmentUpdateListener;
import com.deerdevelop.androidmessenger.service.FileManager;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.model.*;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class DialogPreviewFragment extends Fragment implements CallBackListener, GUIFragmentUpdateListener {

    private FragmentDialogPreviewBinding binding;
    private RecyclerView dialogsRecyclerView;
    private DialogPreviewAdapter dialogPreviewAdapter;
    private ArrayList<DialogPreview> dialogPreviews;
    private final String URL_GET_ONLINE = "http://192.168.0.5:5000/api/user/getonlineusers/";
    boolean flag = false;
    boolean destroyView = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialogPreviews = new ArrayList<>();
        binding = FragmentDialogPreviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ((MainActivity)getContext()).setGUIFragmentUpdateListener(this);

        dialogsRecyclerView = binding.dialogsRecyclerView;

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyView = true;
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDialogs(flag);
        flag = false;
    }

    @Override
    public void callBack(Object object) {
        ((MainActivity)getContext()).setStartNextActivity(true);

        String nickName = (String)object;

        Intent intent = new Intent(getActivity(), ActiveDialogActivity.class);
        intent.putExtra("nickName", nickName);
        flag = true;
        startActivity(intent);

    }

    public void loadDialogs(boolean flag) {
        new Thread(() -> {
            dialogPreviews.clear();
            Bitmap bitmap = null;

            String uid = UserData.getUid();
            String path = getContext().getFilesDir().getAbsolutePath() + "/" + uid;
            File directoryUser = new File(path);
            File[] listFileUser = directoryUser.listFiles();

            try {
                for (int i = 0; i < listFileUser.length; i++) {
                    String displayName = "", nickName = listFileUser[i].getName(), dateSend ="", textMessage = "";
                    if (!listFileUser[i].getName().equals("photo.jpg"))
                    {
                        File[] listFileNickName = listFileUser[i].listFiles();
                        for (int j = 0; j < listFileNickName.length; j++) {
                            if (listFileNickName[j].getName().equals("chat")) {
                                File[] listFileChat = listFileNickName[j].listFiles();
                                FileManager.sortFilesByMessageId(listFileChat);
                                if (listFileChat.length > 0) {
                                    JSONObject jsonObject = FileManager.readJsonFromFile(getContext(),
                                            listFileChat[listFileChat.length - 1]);
                                    Gson gson = new Gson();

                                    Message message = gson.fromJson(jsonObject.toString(), Message.class);
                                    dateSend = message.getDateSend();
                                    textMessage = message.getTextMessage();
                                }
                            }
                            else if (listFileNickName[j].getName().equals("displayName.json")) {
                                JSONObject jsonObject = FileManager.readJsonFromFile(getContext(),
                                        listFileNickName[j]);
                                displayName = jsonObject.getString("displayName");
                            }
                            else if (listFileNickName[j].getName().equals("photo.jpg")) {
                                bitmap = FileManager.LoadBitmapFromFile(listFileNickName[j]);
                            }

                        }
                        dialogPreviews.add(new DialogPreview(textMessage, displayName, nickName, dateSend, bitmap));
                    }
                }



                if (flag) {
                    getActivity().runOnUiThread(() -> {
                        dialogPreviewAdapter.notifyDataSetChanged();
                    });
                } else
                    getActivity().runOnUiThread(() -> {
                        setAdapter();
                    });
                getOnlineUsers();
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }

        }).start();
    }

    public void setAdapter() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
                RecyclerView.VERTICAL, false);
        dialogPreviewAdapter = new DialogPreviewAdapter(dialogPreviews, this);

        dialogsRecyclerView.setLayoutManager(layoutManager);
        dialogsRecyclerView.setAdapter(dialogPreviewAdapter);
    }

    private int searchDialogPreviewByNickName(String nickName) {
        for (int i = 0; i < dialogPreviews.size(); i++) {
            if (dialogPreviews.get(i).getNickName().equals(nickName)) return i;
        }
        return -1;
    }

    @Override
    public void updateInterface(JSONObject json) {
        Gson gson = new Gson();
        try {
            String typeData = json.getString("typeData");
            String senderNickName = json.getString("senderNickName");
            String pathToDialog = getContext().getFilesDir() + "/" + UserData.getUid() + "/"
                    + senderNickName + "/";

            if (typeData.equals("incomingMessage") || typeData.equals("changeMessage") ||
                    typeData.equals("deleteMessage")) {
                json.remove("typeData");

                Message message = gson.fromJson(json.toString(), Message.class);

                int index = searchDialogPreviewByNickName(message.getSenderNickName());

                if (index != - 1) {
                    if (typeData.equals("incomingMessage") || typeData.equals("changeMessage")) {
                        dialogPreviews.get(index).setLastMessage(message.getTextMessage());
                        dialogPreviews.get(index).setDateSend(message.getDateSend());
                    }
                    else if (typeData.equals("deleteMessage")) {
                        String dateSend = "", textMessage = "";

                        File file = new File(pathToDialog + "chat/");
                        File[] messages = file.listFiles();

                        if (messages.length > 0) {
                            FileManager.sortFilesByMessageId(messages);
                            JSONObject jsonObject = FileManager.readJsonFromFile(getContext(),
                                    messages[messages.length - 1]);

                            Message lastMessage = gson.fromJson(jsonObject.toString(), Message.class);
                            dateSend = message.getDateSend();
                            textMessage = message.getTextMessage();
                        }
                        dialogPreviews.get(index).setLastMessage(textMessage);
                        dialogPreviews.get(index).setDateSend(dateSend);
                    }
                    if (dialogPreviewAdapter.getItemCount() > index) {
                        getActivity().runOnUiThread(() -> dialogPreviewAdapter.notifyItemChanged(index));
                    }
                }
            }
            else if (typeData.equals("changeDisplayName")) {
                int index = searchDialogPreviewByNickName(senderNickName);
                String displayName = json.getString("displayName");

                if (index != - 1) {
                    dialogPreviews.get(index).setDisplayName(displayName);
                    if (dialogPreviewAdapter.getItemCount() > index) {
                        getActivity().runOnUiThread(() -> dialogPreviewAdapter.notifyItemChanged(index));
                    }
                }
            }
            else if (typeData.equals("changePhoto")) {
                int index = searchDialogPreviewByNickName(senderNickName);
                File file = new File(getContext().getFilesDir() + "/" + UserData.getUid() + "/"
                        + senderNickName + "/photo.jpg");
                Bitmap bitmap = FileManager.LoadBitmapFromFile(file);

                if (index != - 1) {
                    dialogPreviews.get(index).setPhoto(bitmap);
                    if (dialogPreviewAdapter.getItemCount() > index) {
                        getActivity().runOnUiThread(() -> dialogPreviewAdapter.notifyItemChanged(index));
                    }
                }
            }
            else if (typeData.equals("onlineStatus")) {
                String status = json.getString("online");
                boolean onlineStatus = false;

                if (status.equals("true")) onlineStatus = true;

                int index = searchDialogPreviewByNickName(senderNickName);

                if (index != -1) {
                    dialogPreviews.get(index).setOnline(onlineStatus);
                    getActivity().runOnUiThread(() -> dialogPreviewAdapter.notifyItemChanged(index));
                }
            }
            else if (typeData.equals("createDialog")) {
                String dateSend = "", textMessage = "", displayName = json.getString("displayName");
                File file = new File(pathToDialog + "chat/");
                File[] messages = file.listFiles();

                if (messages.length > 0) {
                    FileManager.sortFilesByMessageId(messages);
                    JSONObject jsonObject = FileManager.readJsonFromFile(getContext(),
                            messages[messages.length - 1]);

                    Message message = gson.fromJson(jsonObject.toString(), Message.class);
                    dateSend = message.getDateSend();
                    textMessage = message.getTextMessage();
                }

                file = new File(pathToDialog + "photo.jpg");
                Bitmap bitmap = FileManager.LoadBitmapFromFile(file);

                dialogPreviews.add(new DialogPreview(textMessage, displayName, senderNickName, dateSend, bitmap));

                getActivity().runOnUiThread(() -> {
                    try {
                        dialogPreviewAdapter.notifyItemInserted(dialogPreviewAdapter.getItemCount() - 1);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                });
            }
            else if (typeData.equals("deleteDialog")) {
                int index = searchDialogPreviewByNickName(senderNickName);

                if (index != -1) {
                    dialogPreviews.remove(index);
                    getActivity().runOnUiThread(() -> {
                        dialogPreviewAdapter.notifyItemRemoved(index);
                        dialogsRecyclerView.smoothScrollToPosition(0);
                    });
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void getOnlineUsers() {
        new Thread(() -> {
            try {
                String tokenJWT = StorageToken.getTokenJWT();

                while(tokenJWT.equals("") && destroyView == false && flag == false) {
                    tokenJWT = StorageToken.getTokenJWT();
                }

                String url = URL_GET_ONLINE + UserData.getUid();

                JSONObject jsonObject = new JSONObject(HttpRequester.getRequest(url, tokenJWT));
                if (jsonObject.getString("answerServer").equals("Successful")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("users");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonUser = jsonArray.getJSONObject(i);
                        int index = searchDialogPreviewByNickName(jsonUser.getString("nickName"));
                        if (index != -1) {
                            String onlineStr = jsonUser.getString("onlineStatus");
                            if (onlineStr.equals("true")) dialogPreviews.get(index).setOnline(true);
                        }
                    }
                }
                getActivity().runOnUiThread(() -> dialogPreviewAdapter.notifyDataSetChanged());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}