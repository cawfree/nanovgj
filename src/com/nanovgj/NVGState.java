package com.nanovgj;


class NVGState {
	
	/* Member Variables. */
	private final float[]    mTransform;
	private       NVGPaint   mFillPaint;
	private       NVGPaint   mStrokePaint;
	private       float      mStrokeWidth;
	private       float      mMiterLimit;
	private       ELineCap   mLineCap;
	private       ELineJoin  mLineJoin;
	private       float      mAlpha;
	private final NVGScissor mScissor;
	
	/** TODO: Provide an enhanced constructor. **/
	protected NVGState() {
		/* Initialize Member Variables. */
		this.mTransform     = new float[]{ 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f};
		this.mFillPaint     = new NVGPaint();
		this.mStrokePaint   = new NVGPaint();
		this.mStrokeWidth = 1.0f;
		this.mMiterLimit  = 10.0f;
		this.mLineCap     = ELineCap.BUTT;
		this.mLineJoin    = ELineJoin.MITER;
		this.mScissor     = new NVGScissor();
		this.mAlpha       = 1.0f;
	}
	
	public final float[] getTransform() {
		return this.mTransform;
	}
	
	protected final void setFillPaint(final NVGPaint pNVGPaint) {
		this.mFillPaint = pNVGPaint;
	}
	
	public final NVGPaint getFillPaint() {
		return this.mFillPaint;
	}
	
	protected final void setStrokePaint(final NVGPaint pNVGPaint) {
		this.mStrokePaint = pNVGPaint;
	}
	
	public final NVGPaint getStrokePaint() {
		return this.mStrokePaint;
	}
	
	protected final void setStrokeWidth(final float pStrokeWidth) {
		this.mStrokeWidth = pStrokeWidth;
	}
	
	public final float getStrokeWidth() {
		return this.mStrokeWidth;
	}
	
	protected final void setMiterLimit(final float pMiterLimit) {
		this.mMiterLimit = pMiterLimit;
	}
	
	public final float getMiterLimit() {
		return this.mMiterLimit;
	}
	
	protected final void setLineCap(final ELineCap pLineCap) {
		this.mLineCap = pLineCap;
	}
	
	public final ELineCap getLineCap() {
		return this.mLineCap;
	}
	
	protected final void setLineJoin(final ELineJoin pLineJoin) {
		this.mLineJoin = pLineJoin;
	}
	
	public final ELineJoin getLineJoin() {
		return this.mLineJoin;
	}
	
	protected final void setAlpha(final float pAlpha) {
		this.mAlpha = pAlpha;
	}
	
	public final float getAlpha() {
		return this.mAlpha;
	}

	public final NVGScissor getScissor() {
		return this.mScissor;
	}

}
