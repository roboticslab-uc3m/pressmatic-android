package es.uc3m.roboticslab.pressmatic;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import android.view.View.OnClickListener;

import es.uc3m.roboticslab.pressmatic.R;


public class Language extends AppCompatActivity implements OnClickListener{

    private Button spanish, english;
    final Configuration configuration = new Configuration();
    public Locale locale=new Locale("es");
    private ImageButton speakButton;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        spanish =(Button) findViewById(R.id.button3);
        english =(Button) findViewById(R.id.button2);
        speakButton = (ImageButton) findViewById(R.id.speak);
        speakButton.setOnClickListener(this);

        PackageManager pm = getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(this);
        } else {
            speakButton.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Recognizer not present", Toast.LENGTH_SHORT).show();
        }

        spanish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                locale = new Locale("es");
                Locale.setDefault(locale);
                configuration.locale = locale;
                getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
                Toast.makeText(getApplicationContext(), "Lenguaje Español seleccionado", Toast.LENGTH_SHORT).show();
                finish();
            }});

        english.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                locale = new Locale("en");
                Locale.setDefault(locale);
                configuration.locale = locale;
                getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
                Toast.makeText(getApplicationContext(), "English display language", Toast.LENGTH_SHORT).show();
                finish();
            }});
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
            if (matches.contains("spanish")||matches.contains("español")||matches.contains("espanol")) {
                locale = new Locale("es");
                Locale.setDefault(locale);
                configuration.locale = locale;
                getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
                Toast.makeText(getApplicationContext(), "Lenguaje Español seleccionado", Toast.LENGTH_SHORT).show();
              //  startActivity(getIntent());
               finish();
            }

            if (matches.contains("english")||matches.contains("ingles")) {
                locale = new Locale("en");
                Locale.setDefault(locale);
                configuration.locale = locale;
                getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
                Toast.makeText(getApplicationContext(), "English display language", Toast.LENGTH_SHORT).show();
             //   startActivity(getIntent());
                finish();
            }

            if (matches.contains("exit") || matches.contains("salir")|| matches.contains("close")|| matches.contains("cerrar")
                    || matches.contains("desconectar") || matches.contains("disconnectar")) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }

        }
    }
}
