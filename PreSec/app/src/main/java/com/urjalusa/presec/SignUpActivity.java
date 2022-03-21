package com.urjalusa.presec;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView DisplayPassword;
    private EditText name;
    private TextView forDoctor;
    private EditText email;
    private EditText phoneNumber;
    private EditText password;
    private RadioGroup radioGroupMode;
    private RadioButton radioButtonChecked;
    private RadioButton getRadioButtonCheckedMode;
    private TextView askGender;
    private RadioGroup radioGroupGender;
    private TextView askBloodGroup;
    private RadioGroup radioGroupBloodGroup;
    private EditText weight;
    private EditText height;
    private EditText DoB;
    private EditText allergies;
    private EditText registrationNumber;
    private EditText address;
    private EditText licenceNumber;
    private CheckBox termsAndCondition;
    private FirebaseAuth AuthUser;
    private Boolean allFieldsSelected = true;
    private static final String TAG = "SignUpActivity";
    private String UID;
    private String ModeOfUser;
    private String TextOfSelectedGender;
    private String TextOfSelectedBGroup;
    private String ModeCount;
    private FirebaseFirestore db;
    private Button signUp;
    private Boolean EmailFlag;
    private FirebaseUser CurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();
        DisplayPassword = findViewById(R.id.textViewShowHide_SignUpPage);
        signUp = findViewById(R.id.buttonSignUp_SignUpPage);
        progressBar = findViewById(R.id.progressBar_SignUpPage);
        name = findViewById(R.id.editTextName_SignUpPage);
        forDoctor = findViewById(R.id.textViewForDoctor_SignUpPage);
        email = findViewById(R.id.editTextEmail_SignUpPage);
        phoneNumber = findViewById(R.id.editTextPhoneNumber_SignUpPage);
        password = findViewById(R.id.editTextPassword_SignUpPage);
        radioGroupMode = findViewById(R.id.radioGroupMode_SignUpPage);
        askGender = findViewById(R.id.textViewAskGenderPatient_SignUpPage);
        radioGroupGender = findViewById(R.id.radioGroupGenderPatient_SignUpPage);
        askBloodGroup = findViewById(R.id.textViewAskBloodGroupPatient_SignUpPage);
        radioGroupBloodGroup = findViewById(R.id.radioGroupBloodGroupPatient_SignUpPage);
        weight = findViewById(R.id.editTextWeightPatient_SignUpPage);
        height = findViewById(R.id.editTextHeightPatient_SignUpPage);
        DoB = findViewById(R.id.editTextDoBPatient_SignUpPage);
        allergies = findViewById(R.id.editTextAllergiesPatient_SignUpPage);
        registrationNumber = findViewById(R.id.editTextRegistrationNumberDoctor_SignUpPage);
        address = findViewById(R.id.editTextAddressPharmacy_SignUpPage);
        licenceNumber = findViewById(R.id.editTextLicenceNumberPharmacy_SignUpPage);
        termsAndCondition = findViewById(R.id.checkBoxTandC_SignUpPage);
        Button emailVerification = findViewById(R.id.buttonVerifyEmail_SignUpPage);
        AuthUser = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        DisplayPassword.setVisibility(View.GONE);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        DisplayPassword.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (DisplayPassword.getText() == "SHOW") {
                    DisplayPassword.setText("HIDE");
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    password.setSelection(password.length());
                } else {
                    DisplayPassword.setText("SHOW");
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    password.setSelection(password.length());
                }
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

        //verify email before sign up
        //user gets created here in firebase authentication
        emailVerification.setOnClickListener(v -> {
            String Email = email.getText().toString();
            String Password = password.getText().toString();
            if (email.getText().toString().equals("")) {
                email.setError("Enter email");
            } else if (password.getText().toString().equals("")) {
                password.setError("Enter Password");
            } else {
                AuthUser.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            CurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "Mail is sent to " + CurrentUser.getEmail() + " please verify to proceed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Mail not sent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            UID = AuthUser.getCurrentUser().getUid();
                            Map<String, Object> userId = new HashMap<>();
                            userId.put("Category", "Do Not Login");
                            db.collection("UserTypeCategorize").document(UID).set(userId);
                        } else {
                            Toast.makeText(SignUpActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //if sign up button pressed register user
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                allFieldsSelected = true;

                //radioModeID is for getting ID of mode
                int radioModeID = radioGroupMode.getCheckedRadioButtonId();
                getRadioButtonCheckedMode = findViewById(radioModeID);

                //conditions to ensure that followed fields are not left empty
                if (name.getText().toString().trim().equals("")) {
                    name.setError("Enter name");
                    allFieldsSelected = false;
                } else if (email.getText().toString().equals("")) {
                    email.setError("Enter email");
                    allFieldsSelected = false;
                } else if (phoneNumber.getText().toString().trim().equals("")) {
                    phoneNumber.setError("Enter the phone number");
                    allFieldsSelected = false;
                } else if (phoneNumber.getText().toString().length() != 10) {
                    phoneNumber.setError("Phone number needs to be 10 digits ");
                    allFieldsSelected = false;
                } else if (password.getText().toString().trim().equals("")) {
                    password.setError("Enter the password");
                    allFieldsSelected = false;
                } else if (!termsAndCondition.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Please accept terms &amp; conditions", Toast.LENGTH_SHORT).show();
                    allFieldsSelected = false;
                } else if (radioModeID == -1 || radioModeID != -1) {
                    if (radioModeID == -1) {
                        allFieldsSelected = false;
                        Toast.makeText(getApplicationContext(), "Please select one type of user", Toast.LENGTH_LONG).show();
                    } else {
                        findRadioButtonID();
                        if (allFieldsSelected) {
                            String Email = email.getText().toString();
                            String Password = password.getText().toString();

                            //sign up into account
                            AuthUser.signInWithEmailAndPassword(Email, Password)
                                    .addOnCompleteListener(SignUpActivity.this, task -> {
                                        if (task.isSuccessful()) {
                                            UID = AuthUser.getCurrentUser().getUid();
                                            EmailFlag = CurrentUser.isEmailVerified();
                                            if (EmailFlag) {

                                                StoreUserDetails(ModeOfUser);
                                                progressBar.setIndeterminate(false);
                                                progressBar.setVisibility(View.INVISIBLE);
                                                finish();
                                            } else {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                                builder.setTitle("Email Needs to be verified").setMessage("Please verify your Email, if you haven't received the mail then resend it")
                                                        .setCancelable(false).setPositiveButton("Resend Email", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(SignUpActivity.this, "Mail is sent to " + CurrentUser.getEmail() + " please verify to proceed", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(SignUpActivity.this, "Mail not sent", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                        FirebaseAuth.getInstance().signOut();
                                                    }
                                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        FirebaseAuth.getInstance().signOut();
                                                        Toast.makeText(SignUpActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            }
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Please Verify your email, click on mail button to get mail", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progressBar.setIndeterminate(false);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        });

        //this conditions are for visibility of the fields
        radioGroupMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                radioButtonChecked = findViewById(checkedId);
                switch (radioButtonChecked.getId()) {
                    case R.id.radioButtonD_SignUpPage:
                        //show Doctor additional fields
                        forDoctor.setVisibility(View.VISIBLE);
                        askGender.setVisibility(View.INVISIBLE);
                        radioGroupGender.setVisibility(View.INVISIBLE);
                        askBloodGroup.setVisibility(View.INVISIBLE);
                        radioGroupBloodGroup.setVisibility(View.INVISIBLE);
                        weight.setVisibility(View.INVISIBLE);
                        height.setVisibility(View.INVISIBLE);
                        DoB.setVisibility(View.INVISIBLE);
                        allergies.setVisibility(View.INVISIBLE);
                        registrationNumber.setVisibility(View.VISIBLE);
                        address.setVisibility(View.INVISIBLE);
                        licenceNumber.setVisibility(View.INVISIBLE);
                        break;

                    case R.id.radioButtonPa_SignUpPage:
                        //show Patient additional fields
                        forDoctor.setVisibility(View.INVISIBLE);
                        askGender.setVisibility(View.VISIBLE);
                        radioGroupGender.setVisibility(View.VISIBLE);
                        askBloodGroup.setVisibility(View.VISIBLE);
                        radioGroupBloodGroup.setVisibility(View.VISIBLE);
                        weight.setVisibility(View.VISIBLE);
                        height.setVisibility(View.VISIBLE);
                        DoB.setVisibility(View.VISIBLE);
                        allergies.setVisibility(View.VISIBLE);
                        registrationNumber.setVisibility(View.INVISIBLE);
                        address.setVisibility(View.INVISIBLE);
                        licenceNumber.setVisibility(View.INVISIBLE);
                        break;

                    case R.id.radioButtonPh_signUpPage:
                        //show Pharmacy fields
                        forDoctor.setVisibility(View.INVISIBLE);
                        askGender.setVisibility(View.INVISIBLE);
                        radioGroupGender.setVisibility(View.INVISIBLE);
                        askBloodGroup.setVisibility(View.INVISIBLE);
                        radioGroupBloodGroup.setVisibility(View.INVISIBLE);
                        weight.setVisibility(View.INVISIBLE);
                        height.setVisibility(View.INVISIBLE);
                        DoB.setVisibility(View.INVISIBLE);
                        allergies.setVisibility(View.INVISIBLE);
                        registrationNumber.setVisibility(View.INVISIBLE);
                        address.setVisibility(View.VISIBLE);
                        licenceNumber.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    //This function is used to check which radiobutton is selected for the type of users
    //And checks if the fields are filled
    private void findRadioButtonID() {
        switch (getRadioButtonCheckedMode.getId()) {
            case R.id.radioButtonD_SignUpPage:
                if (registrationNumber.getText().toString().trim().equals("")) {
                    registrationNumber.setError("Please enter the registration number");
                    allFieldsSelected = false;
                    return;
                }
                ModeOfUser = "DoctorDb";
                break;
            case R.id.radioButtonPa_SignUpPage:
                int radioGenderID = radioGroupGender.getCheckedRadioButtonId();
                int radioBloodID = radioGroupBloodGroup.getCheckedRadioButtonId();
                if (radioGenderID == -1 || radioBloodID == -1) {
                    if (radioGenderID == -1) {
                        Toast.makeText(getApplicationContext(), "Select a Gender", Toast.LENGTH_SHORT).show();
                        allFieldsSelected = false;
                    } else {
                        Toast.makeText(this, " Select Blood Group", Toast.LENGTH_SHORT).show();
                        allFieldsSelected = false;
                    }
                } else {
                    TextOfSelectedGender = ((RadioButton) this.findViewById(radioGenderID)).getText().toString();
                    TextOfSelectedBGroup = ((RadioButton) this.findViewById(radioBloodID)).getText().toString();
                }
                if (height.getText().toString().trim().equals("")) {
                    height.setError("Height needs to be filled");
                    allFieldsSelected = false;
                    return;
                }
                if (weight.getText().toString().trim().equals("")) {
                    weight.setError("Weight needs to be filled");
                    allFieldsSelected = false;
                    return;
                }
                if (DoB.getText().toString().trim().equals("")) {
                    DoB.setError("Date of Birth needs to be filled");
                    allFieldsSelected = false;
                    return;
                }
                ModeOfUser = "UserDb";
                break;

            case R.id.radioButtonPh_signUpPage:
                if (address.getText().toString().trim().equals("")) {
                    address.setError("Address needs to be filled");
                    allFieldsSelected = false;
                    return;
                }
                if (licenceNumber.getText().toString().trim().equals("")) {
                    licenceNumber.setError("Enter license number");
                    allFieldsSelected = false;
                    return;
                }
                ModeOfUser = "PharmacyDb";
                break;
            default:
                Log.d(TAG, "findRadioButtonID: Error in selecting type of users");
        }
    }

    private void StoreUserDetails(final String ModeOfUser) {
        final String Name = name.getText().toString();
        final String PhoneNum = phoneNumber.getText().toString();
        final String paAllergies = allergies.getText().toString();
        final String dRegistrationNumber = registrationNumber.getText().toString();
        final String phAddress = address.getText().toString();
        final String phLicenceNum = licenceNumber.getText().toString();
        final String Email = email.getText().toString().trim();

        //finds the number of entries in the mode selected
        db.collection("UserTypeCategorize").document("Count").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot docGetCount = task.getResult();
                ModeCount = docGetCount.getString(ModeOfUser);

                //Stores the data of user in firestore
                switch (ModeOfUser) {
                    case "DoctorDb":
                        db.collection("VerifyDoctorDataBase").document(registrationNumber.getText().toString()).get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task1.getResult();
                                        if (documentSnapshot.exists()) {
                                            if (!documentSnapshot.getBoolean("Status")) {
                                                db.collection("VerifyDoctorDataBase").document(registrationNumber.getText().toString()).
                                                        update("Status", true);
                                                Map<String, Object> Doctor = new HashMap<>();
                                                Doctor.put("Certified Number", dRegistrationNumber);
                                                Doctor.put("Contact", PhoneNum);
                                                Doctor.put("Email", Email);
                                                Doctor.put("Name", "Dr." + Name);
                                                Doctor.put("Prescriptions made", "000000");
                                                db.collection("DoctorDb").document(ModeCount).set(Doctor);

                                                //enters the users category and id in UserTypeCategorize
                                                Map<String, Object> userId = new HashMap<>();
                                                userId.put("Category", ModeOfUser);
                                                userId.put("id", ModeCount);
                                                db.collection("UserTypeCategorize").document(UID).set(userId);
                                                IncrementCount(ModeCount);//Increments the count
                                                goToNextPage(UID);
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Doctor registration already has an account", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Invalid Registration Number", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        break;

                    case "UserDb":
                        Map<String, Object> Patient = new HashMap<>();
                        if (paAllergies.equals("")) {
                            Patient.put("Allergies", "None");
                        } else {
                            Patient.put("Allergies", paAllergies);
                        }
                        Patient.put("Height", height.getText().toString());
                        Patient.put("Weight", weight.getText().toString());
                        Patient.put("DoB", DoB.getText().toString());
                        Patient.put("Blood Group", TextOfSelectedBGroup);
                        Patient.put("Contact", PhoneNum);
                        Patient.put("Email", Email);
                        Patient.put("Gender", TextOfSelectedGender);
                        Patient.put("Name", Name);
                        db.collection("UserDb").document(ModeCount).set(Patient);

                        //enters the users category and id in UserTypeCategorize
                        Map<String, Object> userId = new HashMap<>();
                        userId.put("Category", ModeOfUser);
                        userId.put("id", ModeCount);
                        db.collection("UserTypeCategorize").document(UID).set(userId);
                        IncrementCount(ModeCount);//Increments the count
                        goToNextPage(UID);
                        break;

                    case "PharmacyDb":
                        db.collection("VerifyPharmacyDatabase").document(licenceNumber.getText().toString()).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            if (documentSnapshot.exists()) {
                                                if (!documentSnapshot.getBoolean("Status")) {
                                                    db.collection("VerifyPharmacyDatabase").document(licenceNumber.getText().toString()).
                                                            update("Status", true);
                                                    Map<String, Object> Pharmacy = new HashMap<>();
                                                    Pharmacy.put("Address", phAddress);
                                                    Pharmacy.put("Email", Email);
                                                    Pharmacy.put("License No", phLicenceNum);
                                                    Pharmacy.put("Name", Name);
                                                    Pharmacy.put("Contact", PhoneNum);
                                                    db.collection("PharmacyDb").document(ModeCount).set(Pharmacy);

                                                    //enters the users category and id in UserTypeCategorize
                                                    Map<String, Object> userId = new HashMap<>();
                                                    userId.put("Category", ModeOfUser);
                                                    userId.put("id", ModeCount);
                                                    db.collection("UserTypeCategorize").document(UID).set(userId);
                                                    IncrementCount(ModeCount);//Increments the count
                                                    goToNextPage(UID);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "License already has an account", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Invalid License", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                        break;
                }
            } else {
                Toast.makeText(SignUpActivity.this, "Error in getting count " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void IncrementCount(String Count) {
        int digit1;
        int digit2;
        int digit3;
        int digit4;
        int digit5;

        char Char1 = Count.charAt(0);
        char Char2 = Count.charAt(1);
        char Char3 = Count.charAt(2);
        char Char4 = Count.charAt(3);
        char Char5 = Count.charAt(4);

        System.out.println(Char1);
        System.out.println(Char2);
        System.out.println(Char3);
        System.out.println(Char4);
        System.out.println(Char5);

        digit1 = Character.getNumericValue(Char1);
        digit2 = Character.getNumericValue(Char2);
        digit3 = Character.getNumericValue(Char3);
        digit4 = Character.getNumericValue(Char4);
        digit5 = Character.getNumericValue(Char5);

        System.out.println(digit1);
        System.out.println(digit2);
        System.out.println(digit3);
        System.out.println(digit4);
        System.out.println(digit5);

        if (digit5 > 8) {
            digit5 = 0;
            if (digit4 > 8) {
                digit4 = 0;
                if (digit3 > 8) {
                    digit3 = 0;
                    if (digit2 > 8) {
                        digit2 = 0;
                        if (digit1 <= 8) {
                            digit1 = (digit1 + 1);
                        }
                    } else {
                        digit2 = (digit2 + 1);
                    }
                } else {
                    digit3 = (digit3 + 1);
                }
            } else {
                digit4 = (digit4 + 1);
            }
        } else {
            digit5 = (digit5 + 1);
        }

        Char1 = Character.forDigit(digit1, 10);
        Char2 = Character.forDigit(digit2, 10);
        Char3 = Character.forDigit(digit3, 10);
        Char4 = Character.forDigit(digit4, 10);
        Char5 = Character.forDigit(digit5, 10);
        Count = Character.toString(Char1) + Char2 + Char3 + Char4 + Char5;

        //update count in its document
        DocumentReference documentReference = db.collection("UserTypeCategorize").document("Count");
        documentReference.update(ModeOfUser, Count).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        });
    }

    private void goToNextPage(String uid) {
        db.collection("UserTypeCategorize").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String id = document.getString("id");
                    String mode = document.getString("Category");

                    Intent intent;

                    switch (mode) {
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
                    progressBar.setVisibility(View.INVISIBLE);
                    intent.putExtra("id", id);
                    startActivity(intent);
                } else {
                    Log.d(TAG, ("Get failed with " + task.getException().getMessage()));
                }
            }
        });
    }
}