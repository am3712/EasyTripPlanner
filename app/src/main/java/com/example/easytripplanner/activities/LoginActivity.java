package com.example.easytripplanner.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easytripplanner.R;
import com.example.easytripplanner.databinding.ActivityLoginBinding;
import com.example.easytripplanner.utility.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login Activity";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        loginAction();

        binding.logSignUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loginAction() {
        //login Action
        binding.logLoginBtn.setOnClickListener(v -> {
            if (binding.usrEmail.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        LoginActivity.this
                        , getResources().getString(R.string.login_failed)
                        , getResources().getString(R.string.validation_email));
            } else if (!Common.isValidEmail(binding.usrEmail.getText().toString().trim())) {
                Common.alertErrorOrValidationDialog(
                        LoginActivity.this
                        , getResources().getString(R.string.login_failed)
                        , getResources().getString(R.string.validation_valid_email));
            } else if (binding.usrPassword.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        LoginActivity.this
                        , getResources().getString(R.string.login_failed)
                        , getResources().getString(R.string.validation_password));

            } else {
                if (Common.isCheckNetwork(getApplicationContext())) {
                    login();

                } else {
                    Common.alertErrorOrValidationDialog(
                            LoginActivity.this
                            , getString(R.string.network_info)
                            , getResources().getString(R.string.no_internet));
                }
                Toast.makeText(this, "Network Info return true", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login() {
        String email = binding.usrEmail.getText().toString().trim();
        String password = binding.usrPassword.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}