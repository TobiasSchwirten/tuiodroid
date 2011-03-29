/*
 TUIOdroid http://www.tuio.org/
 An Open Source TUIO Tracker for Android
 (c) 2011 by Tobias Schwirten and Martin Kaltenbrunner
 
 TUIOdroid is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 TUIOdroid is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with TUIOdroid.  If not, see <http://www.gnu.org/licenses/>.
*/

package tuioDroid.impl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import java.util.*;
import java.net.*;

/**
 * Main Activity
 * @author Tobias Schwirten
 * @author Martin Kaltenbrunner
 */
public class MainActivity extends Activity {
  
	/**
	 * View that shows the Touch points etc
	 */
	private TouchView touchView;
	
	/**
	 * Request Code for the Settings activity to define 
	 * which child activity calls back
	 */
	private static final int REQUEST_CODE_SETTINGS = 0;

	/**
	 * Device IP Address
	 */
	private String devIP;
	
	/**
	 * IP Address for OSC connection
	 */
	private String oscIP;
	
	/**
	 * Port for OSC connection
	 */
	private int oscPort;
	
	/**
	 * Adjusts the Touch View
	 */
	private boolean drawAdditionalInfo;
	
	/**
	 * Adjusts the Touch View
	 */
	private int screenOrientation;
	
	
	/**
	 *  Called when the activity is first created. 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* load preferences */
        SharedPreferences settings = this.getPreferences(MODE_PRIVATE);
      
        /* get Values */
        devIP = getLocalIpAddress();
        oscIP = settings.getString("myIP", "192.168.1.2");
        oscPort = settings.getInt("myPort", 3333);
        drawAdditionalInfo = settings.getBoolean("ExtraInfo", true);
        screenOrientation = settings.getInt ("ScreenOrientation", 0);
        this.adjustScreenOrientation(this.screenOrientation);
        
        touchView  = new TouchView(this,devIP,oscIP,oscPort,drawAdditionalInfo);
        setContentView(touchView);
    }
   
    
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {}
        return null;
    }
    
    /**
     *  Called when the options menu is created
     *  Options menu is defined in m.xml 
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.m, menu);
    	return true;
    }

    
    /**
     * Called when the user selects an Item in the Menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
    	// Handle item selection
        switch (item.getItemId()) {
	        case R.id.settings:
	            this.openSettingsActivity();
	            return true;
	 
	        case R.id.help:
	        	this.openHelpActivity();
	            return true;
	            
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    

	
	/**
	 * Opens the Activity that provides the Settings
	 */
    private void openSettingsActivity (){
    	Intent myIntent = new Intent();
    	myIntent.setClassName("tuioDroid.impl", "tuioDroid.impl.SettingsActivity"); 
    	myIntent.putExtra("IP_in", oscIP);
    	myIntent.putExtra("Port_in", oscPort);
    	myIntent.putExtra("ExtraInfo", this.drawAdditionalInfo);
    	myIntent.putExtra("ScreenOrientation", this.screenOrientation);
    	startActivityForResult(myIntent, REQUEST_CODE_SETTINGS);
    }
    
    
    /**
	 * Opens the Activity that Help information
	 */
    private void openHelpActivity (){
    	Intent myIntent = new Intent();
     	myIntent.setClassName("tuioDroid.impl", "tuioDroid.impl.HelpActivity");
     	startActivity(myIntent);  
    }
    
    

    /**
     * Listens for results of new child activities. 
     * Different child activities are indentified by their requestCode
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
       
    	 // See which child activity is calling us back.
    	if(requestCode == REQUEST_CODE_SETTINGS){
        	
        	switch (resultCode){
        	
        		case RESULT_OK:
        			Bundle dataBundle = data.getExtras(); 
        		    
        	    	String ip = dataBundle.getString("IP");
        	    	String port_String = dataBundle.getString("Port");
        	    	int port = Integer.parseInt(port_String);
        	    	
        	    		
        	    	if(this.checkNetworkData(ip, port)){
        	    		
        	    	 	this.oscIP = ip;
            	    	this.oscPort = port;        	
            	    	this.drawAdditionalInfo = dataBundle.getBoolean("ExtraInfo");
            	    	
            	    	this.touchView.setNewOSCConnection(ip, port);
            	    	this.touchView.setDrawAdditionalInfo(this.drawAdditionalInfo);
            	    	
            	    	/* Change behaviour of screen rotation */
            	    	this.screenOrientation  = dataBundle.getInt("ScreenOrientation");
            	    	this.adjustScreenOrientation(this.screenOrientation);
    	
            	    	
        	    		/* Get preferenced, edit and commit */
            	    	SharedPreferences settings = this.getPreferences(MODE_PRIVATE);
            	    	SharedPreferences.Editor editor = settings.edit();
            	    	/* define Key/Value */
            	    	editor.putString("myIP", this.oscIP);
            	    	editor.putInt("myPort", this.oscPort);
            	    	editor.putBoolean("ExtraInfo",this.drawAdditionalInfo);
            	    	editor.putInt("ScreenOrientation",this.screenOrientation);
            	    	/* speichern */
            	    	editor.commit();            	    	        			
        	    	}
        	    	
        	    	else{
        	    		CharSequence text ="Invalid IP or Port! \nPort number only between 3330 and 3340 \nIP must be like this n.n.n.n \nwhere n is number between 0 and 255!";
        	    		int duration = Toast.LENGTH_LONG;
        	    
        	    		Toast toast = Toast.makeText(this, text, duration);
        	    		toast.show();
        	    	}
        	    	
        	    	break;
        	    
        	    
        	    default:
        	    	// Do nothing
        		
        	}
    	}
    }

    
    /**
     * Adjusts the screen orientation
     */
    private void adjustScreenOrientation (int screenOrientation){
    	
    	switch(screenOrientation){
    	
    	case 0: this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    			break;
    			
    	case 1: this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
				
    	case 2: this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
	
    	default: 	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					break;
	
	}	
    	
    }
    
    
    /**
     * Checks if the IP and port is valid
     */
    private boolean checkNetworkData (String ip, int port){
    	
    	boolean valid = false;
    	
    	if (port >=3330 && port <=3340){
    		valid = true;
    		System.out.println("Port number valid!");
    	}
    	else{
    		valid = false;
    	}
    	
    	
		if (valid) {
			
			String[] parts = ip.split("\\.");

			for (String s : parts) {

				int i = Integer.parseInt(s);
				if (i < 0 || i > 255) {
					valid = false;
				}
			}
    		
    	}
    	
    	return valid;
    	
    }
    
}