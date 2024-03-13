package com.example.sfolder;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Newpswd extends AppCompatActivity {
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpswd);
        findViewById(R.id.submitpswd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPasswordSubmitAction();
                Intent intent = new Intent(getApplicationContext(), Getpswd.class);
                startActivity(intent);
            }
        });
    }

    // Application validates both submitted passwords.
    // On successful password validation, Repository cached password is updated.
    private void newPasswordSubmitAction() {
        Log.i("message","NewPasswordSubmitAction method started.");
        try {
            Boolean passwordFieldsValidation = validatePasswordFields();
            if (passwordFieldsValidation) {
                String hashedPassword = Splashscreen.hashPassword(this, ((EditText) findViewById(R.id.newpswd)).getText().toString());
                if (!hashedPassword.equals("")) {
                    SharedPreferences sharedPreferences = getSharedPreferences("com.example.sfolder", Context.MODE_PRIVATE);;
                    sharedPreferences.edit().putString("password", hashedPassword).commit();
                }
            }
            Log.i("message","NewPasswordSubmitAction method completed successfully.");
            if (passwordFieldsValidation) finish();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during NewPasswordSubmitAction method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // Validates User submitted passwords.
    // Passwords cannot be empty and must match.
    private Boolean validatePasswordFields() {
        Boolean valid = true;
        EditText newPasswordEditText = findViewById(R.id.newpswd);
        if (newPasswordEditText.getText().toString().trim().length() == 0) {
            newPasswordEditText.setError("Password cannot be empty!");
            valid = false;
        }
        EditText repeatNewPasswordEditText = findViewById(R.id.repeatnewpswd);
        if (repeatNewPasswordEditText.getText().toString().trim().length() == 0) {
            repeatNewPasswordEditText.setError("Password cannot be empty");
            valid = false;
        } else if (!repeatNewPasswordEditText.getText().toString().equals(newPasswordEditText.getText().toString())) {
            repeatNewPasswordEditText.setError("Passwords must match!");
            valid = false;
        }
        return valid;
    }

}
