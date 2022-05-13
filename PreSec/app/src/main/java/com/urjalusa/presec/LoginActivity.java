package com.urjalusa.presec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText emailId;
    private EditText password;
    private FirebaseAuth deviceUser;
    private FirebaseFirestore db;
    private TextView DisplayPassword;
    private static final String TAG = "LoginActivity";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).hide();

        //setting values by id
        Button loginButton = findViewById(R.id.buttonLogin_LoginPage);
        Button signUpButton = findViewById(R.id.buttonSignUp_LoginPage);
        emailId = findViewById(R.id.editTextEmail_LoginPage);
        password = findViewById(R.id.editTextPassword_LoginPage);
        DisplayPassword = findViewById(R.id.ToggleTextView_password);
        deviceUser = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        DisplayPassword.setVisibility(View.GONE);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        DisplayPassword.setOnClickListener(v -> {
            if (DisplayPassword.getText() == "SHOW") {
                DisplayPassword.setText("HIDE");
                password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                password.setSelection(password.length());
            } else {
                DisplayPassword.setText("SHOW");
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                password.setSelection(password.length());
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (password.getText().length() > 0) {
                    DisplayPassword.setVisibility(View.VISIBLE);
                } else {
                    DisplayPassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //when login button pressed redirect to respective mode's home page
        loginButton.setOnClickListener(v -> {
            String emailString = emailId.getText().toString().trim();
            String passwordString = password.getText().toString().trim();

            if (emailString.equals("")) {
                emailId.setError("Enter email id");
            } else if (passwordString.equals("")) {
                password.setError("Enter password");
            } else {
                deviceUser.signInWithEmailAndPassword(emailString, passwordString)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                String uid = deviceUser.getUid();
                                goToNextPage(uid);
                            } else {
                                Toast.makeText(LoginActivity.this, "Invalid Email or Password, Please Try again or Create an Account", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        //if user not registered go to sign up page
        signUpButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    //redirects to next page by checking mode
    private void goToNextPage(final String uid) {
        db.collection("UserTypeCategorize").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String id = document.getString("id");
                    String mode = document.getString("Category");

                    Intent intent;

                    switch (mode) {
                        case "Do Not Login":
                            deviceUser.getCurrentUser().delete();
                            deviceUser.signOut();
                            Toast.makeText(getApplicationContext(), "Invalid User", Toast.LENGTH_SHORT).show();
                            return;

                        case "DoctorDb":
                            intent = new Intent(getApplicationContext(), DoctorCreatePrescriptionActivity.class);
                            break;

                        case "UserDb":
                            intent = new Intent(getApplicationContext(), PatientPrescriptionListActivity.class);
                            break;

                        case "PharmacyDb":
                            intent = new Intent(getApplicationContext(), PharmacyQRScanActivity.class);
                            break;

                        default:
                            Toast.makeText(getApplicationContext(), "Invalid Login", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "The user id: " + id + " is not registered properly");
                            return;
                    }

                    Toast.makeText(LoginActivity.this, "Signed in", Toast.LENGTH_SHORT).show();
                    intent.putExtra("id", id);
                    startActivity(intent);
                    finish();

                } else {
                    Log.d(TAG, ("get failed with " + task.getException().getMessage()));
                }
            }
        });
    }

    //to forcefully exit app when pressed back from this activity
    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}