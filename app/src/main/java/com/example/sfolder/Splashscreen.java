package com.example.sfolder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        Intent intent;

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.sfolder", Context.MODE_PRIVATE);
        if(! sharedPreferences.contains("password")) intent = new Intent(getApplicationContext(), Newpswd.class);
        else intent = new Intent(getApplicationContext(), Getpswd.class);
        startActivity(intent);
        finish();
    }





    public static String hashPassword(Context context, String password) {
        Log.i("message","HashPassword method started.");
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA256");
            digest.update(password.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(Integer.toHexString(0xFF & b));
            }
            Log.i("message","HashPassword method completed successfully.");
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.i("message","Exception during HashPassword method:" + e.getMessage());
            Toast.makeText(context, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
            return "";
        }
    }
}