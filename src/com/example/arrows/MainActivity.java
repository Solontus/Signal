package com.example.arrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.example.arrows.R.string;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
	OutputStream mmOutputStream;
	BluetoothSocket mmSocket;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
       
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView sign = (TextView) findViewById(R.id.textView1);
    	sign.setText("Straight Ahead"); 
    	checkVoiceRecognition();
    	AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
    	dlgAlert.setMessage("Jacket not paired.");
    	dlgAlert.setTitle("Cannot start.");
    	dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//MainActivity.this.finish();
			}
		});
    	dlgAlert.setCancelable(false);
    	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if(!mBluetoothAdapter.isEnabled())
    	{
    	   Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	   startActivityForResult(enableBluetooth, 0);
    	}
    	BluetoothDevice mmDevice = null;
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("RN42-DCBD")) //Note, you will need to change this to match the name of your device
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
		try {
			mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
			mmSocket.connect();
	        mmOutputStream = mmSocket.getOutputStream();
	        InputStream mmInputStream = mmSocket.getInputStream();
		} catch (IOException e) {
        	dlgAlert.create().show();
		}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void checkVoiceRecognition() {
    	  // Check if voice recognition is present
    	  Button mbtSpeak = (Button) findViewById(R.id.button3);
    	  PackageManager pm = getPackageManager();
    	  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
    	    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
    	  if (activities.size() == 0) {
    	   mbtSpeak.setEnabled(false);
    	   mbtSpeak.setText("Voice recognizer not present");
    	   Toast.makeText(this, "Voice recognizer not present",
    	     Toast.LENGTH_SHORT).show();
    	  }
    }
    
    public void speak(View view){
    	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    	intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
    	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    	intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);
    	startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
    
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
    	if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
	    	if(resultCode == RESULT_OK) {
	    		 
	    	    ArrayList<String> textMatchList = data
	    	    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	    	 
	    	    if (!textMatchList.isEmpty()) {
	    	    	for(String s: textMatchList){
	    	    		System.out.println(s.toLowerCase());
	    	    		if (s.toLowerCase().equals("left")){
	    	    			turnLeft(null);
	    	    			return;
	    	    		}
	    	    		else if (s.toLowerCase().equals("right")){
	    	    			turnRight(null);
	    	    			return;
	    	    		}
	    	    		else if (s.toLowerCase().equals("stop")){
	    	    			stopAll(null);
	    	    			return;
	    	    		}
	    	    	}
	    	    }
	    	   //Result code for various error.
	    	   }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
	    	    showToastMessage("Audio Error");
	    	   }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
	    	    showToastMessage("Client Error");
	    	   }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
	    	    showToastMessage("Network Error");
	    	   }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
	    	    showToastMessage("No Match");
	    	   }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
	    	    showToastMessage("Server Error");
	    	   }
    	  super.onActivityResult(requestCode, resultCode, data);
    	 }
    	 /**
    	 * Helper method to show the toast message
    	 **/
    void showToastMessage(String message){
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    public void turnLeft(View view){
    	String disp = "Left Turn";
    	Boolean connected = true;
    	System.out.println("Turn Left Pressed");
    	TextView sign = (TextView) findViewById(R.id.textView1);
    	String msg = "L\n";
    	try {
			mmOutputStream.write(msg.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(!retryConnection()) {
				disp = "Jacket not connected";
				connected = false;
			}
		}
    	sign.setText(disp);
    	Animation c = (Animation) AnimationUtils.loadAnimation(this, R.anim.flash);
    	c.reset();
    	c.setRepeatMode(Animation.REVERSE);
    	c.setRepeatCount(Animation.INFINITE);
    	Button left = (Button) findViewById(R.id.button1);
    	Button right = (Button) findViewById(R.id.button2);
    	right.clearAnimation();
    	left.startAnimation(c);
    	if (!connected){
    		left.clearAnimation();
    	}
    }
    
    public void turnRight(View view){
    	String disp = "Right Turn";
    	Boolean connected = true;
    	TextView sign = (TextView) findViewById(R.id.textView1);
    	String msg = "R\n";
    	try {
			mmOutputStream.write(msg.getBytes());
		} catch (Exception e) {
			if(!retryConnection()) {
				disp = "Jacket not connected";
				connected = false;
			}
		}
    	sign.setText(disp);
    	Animation c = (Animation) AnimationUtils.loadAnimation(this, R.anim.flash);
    	c.reset();
    	c.setRepeatMode(Animation.REVERSE);
    	c.setRepeatCount(Animation.INFINITE);
    	Button right = (Button) findViewById(R.id.button2);
    	Button left = (Button) findViewById(R.id.button1);
    	left.clearAnimation();
    	right.startAnimation(c);
    	if (!connected){
    		right.clearAnimation();
    	}
    }
    
    public void stopAll(View view){
    	String disp = "Straight Ahead";
    	Button right = (Button) findViewById(R.id.button1);
    	Button left = (Button) findViewById(R.id.button2);
    	String msg = "S\n";
    	try {
			mmOutputStream.write(msg.getBytes());
		} catch (Exception e) {
			if(!retryConnection()) {
				disp = "Jacket not connected";
			}
		}
    	left.clearAnimation();
    	right.clearAnimation();
    	TextView sign = (TextView) findViewById(R.id.textView1);
    	sign.setText(disp);
    	System.out.println("Stopped");
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.e("rotate", "ORIENTATION_LANDSCAPE");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.e("rotate", "ORIENTATION_PORTRAIT");
        }
    }
    
    public boolean retryConnection(){
    	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if(!mBluetoothAdapter.isEnabled())
    	{
    	   Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	   startActivityForResult(enableBluetooth, 0);
    	}
    	BluetoothDevice mmDevice = null;
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("RN42-DCBD")) //Note, you will need to change this to match the name of your device
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
		try {
			mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
			mmSocket.connect();
	        mmOutputStream = mmSocket.getOutputStream();
	        InputStream mmInputStream = mmSocket.getInputStream();
		} catch (Exception e) {
        	return false;
		}
		return true;
    }
}
