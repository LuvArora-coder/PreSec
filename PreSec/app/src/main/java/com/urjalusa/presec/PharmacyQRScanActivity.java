package com.urjalusa.presec;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class PharmacyQRScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private FirebaseAuth deviceUser;
    private ZXingScannerView PharmacyScanner;
    private String QRCodeResult;
    private FirebaseFirestore db;
    private Boolean EncryptionStatus;
    private String Prescription;
    private DocumentReference documentReference;
    private String decryptedOutput;
    private String Year;
    private String Month;
    private String Day;
    private String Hour;
    private String Minute;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_qr_scan);
        PharmacyScanner = findViewById(R.id.Scans_QRCode);
        deviceUser = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Bundle extras = getIntent().getExtras();
        id = extras.getString("id");

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        PharmacyScanner.setResultHandler(PharmacyQRScanActivity.this);
                        PharmacyScanner.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(PharmacyQRScanActivity.this, "You Must accept these permissions", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    }
                }).check();
    }

    @Override
    protected void onDestroy() {
        PharmacyScanner.stopCamera();
        super.onDestroy();
    }

    //for showing settings menu
    @Override
    public boolean onCreateOptionsMenu(Menu Menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, Menu);
        return true;
    }

    //for redirecting when menu item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuItemLogOut_SettingsPage:
                deviceUser.signOut();
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.menuItemMyProfile_SettingsPage:
                intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                intent.putExtra("UserId", id);
                intent.putExtra("UserType", "PharmacyDb");
                intent.putExtra("context", "PharmacyQRScan");
                startActivity(intent);
                finish();
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

    @Override
    public void handleResult(Result rawResult) {

        QRCodeResult = rawResult.getText();
        db.collection("PrescriptionDb").document(QRCodeResult).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot Document = task.getResult();
                        EncryptionStatus = Document.getBoolean("Encryption Status");
                        if (EncryptionStatus) {
                            DecryptPrescription();
                            documentReference = db.collection("PrescriptionDb").document(QRCodeResult);
                            documentReference.update("Encryption Status", false);
                            //recreate();
                        } else {
                            Toast.makeText(PharmacyQRScanActivity.this, "Medicine already provided", Toast.LENGTH_SHORT).show();
                            recreate();

                        }


                    } else {
                        Toast.makeText(PharmacyQRScanActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        PharmacyScanner.startCamera();
    }

    private void DecryptPrescription() {
        db.collection("PrescriptionDb").document(QRCodeResult).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot PrescriptionDoc = task.getResult();
                Prescription = PrescriptionDoc.getString("Prescription Details");
                String timeStampVal = PrescriptionDoc.getString("Date");
                decryptedOutput = null;
                try {
                    String dateGiven = timeStampVal.substring(0, 10);
                    String timeGiven = timeStampVal.substring(10);
                    Year = dateGiven.substring(0, 4);
                    Month = dateGiven.substring(5, 7);
                    Day = dateGiven.substring(8);
                    timeGiven = timeGiven.trim();
                    Hour = timeGiven.substring(0, 2);
                    Minute = timeGiven.substring(3, 5);
                    String decryptInputText = Prescription;
                    String generateKeyingParameter = Day + Month + Year + Hour + Minute;
                    SecretKeySpec decryptionKey = hashKeyGenerator(generateKeyingParameter); // timestamp format used ddMMyyhhmm
                    @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.DECRYPT_MODE, decryptionKey);
                    byte[] decVal = Base64.decode(decryptInputText, Base64.DEFAULT);
                    byte[] decryptedOutputByte = cipher.doFinal(decVal);
                    decryptedOutput = new String(decryptedOutputByte);
                    documentReference = db.collection("PrescriptionDb").document(QRCodeResult);
                    documentReference.update("Prescription Details", decryptedOutput);
                    Intent intent = new Intent(PharmacyQRScanActivity.this, ViewPrescriptionActivity.class);
                    intent.putExtra("Prescription Id", QRCodeResult);
                    intent.putExtra("context", "PharmacyPage");
                    intent.putExtra("id", id);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private SecretKeySpec hashKeyGenerator(String keyingParameter) throws Exception {
        byte[] inputData = keyingParameter.getBytes();
        byte[] outputData;
        MessageDigest shaVal = MessageDigest.getInstance("SHA-256");
        shaVal.update(inputData);
        outputData = shaVal.digest();
        return new SecretKeySpec(outputData, "AES");
    }
}