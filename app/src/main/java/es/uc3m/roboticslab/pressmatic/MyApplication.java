package es.uc3m.roboticslab.pressmatic;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;


/**
 * Created by javier on 17/12/16.
 */

public class MyApplication extends Application {

    private static MyApplication singleton;
    static String address;
    static BluetoothDevice device;
    private boolean connection;
    public static int flag=0;

    public MyApplication getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        if(flag == 1) {
            android.os.Process.killProcess(android.os.Process.myPid());
            flag=0;
        }
    }

    public void setConnection(boolean connection1){
        this.connection=connection1;
    }
    public boolean getConnection(){return connection;}

    Handler.Callback realCallback = null;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (realCallback != null) {
                realCallback.handleMessage(msg);
            }
        };
    };

    public Handler getHandler() {
        return handler;
    }
    public void setCallBack(Handler.Callback callback) {
        this.realCallback = callback;
    }


    /**
     * FUNCIONES PROPIAS DE MYAPPLICATION PARA OBTENER LOS DATOS DESDE LA ACTIVIDAD PRINCIPAL
     *
     */
    void setAddress (String str){

        address = str;

    }

    void setBluetoothDevice (BluetoothDevice btd){

        device = btd;

    }

    String getAddress (){

        return address;
    }

    BluetoothDevice getDevice (){

        return device;
    }

    void setHandler(Handler hdlr){
        handler = hdlr;
    }


}

