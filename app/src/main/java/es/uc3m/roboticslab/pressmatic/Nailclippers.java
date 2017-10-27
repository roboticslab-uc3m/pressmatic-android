package es.uc3m.roboticslab.pressmatic;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import android.view.View.OnClickListener;

import es.uc3m.roboticslab.pressmatic.R;

public class Nailclippers extends AppCompatActivity implements OnClickListener{

    private ImageButton settings,help,left,right,speakButton;
    private Button select,help_button;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    protected static BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothConexion mBluetoothConexion;
    public static final int MESSAGE_STATE_CHANGE = 1;
    private  String cortauñas = "e",pressm="a",menu="f";
    private TableLayout commands;
    private boolean table=true;
    private MyApplication mApplication;
    private ScrollView scrollView;
    public static int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nailclippers);

        settings= (ImageButton) findViewById(R.id.imageButton3);
        help=(ImageButton) findViewById(R.id.imageButton2);
        select= (Button) findViewById(R.id.button5);
        left= (ImageButton) findViewById(R.id.imageButton);
        right=(ImageButton) findViewById(R.id.imageButton1);
        speakButton = (ImageButton) findViewById(R.id.speak);
        speakButton.setOnClickListener(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        help_button = (Button) findViewById(R.id.Button5);
        commands=(TableLayout) findViewById(R.id.commands);
        mBluetoothConexion = new BluetoothConexion(this, mHandlerBT);
        // mHandlerBT is sent to the App to maintain a global state
        mApplication= (MyApplication)getApplicationContext();
        mApplication.setHandler(mHandlerBT);
        scrollView = (ScrollView) findViewById(R.id.scrollview);

        actualizaInterfaz();

        PackageManager pm = getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(this);
        } else {
            speakButton.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Recognizer not present", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothConexion.class);
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
        mBluetoothConexion.send(pressm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizaInterfaz();
    }

    private void actualizaInterfaz() {

        if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled())){
            startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
    }

        if (!mApplication.getConnection()){
            startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
    }

        left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!mApplication.getConnection()){
                    startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
            }else{
                    startActivity(new Intent(Nailclippers.this, Scissors.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();}
            }});

        right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(!mApplication.getConnection()){
                    startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                    mBluetoothConexion.send(pressm);}
                else
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_last_activity), Toast.LENGTH_SHORT).show();
            }});

        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startActivity(new Intent(Nailclippers.this, Language.class));
            }});

        help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBluetoothConexion.send(menu);
                startActivity(new Intent(Nailclippers.this, HowWorks1.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }});

        help_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(table){
                    commands.setVisibility(TableLayout.VISIBLE);
                    table=false;}
                else{
                    commands.setVisibility(TableLayout.INVISIBLE);
                    table=true;}
            }});

        select.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!mApplication.getConnection()){
                    startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
            }else{
                mBluetoothConexion.send(cortauñas);
                startActivity(new Intent(Nailclippers.this, ModoAgarreInst.class));}

                if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled())) {
                    startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                }
            }});

        scrollView.setOnTouchListener(new OnSwipeTouchListener(Nailclippers.this) {
            @Override
            public void onSwipeLeft() {
                if (!mApplication.getConnection()){
                    startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
            }else
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_last_activity), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSwipeRight() {
                if (!mApplication.getConnection()) {
                    startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                    mBluetoothConexion.send(pressm);

            }else{
                    startActivity(new Intent(Nailclippers.this, Scissors.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }}
        });
    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandlerBT = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case BluetoothConexion.STATE_CONNECTED:
                            mApplication.setConnection(true);
                            break;

                        case BluetoothConexion.STATE_NONE:
                            mApplication.setConnection(false);
                            startActivity(new Intent(Nailclippers.this, Pressmatic.class));
                            break;
                    }}
        }
    };

        private ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                BluetoothConexion.LocalBinder binder = (BluetoothConexion.LocalBinder) service;
                mBluetoothConexion = binder.getService();
                mApplication.setConnection(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mApplication.setConnection(false);
            }
        };

        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition");
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }

         @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
              super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
                // Fill the list view with the strings the recognizer thought it could have heard
                ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Toast.makeText(getApplicationContext(), matches.toString(), Toast.LENGTH_LONG).show();

                // matches is the result of voice input. It is a list of what the user possibly said
                if (matches.contains("left") || matches.contains("izquierda") || matches.contains("izquierdo")) {
                    if (!mApplication.getConnection()){
                        startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        finish();
                }else {
                    startActivity(new Intent(Nailclippers.this, Scissors.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                }
                }

                if (matches.contains("right") || matches.contains("derecha") || matches.contains("derecho")) {
                    if (!mApplication.getConnection()) {
                        startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        finish();
                    }else
                        Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_last_activity), Toast.LENGTH_SHORT).show();
                }

                if (matches.contains("settings") || matches.contains("languaje") || matches.contains("ajustes") || matches.contains("idioma"))
                    startActivity(new Intent(Nailclippers.this, Language.class));

                if (matches.contains("help") || matches.contains("ayuda") || matches.contains("instructions") || matches.contains("instrucciones")) {
                    mBluetoothConexion.send(menu);
                    startActivity(new Intent(Nailclippers.this, HowWorks1.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                }
                if (matches.contains("exit") || matches.contains("salir") || matches.contains("close") || matches.contains("cerrar")
                        || matches.contains("desconectar")) {
                    exit();
                }

                if (matches.contains("select") || matches.contains("seleccionar")) {

                    if (!mApplication.getConnection()){
                        startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        finish();
                    } else {
                        mBluetoothConexion.send(cortauñas);
                        startActivity(new Intent(Nailclippers.this, ModoAgarreInst.class));}

                    if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled())) {
                        startActivity(new Intent(Nailclippers.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        finish();
                    }
            }
        }}
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

                        Intent intent = new Intent(Nailclippers.this, Nailclippers.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        finish();
                        flag = 1;
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
