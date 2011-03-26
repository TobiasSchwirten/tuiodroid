package tuioDroid.impl;

import java.util.ArrayList;
import java.util.List;

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
 * @author T.Schwirten
 */
public class TouchView extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final int MAX_TOUCHPOINTS = 10;
	private Paint textPaint = new Paint();
	private Paint touchPaints[] = new Paint[MAX_TOUCHPOINTS];
	private int colors[] = new int[MAX_TOUCHPOINTS];

	private ArrayList<TuioPoint> tuioPoints;
	
	
	private int width, height;
	private float scale = 1.0f;
	
	private OSCInterface oscInterface ;
	private int counter_fseq = 0;
	private float cw, ch = 0;
	
	private boolean drawAdditionalInfo;
	
	


	/**
	 * Constructor
	 * @param context
	 * @param oscIP
	 * @param oscPort
	 */
	public TouchView(Context context, String oscIP, int oscPort, boolean drawAdditionalInfo) {
		super(context, null);
		oscInterface  = new OSCInterface(oscIP , oscPort);
		this.drawAdditionalInfo = drawAdditionalInfo;
	
		tuioPoints = new ArrayList<TuioPoint>();
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		setFocusable(true); // make sure we get key events
		setFocusableInTouchMode(true); // make sure we get touch events
		initPaints();
	}
	
	



	
	/**
	 * Is called if a touch events occurs
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		printPointerInfo(event);
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
				System.out.println("REMOVED " + event.getPointerCount());
				sendTUIOdata(new ArrayList<TuioPoint>() );
				
				
				
				if(event.getPointerCount() == 1){ //Pointer Count is always >= 1, so if Action up event and pointer count ==1, all pointer gone
					tuioPoints.clear();
				}
				else{
					// clear removed Points				
					for (int i = 0; i < tuioPoints.size(); i++) {
						
						boolean pointStillAlive = false;
						
						for(int j=0; j<event.getPointerCount(); j++){
							
							if(event.getPointerId(j) == tuioPoints.get(i).getId()){
								pointStillAlive = true;
								j = event.getPointerCount();
							}	
						}
						
						
						if(pointStillAlive == false){
							tuioPoints.remove(i);
						}
					}	
				}
				
				
				
			}
			
			
			else {
			
				for (int i = 0; i < pointerCount; i++) {
					
					int id = event.getPointerId(i);
					int x = (int) event.getX(i);
					int y = (int) event.getY(i);
					drawPointer(x, y, touchPaints[id], i, id, c, event);
					
					if(this.drawAdditionalInfo){
						drawInformation(x,y,i,c);
					}
					
					/* Check if this id already exists */
					boolean pointExists = false;
					for(int j=0; j<tuioPoints.size(); j++){
						
						if(tuioPoints.get(j).getId() == id){
							 pointExists = true;
							 j = tuioPoints.size();	
						}
					}
					
					if(pointExists == false){
						tuioPoints.add(new TuioPoint(id, x, y, 0, 0, 0));
					}
					
				}
				generateTUIOdata(event);
			}
			getHolder().unlockCanvasAndPost(c);

		}
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
	 * Draws the pointer
	 * @param x
	 * @param y
	 * @param paint
	 * @param index
	 * @param id
	 * @param c
	 */
	private void drawPointer(int x, int y, Paint paint, int index, int id, Canvas c, MotionEvent event) {
		
		c.drawLine(0, y, width, y, paint);
		c.drawLine(x, 0, x, height, paint);
		c.drawCircle(x, y, 40 * scale, paint);

		
		c.drawText("" +id, x, y, textPaint);
//		c.drawText("" +id, x-20, y-20, textPaint);
		
		for(int i=0; i<tuioPoints.size(); i++){
			c.drawText( ""+tuioPoints.get(i).getId(), 0, height/2 + (20*i), textPaint); 
		}
		
		c.drawText("Counter " + event.getPointerCount(), 50, (height/2) -20, textPaint);
		for(int i=0; i<event.getPointerCount(); i++){
		
			c.drawText( ""+event.getPointerId(i), 50, height/2 + (20*i), textPaint); 
		}
	}


	/**
	 * Draws additional infos
	 * @param c
	 */
	private void drawInformation (int x, int y, int index, Canvas c){
	
		c.drawText("Sending TUIO data to: ", 0, height-textPaint.getTextSize(),textPaint );
		c.drawText(this.oscInterface.getInetAdress() + " / " + this.oscInterface.getPort(), 0, height,textPaint );
		
		int textY = (int) ((15 + 20 * index) * scale);
		c.drawText("x" + index + "=" + x, 10 * scale, textY, textPaint);
		c.drawText("y" + index + "=" + y, 70 * scale, textY, textPaint);
	}
	
	

	/**
	 * Generates data for TUIO
	 * @param event
	 */
	private void generateTUIOdata(MotionEvent event){
		
		// Generating TUIO data
		ArrayList<TuioPoint> list = new ArrayList<TuioPoint>();
		int pointerCount = event.getPointerCount();
		
		float xKoord1=0;
		float yKoord1=0;
		float x1=0;
		float y1=0;
		TuioPoint bufferPoint1=null;
		
		for(int i=0; i<pointerCount; i++){
			
			x1 = event.getX(i);
			y1 = event.getY(i);
			
			xKoord1 = x1  / cw;
			yKoord1= y1  / ch;
			
			bufferPoint1 = new TuioPoint(i, xKoord1, yKoord1, 0.0f, 0.0f, 0.0f);
			list.add(bufferPoint1);
		}
		
		sendTUIOdata(list);
		
	}
	
	
	/**
	 * Sends the TUIO Data
	 * @param blobList
	 */
	public void sendTUIOdata (ArrayList<TuioPoint> blobList){
		
		OSCBundle oscBundle = new OSCBundle();
		
		
		counter_fseq++;
		
		Object outputData[];
		
		/*
		 * Alive data
		 */
		ArrayList<Integer> allIDs = new ArrayList<Integer>();

		for (int i = 0; i < blobList.size(); i++) {
			allIDs.add(blobList.get(i).getId());
		}

		outputData = new Object[allIDs.size() + 1];
		outputData[0] = (String) "alive";

		for (int i = 0; i < allIDs.size(); i++) {
			outputData[1 + i] = (int) allIDs.get(i); // ID
		}

		//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
		oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));

		
		/*
		 * Set data
		 */
		for (int i = 0; i < blobList.size(); i++) {

			outputData = new Object[7];

			outputData[0] = (String) "set";
			outputData[1] = (int) blobList.get(i).getId(); // ID

			outputData[2] = (float) blobList.get(i).getX(); // x KOORD
			outputData[3] = (float) blobList.get(i).getY(); // y KOORD

			outputData[4] = (float) blobList.get(i).getxMovVec(); // Movementvector X
			outputData[5] = (float) blobList.get(i).getyMovVec(); // Movementvector Y

			outputData[6] = (float) blobList.get(i).getmAccel(); // Acceleration

			//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
			oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));
		}

		
		/*
		 * fseq
		 */
		outputData = new Object[2];
		outputData[0] = (String) "fseq";
		outputData[1] = (Integer) counter_fseq;

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
	 * Inits the different Paints
	 */
	private void initPaints() {
		textPaint.setColor(Color.LTGRAY);
		colors[0] = Color.BLUE;
		colors[1] = Color.RED;
		colors[2] = Color.GREEN;
		colors[3] = Color.YELLOW;
		colors[4] = Color.CYAN;
		colors[5] = Color.MAGENTA;
		colors[6] = Color.DKGRAY;
		colors[7] = Color.WHITE;
		colors[8] = Color.LTGRAY;
		colors[9] = Color.GRAY;
		
		for (int i = 0; i < MAX_TOUCHPOINTS; i++) {
			touchPaints[i] = new Paint();
			touchPaints[i].setColor(colors[i]);
			touchPaints[i].setStrokeWidth(2);
//			touchPaints[i].setStyle(Style.STROKE);
			touchPaints[i].setStyle(Style.FILL);
			touchPaints[i].setAntiAlias(true);
			touchPaints[i].setAlpha(150);
		}
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