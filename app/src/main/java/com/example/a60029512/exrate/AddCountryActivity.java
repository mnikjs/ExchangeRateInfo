package com.example.a60029512.exrate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class AddCountryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_country);
    }

    private void addCountry(){
        Log.v("outputadd=","test");
//        Intent intent = new Intent(AddCountryActivity.this, AllCountryListActivity.class);
//        startActivity(intent);
    }

}