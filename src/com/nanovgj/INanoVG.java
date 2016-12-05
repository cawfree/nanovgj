package com.nanovgj;

import javax.media.opengl.GL2ES2;

interface INanoVG {
	
	public abstract void     onNVGCall(final NVGCall pNVGCall);
	public abstract void     onSetUniforms(final GL2ES2 pGL2ES2, final float[] pPaintArray, final NVGImage pNVGImage);
	public abstract float[]  onConvertPaint(final NVGPaint pNVGPaint, final NVGScissor pNVGScissor, final float pWidth, final float pFringe, final float pStrokeThreshold);
	public abstract void     onBufferVertices(final float[] pVertices);
	public abstract NVGState getCurrentState();
	public abstract float    getTesselationTolerance();
	public abstract float    getDistanceTolerance();
	public abstract float    getFringeWidth();
	public abstract int      getVerticesBufferOffset();
	public abstract float[]  getVertices();

}
