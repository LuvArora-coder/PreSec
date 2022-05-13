package com.urjalusa.presec;

import static androidmads.library.qrgenearator.QRGContents.Type.TEXT;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGEncoder;

public class PatientQRDisplayActivity extends AppCompatActivity {

    private String UserId;
    private FirebaseAuth deviceUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_qr_display);
        deviceUser = FirebaseAuth.getInstance();
        ImageView qrImage = findViewById(R.id.QRCode);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        UserId = extras.getString("id");
        String Id = extras.getString("PrescriptionId");

        WindowManager WManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        assert WManager != null;
        Display display = WManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = Math.min(width, height);
        smallerDimension = (smallerDimension * 3) / 4;
        QRGEncoder qrgEncoder = new QRGEncoder(Id, null, TEXT, smallerDimension);
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            // Setting Bitmap to ImageView
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            String TAG = "GenerateQRCode";
            Log.v(TAG, e.toString());
        }
    }

    //for showing settings
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
                intent = new Intent(getApplicationContext(), StartPageActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.menuItemMyProfile_SettingsPage:
                intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                intent.putExtra("UserId", UserId);
                intent.putExtra("UserType", "UserDb");
                startActivity(intent);
                break;
        }
        return true;
    }
}