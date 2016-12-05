package com.nanovgj;

public class NVGPaint {
	
	/* Member Variables. */
	private final float[]  mTransform;
	private final float[]  mExtent;
	private       float[]  mInnerColor;
	private       float[]  mOuterColor;
	private       float    mRadius;
	private       float    mFeather;
	private final NVGImage mImage;
	
	protected NVGPaint(final float[] pTransform, final float[] pExtent, final float[] pInnerColor, final float[] pOuterColor, final float pRadius, final float pFeather, final NVGImage pNVGImage) {
		/* Initialize Member Variables. */
		this.mTransform  = pTransform;
		this.mExtent     = pExtent;
		this.mInnerColor = pInnerColor;
		this.mOuterColor = pOuterColor;
		this.mRadius     = pRadius;
		this.mFeather    = pFeather;
		this.mImage      = pNVGImage;
	}
	
	public NVGPaint(final float[] pInnerRGBA, final float[] pOuterRGBA) {
		/* Initialize Member Variables. */
		this.mTransform     = new float[]{ 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f};
		this.mExtent        = new float[2];
		this.mInnerColor    = pInnerRGBA;
		this.mOuterColor    = pOuterRGBA;
		this.mRadius        = 0.0f;
		this.mFeather       = 1.0f;
		this.mImage         = null;
	}
	
	public NVGPaint() {
		/* Initialize Member Variables. */
		this.mTransform     = new float[]{ 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f};
		this.mExtent        = new float[2];
		this.mInnerColor    = new float[]{ 1.0f, 1.0f, 1.0f, 1.0f };
		this.mOuterColor    = new float[]{ 0.0f, 0.0f, 0.0f, 1.0f };
		this.mRadius        = 0.0f;
		this.mFeather       = 1.0f;
		this.mImage         = null;
	}
	
	public final float[] getTransform() {
		return this.mTransform;
	}
	
	public final float[] getExtent() {
		return this.mExtent;
	}
	
	public final float[] getInnerColor() {
		return this.mInnerColor;
	}
	
	public final float[] getOuterColor() {
		return this.mOuterColor;
	}
	
	public final void setRadius(final float pRadius) {
		this.mRadius = pRadius;
	}
	
	public final float getRadius() {
		return this.mRadius;
	}
	
	public final void setFeather(final float pFeather) {
		this.mFeather = pFeather;
	}
	
	public final float getFeather() {
		return this.mFeather;
	}
	
	public final NVGImage getImage() {
		return this.mImage;
	}

}
