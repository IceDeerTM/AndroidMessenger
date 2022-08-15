package com.deerdevelop.androidmessenger.service;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.deerdevelop.androidmessenger.guiupdate.GUIActivityUpdateListener;
import com.deerdevelop.androidmessenger.http.HttpRequester;
import com.deerdevelop.androidmessenger.model.Message;
import com.deerdevelop.androidmessenger.model.StorageToken;
import com.deerdevelop.androidmessenger.model.UserData;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BackgroundWorker extends Worker {

    private static com.deerdevelop.androidmessenger.guiupdate.GUIActivityUpdateListener guiActivityUpdateListener;
    private final String pathToUserData = getApplicationContext().getFilesDir().getAbsolutePath() + "/" +
            UserData.getUid() + "/";
    private String URL_GET_PHOTO = "http://192.168.0.5:5000/api/user/getphoto/";

    public static void setUpdatedListener(GUIActivityUpdateListener guiActivityUpdateListener) {
        BackgroundWorker.guiActivityUpdateListener = guiActivityUpdateListener;
    }

    private static final String TAG = "BackgroundWorker";

    public BackgroundWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = this.getInputData();
        Map<String, Object> hashMapObject = data.getKeyValueMap();
        HashMap<String, String> hashMap = new HashMap<>();

        Gson gson = new Gson();

        for (HashMap.Entry<String, Object> entry: hashMapObject.entrySet()) {

            hashMap.put(entry.getKey(), (String)entry.getValue());
        }
        JSONObject jsonObject = new JSONObject();
        //(String messageId, String dateSend, String textMessage, String senderNickName, String receiverNickName)
        String typeData = hashMap.get("typeData");
        String senderNickName = hashMap.get("senderNickName");

        if (typeData.equals("incomingMessage") || typeData.equals("deleteMessage") || typeData.equals("changeMessage")) {

            Message message = new Message(hashMap.get("messageId"), hashMap.get("dateSend"), hashMap.get("textMessage"),
                    hashMap.get("senderNickName"), hashMap.get("receiverNickName"));
            try {

                jsonObject = new JSONObject(gson.toJson(message));

                if (guiActivityUpdateListener != null) {
                    jsonObject.put("typeData", typeData);
                    guiActivityUpdateListener.updateInterface(jsonObject);
                    jsonObject.remove("typeData");
                }

                File file = new File(pathToUserData + message.getSenderNickName() + "/" + "chat" + "/"
                        + message.getMessageId() + ".json");

                if (!typeData.equals("deleteMessage")) {
                    file.createNewFile();
                    FileManager.writeJsonInFile(getApplicationContext(), jsonObject.toString(), file);
                }
                else file.delete();

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
        else if (typeData.equals("createDialog") || typeData.equals("deleteDialog")) {
            try {
                String displayName = hashMap.get("displayName");
                if (typeData.equals("createDialog")) {

                    File file = new File(pathToUserData + senderNickName + "/" + "chat");
                    file.mkdirs();
                    file = new File(pathToUserData + senderNickName + "/displayName.json");
                    file.createNewFile();

                    Bitmap newPhotoBitmap = HttpRequester.downloadBitmap(URL_GET_PHOTO + senderNickName,
                            StorageToken.getTokenJWT());
                    String pathToFile = pathToUserData + senderNickName + "/";
                    FileManager.SaveBitmapInFile(newPhotoBitmap, pathToFile, "photo.jpg");

                    jsonObject.put("displayName", displayName);
                    FileManager.writeJsonInFile(getApplicationContext(), jsonObject.toString(), file);
                }
                else {
                    File file = new File(pathToUserData + senderNickName);
                    file.delete();
                }

                if (guiActivityUpdateListener != null) {
                    jsonObject.put("typeData", typeData);
                    jsonObject.put("senderNickName", senderNickName);
                    jsonObject.put("displayName", displayName);
                    guiActivityUpdateListener.updateInterface(jsonObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (typeData.equals("onlineStatus")) {
            try {
                if (guiActivityUpdateListener != null) {
                    jsonObject.put("typeData", typeData);
                    jsonObject.put("online", hashMap.get("online"));
                    jsonObject.put("senderNickName", senderNickName);
                    guiActivityUpdateListener.updateInterface(jsonObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (typeData.equals("changePhoto")) {
            Bitmap newPhotoBitmap = HttpRequester.downloadBitmap(URL_GET_PHOTO + senderNickName,
                    StorageToken.getTokenJWT());
            String pathToFile = pathToUserData;

            if (!senderNickName.equals(UserData.getNickName())) {
                pathToFile += senderNickName + "/";
            }
            FileManager.SaveBitmapInFile(newPhotoBitmap, pathToFile, "photo.jpg");

            UserData.setPhoto(newPhotoBitmap);

            if (guiActivityUpdateListener != null) {
                try {
                    jsonObject.put("typeData", typeData);
                    jsonObject.put("senderNickName", senderNickName);
                    guiActivityUpdateListener.updateInterface(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (typeData.equals("changeDisplayName")) {
            String displayName = hashMap.get("displayName");
            if (senderNickName.equals(UserData.getNickName())) {
                changeUserDisplayName("displayName", displayName);
                UserData.setDisplayName(displayName);
            }
            else {
                try {
                    String path = pathToUserData + "/" + senderNickName + "/" + "displayName.json";
                    File file = new File(path);
                    JSONObject jsonFile = FileManager.readJsonFromFile(getApplicationContext(), file);
                    jsonFile.remove("displayName");
                    jsonFile.put("displayName", displayName);
                    FileManager.writeJsonInFile(getApplicationContext(), jsonFile.toString(), file);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (guiActivityUpdateListener != null) {
                try {
                    jsonObject.put("typeData", typeData);
                    jsonObject.put("displayName", displayName);
                    jsonObject.put("senderNickName", senderNickName);
                    guiActivityUpdateListener.updateInterface(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (typeData.equals("closeSessions")) {
            String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "userdata.json";
            File file = new File(path);
            file.delete();

            if (guiActivityUpdateListener != null) {
                try {
                    jsonObject.put("typeData", typeData);
                    jsonObject.put("senderNickName", senderNickName);
                    guiActivityUpdateListener.updateInterface(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }

        return Result.success();
    }

    private void changeUserDisplayName(String nameField, String field) {
        try {
            String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "userdata.json";

            File file = new File(path);
            JSONObject jsonFile = FileManager.readJsonFromFile(getApplicationContext(), file);
            jsonFile.remove(nameField);
            jsonFile.put(nameField, field);
            FileManager.writeJsonInFile(getApplicationContext(), jsonFile.toString(), file);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
