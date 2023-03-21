package com.example.roadsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    TextView doNotHaveAccount;
    EditText lEmail,lPassword;
    Button loginBtn;
    ProgressBar loginProgress;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lEmail = findViewById(R.id.loginEmail);
        lPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        loginProgress = findViewById(R.id.loginProgress);
        fAuth = FirebaseAuth.getInstance();

        if(fAuth.getCurrentUser() != null){
            startActivity( new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateEmail() | !validatePassword()){
                    return;
                }

                String strLEmail = lEmail.getText().toString().trim();
                String strLPassword = lPassword.getText().toString().trim();

                loginProgress.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(strLEmail,strLPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                            loginProgress.setVisibility(View.GONE);
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Error ! "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            loginProgress.setVisibility(View.GONE);
                        }
                    }
                });


            }
        });

        doNotHaveAccount = findViewById(R.id.doNotHaveAccount);
        doNotHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
            }
        });
    }

    private Boolean validateEmail(){
        String val = lEmail.getText().toString().trim();
        String emailPattern = "^[a-zA-Z0-9_.]+@[a-zA-Z-._]+?\\.[a-zA-Z]{2,}$";
        if(TextUtils.isEmpty(val)){
            lEmail.setError("Email is Required");
            return false;
        }
        else if(!val.matches(emailPattern)){
            lEmail.setError("Enter valid Domain Email");
            return  false;
        }
        else{
            lEmail.setError(null);
            return true;
        }
    }

    private Boolean validatePassword(){
        String val = lPassword.getText().toString().trim();
        if(TextUtils.isEmpty(val)){
            lPassword.setError("Password is required");
            return false;
        }
        else{
            lPassword.setError(null);
            return true;
        }
    }
}