package com.exbook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.exbook.R;
import com.exbook.common.Common;
import com.exbook.db.SharedPreferenceHelper;
import com.exbook.services.IResponse;
import com.exbook.services.UserDetails;
import com.exbook.services.UserLogin;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SignInActivity extends AppCompatActivity {
TextInputEditText nic, password;
Button signIn_btn;
Common common;
Button newAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        nic = findViewById(R.id.nic);
        password = findViewById(R.id.password);
        signIn_btn = findViewById(R.id.btn_signIn);
        newAccount = findViewById(R.id.signUpLog);
        common = Common.getInstance(SignInActivity.this);

        signIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(common.validate_password(password.getText().toString()) && common.validate_nic(nic.getText().toString())){
                    //login(nic.getText().toString(),password.getText().toString());
                    login("198856123015","james123");
                    Toast.makeText(SignInActivity.this, "Login", Toast.LENGTH_SHORT).show();
                }else{
                    //show alert
                }
            }
        });
        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignInActivity.this,SignUpActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


    /**
     * Method for user login
     * @param nic
     * @param password
     */
    public void login(String nic,String password) {
        UserLogin userLogin = new UserLogin(nic, password, SignInActivity.this);
        IResponse iResponse = new IResponse() {
            @Override
            public void onSuccess(String response) {
                IResponse.super.onSuccess(response);
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                String token = jsonObject.get("token").getAsString();
                System.out.println("Token: " + token);
                SharedPreferenceHelper.getInstance(SignInActivity.this).saveToken(token);
                SharedPreferenceHelper.getInstance(SignInActivity.this).saveNIC(nic);

                IResponse iResponse1 = new IResponse() {
                    @Override
                    public void onSuccess(String response) {
                        IResponse.super.onSuccess(response);
                        System.out.println("USER DETAILS " + response);
                        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                        String isSendActiveStatus = jsonObject.get("isSendActiveStatus").getAsString();
                        String isActive = jsonObject.get("isActive").getAsString();
                        if (isActive == "true") {
                            Intent i = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            if (isSendActiveStatus == "true") {
                                //activation request already sent
                                System.out.println("Account activation request already sent");
                                Intent i = new Intent(SignInActivity.this, CommonMessageActivity.class);
                                i.putExtra("msg","Activation request already sent.");
                                startActivity(i);
                                finish();
                            } else {
                                //account deactive
                                System.out.println("Your account is in de-active state");
                                Intent i = new Intent(SignInActivity.this, CommonMessageActivity.class);
                                i.putExtra("msg","Account is deactivated.");
                                startActivity(i);
                                finish();
                            }
                        }

                    }

                    @Override
                    public void onFailled(String error) {
                        IResponse.super.onFailled(error);
                    }
                };
                UserDetails userDetails = new UserDetails(SignInActivity.this);
                userDetails.getDetails(iResponse1);
            }

            @Override
            public void onFailled(String error) {
                IResponse.super.onFailled(error);
            }
        };
        userLogin.auth(iResponse);
    }
}