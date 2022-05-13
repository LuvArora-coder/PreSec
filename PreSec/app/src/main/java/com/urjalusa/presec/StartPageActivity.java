package com.urjalusa.presec;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StartPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
        ViewPager screenPager = findViewById(R.id.screen_viewPager);
        Button login = findViewById(R.id.buttonLoginStartPage);
        Button signUp = findViewById(R.id.buttonSignUpStartPage);
        TabLayout tabIndicator = findViewById(R.id.tab_indicator);

        List<ScreenFragment> screenFragmentList = new ArrayList<>();
        screenFragmentList.add(new ScreenFragment("Expert Solutions, Outstanding Service", R.drawable.common_platform_fragment_1));
        screenFragmentList.add(new ScreenFragment("More Intelligent, More Secured", R.drawable.secured_prescriptions_frragment_2));
        screenFragmentList.add(new ScreenFragment("Any Time,\t\t\t\tAny Where,\t\t\t\tAny Device", R.drawable.accessible_everywhere_fragment_3));

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, screenFragmentList);
        screenPager.setAdapter(viewPagerAdapter);
        tabIndicator.setupWithViewPager(screenPager);
        Objects.requireNonNull(getSupportActionBar()).hide();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
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