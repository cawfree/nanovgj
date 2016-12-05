package com.nanovgj;

import javax.media.opengl.GL2ES2;

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
		public final void onRenderGraphics(final INanoVG pNanoVG, final GL2ES2 pGL2ES2) {
			pGL2ES2.glEnable(GL2ES2.GL_STENCIL_TEST);
			pGL2ES2.glStencilMask(0xFF);
			pGL2ES2.glStencilFunc(GL2ES2.GL_ALWAYS, 0, 0xFF);
			pGL2ES2.glColorMask(false, false, false, false);
			pNanoVG.onSetUniforms(pGL2ES2, this.getUniformArray1(), null);
			
			pGL2ES2.glStencilOpSeparate(GL2ES2.GL_FRONT, GL2ES2.GL_KEEP, GL2ES2.GL_KEEP, GL2ES2.GL_INCR_WRAP);
			pGL2ES2.glStencilOpSeparate(GL2ES2.GL_BACK,  GL2ES2.GL_KEEP, GL2ES2.GL_KEEP, GL2ES2.GL_DECR_WRAP);
			pGL2ES2.glDisable(GL2ES2.GL_CULL_FACE);
			
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGL2ES2.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN, (lNVGPath.getFillOffset() / 4), lNVGPath.getFillCount()); }
			
			pGL2ES2.glEnable(GL2ES2.GL_CULL_FACE);
			/* Draw anti-aliased pixels. */
			pGL2ES2.glColorMask(true, true, true, true);
			
			pNanoVG.onSetUniforms(pGL2ES2, this.getUniformArray2(), null);
			pGL2ES2.glStencilFunc(GL2ES2.GL_EQUAL, 0, 0xFF);
			pGL2ES2.glStencilOp(GL2ES2.GL_KEEP, GL2ES2.GL_KEEP, GL2ES2.GL_KEEP);
			
			/* Draw fringes. */
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGL2ES2.glDrawArrays(GL2ES2.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
			
			pGL2ES2.glStencilFunc(GL2ES2.GL_NOTEQUAL, 0, 0xFF);
			pGL2ES2.glStencilOp(GL2ES2.GL_ZERO, GL2ES2.GL_ZERO, GL2ES2.GL_ZERO);
			
			pGL2ES2.glDrawArrays(GL2ES2.GL_TRIANGLES, (this.getTriangleOffset() / 4), this.getTriangleCount());
			
			pGL2ES2.glDisable(GL2ES2.GL_STENCIL_TEST);
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
		public final void onRenderGraphics(final INanoVG pNanoVG, final GL2ES2 pGL2ES2) {
			//glnvg__setUniforms(gl, call->uniformOffset, call->image);
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGL2ES2.glDrawArrays(GL2ES2.GL_TRIANGLE_FAN,   (lNVGPath.getFillOffset()   / 4), lNVGPath.getFillCount());   }
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGL2ES2.glDrawArrays(GL2ES2.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
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
		public final void onRenderGraphics(final INanoVG pNanoVG, final GL2ES2 pGL2ES2) {
			pGL2ES2.glEnable(GL2ES2.GL_STENCIL_TEST);
			pGL2ES2.glStencilMask(0xFF);
			/* Fill stroke base without overlap. */
			pGL2ES2.glStencilFunc(GL2ES2.GL_EQUAL, 0x0, 0xff);
			pGL2ES2.glStencilOp(GL2ES2.GL_KEEP, GL2ES2.GL_KEEP, GL2ES2.GL_INCR);
			pNanoVG.onSetUniforms(pGL2ES2, this.getUniform2(), this.getNVGImage());
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGL2ES2.glDrawArrays(GL2ES2.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
			/* Draw antialiased pixels. */
			pNanoVG.onSetUniforms(pGL2ES2, this.getUniform1(), this.getNVGImage());
			pGL2ES2.glStencilOp(GL2ES2.GL_KEEP, GL2ES2.GL_KEEP, GL2ES2.GL_KEEP);
			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGL2ES2.glDrawArrays(GL2ES2.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
			//* Clear stencil buffer. */
			pGL2ES2.glColorMask(false, false, false, false);
			pGL2ES2.glStencilFunc(GL2ES2.GL_ALWAYS, 0, 0xFF);
			pGL2ES2.glStencilOp(GL2ES2.GL_ZERO, GL2ES2.GL_ZERO, GL2ES2.GL_ZERO);

			for(final NVGGLPath lNVGPath: this.getNVGPaths()) { pGL2ES2.glDrawArrays(GL2ES2.GL_TRIANGLE_STRIP, (lNVGPath.getStrokeOffset() / 4), lNVGPath.getStrokeCount()); }
			
			pGL2ES2.glColorMask(true, true, true, true);
			pGL2ES2.glDisable(GL2ES2.GL_STENCIL_TEST);
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
		public final void onRenderGraphics(final INanoVG pNanoVG, final GL2ES2 pGL2ES2) {
			//glnvg__setUniforms(gl, call->uniformOffset, call->image);
			pGL2ES2.glDrawArrays(GL2ES2.GL_TRIANGLES, (this.getTriangleOffset() / 4), this.getTriangleCount());
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
	
	public abstract void onRenderGraphics(final INanoVG pNanoVG, final GL2ES2 pGL2ES2);
	
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
