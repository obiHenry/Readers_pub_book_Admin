package com.example.readers_pub_ebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    Button emailButton, googleButton;
    public static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mSignInClient;
    FirebaseAuth firebaseAuth;
    FirebaseUser User;
    private ProgressDialog progressDialog;
    public static final String TAG = "loginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailButton = findViewById(R.id.emailAndPassword);
        googleButton = findViewById(R.id.googleAccount);
        firebaseAuth = FirebaseAuth.getInstance();
        User = firebaseAuth.getCurrentUser();
        progressDialog = new ProgressDialog(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mSignInClient = GoogleSignIn.getClient(this, gso);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,EmailAndPasswordActivity.class));
            }
        });

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogleAccount();
            }
        });
    }

    private void signInWithGoogleAccount() {
        Intent intent = mSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);


            if (task.isSuccessful()){
                progressDialog.setTitle("google sign in");
                progressDialog.setMessage("wait while we allow u login with your google account ");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(true);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    progressDialog.dismiss();
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                    Toast.makeText(this, "please wait while we get ur authentication result", Toast.LENGTH_LONG).show();
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                }
            }else {
                String message = Objects.requireNonNull(task.getException()).getMessage();
                Log.w(TAG, message );
                progressDialog.dismiss();
                Toast.makeText(this, "we can't get ur authentication result : " + message  , Toast.LENGTH_SHORT).show();
            }

        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            sendUserTOMainActivity();

                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = Objects.requireNonNull(task.getException()).getMessage();
                            Toast.makeText(LoginActivity.this, "not authenticated try again : " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserTOMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}