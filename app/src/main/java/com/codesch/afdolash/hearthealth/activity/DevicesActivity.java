package com.codesch.afdolash.hearthealth.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.codesch.afdolash.hearthealth.R;
import com.codesch.afdolash.hearthealth.adapter.DevicesAdapter;
import com.codesch.afdolash.hearthealth.modal.Devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import at.markushi.ui.CircleButton;

public class DevicesActivity extends AppCompatActivity {

    // Global variable
    private CircleButton btn_btConnect;
    private BluetoothAdapter mBtAdapter;
    private DevicesAdapter mDevicesAdapter;

    private List<Devices> mDevicesList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        changeStatusBarColor();

        // Initialize variable
        btn_btConnect = (CircleButton) findViewById(R.id.btn_btConnect);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize array adapter
        mDevicesAdapter = new DevicesAdapter(getBaseContext(), mDevicesList);

        // Set button bt connect
        btn_btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check bt state
                checkBtState();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Bluetooth devices dialog show
     */
    private void deviceDialog() {
        mDevicesList.clear(); // Clear the array

        // Create view
        View mView = getLayoutInflater().inflate(R.layout.dialog_devices, null);

        // Variable
        RecyclerView recyclerView = (RecyclerView) mView.findViewById(R.id.recylerView);

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Add previously paired devices to the array
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDevicesList.add(new Devices(device.getName(), device.getAddress()));
            }
        }

        // Find and set up Recyclerview for paired devices
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mDevicesAdapter);

        // Alert dialog
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DevicesActivity.this);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.setTitle("Paired devices...");
        dialog.show();
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * Method to chech if the device has Bluetooth and turned on
     */
    private void checkBtState() {
        if (mBtAdapter == null) {
            Toast.makeText(DevicesActivity.this, "Error - Device does not support Bluetooth.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (mBtAdapter.isEnabled()) {
                deviceDialog();
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}

