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
//import com.example.easytripplanner.databinding.FragmentLoginBinding;
import com.example.easytripplanner.databinding.FragmentLoginBinding;
import com.example.easytripplanner.utility.Common;
import com.example.easytripplanner.utility.NetworkMonitorUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private final FirebaseAuth mAuth;
    private static final String TAG = "LoginFragment";


    public LoginFragment() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetworkMonitorUtil.startTriggerNetwork(getContext());
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginProcess();

        binding.logSignUpBtn.setOnClickListener(v -> {
            Navigation.findNavController(binding.getRoot()).navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment());
        });
    }


    private void loginProcess() {
        //login Action
        binding.logLoginBtn.setOnClickListener(v -> {
            if (binding.usrEmail.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getResources().getString(R.string.login_failed)
                        , getResources().getString(R.string.validation_email));
            } else if (Common.isValidEmail(binding.usrEmail.getText().toString().trim())) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getResources().getString(R.string.login_failed)
                        , getResources().getString(R.string.validation_valid_email));
            } else if (binding.usrPassword.getText().toString().trim().isEmpty()) {
                Common.alertErrorOrValidationDialog(
                        getContext()
                        , getResources().getString(R.string.login_failed)
                        , getResources().getString(R.string.validation_password));

            } else {
                if (NetworkMonitorUtil.networkStatus) {
                    Timber.i("loginProcess: NETWORK STATUS: %s", NetworkMonitorUtil.networkStatus);
                    login();
                } else {
                    Common.alertErrorOrValidationDialog(
                            getContext()
                            , getString(R.string.network_info)
                            , getResources().getString(R.string.no_internet));
                }
            }
        });
    }


    private void login() {
        String email = binding.usrEmail.getText().toString().trim();
        String password = binding.usrPassword.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
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

    @Override
    public void onStop() {
        super.onStop();
        NetworkMonitorUtil.stopTriggerNetwork();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void updateUI(FirebaseUser currentUser) {
        Timber.i("updateUI: %s", currentUser);
        if (currentUser != null)
            Navigation.findNavController(binding.getRoot()).navigate(LoginFragmentDirections.actionLoginFragmentToUpcomingFragment());
    }

}