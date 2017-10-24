package com.javier.pressmatic;

/**
 * Created by javier on 17/12/16.
 */

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;



/**
 * Created by Alex on 11/05/14.
 * Esta clase crea todo el protocolo de comunicacion
 * con otros dispositivos. Tendra 3 threads , uno que escucha
 * las conexiones entrantes, una que conecta con un dispositivo
 * y otra para realizar la transmision de datos una vez conectado
 *
 */
public class BluetoothConexion extends Service {

    private String LOG_TAG = "Pressmatic";

    private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    //BluetoothAdapter y Handler antes eran final
    private BluetoothAdapter mAdapter;
    private Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    private boolean mAllowInsecureConnections;
    private Context mContext;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    /**
     * Constructores para el objeto BluetoothConexion
     *
     *
     */

    public BluetoothConexion(){
        mState = STATE_NONE;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAllowInsecureConnections = true;
    }
    public BluetoothConexion(Context context, Handler handler){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mContext = context;
        mAllowInsecureConnections = true;

    }

    public BluetoothConexion(Context context, Handler handler,int State){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = State;
        mHandler = handler;
        mContext = context;
        mAllowInsecureConnections = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BluetoothConexion", "Service started");
    }

    /**
     * Obtiene el estado de la conexion
     * @param state define el estado actual
     */
    private synchronized void setState (int state){
        mState = state;
        mHandler.obtainMessage(Pressmatic.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Devuelve el estado de la conexion
     */
    public synchronized int getState() {
        return mState;}


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */

    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
    }


    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {


        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Pressmatic.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Pressmatic.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
        // Si estado actual es conectado, para el servicio
        if (getState()==3){
            stopSelf();
        }
    }

    @Override
    public boolean stopService(Intent name) {
        setState(STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mAdapter.cancelDiscovery();

        // Si estado actual es conectado, para el servicio
        if (getState()==3){
            return super.stopService(name);
        } else {
            return false;
        }

    }

    @Override
    public void onDestroy() {
        if (getState()==3){
            stop();
            super.onDestroy();
        }

    }

    /**
     * Write to the ConnectedThread in an asynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_NONE);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Pressmatic.MESSAGE_TOAST);
        Bundle bundle = new Bundle();


        bundle.putString(Pressmatic.TOAST, getApplicationContext().getString(R.string.toast_unable_to_connect));
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {

        setState(STATE_NONE);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Pressmatic.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        //bundle.putString(Pressmatic.TOAST, mContext.getString(R.string.toast_connection_lost) );
        bundle.putString(Pressmatic.TOAST, getApplicationContext().getString(R.string.toast_connection_lost) );
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }


    /**
     * FUNCIONES Y PARÁMETROS RELACIONADOS CON EL SERVICE
     */

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        mHandler = ((MyApplication) getApplication()).getHandler();
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BluetoothConexion getService() {
            return BluetoothConexion.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

          /* Le pasamos el estado del Handler a MyApplication */
        mHandler = ((MyApplication) getApplication()).getHandler();
        if (mAdapter != null) {
            MyApplication mApplication = (MyApplication) getApplicationContext();
            BluetoothDevice dispositivo = mApplication.getDevice();
            String macAddress = mApplication.getAddress();
            if (macAddress != null && macAddress.length() > 0) {
                connect(dispositivo);
            } else {
                if (getState()==3)
                    stopSelf();
                return 0;
            }
        }
        String stopservice = intent.getStringExtra("stopservice");
        if (stopservice != null && stopservice.length() > 0) {
            stop();
        }
        return START_STICKY;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread (BluetoothDevice device){
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try{
                if (mAllowInsecureConnections ) {
                    Method method;

                    method= device.getClass().getMethod("createRfcommSocket", new Class[] {int.class} );
                    tmp = (BluetoothSocket) method.invoke(device,1);

                }
                // de lo contrario el canal al que se conecta será el seguro
                else {
                    tmp = device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
                }
            } catch (Exception e){
            }
            mmSocket = tmp;
        }


        public void run() {
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {

                }
                // Start the service over to restart listening mode
                //BluetoothSerialService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConexion.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {

            }
        }

    }


    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    //mEmulatorView.write(buffer, bytes);
                    // Send the obtained bytes to the UI Activity
                    //mHandler.obtainMessage(BlueTerm.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {

                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Pressmatic.MESSAGE_WRITE, buffer.length, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {

            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {

            }
        }
    }


    public void setAllowInsecureConnections( boolean allowInsecureConnections ) {
        mAllowInsecureConnections = allowInsecureConnections;
    }

    public boolean getAllowInsecureConnections() {
        return mAllowInsecureConnections;
    }

    public void send(String data) {
        send(data.getBytes());
    }
    public void send(byte[] data) {
        write(data);

    }

}