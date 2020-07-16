package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    static ArrayList<String> arrayList=new ArrayList<>();
    static ArrayList<LatLng> locationArrayList=new ArrayList<>();
    static ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView=findViewById(R.id.listView);
        ArrayList<String> latitudes=new ArrayList<>();
        ArrayList<String> longitudes=new ArrayList<>();

        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);

        arrayList.clear();
        locationArrayList.clear();
        latitudes.clear();
        latitudes.clear();

        try {
            arrayList=(ArrayList<String>) objectSerializer.deserialize(sharedPreferences.getString("places",objectSerializer.serialize(new ArrayList<String>())));
            latitudes=(ArrayList<String>) objectSerializer.deserialize(sharedPreferences.getString("latitudes",objectSerializer.serialize(new ArrayList<String>())));
            longitudes=(ArrayList<String>) objectSerializer.deserialize(sharedPreferences.getString("longitudes",objectSerializer.serialize(new ArrayList<String>())));
        }catch (Exception e) {
            e.printStackTrace();
        }

        if (arrayList.size()>0 && longitudes.size()>0 && latitudes.size()>0) {
            if (arrayList.size()==latitudes.size() && arrayList.size()==longitudes.size()) {
                for (int i=0;i<arrayList.size();i++) {
                    locationArrayList.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        } else {
            arrayList.add("Add a new place");
            locationArrayList.add(new LatLng(0,0));
        }

        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,arrayList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }



}