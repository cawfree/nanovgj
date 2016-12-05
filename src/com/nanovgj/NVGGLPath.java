package com.nanovgj;

public class NVGGLPath {
	
	/* Member Variables. */
	private final int mFillOffset;
	private final int mFillCount;
	private final int mStrokeOffset;
	private final int mStrokeCount;
	
	protected NVGGLPath(final int pFillOffset, final int pFillCount, final int pStrokeOffset, final int pStrokeCount) {
		/* Initialize Member Variables. */
		this.mFillOffset   = pFillOffset;
		this.mFillCount    = pFillCount;
		this.mStrokeOffset = pStrokeOffset;
		this.mStrokeCount  = pStrokeCount;
	}
	
	public final int getFillOffset() {
		return this.mFillOffset;
	}
	
	public final int getFillCount() {
		return this.mFillCount;
	}
	
	public final int getStrokeOffset() {
		return this.mStrokeOffset;
	}
	
	public final int getStrokeCount() {
		return this.mStrokeCount;
	}

}
