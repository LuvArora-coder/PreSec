package com.urjalusa.presec;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DoctorCreatePrescriptionActivity extends AppCompatActivity {

    private EditText patientId;
    private TextView patientName;
    private TextView patientAge;
    private TextView patientHeight;
    private TextView patientWeight;
    private TextView allergies;
    private EditText symptoms;
    private EditText medicine;
    private CheckBox medicineMorning;
    private CheckBox medicineAfternoon;
    private CheckBox medicineEvening;
    private EditText medicineQty;
    private EditText medicineDuration;
    private TextView viewMedicines;
    private FirebaseAuth deviceUser;
    private FirebaseFirestore db;
    private String name;
    private String id;
    private String prescriptionCount;
    private String patientIdVal;
    private String prescriptionId;
    private String patientNameVal;
    private String Year;
    private String Month;
    private String Day;
    private String Hour;
    private String Minute;
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String dateOfBirth = null;
    private int age = 0;
    private String currentDay;
    private String currentMonth;
    private String currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_create_prescription);
        Button doctorMadePrescriptionsButton = findViewById(R.id.buttonViewMyPrescriptions_NavigationLayoutDoctor);
        Button userSpecificHistoryButton = findViewById(R.id.buttonViewPatientHistory_NavigationLayoutDoctor);
        deviceUser = FirebaseAuth.getInstance();
        patientId = findViewById(R.id.editTextPatientId_DoctorCreatePrescriptionPage);
        patientName = findViewById(R.id.textViewPatientNameData_DoctorCreatePrescriptionPage);
        patientAge = findViewById(R.id.textViewAgeData_DoctorCreatePrescriptionPage);
        patientHeight = findViewById(R.id.textViewHeightData_DoctorCreatePrescriptionPage);
        patientWeight = findViewById(R.id.textViewWeightData_DoctorCreatePrescriptionPage);
        allergies = findViewById(R.id.textViewAllergiesData_DoctorCreatePrescriptionPage);
        symptoms = findViewById(R.id.editTextSymptoms_DoctorCreatePrescriptionPage);
        medicine = findViewById(R.id.editTextMedicine_DoctorCreatePrescriptionPage);
        medicineMorning = findViewById(R.id.checkBoxMorning_DoctorCreatePrescriptionPage);
        medicineAfternoon = findViewById(R.id.checkBoxAfternoon_DoctorCreatePrescriptionPage);
        medicineEvening = findViewById(R.id.checkBoxEvening_DoctorCreatePrescriptionPage);
        medicineQty = findViewById(R.id.editTextMedicineQuantity_DoctorCreatePrescriptionPage);
        medicineDuration = findViewById(R.id.editTextMedicineDuration_DoctorCreatePrescriptionPage);
        viewMedicines = findViewById(R.id.textViewViewMedicines_DoctorCreatePrescriptionPage);
        Button addMedicine = findViewById(R.id.buttonAddMedicine_DoctorCreatePrescriptionPage);
        Button removeButton = findViewById(R.id.buttonRemoveMedicine_DoctorCreatePrescriptionPage);
        Button createPrescriptionButton = findViewById(R.id.buttonCreatePrescription_DoctorCreatePrescriptionPage);
        db = FirebaseFirestore.getInstance();

        //get doctorId from previous page
        Bundle extras = getIntent().getExtras();
        id = extras.getString("id");

        //get name from Id
        db.collection("DoctorDb").document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                name = document.getString("Name");
                prescriptionCount = document.getString("Prescriptions made");
            }
        });

        //if doctor wants to see his own made prescriptions
        doctorMadePrescriptionsButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), DoctorMyPrescriptionsActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("name", name);
            startActivity(intent);
            finish();
        });

        //if doctor wants to see prescriptions of a specific user
        userSpecificHistoryButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), DoctorPatientHistoryActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("name", name);
            startActivity(intent);
            finish();
        });

        //patient name and allergies should appear on its own when id entered
        patientId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //not required but part of syntax
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //not required but part of syntax
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                if (patientId.getText().toString().equals("")) {
                    return;
                }
                patientIdVal = patientId.getText().toString();
                dateOfBirth = null;
                prescriptionId = patientIdVal + id + prescriptionCount;

                db.collection("UserDb").document(patientIdVal).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        patientAge.setText(" ");
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        patientNameVal = document.getString("Name");
                        patientName.setText(patientNameVal);
                        dateOfBirth = document.getString("DoB");
                        if (dateOfBirth != null) {
                            dateImplication(dateOfBirth.substring(0, 10));
                            final String currentTimeStampVal = dateFormat.format(new Date());
                            currentDay = currentTimeStampVal.substring(8, 10);
                            currentMonth = currentTimeStampVal.substring(5, 7);
                            currentYear = currentTimeStampVal.substring(0, 4);
                            deriveAge();
                            patientAge.setText(age + "");
                        }
                        String patientHeightVal = document.getString("Height");
                        patientHeight.setText(patientHeightVal);
                        String patientWeightVal = document.getString("Weight");
                        patientWeight.setText(patientWeightVal);
                        String patientAllergiesVal = document.getString("Allergies");
                        allergies.setText(patientAllergiesVal);
                    } else {
                        //toast that invalid user is present
                        Toast.makeText(getApplicationContext(), "Invalid User", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        medicine.setHint("Generic / Brand Name");
        viewMedicines.setText("");

        //enters one entry of medicine
        addMedicine.setOnClickListener(view -> {
            if (medicine.getText().toString().equals("")) {
                medicine.setError("Medicine cannot be empty");
                return;
            } else if (medicineQty.getText().toString().equals("")) {
                medicineQty.setError("Quantity of medicine to be taken cannot be empty");
                return;
            } else if (medicineDuration.getText().toString().equals("")) {
                medicineDuration.setError("Duration of medicine to be taken cannot be empty");
                return;
            } else if ((!medicineMorning.isChecked()) & (!medicineAfternoon.isChecked()) & (!medicineEvening.isChecked())) {
                Toast.makeText(getApplicationContext(), "Select when to give medicine", Toast.LENGTH_LONG).show();
                return;
            }
            String medicineList = viewMedicines.getText().toString();
            String medicineVal = medicine.getText() + "~";
            if (medicineMorning.isChecked()) {
                medicineVal += "Morning" + "~";
            }
            if (medicineAfternoon.isChecked()) {
                medicineVal += "Afternoon" + "~";
            }
            if (medicineEvening.isChecked()) {
                medicineVal += "Evening" + "~";
            }
            medicineVal += medicineQty.getText().toString() + "~";
            medicineVal += medicineDuration.getText().toString();
            if (medicineList.equals("")) {
                medicineList = medicineVal;
            } else {
                medicineList += '\n' + medicineVal;
            }
            viewMedicines.setText(medicineList);
            medicine.setText("");
            medicineMorning.setChecked(false);
            medicineAfternoon.setChecked(false);
            medicineEvening.setChecked(false);
            medicineQty.setText("");
            medicineDuration.setText("");
        });

        //removes the last added medicine
        removeButton.setOnClickListener(view -> {
            String medicineList = viewMedicines.getText().toString();
            if (medicineList.contains("\n")) {
                int lastId = 0;
                for (int i = 0; i < medicineList.length(); i++) {
                    if (medicineList.charAt(i) == '\n') {
                        lastId = i;
                    }
                }
                char[] medicineListUpdated = new char[lastId];
                medicineList.getChars(0, lastId, medicineListUpdated, 0);
                medicineList = String.valueOf(medicineListUpdated);
            } else {
                medicineList = "";
            }
            viewMedicines.setText(medicineList);
        });

        //create prescription, encrypt it and save in storage
        createPrescriptionButton.setOnClickListener(view -> {
            //define prescription id
            prescriptionId = patientIdVal + id + prescriptionCount;

            //Check if all fields entered
            if (patientName.getText().toString().equals("")
                    || symptoms.getText().toString().equals("")
                    || viewMedicines.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Fill the required fields", Toast.LENGTH_SHORT).show();
            } else {
                //date would be by default the current date of the prescription made
                final String timeStampVal = dateFormat.format(new Date());
                dateImplication(timeStampVal.substring(0, 10));
                timeImplication(timeStampVal.substring(10));

                //create string value of prescription
                String prescription = "PatientId : " + patientId.getText().toString()
                        + "\nPatientName : " + patientName.getText().toString()
                        + "\nDate : " + timeStampVal + "\nSymptoms : "
                        + symptoms.getText().toString() + "\nMedicines : "
                        + viewMedicines.getText().toString();

                //encrypt it
                String encryptedOutput = null;
                //encryption -> read file to string>convert to byte>generate a key>encrypt with hashed key>convert to string>write to text file
                try {
                    encryptedOutput = encryptFunction(prescription);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //increment prescription count for that doctor and create document in prescriptionDb
                //IncrementCount(prescriptionCount);
                Map<String, Object> prescriptionRecord = new HashMap<>();
                //add fields to document
                prescriptionRecord.put("Date", timeStampVal);
                prescriptionRecord.put("Doctor Id", id);
                prescriptionRecord.put("Doctor Name", name);
                prescriptionRecord.put("Encryption Status", true);
                prescriptionRecord.put("Patient Id", patientIdVal);
                prescriptionRecord.put("Patient Name", patientNameVal);
                prescriptionRecord.put("Prescription Details", encryptedOutput);
                prescriptionRecord.put("Prescription Id", prescriptionId);
                db.collection("PrescriptionDb").document(prescriptionId).set(prescriptionRecord);
                IncrementCount(prescriptionCount);
                //empty all fields
                patientId.setText("");
                patientName.setText("");
                allergies.setText("");
                symptoms.setText("");
                medicine.setText("");
                patientHeight.setText("");
                patientWeight.setText("");
                medicineMorning.setChecked(false);
                medicineAfternoon.setChecked(false);
                medicineEvening.setChecked(false);
                medicineQty.setText("");
                medicineDuration.setText("");
                viewMedicines.setText("");
                patientAge.setText("");
            }
        });
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

    //for showing settings menu
    @Override
    public boolean onCreateOptionsMenu(Menu Menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, Menu);
        return true;
    }

    //for redirecting to menu item selected
    @SuppressLint("NonConstantResourceId")
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
                intent.putExtra("UserType", "DoctorDb");
                startActivity(intent);
                break;
        }
        return true;
    }

    //to forcefully exit app when pressed back from this activity
    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    private String encryptFunction(String prescription) throws Exception {
        String generateKeyingParameter = Day + Month + Year + Hour + Minute;
        SecretKeySpec encryptionKey = hashKeyGenerator(generateKeyingParameter); // timestamp format used ddMMyyhhmm
        @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        byte[] encVal = cipher.doFinal(prescription.getBytes());
        return Base64.encodeToString(encVal, Base64.DEFAULT);
    }

    private SecretKeySpec hashKeyGenerator(String keyingParameter) throws Exception {
        byte[] inputData = keyingParameter.getBytes();
        byte[] outputData;
        MessageDigest shaVal = MessageDigest.getInstance("SHA-256");
        shaVal.update(inputData);
        outputData = shaVal.digest();
        return new SecretKeySpec(outputData, "AES");
    }

    private void dateImplication(String dateGiven) {
        Year = dateGiven.substring(0, 4);
        Month = dateGiven.substring(5, 7);
        Day = dateGiven.substring(8);
    }

    private void timeImplication(String timeGiven) {
        timeGiven = timeGiven.trim();
        Hour = timeGiven.substring(0, 2);
        Minute = timeGiven.substring(3, 5);
    }

    private void IncrementCount(String Count) {
        int digit1;
        int digit2;
        int digit3;
        int digit4;
        int digit5;
        int digit6;

        char Char1 = Count.charAt(0);
        char Char2 = Count.charAt(1);
        char Char3 = Count.charAt(2);
        char Char4 = Count.charAt(3);
        char Char5 = Count.charAt(4);
        char Char6 = Count.charAt(5);

        digit1 = Character.getNumericValue(Char1);
        digit2 = Character.getNumericValue(Char2);
        digit3 = Character.getNumericValue(Char3);
        digit4 = Character.getNumericValue(Char4);
        digit5 = Character.getNumericValue(Char5);
        digit6 = Character.getNumericValue(Char6);

        if (digit6 > 8) {
            digit6 = 0;
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
        } else {
            digit6 = (digit6 + 1);
        }
        Char1 = Character.forDigit(digit1, 10);
        Char2 = Character.forDigit(digit2, 10);
        Char3 = Character.forDigit(digit3, 10);
        Char4 = Character.forDigit(digit4, 10);
        Char5 = Character.forDigit(digit5, 10);
        Char6 = Character.forDigit(digit6, 10);
        Count = Character.toString(Char1) + Char2 + Char3 + Char4 + Char5 + Char6;
        prescriptionCount = Count;

        //update count in its document
        DocumentReference documentReference = db.collection("DoctorDb").document(id);
        documentReference.update("Prescriptions made", Count).addOnSuccessListener(aVoid -> {
        });
    }
}