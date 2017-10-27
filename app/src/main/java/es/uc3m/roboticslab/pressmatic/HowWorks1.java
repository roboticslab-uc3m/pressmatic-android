package es.uc3m.roboticslab.pressmatic;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import es.uc3m.roboticslab.pressmatic.R;

import java.util.ArrayList;
import java.util.List;

public class HowWorks1 extends AppCompatActivity implements OnClickListener {

    private ImageButton left, right, speakButton, zoom;
    private Button button_X;
    protected static BluetoothAdapter mBluetoothAdapter = null;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    protected static TextView text, text2, text3;
    private int i = 16;
    private static BluetoothConexion mBluetoothConexion = null;
    public static final int MESSAGE_STATE_CHANGE = 1;
    private MyApplication mApplication;
    private String pressm = "a", menu = "f";
    private ScrollView scrollView;
    public static int flag=0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_how_works1);

        left = (ImageButton) findViewById(R.id.imageButton);
        right = (ImageButton) findViewById(R.id.imageButton1);
        button_X = (Button) findViewById(R.id.button);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        speakButton = (ImageButton) findViewById(R.id.speak);
        speakButton.setOnClickListener(this);
        text = (TextView) findViewById(R.id.textView);
        text2 = (TextView) findViewById(R.id.textView2);
        text3 = (TextView) findViewById(R.id.textView3);
        zoom = (ImageButton) findViewById(R.id.imageButton4);
        scrollView = (ScrollView) findViewById(R.id.scrollview);

        mBluetoothConexion = new BluetoothConexion(this, mHandlerBT);
        // mHandlerBT is sent to the App to maintain a global state
        mApplication = (MyApplication) getApplicationContext();
        mApplication.setHandler(mHandlerBT);

        actualizaInterfaz();

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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        actualizaInterfaz();
    }


    private void actualizaInterfaz() {

        left.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_first_activity), Toast.LENGTH_SHORT).show();
            }
        });

        right.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                startActivity(new Intent(HowWorks1.this, HowWorks2.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
        });

        button_X.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mBluetoothConexion.send(pressm);

                if (mApplication.getConnection()) {
                    startActivity(new Intent(HowWorks1.this, Snippers.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
                }else{
                    startActivity(new Intent(HowWorks1.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                }
            }
        });

        zoom.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                if (i == 22) {
                    text.setTextSize(i);
                    i = 16;
                }
                if (i >= 16 & i < 22) {
                    text.setTextSize(i);
                    i += 2;
                }
            }
        });

        scrollView.setOnTouchListener(new OnSwipeTouchListener(HowWorks1.this) {
            @Override
            public void onSwipeLeft() {
                startActivity(new Intent(HowWorks1.this, HowWorks2.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
            @Override
            public void onSwipeRight() {
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_first_activity), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandlerBT = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // actualizar los elementos de la intefaz según el estado de conexión.
            switch (msg.arg1) {

                case BluetoothConexion.STATE_CONNECTED:
                    mApplication.setConnection(true);
                    break;

                case BluetoothConexion.STATE_NONE:
                    mApplication.setConnection(false);
                    break;
            }
        }
    };

    public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            Toast.makeText(getApplicationContext(), matches.toString(), Toast.LENGTH_LONG).show();

            // matches is the result of voice input. It is a list of what the user possibly said
            if (matches.contains("left") || matches.contains("izquierda") || matches.contains("izquierdo")){
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.toast_first_activity), Toast.LENGTH_SHORT).show();
                }

            if (matches.contains("right") || matches.contains("derecha") || matches.contains("derecho")){
                startActivity(new Intent(HowWorks1.this, HowWorks2.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();}

            if (matches.contains("close") || matches.contains("cerrar") || matches.contains("salir")) {
                mBluetoothConexion.send(pressm);

                if (mApplication.getConnection()){
                    startActivity(new Intent(HowWorks1.this, Snippers.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
                }else {
                    startActivity(new Intent(HowWorks1.this, Pressmatic.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                }

            }
            if (matches.contains("exit") || matches.contains("salir") || matches.contains("close") || matches.contains("cerrar")
                    || matches.contains("desconectar") || matches.contains("disconnectar")) {
                unbindService(mConnection);
                mBluetoothConexion.stop();
                exit();
            }
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("HowWorks1 Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
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
                        Intent intent = new Intent(HowWorks1.this, HowWorks1.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        flag = 1;
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
