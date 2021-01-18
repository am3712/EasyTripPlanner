package com.example.easytripplanner.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.easytripplanner.R;
import com.example.easytripplanner.databinding.FragmentLoginBinding;
import com.example.easytripplanner.utility.Common;
import com.example.easytripplanner.utility.NetworkMonitorUtil;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.jetbrains.annotations.NotNull;

import timber.log.Timber;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.concurrent.Executor;

import static com.facebook.FacebookSdk.getApplicationContext;


public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private final FirebaseAuth mAuth;
    private static final String TAG = "LoginFragment";
    private CallbackManager mCallbackManager;

    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;


    public LoginFragment() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
      //  binding.loginButton.setReadPermissions("email", "public_profile");
        FacebookSdk.sdkInitialize(getApplicationContext());

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
        binding.loginButton.setReadPermissions("email", "public_profile");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginProcess();
        loginFaceBook();

        binding.logSignUpBtn.setOnClickListener(v -> {

        });
    }

    private void loginFaceBook() {

        binding.loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if(user!=null){
                    updateUI(user);
                }else {
                    updateUI(null);
                }
            }
        };
        accessTokenTracker=new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken==null){
                    mAuth.signOut();
                }
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
    private void handleFacebookAccessToken(AccessToken token) {


        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            Toast.makeText(getContext(), "done",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);

                        }


                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
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