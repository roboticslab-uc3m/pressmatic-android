package es.uc3m.roboticslab.pressmatic;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import es.uc3m.roboticslab.pressmatic.R;

/**
 * Created by javier on 17/12/16.
 */

public class ModoContinuo extends Activity implements OnClickListener{

    private static BluetoothConexion mBluetoothConexion;
    protected static ToggleButton StartStop;
    protected static Button VelocLenta ;
    protected static Button VelocMedia ;
    protected static Button VelocRapida ;
    private Button speakButton,help;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    protected static BluetoothAdapter mBluetoothAdapter = null;
    public static final int MESSAGE_STATE_CHANGE = 1;
    private String encender = "0",lenta="5",media="6",rapida="7",pressm="a";
    protected static TextView text;
    private TableLayout commands;
    private boolean table=true;
    private  MyApplication mApplication;
    public static int flag=0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.modocontinuo);
        text = (TextView) findViewById(R.id.DescripcionContinuo);


        mBluetoothConexion = new BluetoothConexion(this, mHandlerBT);
        // mHandlerBT is sent to the App to maintain a global state
        mApplication= (MyApplication)getApplicationContext();
        mApplication.setHandler(mHandlerBT);

        // Se crean los elementos de la interfaz
        actualizaInterfaz();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothConexion.class);
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se actualizan los elementos de la interfaz
        actualizaInterfaz();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
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
                            startActivity(new Intent(ModoContinuo.this, Pressmatic.class));
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

    private void actualizaInterfaz() {

        StartStop = (ToggleButton) findViewById(R.id.StartStop);
        VelocLenta = (Button) findViewById(R.id.BotonLenta);
        VelocMedia = (Button) findViewById(R.id.BotonMedia);
        VelocRapida = (Button) findViewById(R.id.BotonRapida);
        speakButton = (Button) findViewById(R.id.speak);
        speakButton.setOnClickListener(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        help = (Button) findViewById(R.id.Button5);
        commands=(TableLayout) findViewById(R.id.commands);

        if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
            startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

        if(!mApplication.getConnection())
            startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

        StartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

                if(!mApplication.getConnection())
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));
                else
                    mBluetoothConexion.send(encender);
            }
        });

        VelocLenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

                if(!mApplication.getConnection())
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));
                else
                    mBluetoothConexion.send(lenta);
            }
        });

        VelocMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

                if(!mApplication.getConnection())
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));
                else
                    mBluetoothConexion.send(media);
            }
        });

        VelocRapida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

                if(!mApplication.getConnection())
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));
                else
                    mBluetoothConexion.send(rapida);
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
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
        // Check to see if a recognition activity is present
        // if running on AVD virtual device it will give this message. The mic
        // required only works on an actual android device
        PackageManager pm = getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(this);
        } else {
            speakButton.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Recognizer not present", Toast.LENGTH_SHORT).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Toast.makeText(getApplicationContext(),matches.toString(), Toast.LENGTH_LONG).show();

            // matches is the result of voice input. It is a list of what the user possibly said
            if (matches.contains("on")||matches.contains("encender")||matches.contains("cut")||matches.contains("cortar")
                    ||matches.contains("start")||matches.contains("comenzar")||
                    (matches.contains("off")||matches.contains("apagar")||matches.contains("parar")||matches.contains("stop"))) {

                if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

                if(!mApplication.getConnection())
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));
                else
                    mBluetoothConexion.send(encender);
                }

            if (matches.contains("exit") || matches.contains("salir")|| matches.contains("close")|| matches.contains("cerrar")
                    || matches.contains("desconectar")) {
                exit();

            }
            if (matches.contains("slow")||matches.contains("lenta")) {
                if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

                if(!mApplication.getConnection())
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));
                else
                    mBluetoothConexion.send(lenta);
                }

            if (matches.contains("medium")||matches.contains("media")){
                if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

                if(!mApplication.getConnection())
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));
                else
                    mBluetoothConexion.send(media);
            }

            if (matches.contains("high")||matches.contains("alta")){
                if((mBluetoothAdapter == null)||(!mBluetoothAdapter.isEnabled()))
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));

                if(!mApplication.getConnection())
                    startActivity(new Intent(ModoContinuo.this, Pressmatic.class));
                else
                    mBluetoothConexion.send(rapida);}
        }
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

                        Intent intent = new Intent(ModoContinuo.this, ModoContinuo.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

