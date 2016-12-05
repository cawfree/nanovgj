package com.nanovgj;

public final class NVGPoint {

	public static final int FLAG_POINT_CORNER     = 1 << 0;
	public static final int FLAG_POINT_LEFT       = 1 << 1;
	public static final int FLAG_POINT_BEVEL      = 1 << 2;
	public static final int FLAG_POINT_INNERBEVEL = 1 << 3;
	
	/* Member Variables. */
	private final float[] mPosition;
	private       int     mFlags;
	private final float[] mDelta;
	private       float   mLength;
	private final float[] mDeltaM;
	
	protected NVGPoint(final float pX, final float pY, final int pFlags) {
		/* Initialize Member Variables. */
		this.mPosition = new float[]{ pX, pY };
		this.mFlags    = pFlags;
		this.mDelta    = new float[2];
		this.mLength   = 0.0f;
		this.mDeltaM   = new float[2];
	}
	
	public final float[] getPosition() {
		return this.mPosition;
	}
	
	public final float getX() {
		return this.getPosition()[0];
	}
	
	public final float getY() {
		return this.getPosition()[1];
	}
	
	protected final void setFlags(final int pFlags) {
		this.mFlags = pFlags;
	}
	
	public final int getFlags() {
		return this.mFlags;
	}
	
	public final float[] getDelta() {// dx, dy
		return this.mDelta;
	}
	
	protected final void setLength(final float pLength) {
		this.mLength = pLength;
	}
	
	public final float getLength() {
		return this.mLength;
	}
	
	public final float[] getDeltaM() {// dmx, dmy
		return this.mDeltaM;
	}

}
