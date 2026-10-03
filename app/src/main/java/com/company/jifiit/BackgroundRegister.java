package com.company.jifiit;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class BackgroundRegister extends AsyncTask<String, Void, String>{

    private final Context context;
    private AlertDialog alertDialog;

    BackgroundRegister(Context ctx){
        context = ctx;
}
    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Registration Status");
    }
    @Override
    protected String doInBackground(String... params) {
        String register_url = "https://192.168.235.120/Jifiit/Therapist/therapistRegister.php";
        String type = params[0];
        StringBuilder result = new StringBuilder();

        if (type.equals("register")) {
            try {
                String name = params[1];
                String email = params[2];
                String mobile = params[3];
                String gender = params[4];
                String age  = params[5];
                String address=params[6];
//                String photo = params[7];
//                String idProof = params[8];
//                String pan = params[9];

                URL url = new URL(register_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String post_data =
                        URLEncoder.encode("t_Name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8") + "&" +
                                URLEncoder.encode("t_Email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                                URLEncoder.encode("t_Mobile", "UTF-8") + "=" + URLEncoder.encode(mobile, "UTF-8") + "&" +
                                URLEncoder.encode("t_gender", "UTF-8") + "=" + URLEncoder.encode(gender, "UTF-8") + "&" +
                                URLEncoder.encode("t_age", "UTF-8") + "=" + URLEncoder.encode(age, "UTF-8") + "&" +
                                URLEncoder.encode("t_Address", "UTF-8") + "=" + URLEncoder.encode(address, "UTF-8") ;

                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        alertDialog.setMessage(result);
        alertDialog.show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
