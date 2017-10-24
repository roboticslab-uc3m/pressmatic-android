package com.javier.pressmatic;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    final Handler handler = new Handler();
    public static int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(flag == 1) {
            this.finish();
            flag=0;
        }
        handler.postDelayed(new Runnable(){
            public void run(){

        startActivity(new Intent(MainActivity.this, HowWorks1.class));}}, 3000);
    }

    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);

        if(flag == 1) {
            this.finish();
            flag=0;
        }

    }
}
