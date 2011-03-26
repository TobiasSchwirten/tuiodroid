package tuioDroid.impl;

/**
 * Class representing a TUIO Point
 * @author T. Schwirten
 */
public class TuioPoint {
	
	
	private int id;
	private float x;
	private float y;
	private float xMovVec;
	private float yMovVec;
	private float mAccel;
	
	


	public TuioPoint(int id, float x, float y, float xMovVec, float yMovVec,float mAccel) {
		
		this.id = id;
		this.x = x;
		this.y = y;
		this.xMovVec = xMovVec;
		this.yMovVec = yMovVec;
		this.mAccel = mAccel;
	}

	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getxMovVec() {
		return xMovVec;
	}

	public void setxMovVec(float xMovVec) {
		this.xMovVec = xMovVec;
	}

	public float getyMovVec() {
		return yMovVec;
	}

	public void setyMovVec(float yMovVec) {
		this.yMovVec = yMovVec;
	}

	public float getmAccel() {
		return mAccel;
	}

	public void setmAccel(float mAccel) {
		this.mAccel = mAccel;
	}
	
	
	
	
	
	
	
	

}
