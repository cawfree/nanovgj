package com.nanovgj;

public class NVGScissor {
	
	/* Member Variables. */
	private final float[] mTransform;
	private final float[] mExtent;
	
	public NVGScissor() {
		/* Initialize Member Variables. */
		this.mTransform = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}; /** TODO: Appropriate? **/
		this.mExtent    = new float[]{ -1.0f, -1.0f };
	}
	
	public final float[] getTransform() {
		return this.mTransform;
	}
	
	public final float[] getExtent() {
		return this.mExtent;
	}

}
