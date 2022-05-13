package com.urjalusa.presec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class PatientPrescriptionListActivity extends AppCompatActivity {

    private FirebaseAuth deviceUser;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private RecyclerAdapterAndViewHolder recyclerAdapter;
    private ArrayList<PrescriptionCardView> prescriptionList;
    private String prescriptionId;
    private String id;
    private TextView ifNoPrescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_prescription_list);
        deviceUser = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prescriptionList = new ArrayList<>();
        ifNoPrescription = findViewById(R.id.textViewIfNoPrescription_PatientPrescriptionPage);

        //get patientId from login/load
        Bundle extras = getIntent().getExtras();
        id = extras.getString("id");

        //integrate query to recycler
        db.collection("PrescriptionDb").whereEqualTo("Patient Id", id).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot eachDoc : Objects.requireNonNull(task.getResult())) {
                                String prescriptionId = eachDoc.getString("Prescription Id");
                                String date = eachDoc.getString("Date");
                                String docName = eachDoc.getString("Doctor Name");
                                ifNoPrescription.setVisibility(View.INVISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                                prescriptionList.add(new PrescriptionCardView(prescriptionId, docName, date));
                            }
                        }
                        recyclerView.hasFixedSize();
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                        recyclerAdapter = new RecyclerAdapterAndViewHolder(getApplicationContext(), prescriptionList);
                        recyclerView.setAdapter(recyclerAdapter);
                    }
                });
        recyclerView = findViewById(R.id.recyclerView_PatientPrescriptionListPage);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void OnItemClick(int position) {
                        prescriptionId = prescriptionList.get(position).getPrescriptionId();
                        db.collection("PrescriptionDb").document(prescriptionId).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                            assert doc != null;
                                            Boolean EncryptionStatus = doc.getBoolean("Encryption Status");
                                            Intent intent;
                                            if (EncryptionStatus) {
                                                intent = new Intent(getApplicationContext(), PatientQRDisplayActivity.class);
                                                intent.putExtra("PrescriptionId", prescriptionId);
                                            } else {
                                                intent = new Intent(getApplicationContext(), ViewPrescriptionActivity.class);
                                                intent.putExtra("Prescription Id", prescriptionId);
                                                intent.putExtra("context", "PatientPrescriptionListPage");
                                            }
                                            startActivity(intent);
                                        }
                                    }
                                });
                    }
                })
        );
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
                intent = new Intent(getApplicationContext(), StartPageActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.menuItemMyProfile_SettingsPage:
                intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                intent.putExtra("UserId", id);
                intent.putExtra("UserType", "UserDb");
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
}