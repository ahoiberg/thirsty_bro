package com.example.andrewhoiberg.thirsty_bro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sensoria.sensoriadroidjni.SignalProcessing;
import com.sensoria.sensorialibrary.SAAnklet;
import com.sensoria.sensorialibrary.SAAnkletInterface;
import com.sensoria.sensorialibrary.SAFoundAnklet;

public class MainActivity extends ActionBarActivity implements SAAnkletInterface {
    SAAnklet anklet;
    SignalProcessing signalProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        anklet = new SAAnklet(this);
        signalProcessing = new SignalProcessing();
    }

    @Override
    protected void onResume() {
        super.onResume();
        anklet.resume();

        SharedPreferences settings = getSharedPreferences(UserPreferences.PREFS_NAME, 0);

        if(settings.getInt("age",-1)==-1||settings.getInt("height",-1)==-1||settings.getInt("weight",-1)==-1||!settings.contains("isMale")){
            openUserSettings();
        }else{
            displayUserPreferences();
        }



    }

    public void displayUserPreferences(){
        /*
        TextView age = (TextView) findViewById(R.id.ageValue);
        TextView height = (TextView) findViewById(R.id.heightValue);
        TextView weight = (TextView) findViewById(R.id.weightValue);
        TextView gender = (TextView) findViewById(R.id.genderValue);

        SharedPreferences settings = getSharedPreferences(UserPreferences.PREFS_NAME, 0);

        age.setText(String.format("%d", settings.getInt("age",0)));
        height.setText(String.format("%d", settings.getInt("height",-1)));
        weight.setText(String.format("%d", settings.getInt("weight",0)));
        gender.setText(settings.getBoolean("isMale",true)?"Male":"Female");
        */
    }

    @Override
    protected void onPause() {
        super.onPause();

        anklet.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        anklet.disconnect();
    }

    public void onStartScan(View view) {
        anklet.startScan();
    }

    public void onStopScan(View view) {
        anklet.stopScan();
    }

    public void onConnect(View view) {

        Log.w("SensoriaLibrary", "Connect to " + selectedCode + " " + selectedMac);
        anklet.deviceCode = selectedCode;
        anklet.deviceMac = selectedMac;
        anklet.connect();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openUserSettings();
        }

        if (id == R.id.connect_settings) {
            openConnectionSettings();
        }

        return super.onOptionsItemSelected(item);
    }

    public void openUserSettings(){
        this.startActivity(new Intent(this,UserSettingsActivity.class));
    }

    final static int CONNECTION_REQUEST_CODE=1;
    public void openConnectionSettings(){
        this.startActivity(new Intent(this,ConnectingActivity.class));
        Intent connectionSettingsIntent = new Intent(this,ConnectingActivity.class);
        startActivityForResult(connectionSettingsIntent, CONNECTION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
                SAAnklet anklet = ((AnkletPasser)data.getSerializableExtra("result")).anklet;
                Log.d("TB","WORKS");

            }
        }
    }

    private String selectedCode;
    private String selectedMac;

    @Override
    public void didDiscoverDevice() {

        Log.w("SensoriaLibrary", "Device Discovered!");

        Spinner s = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, anklet.deviceDiscoveredList);
        s.setAdapter(adapter);

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                SAFoundAnklet deviceDiscovered = anklet.deviceDiscoveredList.get(position);
                selectedCode = deviceDiscovered.deviceCode;
                selectedMac = deviceDiscovered.deviceMac;

                Log.d("SensoriaLibrary", selectedCode + " " + selectedMac);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCode = null;
            }
        });
    }

    @Override
    public void didConnect() {

        Log.w("SensoriaLibrary", "Device Connected!");

    }

    @Override
    public void didError(String message) {

        Log.e("SensoriaLibrary", message);

    }

    @Override
    public void didUpdateData() {

        TextView tick = (TextView) findViewById(R.id.tickValue);
        TextView mtb1 = (TextView) findViewById(R.id.mtb1Value);
        TextView mtb5 = (TextView) findViewById(R.id.mtb5Value);
        TextView heel = (TextView) findViewById(R.id.heelValue);
        TextView accX = (TextView) findViewById(R.id.accXValue);
        TextView accY = (TextView) findViewById(R.id.accYValue);
        TextView accZ = (TextView) findViewById(R.id.accZValue);

        tick.setText(String.format("%d", anklet.tick));
        mtb1.setText(String.format("%d", anklet.mtb1));
        mtb5.setText(String.format("%d", anklet.mtb5));
        heel.setText(String.format("%d", anklet.heel));
        accX.setText(String.format("%f", anklet.accX));
        accY.setText(String.format("%f", anklet.accY));
        accZ.setText(String.format("%f", anklet.accZ));


        double[] rawDataBuffer = new double[6];

        //MTB5 S0
        //MTB1 S1
        //Heel S2

        rawDataBuffer[0] = (double) anklet.mtb5;
        rawDataBuffer[1] = (double) anklet.mtb1;
        rawDataBuffer[2] = (double) anklet.heel;
        rawDataBuffer[3] = (double) anklet.accX;
        rawDataBuffer[4] = (double) anklet.accY;
        rawDataBuffer[5] = (double) anklet.accZ;


        signalProcessing.processIncomingData(rawDataBuffer);
        Log.e("Steps", String.format("%d", (int)signalProcessing.getStepCount()));

    }

}
