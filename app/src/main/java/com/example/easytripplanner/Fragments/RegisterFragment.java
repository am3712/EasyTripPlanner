package com.example.easytripplanner.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.easytripplanner.R;
import com.example.easytripplanner.databinding.FragmentRegisterBinding;
import com.example.easytripplanner.utility.Common;
import com.example.easytripplanner.utility.NetworkMonitorUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private final FirebaseAuth mAuth;

    public RegisterFragment() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        signUpAction();

        binding.loginNav.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment());
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
                if (NetworkMonitorUtil.checkNetwork(getContext())) {
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
                        Toast.makeText(getContext(), task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Navigation.findNavController(binding.getRoot()).popBackStack();
            Navigation.findNavController(binding.getRoot()).navigate(LoginFragmentDirections.actionLoginFragmentToUpcomingFragment());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}