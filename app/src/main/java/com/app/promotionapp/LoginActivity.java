package com.app.promotionapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.promotionapp.database.DatabaseOperation;
import com.app.promotionapp.retailer_repository.RetailerRepository;
import com.app.promotionapp.user_repository.UserRepository;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout emailText;
    TextInputLayout passwordText;
    TextView loginBtn;
    TextView createAccountBtn;
    DatabaseOperation databaseInstance;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initUI();

        databaseInstance = DatabaseOperation.getDatabaseInstance(this);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String userEmail = emailText.getEditText().getText().toString();
                String userPassword = passwordText.getEditText().getText().toString();
                databaseInstance.signInUser(userEmail, userPassword);
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void initUI() {
        emailText = findViewById(R.id.loginEmail);
        passwordText = findViewById(R.id.loginPassword);

        loginBtn = findViewById(R.id.login_loginBtn);
        createAccountBtn = findViewById(R.id.login_signupBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage("Loading, Please wait");
    }
}