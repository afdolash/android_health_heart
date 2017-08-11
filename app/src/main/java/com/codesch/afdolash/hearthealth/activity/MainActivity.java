package com.codesch.afdolash.hearthealth.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codesch.afdolash.hearthealth.R;
import com.codesch.afdolash.hearthealth.adapter.DataHelper;
import com.codesch.afdolash.hearthealth.adapter.DevicesAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Widget
    private FloatingActionButton fab_refreshGraph;
    private TextView tv_heartMessage;
    private TextView tv_heartRate;
    private TextView tv_heartStat;
    private ImageView img_heartStat;
    private ImageView img_profile;
    private ImageView img_heartSound;

    // Media player state;
    private boolean mediaState = true;

    // Media player
    private MediaPlayer mediaPlayer;

    // Database helper
    private DataHelper mDbHelper;
    private Cursor cursor;

    // Chart
    private LineChart mChart;

    // Handler
    private Handler mBtIn;
    private final int handlerState = 0;

    // Bluetooth component
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothSocket mBtSocket = null;
    private String mBtAddress;

    // Thread
    private ConnectedThread mConnectedThread;

    // Component to get data from AT Mega
    private StringBuilder recDataString = new StringBuilder();
    private String[] dataSensor;

    // SPP UUID service - this should work for most devices
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //  Double back to exit
    boolean doubleBackToExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize component widget
        mChart = (LineChart) findViewById(R.id.graph_heartGraph);
        tv_heartRate = (TextView) findViewById(R.id.tv_heartRate);
        tv_heartMessage = (TextView) findViewById(R.id.tv_heartMessage);
        tv_heartStat = (TextView) findViewById(R.id.tv_heartStat);
        img_heartStat = (ImageView) findViewById(R.id.img_heartStat);
        img_heartSound = (ImageView) findViewById(R.id.img_heartSound);
        img_profile = (ImageView) findViewById(R.id.img_profile);
        fab_refreshGraph = (FloatingActionButton) findViewById(R.id.fab_refreshGraph);

        // Database helper
        mDbHelper = new DataHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM profile WHERE no = 1",null);
        cursor.moveToFirst();

        // Media player create
        mediaPlayer = MediaPlayer.create(this, R.raw.heart_sound);

        // Set media state
        if (mediaState) {
            mediaPlayer.setVolume(1, 1);
        } else {
            mediaPlayer.setVolume(0, 0);
        }

        // Chart heart
        mChart.getDescription().setEnabled(false);

        // Enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);

        // Line data
        final LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // Add empty data
        mChart.setData(data);

        // Get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // Modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(200f);
        leftAxis.setAxisMinimum(-200f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Floating action bar events
        fab_refreshGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChart.invalidate();
                mChart.notifyDataSetChanged();
                recDataString = new StringBuilder();
                dataSensor = null;
            }
        });

        // Heart sound event
        img_heartSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaState) {
                    mediaState = false;
                    mediaPlayer.setVolume(0, 0);
                    Toast.makeText(MainActivity.this, "Sound mute.", Toast.LENGTH_SHORT).show();
                } else {
                    mediaState = true;
                    mediaPlayer.setVolume(1, 1);
                    Toast.makeText(MainActivity.this, "Sound unmute.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Profile
        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Handler to get data sensor from AT Mega
        mBtIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    // Get data from msg
                    String readMessage = (String) msg.obj;

                    // Append data from readMessage to String Builder
                    recDataString.append(readMessage);

                    // Trim data from String Builder to String Array
                    dataSensor = recDataString.toString().split("\n");

                    // Heart sound play
                    try {
                        if (Integer.parseInt(dataSensor[dataSensor.length - 4]) > 840) {
                            mediaPlayer.start();
                        }
                    } catch (Exception e) {
                        // Set your error action here
                    }

                    // Set textview heart rate
                    try {
                        tv_heartRate.setText(dataSensor[dataSensor.length - 3]);
                    } catch (Exception e) {
                        // Set your error action here
                    }

                    // Set heart statistic view
                    try {
                        if (Integer.parseInt(dataSensor[dataSensor.length - 3]) < 63) {
                            tv_heartStat.setText("Low");
                            img_heartStat.setImageResource(R.drawable.ic_favorite_orange_24dp);
                            tv_heartMessage.setText("Hello! Mr./Mrs. "+ cursor.getString(1).toString() +" your heart rate interval is under normal limits. Be carefull !!");
                        } else if (Integer.parseInt(dataSensor[dataSensor.length - 3]) > 103) {
                            tv_heartStat.setText("High");
                            img_heartStat.setImageResource(R.drawable.ic_favorite_red_24dp);
                            tv_heartMessage.setText("Hello! Mr./Mrs. "+ cursor.getString(1).toString() +" your heart rate interval is above the normal limit. Be carefull !!");
                        } else {
                            tv_heartStat.setText("Good");
                            img_heartStat.setImageResource(R.drawable.ic_favorite_teal_24dp);
                            tv_heartMessage.setText("Hello! Mr./Mrs. "+ cursor.getString(1).toString() +" your heart rate interval is normal. Keep up :)");
                        }
                    } catch (Exception e) {
                        // Set your error action here
                    }

                    // Draw line graph
                    try {
                        addEntry((Float.parseFloat(dataSensor[dataSensor.length - 4])) - 840f);
                    } catch (Exception e) {
                        // Set your error action here
                    }

                    // Clear data from String Builder
                    // recDataString.delete(0, recDataString.length());
                }
            }
        };

        // Check Bluetooth connection
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    private void addEntry(float dataSensor) {
        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), dataSensor), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(50);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(0, 0, YAxis.AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Heart");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(1f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get new data user
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM profile WHERE no = 1",null);
        cursor.moveToFirst();

        // Get MAC Bluetooth from DevicesActivity via intent
        Intent intent = getIntent();

        // Get MAC Bluetooth from DeviceListActivity
        mBtAddress = intent.getStringExtra(DevicesAdapter.EXTRA_DEVICE_ADDRESS);

        // Create device and set the MAC mBtAddress
        BluetoothDevice device = mBtAdapter.getRemoteDevice(mBtAddress);

        // Create Bluetooth socket
        try {
            mBtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Error - Unable to create Bluetooth socket.", Toast.LENGTH_LONG).show();
        }

        // Establish the Bluetooth socket connection
        try {
            mBtSocket.connect();
        } catch (IOException e) {
            try {
                mBtSocket.close();
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket.", Toast.LENGTH_SHORT).show();
            }
        }

        mConnectedThread = new ConnectedThread(mBtSocket);
        mConnectedThread.start();

        // I send a character when resuming.beginning transmission to check device is connected
        // If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("X");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try {
            // Don't leave Bluetooth sockets open when leaving activity
            mBtSocket.close();
        } catch (IOException e2) {
            // Insert code to deal with this
        }
    }

    // Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "Error - Device does not support Bluetooth.", Toast.LENGTH_LONG).show();
        } else {
            if (mBtAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExit) {
            super.onBackPressed();
            try {
                mBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        this.doubleBackToExit = true;
        Toast.makeText(this, "Please click back again to exit.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExit = false;
            }
        }, 2000);
    }

    // Create new class for connect thread
    private class ConnectedThread extends Thread {
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        // Creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            try {
                // Create I/O streams for connection
                this.mmInStream = socket.getInputStream();
                this.mmOutStream = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplication(), "Error - Unable to create IO Stream.", Toast.LENGTH_SHORT).show();
            }
        }

        // When thread started
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    // Send the obtained bytes to the UI Activity via handler
                    mBtIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        // Write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();

            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Error - Connection failure.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
