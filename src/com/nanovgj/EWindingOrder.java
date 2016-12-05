package com.nanovgj;

public enum EWindingOrder {
	
	CCW { @Override public final float toFloat() { return 0.0f; } }, /* Solid Fill. TODO: (Expects 1?) */
	CW  { @Override public final float toFloat() { return 1.0f; } }; /* Holes.      TODO: (Expects 2?) */
	
	public abstract float toFloat();
	
}
