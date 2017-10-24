package com.javier.pressmatic;

/**
 * Created by javier on 17/12/16.
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.*;
import android.widget.ImageButton;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;

public class Pressmatic extends AppCompatActivity implements OnClickListener{

    private boolean mEnablingBT;
    private boolean mLocalEcho = false;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    // Message types sent from the BluetoothReadService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    protected static BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothConexion mBluetoothConexion = null;


    // Name of the connected device
    protected static String mConnectedDeviceName = null;

    /**
     * PARÁMETROS DEL SERVICE
     **/
    private static boolean mBound = false;
    private static boolean mFirstConnection = true;
    /**
     * ELEMENTOS DE LA INTERFAZ
     */

    protected static Button BluetoothButton;
    private static boolean EstadoBoton;

    private Button no,yes,help_button;
    private Button exit;
    private ImageButton settings,help,speakButton,speakButton1;
    private LinearLayout enable;
    protected static TextView text5,text6,text7;
    private int i=16;
    private String menu="f",pressm="a";
    private TableLayout commands;
    private boolean table=true;
    private  MyApplication mApplication;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_connect);

        BluetoothButton = (Button) findViewById(R.id.button5);
        no = (Button) findViewById(R.id.button2);
        yes = (Button) findViewById(R.id.button3);
        settings = (ImageButton) findViewById(R.id.imageButton3);
        help = (ImageButton) findViewById(R.id.imageButton2);
        enable = (LinearLayout) findViewById(R.id.activity_enable);
        speakButton = (ImageButton) findViewById(R.id.speak);
        speakButton1 = (ImageButton) findViewById(R.id.speak1);
        speakButton.setOnClickListener(this);
        speakButton1.setOnClickListener(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        help_button = (Button) findViewById(R.id.Button5);
        commands=(TableLayout) findViewById(R.id.commands);
        exit = (Button) findViewById(R.id.button);


        mBluetoothConexion = new BluetoothConexion(this, mHandlerBT);
        // mHandlerBT is sent to the App to maintain a global state
        mApplication= (MyApplication)getApplicationContext();
        mApplication.setHandler(mHandlerBT);

        actualizaInterfaz();

        PackageManager pm = getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(this);
            speakButton1.setOnClickListener(this);
        } else {
            speakButton.setEnabled(false);
            speakButton1.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Recognizer not present", Toast.LENGTH_LONG).show();
        }
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!mFirstConnection){
            mBluetoothConexion.send(menu);
            Intent intent = new Intent(this, BluetoothConexion.class);
            bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
        }
        mEnablingBT = false;
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        // Se actualizan los elementos de la interfaz
        actualizaInterfaz();

        if (!mEnablingBT) { // If we are turning on the BT we cannot check if it's enable
            if ( (mBluetoothAdapter != null) &&(!mBluetoothAdapter.isEnabled()) )
                enable.setVisibility(LinearLayout.VISIBLE);

            if (mBluetoothConexion != null) {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (!mApplication.getConnection())
                    // Start the Bluetooth chat services
                    mBluetoothConexion.start();
                }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound){
            unbindService(mConnection);
            mBound=false;
            mFirstConnection=true;
        }
    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandlerBT = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    // actualizar los elementos de la intefaz según el estado de conexión.
                    switch (msg.arg1) {

                        case BluetoothConexion.STATE_CONNECTED:
                            mApplication.setConnection(true);
                            mBluetoothConexion.send(pressm);
                            startActivity(new Intent(Pressmatic.this, Snippers.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            finish();
                            break;

                        case BluetoothConexion.STATE_CONNECTING:
                            break;

                        case BluetoothConexion.STATE_LISTEN:

                        case BluetoothConexion.STATE_NONE:
                            mApplication.setConnection(false);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    if (mLocalEcho) {
                        byte[] writeBuf = (byte[]) msg.obj;
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_connected_to) + " "+ mConnectedDeviceName, Toast.LENGTH_LONG).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_LONG).show();
                    //mBound debe ser falso, de lo contrario se intentará destruir el servicio
                    mBound = false;
                    break;
            }}};

    /**
     * Con esta función se determina lo que se hace con la información
     * proveniente de elegir o no elegir un dispositivo, con el
     * resultado de dicha acción.
     * @param requestCode es el código que identifica el caso
     * @param resultCode puede ser afirmativo o positivo
     * @param data obtiene información extra
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(ListaDispositivos.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                   // MyApplication mApplication = (MyApplication)getApplicationContext();
                    mApplication.setAddress(address);
                    mApplication.setBluetoothDevice(device);
                    // Attempt to connect to the device VIA SERVICE
                    startService(new Intent(Pressmatic.this,BluetoothConexion.class));
                    Intent intent = new Intent(this, BluetoothConexion.class);
                    bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
                    mFirstConnection = false;
                    mApplication.setConnection(true);

                } else if (resultCode == Activity.RESULT_CANCELED){
                    mApplication.setConnection(false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    enable.setVisibility(LinearLayout.VISIBLE);
                    mApplication.setConnection(false);
                }
        }

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            Toast.makeText(getApplicationContext(),matches.toString(), Toast.LENGTH_LONG).show();

            // matches is the result of voice input. It is a list of what the user possibly said
            if (matches.contains("settings") || matches.contains("languaje") || matches.contains("ajustes") || matches.contains("idioma"))
                startActivity(new Intent(Pressmatic.this, Language.class));

            if (matches.contains("help") || matches.contains("ayuda") || matches.contains("instructions") || matches.contains("instrucciones")){
                mBluetoothConexion.send(menu);
                startActivity(new Intent(Pressmatic.this, HowWorks1.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();}

            if (matches.contains("no"))
                enable.setVisibility(LinearLayout.INVISIBLE);

            if (matches.contains("yes") || matches.contains("si")) {
                mEnablingBT = true;
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                enable.setVisibility(LinearLayout.INVISIBLE);
            }

            if (matches.contains("exit") || matches.contains("salir")|| matches.contains("close")|| matches.contains("cerrar")
                    || matches.contains("desconectar") || matches.contains("disconnectar")) {
                unbindService(mConnection);
                mBluetoothConexion.stop();
                exit();
            }

            if (matches.contains("connect")||matches.contains("conectar")) {

                if ((mBluetoothAdapter != null)&&(!mBluetoothAdapter.isEnabled()))
                    enable.setVisibility(LinearLayout.VISIBLE);

                else {
                    if (!mBound && mFirstConnection) {
                        Intent serverIntent = new Intent(Pressmatic.this, ListaDispositivos.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                    }
                    else {
                        if(mApplication.getConnection()){
                            startActivity(new Intent(Pressmatic.this, Snippers.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            finish();
                            mBluetoothConexion.send(pressm);
                        }else{
                           // mBluetoothConexion.send("9");
                            unbindService(mConnection);
                            mBluetoothConexion.stop();
                            mBluetoothConexion.start();
                            mFirstConnection = true;
                            startActivity(getIntent());
                        }
                    }}}
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }
    /**
     * A multi-thread-safe produce-consumer byte array.
     * Only allows one producer and one consumer.
     */
    class ByteQueue {
        public ByteQueue(int size) {
            mBuffer = new byte[size];
        }

        public int getBytesAvailable() {
            synchronized(this) {
                return mStoredBytes;
            }
        }

        public int read(byte[] buffer, int offset, int length)
                throws InterruptedException {
            if (length + offset > buffer.length) {
                throw
                        new IllegalArgumentException("length + offset > buffer.length");
            }
            if (length < 0) {
                throw
                        new IllegalArgumentException("length < 0");

            }
            if (length == 0) {
                return 0;
            }
            synchronized(this) {
                while (mStoredBytes == 0) {
                    wait();
                }
                int totalRead = 0;
                int bufferLength = mBuffer.length;
                boolean wasFull = bufferLength == mStoredBytes;
                while (length > 0 && mStoredBytes > 0) {
                    int oneRun = Math.min(bufferLength - mHead, mStoredBytes);
                    int bytesToCopy = Math.min(length, oneRun);
                    System.arraycopy(mBuffer, mHead, buffer, offset, bytesToCopy);
                    mHead += bytesToCopy;
                    if (mHead >= bufferLength) {
                        mHead = 0;
                    }
                    mStoredBytes -= bytesToCopy;
                    length -= bytesToCopy;
                    offset += bytesToCopy;
                    totalRead += bytesToCopy;
                }
                if (wasFull) {
                    notify();
                }
                return totalRead;
            }
        }

        public void write(byte[] buffer, int offset, int length)
                throws InterruptedException {
            if (length + offset > buffer.length) {
                throw
                        new IllegalArgumentException("length + offset > buffer.length");
            }
            if (length < 0) {
                throw
                        new IllegalArgumentException("length < 0");

            }
            if (length == 0) {
                return;
            }
            synchronized(this) {
                int bufferLength = mBuffer.length;
                boolean wasEmpty = mStoredBytes == 0;
                while (length > 0) {
                    while(bufferLength == mStoredBytes) {
                        wait();
                    }
                    int tail = mHead + mStoredBytes;
                    int oneRun;
                    if (tail >= bufferLength) {
                        tail = tail - bufferLength;
                        oneRun = mHead - tail;
                    } else {
                        oneRun = bufferLength - tail;
                    }
                    int bytesToCopy = Math.min(oneRun, length);
                    System.arraycopy(buffer, offset, mBuffer, tail, bytesToCopy);
                    offset += bytesToCopy;
                    mStoredBytes += bytesToCopy;
                    length -= bytesToCopy;
                }
                if (wasEmpty) {
                    notify();
                }
            }
        }
        private byte[] mBuffer;
        private int mHead;
        private int mStoredBytes;
    }
    /**
     * Defines callbacks for service binding, passed to bindService()
     *
     */

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothConexion.LocalBinder binder = (BluetoothConexion.LocalBinder) service;
            mBluetoothConexion = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mApplication.setConnection(false);
        }
    };

    /**
     * Método que contiene los listeners de los distintos elementos
     * de la interfaz y servirá para actualizarlos.
     * Este método está especialmente creado para modificar
     * la interfaz de manera dinámica.
     */
    private void actualizaInterfaz (){

        text5 = (TextView) findViewById(R.id.textView5);
        text6 = (TextView) findViewById(R.id.textView6);
        text7 = (TextView) findViewById(R.id.textView7);


        if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
            enable.setVisibility(LinearLayout.VISIBLE);

        if(mApplication.getConnection()){
            startActivity(new Intent(Pressmatic.this, Snippers.class));
           }

        BluetoothButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                    if ((mBluetoothAdapter != null)&&(!mBluetoothAdapter.isEnabled())) {
                        enable.setVisibility(LinearLayout.VISIBLE);
                    } else {
                        if(mApplication.getConnection()) {
                            startActivity(new Intent(Pressmatic.this, Snippers.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            finish();
                            mBluetoothConexion.send(pressm);
                        }else {
                            if (!mBound && mFirstConnection) {
                            Intent serverIntent = new Intent(Pressmatic.this, ListaDispositivos.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                        }else{
                            unbindService(mConnection);
                            mBluetoothConexion.stop();
                            mBluetoothConexion.start();
                            mFirstConnection = true;
                            startActivity(getIntent());
                        }}}
            }});


        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startActivity(new Intent(Pressmatic.this, Language.class));
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mBluetoothConexion.send(menu);
                startActivity(new Intent(Pressmatic.this, HowWorks1.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                enable.setVisibility(LinearLayout.INVISIBLE);
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mEnablingBT = true;
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                enable.setVisibility(LinearLayout.INVISIBLE);
            }
        });


        exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                exit();
            }
        });

        help_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(table){
                    commands.setVisibility(TableLayout.VISIBLE);
                    table=false;}
                else{
                    commands.setVisibility(TableLayout.INVISIBLE);
                    table=true;}
            }
        });
    }

    public void exit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.exit_app)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.app_name)
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        unbindService(mConnection);
                        mBluetoothConexion.stop();
                        Intent intent = new Intent(Pressmatic.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        finish();
                        startActivity(intent);
                    }
                });

        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
            }
            });

        AlertDialog alert = builder.create();
        alert.show();
    }
}

