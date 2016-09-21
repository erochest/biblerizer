package com.bibler.biblerizer;

public class Vector2D {
	protected float x;
	protected float y;
	
	public Vector2D() {
		
	}
	
	public Vector2D(float x1, float y1) {
		x = x1;
		y = y1;
	}
	
	public static float dot(Vector2D a, Vector2D b) {
		return ((a.x * b.x) + (a.y * b.y));
	}
	
	public double dot(Vector2D b) {
		if(b != null)
		return (x * b.x) + (y * b.y);
		else 
			return 0;
	}
	
	public static Vector2D createP(Vector2D t1, Vector2D t2, Vector2D p) {
		Vector2D e = new Vector2D(0,0);
		e = t2.subtract(t1);
		float len = dot(e, e);
		float result = dot(e, p.subtract(t1))/len;
		e.x *= result;
		e.y *= result;
		return t1.add(e);
	}
	
	public Vector2D add(Vector2D other) {
		return new Vector2D(x + other.x, y + other.y);
	}

	public void addSave(Vector2D other) {
		x += other.x;
		y += other.y;
	}
	
	public Vector2D subtract(Vector2D other) {
		return new Vector2D(x - other.x, y - other.y);
	}
	
	public Vector2D perp() {
		return new Vector2D(y, -x);
	}
	
	public boolean overlap(Vector2D p) {
		boolean absFlag = false;
			if((x >= p.x && x <= p.y) || (y >= p.x && y <= p.y) || (x <= p.x && y >= p.y))
				return true;
		
			
		return false;
	}
	
	public float getOverlap(Vector2D p) {
		float max = Math.min(y, p.y);
		float min = Math.max(x, p.x);
		return max - min;
	}
	
	public Vector2D normalize() {
        final float magnitude = length();
        float newx = 0;
        float newy = 0;
        // TODO: I'm choosing safety over speed here.
        if (magnitude != 0.0f) {
            newx = x / magnitude;
            newy = y / magnitude;
        }

        return new Vector2D(newx, newy);
    }
	
	 public final float length() {
	        return (float) Math.sqrt(length2());
	    }

	    public final float length2() {
	        return (x * x) + (y * y);
	    }
	    
	    


}
