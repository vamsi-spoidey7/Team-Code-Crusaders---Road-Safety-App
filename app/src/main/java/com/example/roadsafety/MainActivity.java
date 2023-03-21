package com.example.roadsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Button logoutbtn;
    TextView x,y,z;
    static float THRESHOLD = 40.0f; // The acceleration threshold value
    static float[] prevValues = {0, 0, 0}; // The previous acceleration values
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 100;
    String latitude,longitude,address,city,country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
        z = findViewById(R.id.z);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocation();

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if(sensorManager!=null){
            Sensor acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(acceleroSensor!=null){
                sensorManager.registerListener(this,acceleroSensor,SensorManager.SENSOR_DELAY_NORMAL);
            }
        }else{
            Toast.makeText(this, "Sensor service not detected", Toast.LENGTH_SHORT).show();
        }

        logoutbtn = findViewById(R.id.logoutbtn);
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                Toast.makeText(MainActivity.this, "Logged out Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }
    public static boolean detectSuddenAcceleration(float x, float y, float z) {
        float[] values = {x, y, z};

        // Calculate the high-pass filtered acceleration values
        float[] filteredValues = highPassFilter(values, prevValues);

        // Calculate the magnitude of the high-pass filtered acceleration vector
        float magnitude = (float) Math.sqrt(filteredValues[0] * filteredValues[0] +
                filteredValues[1] * filteredValues[1] +
                filteredValues[2] * filteredValues[2]);

        // Save the current acceleration values for the next iteration
        prevValues = values;

        // Check if the magnitude exceeds the threshold
        // The magnitude exceeds the threshold, indicating a sudden change in acceleration
        return magnitude > THRESHOLD;
    }


    private static float[] highPassFilter(float[] values, float[] prevValues) {
        float[] filteredValues = new float[3];
        float alpha = 0.8f; // The smoothing factor

        // Apply the high-pass filter
        filteredValues[0] = alpha * (prevValues[0] + values[0] - prevValues[0]);
        filteredValues[1] = alpha * (prevValues[1] + values[1] - prevValues[1]);
        filteredValues[2] = alpha * (prevValues[2] + values[2] - prevValues[2]);

        return filteredValues;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            float ax = sensorEvent.values[0];
            float ay = sensorEvent.values[1];
            float az = sensorEvent.values[2];
            x.setText(""+ax);
            y.setText(""+ay);
            z.setText(""+az);
            String phoneNo1 = "8074979796";

            String SMS = "ALERT! It appears that the Vamsi Madugula may have been in a accident. You are receiving this message as he chosen you as emergency contact."+address;
            if(detectSuddenAcceleration(ax,ay,az)){
                try{
                    Toast.makeText(this, "Crash Detected", Toast.LENGTH_SHORT).show();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(checkSelfPermission(android.Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED){
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phoneNo1,null,SMS,null,null);
                        }else{
                            requestPermissions(new String[]{Manifest.permission.SEND_SMS},1);
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(this, "SMS not sent", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void getLastLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = null;
                        try{
                            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                            latitude = String.valueOf(addresses.get(0).getLatitude());
                            longitude = String.valueOf(addresses.get(0).getLongitude());
                            address = addresses.get(0).getAddressLine(0);
                            city = addresses.get(0).getLocality();
                            country = addresses.get(0).getCountryName();

                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }

                    }
                }
            });
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}