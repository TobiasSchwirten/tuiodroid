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

import java.util.ArrayList;


import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;

import tuioDroid.impl.TuioPoint;
import tuioDroid.osc.OSCInterface;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Main View
 * @author Tobias Schwirten
 * @author Martin Kaltenbrunner
 */
public class TouchView extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final int MAX_TOUCHPOINTS = 10;
	private Paint textPaint = new Paint();
	private Paint touchPaint = new Paint();

	private ArrayList<TuioPoint> tuioPoints;
	
	private int width, height;
	private float scale = 1.0f;
	
	private OSCInterface oscInterface ;
	private int counter_fseq = 0;
	private float cw, ch = 0;
	
	private boolean drawAdditionalInfo;
	
	private String sourceName;
	private int sessionId = 0;
	
	private long startTime;

	/**
	 * Constructor
	 * @param context
	 * @param devIP
	 * @param oscIP
	 * @param oscPort
	 */
	public TouchView(Context context, String devIP, String oscIP, int oscPort, boolean drawAdditionalInfo) {
		super(context, null);
		oscInterface  = new OSCInterface(oscIP , oscPort);
		this.drawAdditionalInfo = drawAdditionalInfo;
		startTime = System.currentTimeMillis();
		
		tuioPoints = new ArrayList<TuioPoint>();
		sourceName = "TUIOdroid@"+devIP;
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		setFocusable(true); // make sure we get key events
		setFocusableInTouchMode(true); // make sure we get touch events
		
		textPaint.setColor(Color.LTGRAY);
		touchPaint.setColor(Color.BLUE);
		touchPaint.setStrokeWidth(2);
		touchPaint.setStyle(Style.FILL);
		touchPaint.setAntiAlias(true);
		touchPaint.setAlpha(150);
	}
	
	/**
	 * Is called if a touch events occurs
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		long timeStamp = System.currentTimeMillis() - startTime;
		
		//printPointerInfo(event);
		cw = getWidth();
		ch = getHeight();
		Canvas c = getHolder().lockCanvas();
		
		int pointerCount = event.getPointerCount();
		
		if (pointerCount > MAX_TOUCHPOINTS) {
			pointerCount = MAX_TOUCHPOINTS;
		}
			
		if (c != null) {
			c.drawColor(Color.BLACK);
			
			if (event.getAction() == MotionEvent.ACTION_UP) {				
	
				if(event.getPointerCount() == 1) {
					// PointerCount is always >= 1, 
					// so if ACTION_UP and PointerCount==1, 
					// all Pointers are gone
					tuioPoints.clear(); 
				} 
				
			} else {
			
				// clear removed Points				
				for (int i = 0; i < tuioPoints.size(); i++) {
					
					boolean pointStillAlive = false;		
					for(int j=0; j<event.getPointerCount(); j++){
						
						//android.util.Log.v("compare",event.getPointerId(j)+" "+tuioPoints.get(i).getTouchId());
						
						if(event.getPointerId(j) == tuioPoints.get(i).getTouchId()){
							pointStillAlive = true;
							break;
						}	
					}
					
					if (pointStillAlive == false){
						tuioPoints.remove(i);
						i=0;
					}
				}
				
				// update existing Points
				for (int i = 0; i < pointerCount; i++) {
					
					int id = event.getPointerId(i);
					float x = event.getX(i);
					float y = event.getY(i);
					
					c.drawLine(0, y, width, y, touchPaint);
					c.drawLine(x, 0, x, height, touchPaint);
					c.drawCircle(x, y, 40 * scale, touchPaint);
					c.drawText("" +id, x, y, textPaint);
										
					/* Check if this touch ID already exists */
					boolean pointExists = false;
					for(int j=0; j<tuioPoints.size(); j++){
						
						if(tuioPoints.get(j).getTouchId() == id){
							 tuioPoints.get(j).update(x/cw,y/ch,timeStamp);
							 pointExists = true;
							 break;	
						}
					}
					
					// add new Point
					if(pointExists == false){
						tuioPoints.add(new TuioPoint(sessionId,id,x/cw,y/ch,timeStamp));
						sessionId++;
					} 
					
					if(this.drawAdditionalInfo){
						int textY = (int) ((15 + 20 * i) * scale);
						c.drawText("x" + i + "=" + x, 10 * scale, textY, textPaint);
						c.drawText("y" + i + "=" + y, 115 * scale, textY, textPaint);
					}

				}
				
				if(this.drawAdditionalInfo) {
					c.drawText("Sending TUIO data to: ", 0, height-textPaint.getTextSize(),textPaint );
					c.drawText(this.oscInterface.getInetAdress() + " / " + this.oscInterface.getPort(), 0, height,textPaint );
				}
			}
			getHolder().unlockCanvasAndPost(c);

		}
		
		sendTUIOdata();
		return true;
	}

	
	public void printPointerInfo(MotionEvent event){
		
		System.out.println("Pointer infos (counter: " + event.getPointerCount() + " )");
		
		for(int i=0; i<event.getPointerCount(); i++){
			
			System.out.println(event.getPointerId(i) + " / " +event.getX(i)+ " / " + event.getY(i));
			
		}
		System.out.println();
		
	}
	
	/**
	 * Sends the TUIO Data
	 * @param blobList
	 */
	public void sendTUIOdata (){
		
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

			outputData[4] = (Float) tuioPoints.get(i).getXVel(); // Movementvector X
			outputData[5] = (Float) tuioPoints.get(i).getYVel(); // Movementvector Y

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

	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
		this.width = width;
		this.height = height;
		if (width > height) {
			this.scale = width / 480f;
		} else {
			this.scale = height / 480f;
		}
		textPaint.setTextSize(14 * scale);
		Canvas c = getHolder().lockCanvas();
		
		if (c != null) {
			// clear screen
			c.drawColor(Color.BLACK);
			getHolder().unlockCanvasAndPost(c);
		}
	}

	
	public void surfaceCreated(SurfaceHolder holder) {
	}
	

	public void surfaceDestroyed(SurfaceHolder holder) {
	}
		
	/**
	 * Sets up a new OSC connection
	 * @param ip
	 * @param port
	 */
	public void setNewOSCConnection (String ip, int port){	
		this.oscInterface.closeInteface();
		this.oscInterface = new OSCInterface(ip,port);
	}


	public void setDrawAdditionalInfo(boolean drawAdditionalInfo) {
		this.drawAdditionalInfo = drawAdditionalInfo;
	}





	
	
	
	
	
	
	

}