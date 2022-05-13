package com.urjalusa.presec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class DoctorPatientHistoryActivity extends AppCompatActivity {

    private EditText patientId;
    private TextView patientName;
    private TextView patientBGroup;
    private TextView allergies;
    private FirebaseAuth deviceUser;
    private FirebaseFirestore db;
    private String patientIdVal;
    private RecyclerView recyclerView;
    private RecyclerAdapterAndViewHolder recyclerAdapter;
    private ArrayList<PrescriptionCardView> prescriptionList;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_patient_history);
        Button createPrescriptionButton = findViewById(R.id.buttonViewCreatePrescription_NavigationLayoutDoctor);
        Button doctorMadePrescriptionsButton = findViewById(R.id.buttonViewMyPrescriptions_NavigationLayoutDoctor);
        patientId = findViewById(R.id.editTextPatientId_DoctorPatientHistoryPage);
        patientName = findViewById(R.id.textViewPatientNameData_DoctorPatientHistoryPage);
        patientBGroup = findViewById(R.id.textViewBloodGroupData_DoctorPatientHistoryPage);
        allergies = findViewById(R.id.textViewAllergiesData_DoctorPatientHistoryPage);
        deviceUser = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prescriptionList = new ArrayList<>();

        //get doctorId from previous page
        Bundle extras = getIntent().getExtras();
        id = extras.getString("id");
        final String name = extras.getString("name");

        //if doctor wants to make new prescription
        createPrescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DoctorCreatePrescriptionActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                startActivity(intent);
                finish();
            }
        });

        //if doctor wants to view his own prescriptions
        doctorMadePrescriptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DoctorMyPrescriptionsActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                startActivity(intent);
                finish();
            }
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
                loadData();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //not required but part of syntax
            }
        });
    }

    private void loadData() {
        if (patientId.getText().toString().equals("")) {
            patientName.setText(" ");
            patientBGroup.setText(" ");
            allergies.setText(" ");
            return;
        }
        patientIdVal = patientId.getText().toString();

        db.collection("UserDb").document(patientIdVal).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        String patientNameVal = document.getString("Name");
                        patientName.setText(patientNameVal);
                        if (!patientName.equals("")) {
                            prescriptionList.clear();
                        }
                        String patientBGroupVal = document.getString("Blood Group");
                        patientBGroup.setText(patientBGroupVal);
                        String patientAllergiesVal = document.getString("Allergies");
                        allergies.setText(patientAllergiesVal);

                        //search for records in firestore
                        db.collection("PrescriptionDb").whereEqualTo("Patient Id", patientIdVal).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot eachDoc : Objects.requireNonNull(task.getResult())) {
                                                String prescriptionId = eachDoc.getString("Prescription Id");
                                                String date = eachDoc.getString("Date");
                                                String docName = eachDoc.getString("Doctor Name");
                                                prescriptionList.add(new PrescriptionCardView(prescriptionId, docName, date));
                                            }
                                        }
                                        recyclerView.hasFixedSize();
                                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                        recyclerAdapter = new RecyclerAdapterAndViewHolder(getApplicationContext(), prescriptionList);
                                        recyclerView.setAdapter(recyclerAdapter);
                                    }
                                });
                        recyclerView = findViewById(R.id.recyclerView_DoctorPatientHistoryPage);

                        recyclerView.addOnItemTouchListener(
                                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                                    @Override
                                    public void OnItemClick(int position) {
                                        Intent intent = new Intent(getApplicationContext(), ViewPrescriptionActivity.class);
                                        intent.putExtra("Prescription Id", prescriptionList.get(position).getPrescriptionId());
                                        intent.putExtra("context", "DoctorPatientHistoryPage");
                                        startActivity(intent);
                                    }
                                })
                        );
                    } else {
                        //toast that invalid user is present
                        Toast.makeText(getApplicationContext(), "Invalid User", Toast.LENGTH_SHORT).show();
                    }
                });
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
}