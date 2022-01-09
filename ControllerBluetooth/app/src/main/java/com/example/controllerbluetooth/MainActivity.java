package com.example.controllerbluetooth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private SwitchCompat aDarkSwitch;
    private Boolean aDarkMode;

    private BluetoothAdapter aBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign the variables
        aDarkSwitch = findViewById(R.id.dark_switch);

        //Retrieve the data
        Bundle vBundle = getIntent().getExtras();
        if(vBundle != null){
            aDarkMode = vBundle.getBoolean("Dark");
        } else {
            aDarkMode = false;
        }

        //Initialing the switch
        aDarkSwitch.setChecked(aDarkMode);
        if(aDarkMode) {
            aDarkSwitch.setText(R.string.Light);
        } else {
            aDarkSwitch.setText(R.string.Dark);
        }

        //Define the function of the switch
        aDarkSwitch.setOnCheckedChangeListener((compoundButton, check) -> {
            aDarkMode = check;
            if(check){
                //switch to dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                aDarkSwitch.setText(R.string.Light);
            } else {
                //revert back to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                aDarkSwitch.setText(R.string.Dark);
            }
        });
    }

    public void StartBluetooth(View view) {
        //Start bluetooth and check the permission
        aBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int vResult = InitBluetooth(); //0 = Succes, 1 = No BluetoothAdapter, 2 = Missing Permission
        switch (vResult){
            case (0):
                LaunchNewActivity();
                break;

            case (1):
                Toast.makeText(this, "Pas de Bluetooth trouvé", Toast.LENGTH_SHORT).show();
                break;

            case (2):
                Toast.makeText(this, "Demande de permission", Toast.LENGTH_SHORT).show();
                AskForBluetoothPermission();
                break;

            default:
                Toast.makeText(this, "Unxepected result", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    //Permission
    //Gere la demande de la permission et le dialog de la demande
    private void AskForBluetoothPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH)) {
            new AlertDialog.Builder(this)
                    .setTitle("Besoin de permission")
                    .setMessage("L'apllication a besoin de bluetooth pour fonctionner")
                    .setPositiveButton("Okay", (dialogInterface, i) -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT}, 1001))
                    .setNegativeButton("Non", (dialogInterface, i) -> dialogInterface.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT}, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] Result) {
        super.onRequestPermissionsResult(requestCode, permission, Result);
        if (Result[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission accordé", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission refusé", Toast.LENGTH_SHORT).show();
        }
    }

    private int InitBluetooth(){
        if(aBluetoothAdapter == null){
            return 1;
        } else if(!aBluetoothAdapter.isEnabled()){
            return 2;
        }
        return 0;
    }

    //Start the second activity
    private void LaunchNewActivity(){
        Intent vIntent = new Intent(this, Controller.class);
        Bundle vBundle = new Bundle();
        vBundle.putBoolean("Dark", aDarkMode);
        vIntent.putExtras(vBundle);
        startActivity(vIntent);
        finish();
    }
}