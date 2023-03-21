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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    EditText rUsername,rPhone,rEmail,rPassword,rConfirmPassword;
    Button register;
    TextView alreadyRegistered;
    ProgressBar rProgressBar;
    FirebaseAuth fAuth;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        rUsername = findViewById(R.id.registerUsername);
        rPhone = findViewById(R.id.registerPhone);
        rEmail = findViewById(R.id.registerEmail);
        rPassword = findViewById(R.id.registerPassword);
        rConfirmPassword = findViewById(R.id.registerConfirmPassword);
        register = findViewById(R.id.registerBtn);
        alreadyRegistered = findViewById(R.id.alreadyRegistered);
        rProgressBar = findViewById(R.id.registerProgress);

        fAuth = FirebaseAuth.getInstance();

        alreadyRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateUserName() | !validatePhoneNumber() | !validateEmail() | !validatePassword() | !validateConfirmPassword()){
                    return;
                }

                rProgressBar.setVisibility(View.VISIBLE);

                String strRUsername = rUsername.getText().toString().trim();
                String strRPhone = rPhone.getText().toString().trim();
                String strREmail = rEmail.getText().toString().trim();
                String strRPassword = rPassword.getText().toString().trim();

                fAuth.createUserWithEmailAndPassword(strREmail,strRPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        rootNode = FirebaseDatabase.getInstance();
                        reference = rootNode.getReference("users");
                        uid = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
                        UserHelperClass userHelperClass = new UserHelperClass(uid,strRUsername,strREmail,strRPhone,strRPassword);
                        reference.child(uid).setValue(userHelperClass);
                        Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        rProgressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error! "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        rProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    private Boolean validateUserName(){
        String val = rUsername.getText().toString().trim();
        if(TextUtils.isEmpty(val)){
            rUsername.setError("User Name is Required");
            return false;
        }
        else{
            rUsername.setError(null);
            return true;
        }
    }
    private Boolean validatePhoneNumber(){
        String val = rPhone.getText().toString().trim();
        if(TextUtils.isEmpty(val)){
            rPhone.setError("Phone Number is Required");
            return false;
        }
        else if(val.length()!=10){
            rPhone.setError("Enter valid Phone Number");
            return  false;
        }
        else{
            rPhone.setError(null);
            return true;
        }
    }
    private Boolean validateEmail(){
        String val = rEmail.getText().toString().trim();
        String emailPattern = "^[a-zA-Z0-9_.]+@[a-zA-Z-._]+?\\.[a-zA-Z]{2,}$";
        if(TextUtils.isEmpty(val)){
            rEmail.setError("Email is Required");
            return false;
        }
        else if(!val.matches(emailPattern)){
            rEmail.setError("Enter valid Domain Email");
            return  false;
        }
        else{
            rEmail.setError(null);
            return true;
        }
    }
    private Boolean validatePassword(){
        String val = rPassword.getText().toString().trim();
        String passwordVal = "^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                ".{4,}" +               //at least 4 characters
                "$";

        if(TextUtils.isEmpty(val)){
            rPassword.setError("Password is Required");
            return false;
        }
        else if(!val.matches(passwordVal)){
            rPassword.setError("Password too weak");
            return  false;
        }
        else{
            rPassword.setError(null);
            return true;
        }
    }
    private Boolean validateConfirmPassword(){
        String val1 = rPassword.getText().toString().trim();
        String val2 = rConfirmPassword.getText().toString().trim();
        if(TextUtils.isEmpty(val2) || !TextUtils.equals(val1,val2)){
            rConfirmPassword.setError("Password not Matched");
            return false;
        }
        else{
            rConfirmPassword.setError(null);
            return true;
        }
    }
}