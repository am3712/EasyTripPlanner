package com.example.easytripplanner.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easytripplanner.R;
import com.example.easytripplanner.databinding.ActivityRegistrationBinding;
import com.example.easytripplanner.utility.Common;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;

public class RegistrationActivity extends AppCompatActivity {


    private static final String TAG = "SignUp Activity";
    private ActivityRegistrationBinding binding;
    private FirebaseAuth mAuth;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        signUpAction();

        binding.regLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void signUpAction() {
        binding.regSignUpBtn.setClickable(true);
        binding.regSignUpBtn.setOnClickListener(v -> {
            if (binding.edFullName.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        RegistrationActivity.this
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_name));
            } else if (binding.edEmail.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        RegistrationActivity.this
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_email));
            } else if (!Common.isValidEmail(binding.edEmail.getText().toString().trim())) {
                Common.alertErrorOrValidationDialog(
                        RegistrationActivity.this
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_valid_email));
            } else if (binding.edPassword.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        RegistrationActivity.this
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_password));
            } else if (binding.edPassword.getText().toString().trim().length() < 8) {
                Common.alertErrorOrValidationDialog(
                        RegistrationActivity.this
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_valid_password));
            } else if (binding.edCPassword.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        RegistrationActivity.this
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_current_password));
            } else if (!binding.edPassword.getText().toString().trim().equals(binding.edCPassword.getText().toString().trim())) {
                Common.alertErrorOrValidationDialog(
                        RegistrationActivity.this
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_valid_current_password));
            } else {
                if (Common.isCheckNetwork(RegistrationActivity.this)) {
                    signUp();
                } else {
                    Common.alertErrorOrValidationDialog(
                            RegistrationActivity.this
                            , getString(R.string.registeration_failed)
                            , getResources().getString(R.string.no_internet));
                }
            }
        });

    }

    private void signUp() {
        mAuth.createUserWithEmailAndPassword(
                binding.edEmail.getText().toString().trim()
                , binding.edPassword.getText().toString().trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Timber.d("signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Timber.tag(TAG).w(task.getException(), "signInWithEmail:failure");
                        Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}