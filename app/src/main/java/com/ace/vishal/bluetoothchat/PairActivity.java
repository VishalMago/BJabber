package com.ace.vishal.bluetoothchat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by Vishal Mago on 8/5/2017.
 */

public class PairActivity extends Activity {
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private Button pair,cont;
    private ProgressDialog mProgressDlg;
    public boolean isRegistered=false;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private static final int DISCOVER_DURATION = 300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);
        pair=(Button)findViewById(R.id.pairbutton);
        cont=(Button)findViewById(R.id.continuebutton);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mProgressDlg 		= new ProgressDialog(this);

        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                mBluetoothAdapter.cancelDiscovery();
                if(mReceiver!=null&&isRegistered) {
                    unregisterReceiver(mReceiver);
                    isRegistered=false;
                }
            }
        });
        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i=new Intent(PairActivity.this,HomeActivity.class);
                if(mReceiver!=null&&isRegistered) {
                    unregisterReceiver(mReceiver);
                    isRegistered=false;
                }

                startActivity(i);
            }
        });
        pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mBluetoothAdapter.startDiscovery();
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(mReceiver, filter);
                mProgressDlg.show();
                isRegistered=true;

            }
        });


    }
    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent  discoverableIntent  = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,DISCOVER_DURATION);
            startActivityForResult(discoverableIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == DISCOVER_DURATION) {
                    return;
                } else {
                    finish();
                }
        }
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();

                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();

                Intent newIntent = new Intent(PairActivity.this, DeviceListActivity.class);

                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                unregisterReceiver(mReceiver);
                isRegistered=false;

                startActivity(newIntent);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);
            }
        }
    };
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        try{
            if(mReceiver!=null) {
                unregisterReceiver(mReceiver);
                isRegistered = false;
            }
        }catch(Exception e)
        {
        }
        super.onDestroy();
    }
}
