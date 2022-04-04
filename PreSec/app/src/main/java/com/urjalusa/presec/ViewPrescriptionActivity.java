package com.urjalusa.presec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class ViewPrescriptionActivity extends AppCompatActivity {

    private FirebaseAuth deviceUser;
    private FirebaseFirestore db;
    private String patientName;
    private String doctorName;
    private String date;
    private String prescriptionDetails;
    private TextView patientNameView;
    private TextView doctorNameView;
    private TextView dateView;
    private TextView symptomsView;
    private TextView medicineListView;
    private String doctorId;
    private String patientId;
    private String pharmacyId;
    private TextView timingsListView;
    private TextView quantityListView;
    private TextView durationListView;
    private String prescriptionCount;
    private Button regenerate;
    private String Year;
    private String Month;
    private String Day;
    private String Hour;
    private String Minute;
    private Boolean encryptionStatus;
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String PrescriptionId;
    private String Context;
    private String prescriptionDetailCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_prescription);
        TextView prescriptionIdView = findViewById(R.id.textViewPresIdData_ViewPrescription);
        patientNameView = findViewById(R.id.textViewPatientNameData_ViewPrescription);
        doctorNameView = findViewById(R.id.textViewDoctorNameData_ViewPrescription);
        dateView = findViewById(R.id.textViewDateData_ViewPrescription);
        symptomsView = findViewById(R.id.textViewSymptomData_ViewPrescription);
        medicineListView = findViewById(R.id.MedicineData_ViewPrescription);
        timingsListView = findViewById(R.id.TimeData_ViewPrescription);
        quantityListView = findViewById(R.id.QtyData_ViewPrescription);
        durationListView = findViewById(R.id.DurationData_ViewPrescription);
        regenerate = findViewById(R.id.buttonRegenerateForDoctor_ViewPrescription);
        deviceUser = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //getting data from respective page
        Intent intent = getIntent();
        PrescriptionId = intent.getStringExtra("Prescription Id");
        Context = intent.getStringExtra("context");
        prescriptionIdView.setText(PrescriptionId);

        if (Context.equals("DoctorMyPrescriptionsPage")) {
            regenerate.setVisibility(View.VISIBLE);
            regenerate.setClickable(true);
        } else if (Context.equals("PharmacyPage")) {
            pharmacyId = intent.getStringExtra("id");
            Toast.makeText(getApplicationContext(), pharmacyId, Toast.LENGTH_SHORT).show();
        }

        //Toast.makeText(getApplicationContext(), PrescriptionId, Toast.LENGTH_SHORT).show();
        db.collection("PrescriptionDb").document(PrescriptionId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot prescriptionRecord = task.getResult();
                        patientName = prescriptionRecord.getString("Patient Name");
                        doctorName = prescriptionRecord.getString("Doctor Name");
                        date = prescriptionRecord.getString("Date");
                        encryptionStatus = prescriptionRecord.getBoolean("Encryption Status");
                        prescriptionDetails = prescriptionRecord.getString("Prescription Details");
                        prescriptionDetailCopy = prescriptionDetails;
                        doctorId = prescriptionRecord.getString("Doctor Id");
                        patientId = prescriptionRecord.getString("Patient Id");
                        patientNameView.setText(patientName);
                        doctorNameView.setText(doctorName);
                        dateView.setText(date.substring(0, 10));
                        if (encryptionStatus) {
                            Toast.makeText(ViewPrescriptionActivity.this, "These medicines are not consumed by the patient", Toast.LENGTH_LONG).show();
                            regenerate.setVisibility(View.INVISIBLE);
                            regenerate.setClickable(false);
                            DecryptPrescription();
                        }
                        retrieveAndDisplay();
                    }
                });

        regenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("DoctorDb").document(doctorId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    prescriptionCount = document.getString("Prescriptions made");
                                    PrescriptionId = patientId + doctorId + prescriptionCount;

                                    final String timeStampVal = dateFormat.format(new Date());
                                    dateImplication(timeStampVal.substring(0, 10));
                                    timeImplication(timeStampVal.substring(10));

                                    String encryptedOutput = null;
                                    try {
                                        encryptedOutput = encryptFunction(prescriptionDetailCopy);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    Map<String, Object> prescriptionRecord = new HashMap<>();
                                    prescriptionRecord.put("Date", timeStampVal);
                                    prescriptionRecord.put("Doctor Id", doctorId);
                                    prescriptionRecord.put("Doctor Name", doctorName);
                                    prescriptionRecord.put("Encryption Status", true);
                                    prescriptionRecord.put("Patient Id", patientId);
                                    prescriptionRecord.put("Patient Name", patientName);
                                    prescriptionRecord.put("Prescription Details", encryptedOutput);
                                    prescriptionRecord.put("Prescription Id", PrescriptionId);
                                    db.collection("PrescriptionDb").document(PrescriptionId).set(prescriptionRecord);
                                    IncrementCount(prescriptionCount);
                                    //Toast
                                    Toast.makeText(getApplicationContext(), "Prescription Recreated", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void DecryptPrescription() {
        try {
            String dateGiven = date.substring(0, 10);
            String timeGiven = date.substring(10);
            Year = dateGiven.substring(0, 4);
            Month = dateGiven.substring(5, 7);
            Day = dateGiven.substring(8);
            timeGiven = timeGiven.trim();
            Hour = timeGiven.substring(0, 2);
            Minute = timeGiven.substring(3, 5);
            String decryptInputText = prescriptionDetails;
            String generateKeyingParameter = Day + Month + Year + Hour + Minute;
            SecretKeySpec decryptionKey = hashKeyGenerator(generateKeyingParameter); // timestamp format used ddMMyyyyhhmm
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey);
            byte[] decVal = Base64.decode(decryptInputText, Base64.DEFAULT);
            byte[] decryptedOutputByte = cipher.doFinal(decVal);
            prescriptionDetails = new String(decryptedOutputByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void retrieveAndDisplay() {
        //prescriptionDetails is a global variable can be retrieved directly
        int i;
        int length = prescriptionDetails.length();

        String currentField;
        while (true) {
            for (i = 0; i < length; i++) {
                if (prescriptionDetails.charAt(i) == '\n')
                    break;
            }
            currentField = prescriptionDetails.substring(0, i);
            displayField(currentField);
            prescriptionDetails = prescriptionDetails.substring(i + 1);
            length = prescriptionDetails.length();
            if (prescriptionDetails.startsWith("Medicines")) {
                substituteMedicines(prescriptionDetails);
                break;
            }
        }
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

    private void substituteMedicines(String prescriptionDetails) {
        String medicineRecord;
        prescriptionDetails = prescriptionDetails.substring(12);
        while (prescriptionDetails.contains("\n")) {
            int getIndex = prescriptionDetails.indexOf('\n');
            medicineRecord = prescriptionDetails.substring(0, getIndex);
            prescriptionDetails = prescriptionDetails.substring(getIndex + 1);
            processRecord(medicineRecord);
        }
        processRecord(prescriptionDetails);
    }

    private void processRecord(String medicineRecord) {
        int firstDelimiter;
        int lastDelimiter;
        int sLastDelimiter;
        String medicine;
        String duration;
        String quantity;
        String timings;

        firstDelimiter = medicineRecord.indexOf('~');
        lastDelimiter = medicineRecord.lastIndexOf('~');
        medicine = medicineRecord.substring(0, firstDelimiter);
        duration = medicineRecord.substring(lastDelimiter + 1);
        medicineRecord = medicineRecord.substring(firstDelimiter + 1, lastDelimiter);
        sLastDelimiter = medicineRecord.lastIndexOf('~');
        quantity = medicineRecord.substring(sLastDelimiter + 1);
        timings = medicineRecord.substring(0, sLastDelimiter);

        String medicineList = medicineListView.getText().toString();
        medicineList += '\n' + medicine;
        medicineListView.setText(medicineList);
        String timingsList = timingsListView.getText().toString();
        timingsList += '\n' + timings;
        timingsListView.setText(timingsList);
        String quantityList = quantityListView.getText().toString();
        quantityList += '\n' + quantity;
        quantityListView.setText(quantityList);
        String durationList = durationListView.getText().toString();
        durationList += '\n' + duration;
        durationListView.setText(durationList);
    }

    private void displayField(String currentField) {
        int dividerIndex = currentField.indexOf(':');
        String key = currentField.substring(0, dividerIndex).trim();
        String value = currentField.substring(dividerIndex + 1).trim();
        if ("Symptoms".equals(key)) {
            symptomsView.setText(value);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Context.equals("PharmacyPage")) {
            Intent intent = new Intent(getApplicationContext(), PharmacyQRScanActivity.class);
            intent.putExtra("id", pharmacyId);
            startActivity(intent);
        }
        finish();
    }

    private void timeImplication(String timeGiven) {
        timeGiven = timeGiven.trim();
        Hour = timeGiven.substring(0, 2);
        Minute = timeGiven.substring(3, 5);
    }

    private void dateImplication(String dateGiven) {
        Year = dateGiven.substring(0, 4);
        Month = dateGiven.substring(5, 7);
        Day = dateGiven.substring(8);
    }

    //for showing settings menu
    @Override
    public boolean onCreateOptionsMenu(Menu Menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, Menu);
        return true;
    }

    //for redirecting to selected menu item
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
                if (Context.equals("DoctorPatientHistoryPage") || Context.equals("DoctorMyPrescriptionsPage")) {
                    intent.putExtra("UserId", doctorId);
                    intent.putExtra("UserType", "DoctorDb");
                } else if (Context.equals("PatientPrescriptionListPage")) {
                    intent.putExtra("UserId", patientId);
                    intent.putExtra("UserType", "UserDb");
                } else {
                    intent.putExtra("UserId", pharmacyId);
                    intent.putExtra("UserType", "PharmacyDb");
                    intent.putExtra("context", "ViewPrescription");
                }
                startActivity(intent);
                break;
        }
        return true;
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
        DocumentReference documentReference = db.collection("DoctorDb").document(doctorId);
        documentReference.update("Prescriptions made", Count).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        });
    }
}
