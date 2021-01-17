package com.example.easytripplanner.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.easytripplanner.R;
import com.example.easytripplanner.databinding.FragmentRegisterBinding;
import com.example.easytripplanner.utility.Common;
import com.example.easytripplanner.utility.NetworkMonitorUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private FirebaseAuth mAuth;
    private static final String TAG = "RegisterFragment";

    public RegisterFragment() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetworkMonitorUtil.startTriggerNetwork(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        signUpAction();

        binding.regLoginBtn.setOnClickListener(v -> {

        });
    }

    private void signUpAction() {
        binding.regSignUpBtn.setClickable(true);
        binding.regSignUpBtn.setOnClickListener(v -> {
            if (binding.edFullName.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_name));
            } else if (binding.edEmail.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_email));
            } else if (Common.isValidEmail(binding.edEmail.getText().toString().trim())) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_valid_email));
            } else if (binding.edPassword.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_password));
            } else if (binding.edPassword.getText().toString().trim().length() < 8) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_valid_password));
            } else if (binding.edCPassword.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_current_password));
            } else if (!binding.edPassword.getText().toString().trim().equals(binding.edCPassword.getText().toString().trim())) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getString(R.string.registeration_failed)
                        , getResources().getString(R.string.validation_valid_current_password));
            } else {
                if (NetworkMonitorUtil.networkStatus) {
                    signUp();
                } else {
                    Common.alertErrorOrValidationDialog(
                            getContext()
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
                        Toast.makeText(getContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}