package com.nanovgj;

import com.github.cawfree.boilerplate.IGLES20;

public abstract class NVGCall {
	
	protected static final class RenderFill       extends NVGCall {
		
		/* Member Variables. */
		private final float[] mUniformArray1;
		private final float[] mUniformArray2;

		protected RenderFill(final NVGGLPath[] pNVGPaths, final int pTriangleOffset, final int pTriangleCount, final NVGImage pNVGImage, final float[] pUniformArray1, final float[] pUniformArray2) {
			super(pNVGPaths, pTriangleOffset, pTriangleCount, pNVGImage);
			/* Initialize Member Variables. */
			this.mUniformArray1 = pUniformArray1;
			this.mUniformArray2 = pUniformArray2;
		}

		@Override
		public final void onRenderGraphics(final INanoVG pNanoVG, final IGLES20 pGLES20) {
			pGLES20.glEnable(IGLES20.GL_STENCIL_TEST);
			pGLES20.glStencilMask(0xFF);
			pGLES20.glStencilFunc(IGLES20.GL_ALWAYS, 0, 0xFF);
			pGLES20.glColorMask(false, false, false, false);
			pNanoVG.onSetUniforms(pGLES20, this.getUniformArray1(), null);
			
			pGLES20.glStencilOpSeparate(IGLES20.GL_FRONT, IGLES20.GL_KEEP, IGLES20.GL_KEEP, IGLES20.GL_INCR_WRAP);
			pGLES20.glStencilOpSeparate(IGLES20.GL_BACK,  IGLES20.GL_KEEP, IGLES20.GL_KEEP, IGLES20.GL_DECR_WRAP);
			pGLES20.glDisable(IGLES20.GL_CULL_FACE);
			
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGLES20.glDrawArrays(IGLES20.GL_TRIANGLE_FAN, (lNVGPath.getFillOffset() / 4), lNVGPath.getFillCount()); }
			
			pGLES20.glEnable(IGLES20.GL_CULL_FACE);
			/* Draw anti-aliased pixels. */
			pGLES20.glColorMask(true, true, true, true);
			
			pNanoVG.onSetUniforms(pGLES20, this.getUniformArray2(), null);
			pGLES20.glStencilFunc(IGLES20.GL_EQUAL, 0, 0xFF);
			pGLES20.glStencilOp(IGLES20.GL_KEEP, IGLES20.GL_KEEP, IGLES20.GL_KEEP);
			
			/* Draw fringes. */
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGLES20.glDrawArrays(IGLES20.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
			
			pGLES20.glStencilFunc(IGLES20.GL_NOTEQUAL, 0, 0xFF);
			pGLES20.glStencilOp(IGLES20.GL_ZERO, IGLES20.GL_ZERO, IGLES20.GL_ZERO);
			
			pGLES20.glDrawArrays(IGLES20.GL_TRIANGLES, (this.getTriangleOffset() / 4), this.getTriangleCount());
			
			pGLES20.glDisable(IGLES20.GL_STENCIL_TEST);
		}
		
		private final float[] getUniformArray1() {
			return this.mUniformArray1;
		}
		
		private final float[] getUniformArray2() {
			return this.mUniformArray2;
		}
		
	}
	
	protected static final class RenderConvexFill extends NVGCall {
		
		protected RenderConvexFill(final NVGGLPath[] pNVGPaths, final int pTriangleOffset, final int pTriangleCount, final NVGImage pNVGImage) {
			super(pNVGPaths, pTriangleOffset, pTriangleCount, pNVGImage);
		}

		@Override
		public final void onRenderGraphics(final INanoVG pNanoVG, final IGLES20 pGLES20) {
			//glnvg__setUniforms(gl, call->uniformOffset, call->image);
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGLES20.glDrawArrays(IGLES20.GL_TRIANGLE_FAN,   (lNVGPath.getFillOffset()   / 4), lNVGPath.getFillCount());   }
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGLES20.glDrawArrays(IGLES20.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
		}
		
	}
	
	protected static final class RenderStroke     extends NVGCall {
		
		/* Member Variables. */
		private final float[] mUniform1;
		private final float[] mUniform2;
		
		protected RenderStroke(final NVGGLPath[] pNVGPaths, final int pTriangleOffset, final int pTriangleCount, final NVGImage pNVGImage, final float[] pUniform1, final float[] pUniform2) {
			super(pNVGPaths, pTriangleOffset, pTriangleCount, pNVGImage);
			/* Initialize Member Variables. */
			this.mUniform1 = pUniform1;
			this.mUniform2 = pUniform2;
		}

		@Override
		public final void onRenderGraphics(final INanoVG pNanoVG, final IGLES20 pGLES20) {
			pGLES20.glEnable(IGLES20.GL_STENCIL_TEST);
			pGLES20.glStencilMask(0xFF);
			/* Fill stroke base without overlap. */
			pGLES20.glStencilFunc(IGLES20.GL_EQUAL, 0x0, 0xff);
			pGLES20.glStencilOp(IGLES20.GL_KEEP, IGLES20.GL_KEEP, IGLES20.GL_INCR);
			pNanoVG.onSetUniforms(pGLES20, this.getUniform2(), this.getNVGImage());
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGLES20.glDrawArrays(IGLES20.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
			/* Draw antialiased pixels. */
			pNanoVG.onSetUniforms(pGLES20, this.getUniform1(), this.getNVGImage());
			pGLES20.glStencilOp(IGLES20.GL_KEEP, IGLES20.GL_KEEP, IGLES20.GL_KEEP);
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGLES20.glDrawArrays(IGLES20.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
			//* Clear stencil buffer. */
			pGLES20.glColorMask(false, false, false, false);
			pGLES20.glStencilFunc(IGLES20.GL_ALWAYS, 0, 0xFF);
			pGLES20.glStencilOp(IGLES20.GL_ZERO, IGLES20.GL_ZERO, IGLES20.GL_ZERO);

			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGLES20.glDrawArrays(IGLES20.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
			
			pGLES20.glColorMask(true, true, true, true);
			pGLES20.glDisable(IGLES20.GL_STENCIL_TEST);
		}
		
		private final float[] getUniform1() {
			return this.mUniform1;
		}
		
		private final float[] getUniform2() {
			return this.mUniform2;
		}
		
	}
	
	protected static final class RenderTriangles  extends NVGCall {
		
		protected RenderTriangles(final NVGGLPath[] pNVGPaths, final int pTriangleOffset, final int pTriangleCount, final NVGImage pNVGImage) {
			super(pNVGPaths, pTriangleOffset, pTriangleCount, pNVGImage);
		}

		@Override
		public final void onRenderGraphics(final INanoVG pNanoVG, final IGLES20 pGLES20) {
			//glnvg__setUniforms(gl, call->uniformOffset, call->image);
			pGLES20.glDrawArrays(IGLES20.GL_TRIANGLES, (this.getTriangleOffset() / 4), this.getTriangleCount());
		}
		
	}
	
	/* Member Variables. */
	private final NVGGLPath[] mNVGPaths;
	private final int         mTriangleOffset; /** TODO: Abstract this call. **/
	private final int         mTriangleCount;
	private final NVGImage    mNVGImage;
	
	protected NVGCall(final NVGGLPath[] pNVGPaths, final int pTriangleOffset, final int pTriangleCount, final NVGImage pNVGImage) {
		/* Initialize Member Variables. */
		this.mNVGPaths       = pNVGPaths;
		this.mTriangleOffset = pTriangleOffset;
		this.mTriangleCount  = pTriangleCount;
		this.mNVGImage       = pNVGImage;
	}
	
	public abstract void onRenderGraphics(final INanoVG pNanoVG, final IGLES20 pGLES20);
	
	public final NVGGLPath[] getNVGPaths() {
		return this.mNVGPaths;
	}
	
	public final int getTriangleOffset() {
		return this.mTriangleOffset;
	}
	
	public final int getTriangleCount() {
		return this.mTriangleCount;
	}
	
	public final NVGImage getNVGImage() {
		return this.mNVGImage;
	}

}
