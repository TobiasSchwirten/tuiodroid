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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.util.DisplayMetrics;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;

import tuioDroid.impl.TuioPoint;
import tuioDroid.osc.OSCInterface;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.*;
import android.view.*;

/**
 * Main View
 * @author Tobias Schwirten
 * @author Martin Kaltenbrunner
 */
public class TouchView extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final int MAX_TOUCHPOINTS = 10;
	private static final int FRAME_RATE = 40;
	private Paint textPaint = new Paint();
	private Paint idPaint = new Paint();
	private Paint touchPaint = new Paint();

	private ArrayList<TuioPoint> tuioPoints;
	
	private int width, height;
	private float scale = 1.0f;
	
	private OSCInterface oscInterface ;
	private int counter_fseq = 0;
	private int cw, ch = 0;
	private int bx, by = 0;
	
	public boolean drawAdditionalInfo;
	public boolean sendPeriodicUpdates;
	
	private String sourceName;
	private int sessionId = 0;
	
	private long startTime;
	private long lastTime = 0;

	private Bitmap backgroundImage;

	private boolean running = false;

	/**
	 * Constructor
	 * @param context
	 * @param devIP
	 * @param oscIP
	 * @param oscPort
	 */
	public TouchView(Context context, String oscIP, int oscPort, boolean drawAdditionalInfo, boolean sendPeriodicUpdates) {
		super(context, null);
		oscInterface  = new OSCInterface(oscIP , oscPort);
		this.drawAdditionalInfo = drawAdditionalInfo;
		this.sendPeriodicUpdates = sendPeriodicUpdates;
		startTime = System.currentTimeMillis();
		
		tuioPoints = new ArrayList<TuioPoint>();
		sourceName = "TUIOdroid@"+getLocalIpAddress();
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		setFocusable(true); // make sure we get key events
		setFocusableInTouchMode(true); // make sure we get touch events

		textPaint.setColor(Color.DKGRAY);
		idPaint.setColor(Color.LTGRAY);
		touchPaint.setColor(Color.rgb(34,68,136));
		touchPaint.setStrokeWidth(2);
		touchPaint.setStyle(Style.FILL);
		touchPaint.setAntiAlias(true);
		touchPaint.setTextSize(0.1f);
		//touchPaint.setAlpha(150);
		
		backgroundImage = BitmapFactory.decodeResource(getResources(),R.drawable.up);
	}
	
	/**
	 * Is called if a touch events occurs
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		long timeStamp = System.currentTimeMillis() - startTime;
		long dt = timeStamp - lastTime;
		lastTime = timeStamp;
		
		//always send on ACTION_DOWN & ACTION_UP
		if ((event.getActionMasked() == MotionEvent.ACTION_DOWN) || (event.getActionMasked() == MotionEvent.ACTION_UP)) dt = 1000;
		if ((event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) || (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)) dt = 1000;

		int pointerCount = event.getPointerCount();
		//android.util.Log.v("PointerCount",""+pointerCount);
		
		if (pointerCount > MAX_TOUCHPOINTS) {
			pointerCount = MAX_TOUCHPOINTS;
		}
		
		cw = getWidth();
		ch = getHeight();
		
		bx = cw/2 - backgroundImage.getWidth()/2;
		by = ch/2 - backgroundImage.getHeight()/2;
		
		Canvas c = getHolder().lockCanvas();
		
		if (c != null) {
			c.drawColor(Color.WHITE);
			c.drawBitmap(backgroundImage,bx,by,null);
			
			if ((event.getActionMasked() == MotionEvent.ACTION_UP) || (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)) {				
	
				int i =  event.getActionIndex();

				for (int j = 0; j < tuioPoints.size(); j++) {
														
					if(event.getPointerId(i) == tuioPoints.get(j).getTouchId()){
						tuioPoints.remove(j);
						break;
					}	
				}
			
			} else if ((event.getActionMasked() == MotionEvent.ACTION_DOWN) || (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)) {

				int i =  event.getActionIndex();
				int id = event.getPointerId(i);
				float x = width * event.getX(i);
				float y = height * event.getY(i);						

				// add new Point
				tuioPoints.add(new TuioPoint(sessionId,id,x/cw,y/ch,timeStamp));
				sessionId++;

			} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {

				// update existing Points
				for (int i = 0; i < pointerCount; i++) {
					int id = event.getPointerId(i);
					float x = width * event.getX(i);
					float y = height * event.getY(i);
										
					/* Check if this touch ID already exists */
					for(int j=0; j<tuioPoints.size(); j++){
						
						if(tuioPoints.get(j).getTouchId() == id){
							 tuioPoints.get(j).update(x/cw,y/ch,timeStamp);
							 break;	
						}
					}
				}	
			}
			
			// draw all existing Points
			for(int i=0; i<tuioPoints.size(); i++) {
				int x = (int)Math.round(tuioPoints.get(i).getX());
				int y = (int)Math.round(tuioPoints.get(i).getY());
				int id = tuioPoints.get(i).getTouchId();
				
				c.drawLine(0, y, width, y, touchPaint);
				c.drawLine(x, 0, x, height, touchPaint);
				c.drawCircle(x, y, 40 * scale, touchPaint);
				c.drawText("" +id, x, y, idPaint);
				
				if(drawAdditionalInfo){
					int textPos = 5 + (i+1)*(int)(textPaint.getTextSize());
					c.drawText("x" + i + "=" + x + " y" + i + "=" + y, 5, textPos, textPaint);
				}
			}
			
			if ((!oscInterface.isReachable()) || (drawAdditionalInfo)) drawInfo(c);
			getHolder().unlockCanvasAndPost(c);
		}
		
		if ((!sendPeriodicUpdates) && (dt>(1000/FRAME_RATE)) ) sendTUIOdata();
		return true;
	}
	
	private void drawInfo(Canvas c) {
		
		if (!oscInterface.isReachable()) {
			
			c.drawText("client does not respond (check firewall)", 5, height-2*textPaint.getTextSize()-5,textPaint );
			
		}
		
		String sourceString = "TUIO source: "+sourceName;
	
		c.drawText(sourceString, 5, height-textPaint.getTextSize()-5,textPaint );
		String clientString = "TUIO/UDP client: "+this.oscInterface.getInetAdress() + ":" + this.oscInterface.getPort();
		c.drawText(clientString, 5, height-5,textPaint );
	}
	
	/**
	 * Sends the TUIO Data
	 * @param blobList
	 */
	public void sendTUIOdata () throws ArrayIndexOutOfBoundsException {
	
		OSCBundle oscBundle = new OSCBundle();

		/*
		 * SOURCE Message
		 */
		Object outputData[] = new Object[2];
		outputData[0] = "source";
		outputData[1] = sourceName;
		//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
		oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));
		
		/*
		 * ALIVE Message
		 */
		outputData = new Object[tuioPoints.size() + 1];
		outputData[0] = "alive";
	
		for (int i = 0; i < tuioPoints.size(); i++) {
			outputData[1 + i] = (Integer)tuioPoints.get(i).getSessionId(); // ID
		}

		//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
		oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));

		
		/*
		 * SET Message
		 */
		for (int i = 0; i < tuioPoints.size(); i++) {

			outputData = new Object[7];

			outputData[0] = "set";
			outputData[1] = (Integer) tuioPoints.get(i).getSessionId(); // ID

			outputData[2] = (Float) tuioPoints.get(i).getX(); // x KOORD
			outputData[3] = (Float) tuioPoints.get(i).getY(); // y KOORD

			outputData[4] = (Float) tuioPoints.get(i).getXVel(); // Velocity Vector X
			outputData[5] = (Float) tuioPoints.get(i).getYVel(); // Velocity Vector Y

			outputData[6] = (Float) tuioPoints.get(i).getAccel(); // Acceleration

			//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
			oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));
		}

		
		/*
		 * FSEQ Message
		 */
		outputData = new Object[2];
		outputData[0] = (String) "fseq";
		outputData[1] = (Integer) counter_fseq;
		counter_fseq++;

		//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
		oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));

		/*
		 * Sending bundle
		 */
		oscInterface.sendOSCBundle(oscBundle);
	}

	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
					
		DisplayMetrics dm = getResources().getDisplayMetrics();
		this.scale = dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
		this.width = dm.widthPixels;
		this.height = dm.heightPixels;
		
		System.out.println(width+"x"+height+" "+scale);
		
		textPaint.setTextSize(12 * scale);
	    textPaint.setAntiAlias(true);
		idPaint.setTextSize(12 * scale);
	    idPaint.setAntiAlias(true);
		Canvas c = getHolder().lockCanvas();
		
		if (c != null) {
			bx = width/2 - backgroundImage.getWidth()/2;
			by = height/2 - backgroundImage.getHeight()/2;
			// clear screen
			c.drawColor(Color.WHITE);
			c.drawBitmap(backgroundImage,bx,by,null);
			
			if ((!oscInterface.isReachable()) || (drawAdditionalInfo)) drawInfo(c);
			getHolder().unlockCanvasAndPost(c);
		}
	}

	
	public void surfaceCreated(SurfaceHolder holder) {
		
		running = true;

		new Thread(new Runnable() {
		    public void run() {
		      boolean network = oscInterface.isReachable();
		      while (running) {
		    	  
    			  oscInterface.checkStatus();
    			  boolean status = oscInterface.isReachable();
		    	  if (network!=status) {
		    		  network = status;
		    		  sourceName = "TUIOdroid@"+getLocalIpAddress();
		    		  System.out.println("Source Name: " +sourceName);
		    		  Canvas c = getHolder().lockCanvas();
		    		  if (c != null) {
		    				bx = width/2 - backgroundImage.getWidth()/2;
		    				by = height/2 - backgroundImage.getHeight()/2;
		    				c.drawColor(Color.WHITE);
		    				c.drawBitmap(backgroundImage,bx,by,null);
		    				if (!network || drawAdditionalInfo) drawInfo(c);
		    				getHolder().unlockCanvasAndPost(c);
		    		  }		    		  
		    	  }
		    	 
		    	  if (sendPeriodicUpdates) {
		    		  try { sendTUIOdata(); }
		    	  	  catch (Exception e) {}
		    	  }
		    	  try { Thread.sleep(1000/FRAME_RATE); }
		    	  catch (Exception e) {}
		      }
		    }
		}).start();
	}
	

	public void surfaceDestroyed(SurfaceHolder holder) {
		running = false;
		tuioPoints.clear();
		counter_fseq = 0;
		sessionId = 0;
		startTime = System.currentTimeMillis();
		lastTime = 0;
	}
		
	/**
	 * Sets up a new OSC connection
	 * @param ip
	 * @param port
	 */
	public void setNewOSCConnection (String oscIP, int oscPort){	
		oscInterface.closeInteface();
		oscInterface = new OSCInterface(oscIP,oscPort);
		sourceName = "TUIOdroid@"+getLocalIpAddress();
	}

    public String getLocalIpAddress() {
    
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if ((!inetAddress.isLoopbackAddress()) && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {}
        return "127.0.0.1";
    }

    
    public void printAllNetworkInterfaces (){
    	
    	   try {
               for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                   NetworkInterface intf = en.nextElement();
                   for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                       InetAddress inetAddress = enumIpAddr.nextElement();
                       System.out.println("Network Interfaces:  " + inetAddress.getHostAddress());
                       if (inetAddress instanceof Inet4Address) {
                    	System.out.println("Die letzte war IPV4");   
                       }
                  
                   }
               }
           } catch (SocketException ex) {}
    	
    }
	
	
	
	
	
	
	

}