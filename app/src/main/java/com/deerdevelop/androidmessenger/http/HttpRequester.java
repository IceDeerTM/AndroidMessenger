package com.deerdevelop.androidmessenger.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpRequester {

    public static String postRequest(String url, String json) {

        HttpURLConnection httpURLConnection = getDefaultHttpURLConnection(json, url, "POST");

        return sendRequest(httpURLConnection, json);
    }

    public static String patchRequest(String url, String json) {

        HttpURLConnection httpURLConnection = getDefaultHttpURLConnection(json, url, "PATCH");

        return sendRequest(httpURLConnection, json);
    }

    public static String postRequest(String url, String json, String token) {

        HttpURLConnection httpURLConnection = getDefaultHttpURLConnection(json, url, "POST");
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        String result = sendRequest(httpURLConnection, json);

        return result;
    }

    public static String patchRequest(String url, String json, String token) {

        HttpURLConnection httpURLConnection = getDefaultHttpURLConnection(json, url, "PATCH");
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);

        return sendRequest(httpURLConnection, json);
    }

    public static String getRequest(String url) {

        HttpURLConnection httpURLConnection = getSetupDefaultGetHttp(url);

        return sendGetRequest(httpURLConnection);
    }

    public static String getRequest(String url, String token) {

        HttpURLConnection httpURLConnection = getSetupDefaultGetHttp(url);
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);

        return sendGetRequest(httpURLConnection);
    }

    public static String deleteRequest(String url, String json) {

        HttpURLConnection httpURLConnection = getDefaultHttpURLConnection(json, url, "DELETE");

        return sendRequest(httpURLConnection, json);
    }

    public static String deleteRequest(String url, String json, String token) {

        HttpURLConnection httpURLConnection = getDefaultHttpURLConnection(json, url, "DELETE");
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);

        return sendRequest(httpURLConnection, json);
    }

    public static Bitmap downloadBitmap(String url, String token) {

        HttpURLConnection httpURLConnection = getSetupDefaultGetHttp(url);
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);

        return DownloadBitmapFromServer(httpURLConnection);
    }

    private static Bitmap DownloadBitmapFromServer(HttpURLConnection httpURLConnection) {

        Bitmap bitmapDownload = null;
        try {

            httpURLConnection.setReadTimeout(15000000);
            httpURLConnection.setConnectTimeout(15000000);
            httpURLConnection.connect();

            int code = httpURLConnection.getResponseCode();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String str = "Успешно";
                // все ок
            } else {
                String str = "Ошибка";
                // ошибка
            }

            //Read
            bitmapDownload = BitmapFactory.decodeStream(httpURLConnection.getInputStream());


        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmapDownload;
    }

    private static String sendGetRequest(HttpURLConnection httpURLConnection) {

        String result = "";
        try {

            httpURLConnection.connect();

            int code = httpURLConnection.getResponseCode();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String str = "Успешно";
                // все ок
            } else {
                String str = "Ошибка";
                // ошибка
            }

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static String sendRequest(HttpURLConnection httpURLConnection, String json) {

        String result = null;
        try {
            //Connect
            httpURLConnection.connect();

            //Write
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(json);

            writer.close();
            outputStream.close();

            int code = httpURLConnection.getResponseCode();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String str = "Успешно";
                // все ок
            } else {
                String str = "Ошибка";
                // ошибка
            }

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            result = sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return result;
    }

    public static String sendFile(String url, String token, File selectedFile) {
        HttpURLConnection httpURLConnection = null;
        String result = null;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            FileInputStream fileInputStream = new FileInputStream(selectedFile);
            httpURLConnection = (HttpURLConnection) ((new URL(url).openConnection()));
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setReadTimeout(1500000);
            httpURLConnection.setConnectTimeout(1500000);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpURLConnection.setRequestProperty("service",selectedFile.getName());
            if (!token.equals("")) httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);

            //Connect
            httpURLConnection.connect();

            dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());

            //writing bytes to data outputstream
            dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                    + selectedFile.getName() + "\"" + lineEnd);

            dataOutputStream.writeBytes(lineEnd);

            //returns no. of bytes present in fileInputStream
            bytesAvailable = fileInputStream.available();
            //selecting the buffer size as minimum of available bytes or 1 MB
            bufferSize = Math.min(bytesAvailable,maxBufferSize);
            //setting the buffer as byte array of size of bufferSize
            buffer = new byte[bufferSize];

            //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
            bytesRead = fileInputStream.read(buffer,0,bufferSize);

            //loop repeats till bytesRead = -1, i.e., no bytes are left to read
            while (bytesRead > 0){
                //write the bytes read from inputstream
                dataOutputStream.write(buffer,0,bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                bytesRead = fileInputStream.read(buffer,0,bufferSize);
            }

            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int code = httpURLConnection.getResponseCode();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String str = "Успешно";
                // все ок
            } else {
                String str = "Ошибка";
                // ошибка
            }

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            result = sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return result;
    }

    private static HttpURLConnection getDefaultHttpURLConnection(String data, String url, String typeRequest) {

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) ((new URL(url).openConnection()));
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Content-Length",
                    Integer.toString(data.getBytes(StandardCharsets.UTF_8).length));
            httpURLConnection.setReadTimeout(1500000);
            httpURLConnection.setConnectTimeout(1500000);
            httpURLConnection.setRequestMethod(typeRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpURLConnection;
    }

    private static HttpURLConnection getSetupDefaultGetHttp(String url) {

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) ((new URL(url).openConnection()));
            httpURLConnection.setDoInput(true);
            httpURLConnection.setReadTimeout(1500000);
            httpURLConnection.setConnectTimeout(1500000);
            httpURLConnection.setRequestMethod("GET");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpURLConnection;
    }
}
