package com.nanovgj;

import java.util.ArrayList;
import java.util.List;

public final class NVGPath {
	
	/* Member Variables. */
	private       EWindingOrder  mWindingOrder;
	private final List<NVGPoint> mNVGPoints;
	private       boolean        mClosed;
	private       int            mBevelCount;
	private       boolean        mConvex;
	
	protected NVGPath(final EWindingOrder pWindingOrder) {
		/* Initialize Member Variables. */
		this.mWindingOrder = pWindingOrder;
		this.mNVGPoints    = new ArrayList<NVGPoint>();
		this.mClosed       = false;
		this.mBevelCount   = 0;
		this.mConvex       = false;
	}
	
	protected final void setWindingOrder(final EWindingOrder pWindingOrder) {
		this.mWindingOrder = pWindingOrder;
	}
	
	public final EWindingOrder getWindingOrder() {
		return this.mWindingOrder;
	}
	
	protected final List<NVGPoint> getNVGPoints() {
		return this.mNVGPoints;
	}
	
	protected final void setClosed(final boolean pIsClosed) {
		this.mClosed = pIsClosed;
	}
	
	public final boolean isClosed() {
		return this.mClosed;
	}
	
	protected final void setBevelCount(final int pBevelCount) {
		this.mBevelCount = pBevelCount;
	}
	
	public final int getBevelCount() {
		return this.mBevelCount;
	}
	
	protected final void setConvex(final boolean pIsConvex) {
		this.mConvex = pIsConvex;
	}
	
	public final boolean isConvex() {
		return this.mConvex;
	}

}
