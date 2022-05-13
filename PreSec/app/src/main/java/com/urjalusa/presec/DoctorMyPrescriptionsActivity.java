package com.urjalusa.presec;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DoctorMyPrescriptionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerAdapterAndViewHolder recyclerAdapter;
    private ArrayList<PrescriptionCardView> prescriptionList;
    private FirebaseAuth deviceUser;
    private String id;
    private TextView ifNoPrescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_my_prescriptions);
        Button createPrescriptionButton = findViewById(R.id.buttonViewCreatePrescription_NavigationLayoutDoctor);
        Button userSpecificHistoryButton = findViewById(R.id.buttonViewPatientHistory_NavigationLayoutDoctor);
        deviceUser = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        prescriptionList = new ArrayList<>();
        ifNoPrescription = findViewById(R.id.textViewIfNoPrescription_MyPrescriptionPage);

        //get doctorId from previous page
        Bundle extras = getIntent().getExtras();
        id = extras.getString("id");
        final String name = extras.getString("name");

        //integrate query to recycler
        db.collection("PrescriptionDb").whereEqualTo("Doctor Id", id).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot eachDoc : task.getResult()) {
                                String prescriptionId = eachDoc.getString("Prescription Id");
                                String date = eachDoc.getString("Date");
                                String patName = eachDoc.getString("Patient Name");
                                ifNoPrescription.setVisibility(View.INVISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                                prescriptionList.add(new PrescriptionCardView(prescriptionId, patName, date));
                            }
                        }
                        recyclerView.hasFixedSize();
                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerAdapter = new RecyclerAdapterAndViewHolder(getApplicationContext(), prescriptionList);
                        recyclerView.setAdapter(recyclerAdapter);
                    }
                });
        recyclerView = findViewById(R.id.recyclerView_DoctorMyPrescriptionsPage);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void OnItemClick(int position) {
                        Intent intent = new Intent(getApplicationContext(), ViewPrescriptionActivity.class);
                        intent.putExtra("Prescription Id", prescriptionList.get(position).getPrescriptionId());
                        intent.putExtra("context", "DoctorMyPrescriptionsPage");
                        startActivity(intent);
                    }
                })
        );

        //if doctor wants to make a new prescription
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

        //if doctor wants to view prescriptions of a specific user
        userSpecificHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DoctorPatientHistoryActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                startActivity(intent);
                finish();
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