package com.nanovgj;

import com.github.cawfree.boilerplate.IGLES20;

interface INanoVG {
	
	public abstract void     onNVGCall(final NVGCall pNVGCall);
	public abstract void     onSetUniforms(final IGLES20 pGLES20, final float[] pPaintArray, final NVGImage pNVGImage);
	public abstract float[]  onConvertPaint(final NVGPaint pNVGPaint, final NVGScissor pNVGScissor, final float pWidth, final float pFringe, final float pStrokeThreshold);
	public abstract void     onBufferVertices(final float[] pVertices);
	public abstract NVGState getCurrentState();
	public abstract float    getTesselationTolerance();
	public abstract float    getDistanceTolerance();
	public abstract float    getFringeWidth();
	public abstract int      getVerticesBufferOffset();
	public abstract float[]  getVertices();

}
