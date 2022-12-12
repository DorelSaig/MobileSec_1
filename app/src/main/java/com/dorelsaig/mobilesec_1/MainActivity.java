package com.dorelsaig.mobilesec_1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Calendar;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private LottieAnimationView main_LOTT_anim;
    private EditText main_EDT_pass;
    private Button main_BTN_login;
    private TextView main_TXT_headline, main_TXT_compass;
    private LocationManager locationManager;
    private Location myLocation = new Location("A");
    private Location target = new Location("B");
    private GeomagneticField field;
    private String ssid = "\"Dorel\"";

    private SensorManager mSensorManager;

    private CheckBox main_CHCK_pass;
    private CheckBox main_CHCK_wifi;
    private CheckBox main_CHCK_darkmode;
    private CheckBox main_CHCK_evenday;
    private CheckBox main_CHCK_jerusalem;
    private CheckBox main_CHCK_Battery;

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifi.getConnectionInfo();

            Log.d("pttt","Network connectivity change" + wifiInfo.getSSID());

            String currentConnectedSSID = wifiInfo.getSSID();

            if (ssid.equals(currentConnectedSSID)) {
                main_CHCK_wifi.setChecked(true);
            }else {
                main_CHCK_wifi.setChecked(false);
            }

        }
    };

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float)scale;

            if (batteryPct % 5 == 0){
                main_CHCK_Battery.setChecked(true);
            }else {
                main_CHCK_Battery.setChecked(false);
            }
            main_TXT_compass.setText(String.valueOf(batteryPct) + "%");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initButton();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        myLocation.setLatitude(0);
        myLocation.setLongitude(0);

        target.setLatitude(31.777565156983574);
        target.setLongitude(35.23471553985782);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        main_EDT_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String pass = charSequence.toString();
                if(pass.contains("123123") && pass.startsWith("DOR")){
                    main_CHCK_pass.setChecked(true);
                } else {
                    main_CHCK_pass.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        main_CHCK_evenday.setChecked(checkTime());
        main_CHCK_darkmode.setChecked(checkDarkMode(this));

        getLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this); // to stop the listener and save battery
        unregisterReceiver(networkChangeReceiver);
    }

    private void initButton() {
        main_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verify();
            }
        });
    }

    private void verify() {
        main_CHCK_evenday.setChecked(checkTime());
        main_CHCK_darkmode.setChecked(checkDarkMode(this));

        if (main_CHCK_pass.isChecked() && main_CHCK_wifi.isChecked() && main_CHCK_jerusalem.isChecked() && main_CHCK_evenday.isChecked() && main_CHCK_darkmode.isChecked() && main_CHCK_Battery.isChecked()) {
            main_LOTT_anim.setVisibility(View.VISIBLE);
            main_TXT_headline.setText("Success!!!");
        } else {
            main_LOTT_anim.setVisibility(View.INVISIBLE);
            main_TXT_headline.setText("Hack Me, If You Can");
        }
    }

    private boolean checkTime() {
        boolean retVal = false;
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        Log.d("pttt", "what day: " + dayOfWeek);

        if (dayOfWeek % 2 == 0){
            retVal = true;
        }
        return retVal;
    }

    private boolean checkDarkMode(MainActivity mainActivity) {
        boolean retVal = false;

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                retVal = true;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                retVal = false;
                break;
        }
        return retVal;
    }

    private void findViews() {
        main_EDT_pass = findViewById(R.id.main_EDT_pass);
        main_BTN_login = findViewById(R.id.main_BTN_login);
        main_TXT_headline = findViewById(R.id.main_TXT_headline);
        main_TXT_compass = findViewById(R.id.main_TXT_compass);
        main_CHCK_pass = findViewById(R.id.main_CHCK_pass);
        main_CHCK_wifi = findViewById(R.id.main_CHCK_wifi);
        main_CHCK_darkmode = findViewById(R.id.main_CHCK_darkmode);
        main_CHCK_evenday = findViewById(R.id.main_CHCK_evenday);
        main_CHCK_jerusalem = findViewById(R.id.main_CHCK_jerusalem);
        main_CHCK_Battery = findViewById(R.id.main_CHCK_Battery);
        main_LOTT_anim = findViewById(R.id.main_LOTT_anim);
    }


    //get access to location permission
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "your message", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //Get location
    public void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null)
        {
            myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        }
        field = new GeomagneticField((float) myLocation.getLatitude(),
                (float) myLocation.getLongitude(), (float) myLocation.getAltitude(), System.currentTimeMillis());


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        degree += field.getDeclination();


        float bearing = myLocation.bearingTo(target);
        degree = (bearing - degree) * -1;
        degree = normalizeDegree(degree);

        //main_TXT_compass.setText("Heading: " + Float.toString(degree) + " degrees");

        if(degree<10 || degree > 350){
            main_CHCK_jerusalem.setChecked(true);
            verify();
        } else {
            main_CHCK_jerusalem.setChecked(false);
            verify();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private float normalizeDegree(float value) {
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return 180 + (180 + value);
        }
    }
}