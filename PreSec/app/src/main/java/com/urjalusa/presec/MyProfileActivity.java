package com.urjalusa.presec;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyProfileActivity extends AppCompatActivity {

    private String id;
    private String UserType;
    private String oldPass;
    private String newPass;
    private String confirmPass;
    private TextView UserName;
    private TextView EmailValue;
    private TextView ContactValue;
    private TextView AgeCertificationLicenseValue;
    private TextView HeightValue;
    private TextView GenderAddressPrescriptionCountValue;
    private TextView WeightValue;
    private TextView BloodGroupValue;
    private TextView AllergiesValue;
    private FirebaseFirestore db;
    private Integer age;
    private String dateOfBirth;
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String currentYear;
    private String Year;
    private String currentMonth;
    private String Month;
    private String currentDay;
    private String Day;
    private FirebaseAuth deviceUser;
    private FirebaseUser firebaseUser;
    private Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        ImageView profileClipart = findViewById(R.id.imageViewProfileClipart);
        UserName = findViewById(R.id.textViewNameOfUser);
        EmailValue = findViewById(R.id.textViewUserEmail);
        ContactValue = findViewById(R.id.textViewUserContactNumber);
        TextView ageCertificationLicenseLabel = findViewById(R.id.textViewAgeCertificationLicenseView);
        AgeCertificationLicenseValue = findViewById(R.id.textViewAgeCertificationLicense);
        HeightValue = findViewById(R.id.textViewPatientHeight);
        TextView heightLabel = findViewById(R.id.textViewPatientHeightView);
        TextView genderAddressPrescriptionCountLabel = findViewById(R.id.textViewGenderAddressPrescriptionCountView);
        GenderAddressPrescriptionCountValue = findViewById(R.id.textViewGenderAddressPrescriptionCount);
        TextView weightLabel = findViewById(R.id.textViewPatientWeightView);
        WeightValue = findViewById(R.id.textViewPatientWeight);
        TextView bloodGroupLabel = findViewById(R.id.textViewPatientBloodGroupView);
        BloodGroupValue = findViewById(R.id.textViewPatientBloodGroup);
        TextView allergiesLabel = findViewById(R.id.textViewPatientAllergiesView);
        AllergiesValue = findViewById(R.id.textViewPatientAllergies);
        TextView IDValue = findViewById(R.id.textViewUserID);
        deviceUser = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        extras = getIntent().getExtras();
        id = extras.getString("UserId");
        UserType = extras.getString("UserType");

        //Displays fields as per the user
        switch (UserType) {
            case "DoctorDb":
                ageCertificationLicenseLabel.setText("Certification Number");
                heightLabel.setVisibility(View.INVISIBLE);
                HeightValue.setVisibility(View.INVISIBLE);
                genderAddressPrescriptionCountLabel.setText("Prescription Count");
                weightLabel.setVisibility(View.INVISIBLE);
                WeightValue.setVisibility(View.INVISIBLE);
                bloodGroupLabel.setVisibility(View.INVISIBLE);
                BloodGroupValue.setVisibility(View.INVISIBLE);
                allergiesLabel.setVisibility(View.INVISIBLE);
                AllergiesValue.setVisibility(View.INVISIBLE);
                IDValue.setText(id);
                db.collection("DoctorDb").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            UserName.setText(doc.getString("Name"));
                            EmailValue.setText(doc.getString("Email"));
                            ContactValue.setText(doc.getString("Contact"));
                            AgeCertificationLicenseValue.setText(doc.getString("Certified Number"));
                            long PrescriptionCount = Long.parseLong(doc.getString("Prescriptions made"));
                            GenderAddressPrescriptionCountValue.setText(PrescriptionCount + "");
                        }
                    }
                });
                break;

            case "UserDb":
                profileClipart.setImageResource(R.drawable.ic_patient_myprofile);
                IDValue.setText(id);

                db.collection("UserDb").document(id).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    dateOfBirth = doc.getString("DoB");
                                    if (dateOfBirth != null) {
                                        dateImplication(dateOfBirth.substring(0, 10));
                                        final String currentTimeStampVal = dateFormat.format(new Date());
                                        currentDay = currentTimeStampVal.substring(8, 10);
                                        currentMonth = currentTimeStampVal.substring(5, 7);
                                        currentYear = currentTimeStampVal.substring(0, 4);
                                        deriveAge();
                                        UserName.setText(doc.getString("Name"));
                                        EmailValue.setText(doc.getString("Email"));
                                        ContactValue.setText(doc.getString("Contact"));
                                        AgeCertificationLicenseValue.setText(age + "");
                                        HeightValue.setText(doc.getString("Height"));
                                        GenderAddressPrescriptionCountValue.setText(doc.getString("Gender"));
                                        WeightValue.setText(doc.getString("Weight"));
                                        BloodGroupValue.setText(doc.getString("Blood Group"));
                                        AllergiesValue.setText(doc.getString("Allergies"));
                                    }
                                }
                            }
                        });
                break;

            case "PharmacyDb":
                profileClipart.setImageResource(R.drawable.ic_pharmacy_myprofile);
                ageCertificationLicenseLabel.setText("License Number");
                heightLabel.setVisibility(View.INVISIBLE);
                HeightValue.setVisibility(View.INVISIBLE);
                genderAddressPrescriptionCountLabel.setText("Address");
                weightLabel.setVisibility(View.INVISIBLE);
                WeightValue.setVisibility(View.INVISIBLE);
                bloodGroupLabel.setVisibility(View.INVISIBLE);
                BloodGroupValue.setVisibility(View.INVISIBLE);
                allergiesLabel.setVisibility(View.INVISIBLE);
                AllergiesValue.setVisibility(View.INVISIBLE);
                IDValue.setText(id);

                db.collection("PharmacyDb").document(id).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    UserName.setText(doc.getString("Name"));
                                    EmailValue.setText(doc.getString("Email"));
                                    ContactValue.setText(doc.getString("Contact"));
                                    AgeCertificationLicenseValue.setText(doc.getString("License No"));
                                    GenderAddressPrescriptionCountValue.setText(doc.getString("Address"));
                                }
                            }
                        });
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (UserType.equals("PharmacyDb") && extras.getString("context").equals("PharmacyQRScan")) {
            Intent intent = new Intent(getApplicationContext(), PharmacyQRScanActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }
        finish();
    }

    private void dateImplication(String dateGiven) {
        Year = dateGiven.substring(0, 4);
        Month = dateGiven.substring(5, 7);
        Day = dateGiven.substring(8);
    }

    private void deriveAge() {
        // we have Day, Month and Year of Date Of Birth and today
        //here we need to get current date and compare it with dOB in Day,Month,Year and display age
        age = Integer.parseInt(currentYear) - Integer.parseInt(Year);
        if (Integer.parseInt(currentMonth) < Integer.parseInt(Month)) {
            age--;
        }
        if ((currentMonth.equals(Month)) && (Integer.parseInt(currentDay) < Integer.parseInt(Day))) {
            age--;
        }
    }

    //menu option initialised
    @Override
    public boolean onCreateOptionsMenu(Menu Menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, Menu);
        return true;
    }

    //For showing this menu only in profile page
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem EditBtn = menu.findItem(R.id.Edit);
        EditBtn.setVisible(true);
        return true;
    }

    //for redirecting when menu item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuItemLogOut_SettingsPage:
                deviceUser.signOut();
                intent = new Intent(getApplicationContext(), StartPageActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.menuItemMyProfile_SettingsPage:
                intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                intent.putExtra("UserId", id);
                intent.putExtra("UserType", UserType);
                startActivity(intent);
                finish();
                break;

            case R.id.Edit:
                ShowEditOptions();
                break;
        }
        return true;
    }

    private void ShowEditOptions() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileActivity.this);
        builder.setTitle("Choose Action..");

        switch (UserType) {
            case "UserDb": {
                String[] options = {"Edit Height", "Edit Weight", "Edit Allergies", "Edit Contact", "Change Password"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle the options selected
                        if (which == 0) {
                            ShowHeightWeightAllergiesDialog("Height");
                        } else if (which == 1) {
                            ShowHeightWeightAllergiesDialog("Weight");
                        } else if (which == 2) {
                            ShowHeightWeightAllergiesDialog("Allergies");
                        } else if (which == 3) {
                            ShowHeightWeightAllergiesDialog("Contact");
                        } else if (which == 4) {
                            ChangePassword();
                        }
                    }
                });
                break;
            }
            case "DoctorDb": {
                String[] options = {"Edit Contact", "Change Password"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            ShowHeightWeightAllergiesDialog("Contact");
                        } else if (which == 1) {
                            ChangePassword();
                        }
                    }
                });
                break;
            }
            case "PharmacyDb": {
                String[] options = {"Edit Contact", "Edit Address", "Change Password"};

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            ShowHeightWeightAllergiesDialog("Contact");
                        } else if (which == 1) {
                            ShowHeightWeightAllergiesDialog("Address");

                        } else if (which == 2) {
                            ChangePassword();
                        }
                    }
                });
                break;
            }
        }
        builder.create().show();
    }

    private void ChangePassword() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(MyProfileActivity.this).inflate(R.layout.dialog_change_password, null);

        final EditText CurrentPassword = view.findViewById(R.id.CurrentPassword);
        final EditText NewPassword = view.findViewById(R.id.NewPassword);
        final EditText ConfirmPassword = view.findViewById(R.id.ConfirmPassword);
        Button changePasswordButton = view.findViewById(R.id.ChangePassword);

        final AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileActivity.this);
        builder.setView(view);
        builder.create().show();

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPass = CurrentPassword.getText().toString().trim();
                newPass = NewPassword.getText().toString().trim();
                confirmPass = ConfirmPassword.getText().toString().trim();
                if (TextUtils.isEmpty(oldPass)) {
                    Toast.makeText(MyProfileActivity.this, "Please enter the old password", Toast.LENGTH_SHORT).show();
                    return;
                } else if (newPass.length() < 6) {
                    Toast.makeText(MyProfileActivity.this, "Password needs to be more then 6 digits", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!newPass.equals(confirmPass)) {
                    Toast.makeText(MyProfileActivity.this, "Password Confirmation does not match", Toast.LENGTH_SHORT).show();
                }
                builder.create().dismiss();

                //before changing password re-authenticate user
                firebaseUser = deviceUser.getCurrentUser();

                AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), oldPass);

                firebaseUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseUser.updatePassword(newPass).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //password updated
                                Toast.makeText(MyProfileActivity.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MyProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void ShowHeightWeightAllergiesDialog(final String field) {
        //Creates the Dialog box for all fields except password
        AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileActivity.this);
        builder.setTitle("Update " + field);
        LinearLayout linearLayout = new LinearLayout(MyProfileActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText textField = new EditText(MyProfileActivity.this);
        textField.setHint("New " + field);
        linearLayout.addView(textField);
        builder.setView(linearLayout);

        //adding buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String UpdatedValue = textField.getText().toString().trim();
                if (!TextUtils.isEmpty(UpdatedValue)) {

                    switch (field) {
                        case "Height":
                            db.collection(UserType).document(id).update(field, UpdatedValue + "feet").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MyProfileActivity.this, "Field updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;

                        case "Weight":
                            db.collection(UserType).document(id).update(field, UpdatedValue + "Kgs").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MyProfileActivity.this, "Field updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;

                        case "Contact":
                            if (UpdatedValue.length() != 10) {
                                Toast.makeText(MyProfileActivity.this, "Contact Must have 10 digits", Toast.LENGTH_SHORT).show();
                            } else {
                                db.collection(UserType).document(id).update(field, UpdatedValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MyProfileActivity.this, "Field updated", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            break;

                        default:
                            db.collection(UserType).document(id).update(field, UpdatedValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MyProfileActivity.this, "Field updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                    }
                } else {
                    Toast.makeText(MyProfileActivity.this, "Please enter the field to update", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}