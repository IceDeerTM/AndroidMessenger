package com.deerdevelop.androidmessenger.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;


public class FileManager {

    public static Bitmap LoadBitmapFromFile(File file) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static void SaveBitmapInFile(Bitmap bitmap, String path, String child) {
        OutputStream out = null;
        try {
            File file = new File(path, child); file.createNewFile();
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sortFilesByMessageId(File[] messageFiles) {
        Arrays.sort(messageFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                String nameFile1 = f1.getName().replace("message", "");
                String nameFile2 = f2.getName().replace("message", "");
                int idFile1 = Integer.parseInt(nameFile1.replace(".json", ""));
                int idFile2 = Integer.parseInt(nameFile2.replace(".json", ""));
                return Integer.compare(idFile1, idFile2);
            }
        });
    }

    public static void writeJsonInFile(Context context, String jsonString, String FILE_NAME) {

        FileOutputStream fos = null;

        try {

            fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            //Toaster.show(context, "Файл сохранен", true);
        }
        catch(Exception ex) {
            Toaster.show(context, ex.getMessage(), true);
            //Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally{
            try{
                if(fos!=null)
                    fos.close();
            }
            catch(IOException ex){
                Toaster.show(context, ex.getMessage(), true);
                //Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void writeJsonInFile(Context context, String jsonString, File file) {

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            fos.write(jsonString.getBytes());
            //Toaster.show(context, "Файл сохранен", true);
        }
        catch(Exception ex) {
            Toaster.show(context, ex.getMessage(), true);
            //Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally{
            try{
                if(fos!=null)
                    fos.close();
            }
            catch(IOException ex){
                Toaster.show(context, ex.getMessage(), true);
                //Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static JSONObject readJsonFromFile(Context context, String FILE_NAME){

        FileInputStream fin = null;
        JSONObject jsonObject = null;

        if (isFilePresent(context, FILE_NAME)) {
            try {
                fin = context.openFileInput(FILE_NAME);
                byte[] bytes = new byte[fin.available()];
                fin.read(bytes);
                String json = new String (bytes);

                jsonObject = new JSONObject(json);

            }
            catch(IOException | JSONException ex) {
                Toaster.show(context, ex.getMessage(), true);
            }
            finally{

                try{
                    if(fin!=null)
                        fin.close();
                }
                catch(IOException ex){
                    Toaster.show(context, ex.getMessage(), true);
                }
            }
        }

        return jsonObject;
    }

    public static JSONObject readJsonFromFile(Context context, File file){

        FileInputStream fin = null;
        JSONObject jsonObject = null;

        if (isFilePresent(file)) {
            try {
                fin = new FileInputStream(file);
                byte[] bytes = new byte[fin.available()];
                fin.read(bytes);
                String json = new String (bytes);

                jsonObject = new JSONObject(json);

            }
            catch(IOException | JSONException ex) {
                Toaster.show(context, ex.getMessage(), true);
            }
            finally{

                try{
                    if(fin!=null)
                        fin.close();
                }
                catch(IOException ex){
                    Toaster.show(context, ex.getMessage(), true);
                }
            }
        }

        return jsonObject;
    }

    public static boolean isFilePresent(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }

    public static boolean isFilePresent(File file) {
        return file.exists();
    }

}