package com.helix.smartbluelock;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


import static com.helix.smartbluelock.Registration.My_Id;
import static com.helix.smartbluelock.SplashActivity.MyCode;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();
    BluetoothAdapter badapter;
    int REQUEST_CODE = 1;
    Set<BluetoothDevice> paired_devices;
    String pList[];
    TextView tview;

    //protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    ListView lview;
    private static final String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public LockListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    Bluetoothconnection zBluetoothconnection;
    BluetoothDevice xBTdevice;

    public static final String MyPass = "lockUnlock";
    SharedPreferences prefPass,prefCode;


    Button lock,unlock,logout,oneTime;
    TextView lockName,lockId;
    FloatingActionButton showCode,resetLock;
    RelativeLayout lockunlockfragment;
    String currentState=new String();
    int Login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lview = (ListView) findViewById(R.id.devicesList);
        lock=(Button)findViewById(R.id.lock);
        unlock=(Button)findViewById(R.id.unlock);
        logout=(Button)findViewById(R.id.mLogout);
        lockName=(TextView)findViewById(R.id.lockname);
        lockunlockfragment=(RelativeLayout)findViewById(R.id.lockunlockfragment);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter("bytes"));
        lockId=(TextView)findViewById(R.id.Lockid);
        lvNewDevices = (ListView) findViewById(R.id.devicesList);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBTDevices = new ArrayList<>();
        oneTime=(Button)findViewById(R.id.oneTimeRegistration);
        showCode=(FloatingActionButton) findViewById(R.id.floatCode);
        resetLock=(FloatingActionButton)findViewById(R.id.resetLock);

        final SharedPreferences sharedpreferences =getSharedPreferences(My_Id,MODE_PRIVATE);
        Login = sharedpreferences.getInt("lockid",0);
        lockId.setText(String.valueOf(Login));

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        Log.d(TAG, "onClick: enabling/disabling bluetooth.");
        enableDisableBT();




        lvNewDevices.setOnItemClickListener(MainActivity.this);

        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefPass = getSharedPreferences(MyPass,MODE_PRIVATE);
               String key = prefPass.getString("lockpass",null);
                byte[] Get=key.getBytes();
                zBluetoothconnection.write(Get);
                lock.setVisibility(View.INVISIBLE);
                unlock.setVisibility(View.VISIBLE);

            }
        });

        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefPass = getSharedPreferences(MyPass,MODE_PRIVATE);
                String key = prefPass.getString("lockpass",null);
                byte[] Get=key.getBytes();
                zBluetoothconnection.write(Get);
                unlock.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
            }
        });

        showCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertCode();

            }
        });
        resetLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               alertResetLock();
            }
        });
         logout.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 finish();
             }
         });


    }
    BroadcastReceiver mReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             currentState =intent.getStringExtra("currentState");
             prefPass=getSharedPreferences(MyPass,MODE_PRIVATE);
             SharedPreferences.Editor editor = prefPass.edit();
             editor.putString("lockpass",currentState);
             editor.apply();
            Map<String, Object> primary = new HashMap<>();
            primary.put("lock_pass", currentState);
            mDocRef.collection("primaryuser").document(String.valueOf(Login)).set(primary, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "success");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "fail");
                }
            });

        }
    };


    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG,"startBTConnection : Initializing RFCOM Bluetooth Connection.");
        zBluetoothconnection.startClient(device,uuid);
    }

    public void closeBTConnection(){
        Log.d(TAG,"Closing Bluetooth Connection.........");
       zBluetoothconnection.closeConnection();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            boolean value = mBTDevices.get(i).createBond();
            xBTdevice=mBTDevices.get(i);
            zBluetoothconnection = new Bluetoothconnection(MainActivity.this);
            if (!value) {
                startConnection();
                Toast.makeText(getBaseContext(), "Ready for depature", Toast.LENGTH_SHORT).show();
                lockunlockfragment.setVisibility(View.VISIBLE);
                showCode.setVisibility(View.INVISIBLE);
                resetLock.setVisibility(View.VISIBLE);
                lockName.setText(xBTdevice.getName());
                prefPass = getSharedPreferences(MyPass,MODE_PRIVATE);
                String key = prefPass.getString("lockpass",null);
                if(key== null){

                    oneTime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String oneTimeRegis = "Otr@1234";
                            byte[] regisPass = oneTimeRegis.getBytes();
                            zBluetoothconnection.write(regisPass);

                            if (currentState != null) {
                                oneTime.setVisibility(View.INVISIBLE);
                                lock.setVisibility(View.VISIBLE);


                            }
                        }
                    });oneTime.setVisibility(View.VISIBLE);

                }else{
                    oneTime.setVisibility(View.INVISIBLE);
                    lock.setVisibility(View.VISIBLE);
                }

                logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "2", Toast.LENGTH_SHORT).show();
                        closeBTConnection();
                        finish();
                    }
                });

            }
        }

    }
    public void startConnection(){
        startBTConnection(xBTdevice,MY_UUID_INSECURE);
    }

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new LockListAdapter(context, R.layout.activity_lock_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    xBTdevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    public void enableDisableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");

                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");

                        break;

                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        finish();
        super.onDestroy();

    }


    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");
                checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    @TargetApi(23)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
    public void onBackPressed() {
      finish();
    }

    void alertCode(){
        prefCode=getSharedPreferences(MyCode,MODE_PRIVATE);
        final String temp=prefCode.getString("code",null);

        if(temp!=null){
                     new AlertDialog.Builder(this)
                    .setTitle("Code To Authorise Secondary User")
                    .setMessage(temp)
                    .setNegativeButton("Copy Code", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Copied Text", temp);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(MainActivity.this, "Code Copied To Clipboard", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setPositiveButton("Delete Code", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            mDocRef.collection("checkuser").document(String.valueOf(Login))
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            SharedPreferences.Editor e=prefCode.edit();
                                            e.putString("code",null);
                                            e.apply();
                                            Toast.makeText(MainActivity.this, "Code Deleted Succesfully:)", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    }).create().show();

        }
        else{
            new AlertDialog.Builder(this)
                    .setTitle("Code To Authorise Secondary User")
                    .setMessage("Code has been deleted or not generated yet")
                    .setNegativeButton(android.R.string.ok, null)
                    .show();
        }

    }
    void alertResetLock(){
        prefPass =getSharedPreferences(MyPass,MODE_PRIVATE);
        new AlertDialog.Builder(this)
                .setTitle("Reset Lock?")
                .setMessage("Are you sure you want to reset Lock?")
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this,prefPass.getString("lockpass",null) , Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {

                        SharedPreferences.Editor e =prefPass.edit();
                        e.putString("lockpass",null);
                        e.apply();
                        String kill ="Kill@123456";
                        byte[] killLock =kill.getBytes();
                        zBluetoothconnection.write(killLock);
//                        Intent intent = getIntent();
                        finish();
                        closeBTConnection();
//                        startActivity(intent);
//                        Toast.makeText(MainActivity.this, "done", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();

    }
}