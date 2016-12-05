package com.nanovgj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.nanovgj.exception.NVGException;
import com.nanovgj.global.NVGGlobal;

public final class NVGPathContext {
	
	private static final float KAPPA_90				  = 0.5522847493f;
	private static final float PAINT_GRADIENT_LARGE   = 1.0E5f;
	private static final int   TESSELATION_MAX_LEVEL  = 10;
	private static final float FILL_SOLID_MITER_LIMIT = 2.4f;
	
	private static final void onAddPoint(final NVGPath pCurrentPath, final float pX, final float pY, final int pFlags, final float pTolerance) {
		if(!pCurrentPath.getNVGPoints().isEmpty()) {
			/* Attain a reference to the last point. */
			final NVGPoint lNVGPoint = pCurrentPath.getNVGPoints().get(pCurrentPath.getNVGPoints().size() - 1);
			/* Determine if the points are equal. */
			if(NVGGlobal.isPointEquals(lNVGPoint.getX(), lNVGPoint.getY(), pX, pY, pTolerance)) {
				/* Append additional point flags. */
				lNVGPoint.setFlags(lNVGPoint.getFlags() | pFlags);
				/* Do not append this current vertex. */
				return;
			}
		}
		/* Append the new vertex to the path. */
		pCurrentPath.getNVGPoints().add(new NVGPoint(pX, pY, pFlags));
	}
	
	private static final void onTesselateBezier(final NVGPath pCurrentPath, final float pX1, final float pY1, final float pX2, final float pY2, final float pX3, final float pY3, final float pX4, final float pY4, final int pLevel, final int pType, final float pDistanceTolerance, final float pTesselationTolerance) {
		float x12,y12,x23,y23,x34,y34,x123,y123,x234,y234,x1234,y1234;
		float dx,dy,d2,d3;
		if (pLevel > NVGPathContext.TESSELATION_MAX_LEVEL) { return; }
		x12  = (pX1 + pX2) * 0.5f;
		y12  = (pY1 + pY2) * 0.5f;
		x23  = (pX2 + pX3) * 0.5f;
		y23  = (pY2 + pY3) * 0.5f;
		x34  = (pX3 + pX4) * 0.5f;
		y34  = (pY3 + pY4) * 0.5f;
		x123 = (x12 + x23) * 0.5f;
		y123 = (y12 + y23) * 0.5f;
		dx = pX4 - pX1;
		dy = pY4 - pY1;
		d2 = Math.abs(((pX2 - pX4) * dy - (pY2 - pY4) * dx));
		d3 = Math.abs(((pX3 - pX4) * dy - (pY3 - pY4) * dx));
		if(((d2 + d3) * (d2 + d3)) < (pTesselationTolerance * (dx * dx + dy * dy))) {
			NVGPathContext.onAddPoint(pCurrentPath, pX4, pY4, pType, pDistanceTolerance);
			return;
		}
		x234  = (x23+x34)   * 0.5f;
		y234  = (y23+y34)   * 0.5f;
		x1234 = (x123+x234) * 0.5f;
		y1234 = (y123+y234) * 0.5f;
		NVGPathContext.onTesselateBezier(pCurrentPath, pX1,   pY1,   x12,  y12,  x123, y123, x1234, y1234, pLevel + 1,     0, pDistanceTolerance, pTesselationTolerance);
		NVGPathContext.onTesselateBezier(pCurrentPath, x1234, y1234, x234, y234, x34,  y34,  pX4,   pY4,   pLevel + 1, pType, pDistanceTolerance, pTesselationTolerance);
	}
	
	private static final float onCalculateTriangleArea(final float pAX, final float pAY, final float pBX, final float pBY, final float pCX, final float pCY) {
		final float lABX = pBX - pAX;
		final float lABY = pBY - pAY;
		final float lACX = pCX - pAX;
		final float lACY = pCY - pAY;
		return lACX * lABY - lABX * lACY;
	}
	
	private static final float onCalculatePolygonArea(final List<NVGPoint> pPolygon) {
		float lArea = 0.0f;
		/* Iterate about the Polygon. */
		for(int i = 2; i < pPolygon.size(); i++) {
			/* Build a sub-triangle out of the Polygon's vertices. */
			final NVGPoint lA = pPolygon.get(0);
			final NVGPoint lB = pPolygon.get(i - 1);
			final NVGPoint lC = pPolygon.get(i + 0);
			/* Increase the area count by the sub-triangle's area. */
			lArea += NVGPathContext.onCalculateTriangleArea(lA.getX(), lA.getY(), lB.getX(), lB.getY(), lC.getX(), lC.getY());
		}
		return lArea * 0.5f;
	}
	
	/* Member Variables. */
	private final INanoVG       mNanoVG;
	private final List<NVGPath> mNVGPaths;
	private       float[]       mCommandsBuffer;
	private final float[]       mBounds;
	private       int           mCommandsBufferOffset;
	private       float         mCommandX;
	private       float         mCommandY;
	
	protected NVGPathContext(final INanoVG pNanoVG) {
		/* Initialize Member Variables. */
		this.mNanoVG               = pNanoVG;
		this.mNVGPaths             = new ArrayList<NVGPath>();
		this.mCommandsBuffer       = new float[]{};
		this.mBounds               = new float[4];
		this.mCommandsBufferOffset = 0;
		this.mCommandX             = 0.0f;
		this.mCommandY             = 0.0f;
	}
	
	public final NVGPaint onCreateLinearGradient(final float pSX, final float pSY, final float pEX, final float pEY, final float[] pInnerRGBA, final float[] pOuterRGBA) {
		/* Calculate aligned transform along a line. */
		float lDX      = (pEX - pSX);
		float lDY      = (pEY - pSY);
		final float lD = ((float) Math.sqrt(lDX*lDX + lDY*lDY));
		/* Modulate gradient transform. */
		if(lD > 0.0001f) { lDX /= lD; lDY /= lD; } else { lDX = 0; lDY = 1; }
		return new NVGPaint(new float[]{lDY, -lDX, lDX, lDY, (pSX - lDX * NVGPathContext.PAINT_GRADIENT_LARGE), (pSY - lDY * NVGPathContext.PAINT_GRADIENT_LARGE) }, new float[]{ NVGPathContext.PAINT_GRADIENT_LARGE, (NVGPathContext.PAINT_GRADIENT_LARGE + (lD * 0.5f)) }, pInnerRGBA, pOuterRGBA, 0.0f, Math.max(1.0f, lD), null );
	}
	
	public final NVGPaint onCreateRadialGradient(final float pCentreX, final float pCentreY, final float pInnerRadius, final float pOuterRadius, final float[] pInnerRGBA, final float[] pOuterRGBA) {
		final NVGPaint lNVGPaint = new NVGPaint(pInnerRGBA, pOuterRGBA); /** TODO: Abstract to constructor. **/
		float lRadius = (pInnerRadius + pOuterRadius) * 0.5f;
		float lDelta  = (pOuterRadius - pInnerRadius);
		lNVGPaint.getTransform()[4] = pCentreX;
		lNVGPaint.getTransform()[5] = pCentreY;
		lNVGPaint.getExtent()[0] = lRadius;
		lNVGPaint.getExtent()[1] = lRadius;
		lNVGPaint.setRadius(lRadius);
		lNVGPaint.setFeather(Math.max(1.0f, lDelta));
		return lNVGPaint;
	}
	
	public final void onBeginPath() {
		/* Re-initialize the CommandsBufferOffset. */
		this.mCommandsBufferOffset = 0;
		/* Clear the currently cached paths. */
		this.getNVGPaths().clear();
	}
	
	public final void onMoveTo(final float pX, final float pY) {
		this.onAppendCommands(new float[]{ EPathCommand.MOVETO.toFloat(), pX, pY });
	}
	
	public final void onLineTo(final float pX, final float pY) {
		this.onAppendCommands(new float[]{ EPathCommand.LINETO.toFloat(), pX, pY });
	}
	
	public final void onBezierTo(final float pC1X, final float pC1Y, final float pC2X, final float pC2Y, final float pX, final float pY) {
		this.onAppendCommands(new float[]{ EPathCommand.BEZIERTO.toFloat(), pC1X, pC1Y, pC2X, pC2Y, pX, pY });
	}
	
	public final void onQuadTo(final float pCX, final float pCY, final float pX, final float pY) {
		/* Bezier from the current position. */
		final float lX0 = this.getCommandX();
		final float lY0 = this.getCommandY();
		/* Append the commands. */
		this.onBezierTo(lX0 + 2.0f / 3.0f * (pCX - lX0), lY0 + 2.0f / 3.0f * (pCY - lY0), pX + 2.0f / 3.0f * (pCX - pX), pY + 2.0f / 3.0f * (pCY - pY), pX, pY);
	}
	
	public final void onClosePath() {
		this.onAppendCommands(new float[]{ EPathCommand.CLOSE.toFloat() });
	}
	
	public final void onPathWinding(final EWindingOrder pWindingOrder) {
		this.onAppendCommands(new float[]{ EPathCommand.WINDING.toFloat(), pWindingOrder.toFloat() });
	}
	
	public final void onEllipse(final float pCX, final float pCY, final float pRX, final float pRY) {
		this.onAppendCommands(new float[]{
				EPathCommand.MOVETO.toFloat(),   pCX - pRX, pCY,
				EPathCommand.BEZIERTO.toFloat(), pCX - pRX,            pCY + pRY * KAPPA_90, pCX - pRX * KAPPA_90, pCY + pRY, pCX,       pCY + pRY,
				EPathCommand.BEZIERTO.toFloat(), pCX + pRX * KAPPA_90, pCY + pRY,            pCX + pRX,            pCY + pRY * KAPPA_90, pCX + pRX, pCY,
				EPathCommand.BEZIERTO.toFloat(), pCX + pRX,            pCY - pRY * KAPPA_90, pCX + pRX * KAPPA_90, pCY-pRY, pCX,         pCY - pRY,
				EPathCommand.BEZIERTO.toFloat(), pCX - pRX * KAPPA_90, pCY - pRY,            pCX - pRX,            pCY-pRY*KAPPA_90,     pCX-pRX, pCY,
				EPathCommand.CLOSE.toFloat()
		});
	}
	
	public final void onRect(final float pX, final float pY, final float pW, final float pH) {
		this.onAppendCommands(new float[]{
				EPathCommand.MOVETO.toFloat(), pX,pY,
				EPathCommand.LINETO.toFloat(), pX,pY+pH,
				EPathCommand.LINETO.toFloat(), pX+pW,pY+pH,
				EPathCommand.LINETO.toFloat(), pX+pW,pY,
				EPathCommand.CLOSE.toFloat()
		});
	}
	
	public final void onCircle(final float pCX, final float pCY, final float pRadius) {
		this.onEllipse(pCX, pCY, pRadius, pRadius);
	}
	
	private final void onAppendCommands(final float[] pCommands) {
		/* Determine whether to re-allocate the CommandsBuffer.. */
		if((this.getCommandsBufferOffset() + pCommands.length) > this.getCommandsBuffer().length) {
			/* Extend the CommandsBuffer. */
			this.mCommandsBuffer = Arrays.copyOf(this.getCommandsBuffer(), NVGGlobal.toNearestPowerOfTwo(this.getCommandsBufferOffset() + pCommands.length));
			System.out.println("Reallocated Commands. Size: " + this.getCommandsBuffer().length);
		}
		/* Determine whether to the update the command location. */
		if(pCommands[0] != EPathCommand.CLOSE.toFloat() && pCommands[0] != EPathCommand.WINDING.toFloat()) {
			/* Update the location. */
			this.mCommandX = pCommands[pCommands.length - 2];
			this.mCommandX = pCommands[pCommands.length - 1];
		}
		/* Transform commands. */
		for(int i = 0; i < pCommands.length; i++) {
			/* Acquire the PathCommand. */
			final EPathCommand lPathCommand = EPathCommand.values()[(int)pCommands[i]];
			/* Process the PathCommand. */
			switch(lPathCommand) {
				case MOVETO   : 
					NVGGlobal.onTransformPoint(pCommands, (i + 1), pCommands, (i + 2), this.getNanoVG().getCurrentState().getTransform(), 0, pCommands[i + 1], pCommands[i + 2]);
				break;
				case LINETO   : 
					NVGGlobal.onTransformPoint(pCommands, (i + 1), pCommands, (i + 2), this.getNanoVG().getCurrentState().getTransform(), 0, pCommands[i + 1], pCommands[i + 2]);
				break;
				case BEZIERTO : 
					NVGGlobal.onTransformPoint(pCommands, (i + 1), pCommands, (i + 2), this.getNanoVG().getCurrentState().getTransform(), 0, pCommands[i + 1], pCommands[i + 2]);
					NVGGlobal.onTransformPoint(pCommands, (i + 3), pCommands, (i + 4), this.getNanoVG().getCurrentState().getTransform(), 0, pCommands[i + 3], pCommands[i + 4]);
					NVGGlobal.onTransformPoint(pCommands, (i + 5), pCommands, (i + 6), this.getNanoVG().getCurrentState().getTransform(), 0, pCommands[i + 5], pCommands[i + 6]);
				break;
				case CLOSE    : 
				case WINDING  : 
					/* Nothing to transform. */
				break;
				default :
					throw new NVGException("Malformed path!");
			}
			/* Offset the search to the next PathCommand in the set. */
			i += lPathCommand.getNumberOfComponents() - 1;
		}
		/* Append the Commands. */
		System.arraycopy(pCommands, 0, this.getCommandsBuffer(), this.getCommandsBufferOffset(), pCommands.length);
		/* Increase the CommandsBufferOffset. */
		this.mCommandsBufferOffset += pCommands.length;
	}
	
	public final void onFillPaint(final NVGPaint pNVGPaint) {
		this.getNanoVG().getCurrentState().setFillPaint(pNVGPaint);
		NVGGlobal.onTransformMultiply(
				this.getNanoVG().getCurrentState().getFillPaint().getTransform(), 0, 
				this.getNanoVG().getCurrentState().getTransform(),                0
		);
	}
	
	public final void onStrokePaint(final NVGPaint pNVGPaint) {
		this.getNanoVG().getCurrentState().setStrokePaint(pNVGPaint);
		NVGGlobal.onTransformMultiply(
				this.getNanoVG().getCurrentState().getStrokePaint().getTransform(), 0, 
				this.getNanoVG().getCurrentState().getTransform(),                0
		);
	}
	
	private static final void onSetPaintColor(final NVGPaint pNVGPaint, final float[] pRGBA) {
		NVGGlobal.setMatrixIdentity(pNVGPaint.getTransform(), 0);
		Arrays.fill(pNVGPaint.getExtent(), -1.0f);
		pNVGPaint.setRadius(0.0f);
		pNVGPaint.setFeather(1.0f);
		System.arraycopy(pRGBA, 0, pNVGPaint.getInnerColor(), 0, NVGGlobal.RGBA_LENGTH);
		System.arraycopy(pRGBA, 0, pNVGPaint.getOuterColor(), 0, NVGGlobal.RGBA_LENGTH);
	}

	public final void onFillColor(final float[] pRGBA) {
		NVGPathContext.onSetPaintColor(this.getNanoVG().getCurrentState().getFillPaint(), pRGBA);
	}

	public final void onStrokeColor(final float[] pRGBA) {
		NVGPathContext.onSetPaintColor(this.getNanoVG().getCurrentState().getStrokePaint(), pRGBA);
	}
	
	public final void onStrokeWidth(final float pStrokeWidth) {
		this.getNanoVG().getCurrentState().setStrokeWidth(pStrokeWidth);
	}
	
	private static final void onButtCapStart(final INanoVG pNanoVG, final NVGPoint pNVGPoint, final float pDX, final float pDY, final float pW, final float pD, final float pAA) {
		final float px  = pNVGPoint.getX() - (pDX * pD);
		final float py  = pNVGPoint.getY() - (pDY * pD);
		final float dlx =  pDY;
		final float dly = -pDX;
		pNanoVG.onBufferVertices(new float[]{ px + dlx*pW - pDX*pAA, py + dly*pW - pDY * pAA, 0.0f, 0.0f });
		pNanoVG.onBufferVertices(new float[]{ px - dlx*pW - pDX*pAA, py - dly*pW - pDY * pAA, 1.0f, 0.0f });
		pNanoVG.onBufferVertices(new float[]{ px + dlx*pW, py + dly*pW, 0.0f, 1.0f });
		pNanoVG.onBufferVertices(new float[]{ px - dlx*pW, py - dly*pW, 1.0f, 1.0f });
	}
	
	private static final void onButtCapEnd(final INanoVG pNanoVG, final NVGPoint pNVGPoint, final float pDX, final float pDY, final float pW, final float pD, final float pAA) {
		float px  =  pNVGPoint.getX() + pDX * pD;
		float py  =  pNVGPoint.getY() + pDY * pD;
		float dlx =  pDY;
		float dly = -pDX;
		pNanoVG.onBufferVertices(new float[]{ px + dlx*pW, py + dly*pW, 0,1 });
		pNanoVG.onBufferVertices(new float[]{ px - dlx*pW, py - dly*pW, 1,1 });
		pNanoVG.onBufferVertices(new float[]{ px + dlx*pW + pDX*pAA, py + dly*pW + pDY*pAA, 0,0 });
		pNanoVG.onBufferVertices(new float[]{ px - dlx*pW + pDX*pAA, py - dly*pW + pDY*pAA, 1,0 });
	}
	
	private static final void onRoundCapEnd(final INanoVG pNanoVG, final NVGPoint pNVGPoint, final float pDX, final float pDY, final float pW, final int pNCap) {
		float px  =  pNVGPoint.getX();
		float py  =  pNVGPoint.getY();
		float dlx =  pDY;
		float dly = -pDX;
		pNanoVG.onBufferVertices(new float[]{ px + dlx*pW, py + dly*pW, 0,1 });
		pNanoVG.onBufferVertices(new float[]{ px - dlx*pW, py - dly*pW, 1,1 });
		for (int i = 0; i < pNCap; i++) {
			float a = i/(float)(pNCap-1)*(float)Math.PI;
			float ax = (float)Math.cos(a) * pW, ay = (float)Math.sin(a) * pW;
			pNanoVG.onBufferVertices(new float[]{ px, py, 0.5f,1 });
			pNanoVG.onBufferVertices(new float[]{ px - dlx*ax + pDX*ay, py - dly*ax + pDY*ay, 0,1 });
		}
	}
	
	private static final void onRoundCapStart(final INanoVG pNanoVG, final NVGPoint pNVGPoint, final float pDX, final float pDY, final float pW, final int pNCap) {
		final float px  =  pNVGPoint.getX();
		final float py  =  pNVGPoint.getY();
		final float dlx =  pDY;
		final float dly = -pDX;
		for (int i = 0; i < pNCap; i++) {
			float a = i / ((float)(pNCap-1) * (float)Math.PI);
			float ax = (float)Math.cos(a) * pW, ay = (float)Math.sin(a) * pW;
			pNanoVG.onBufferVertices(new float[]{ px - dlx*ax - pDX*ay, py - dly*ax - pDY*ay, 0,1 });
			pNanoVG.onBufferVertices(new float[]{ px, py, 0.5f,1 });
		}
		pNanoVG.onBufferVertices(new float[]{ px + dlx*pW, py + dly*pW, 0,1 });
		pNanoVG.onBufferVertices(new float[]{ px - dlx*pW, py - dly*pW, 1,1 });
	}
	
	public final void onFill() {
		final NVGState lState     = this.getNanoVG().getCurrentState();
		final NVGPaint lFillPaint = lState.getFillPaint();
		/* Apply global alpha. */
		lFillPaint.getInnerColor()[3] *= lState.getAlpha(); /** TODO: Is this why premultiplication is unnecessary? **/
		lFillPaint.getOuterColor()[3] *= lState.getAlpha();
		
		/* If the current path has already been flattened, do not re-attempt. */
		if(this.getNVGPaths().size() == 0) {
			/* Flatten paths. */
			this.onFlattenPaths();
		}
		/* Expand an anti-aliased fill. */
		final NVGGLPath[] lNVGGLPaths = this.onExpandFill(this.getNanoVG().getFringeWidth()*1.0f, ELineJoin.MITER, NVGPathContext.FILL_SOLID_MITER_LIMIT); /* Scaled, unlike NanoVG implementatoin. */
		
		final int lTriangleOffset = this.getNanoVG().getVerticesBufferOffset();
		/* Deploy fill triangles. */
		this.getNanoVG().onBufferVertices(new float[]{ this.getBounds()[0], this.getBounds()[3], 0.5f, 1.0f });
		this.getNanoVG().onBufferVertices(new float[]{ this.getBounds()[2], this.getBounds()[3], 0.5f, 1.0f });
		this.getNanoVG().onBufferVertices(new float[]{ this.getBounds()[2], this.getBounds()[1], 0.5f, 1.0f });
		this.getNanoVG().onBufferVertices(new float[]{ this.getBounds()[0], this.getBounds()[3], 0.5f, 1.0f });
		this.getNanoVG().onBufferVertices(new float[]{ this.getBounds()[2], this.getBounds()[1], 0.5f, 1.0f });
		this.getNanoVG().onBufferVertices(new float[]{ this.getBounds()[0], this.getBounds()[1], 0.5f, 1.0f });
		
		
		final int lTriangleCount  = (this.getNanoVG().getVerticesBufferOffset() - lTriangleOffset) / 4;

		
		final float[] lUniformArray2 = this.getNanoVG().onConvertPaint(lFillPaint, lState.getScissor(), this.getNanoVG().getFringeWidth(), this.getNanoVG().getFringeWidth(), -1.0f);
		final float[] lUniformArray1 = new float[NanoVG.UNIFORMARRAY_JAVA_LENGTH];//Arrays.copyOf(lUniformArray2, lUniformArray2.length);
		lUniformArray1[NanoVG.UNIFORMARRAY_JAVA_INDEX_STROKE_THRESHOLD] = -1.0f;
		lUniformArray1[NanoVG.UNIFORMARRAY_JAVA_INDEX_STROKE_RENDER_TYPE] = (float)EShadingMode.SIMPLE.ordinal();
		
		/* Create a new NVGCall. */
		final NVGCall lFillCall = (this.getNVGPaths().size() == 1 && this.getNVGPaths().get(0).isConvex() ? new NVGCall.RenderConvexFill(lNVGGLPaths, lTriangleOffset, lTriangleCount, lFillPaint.getImage()) : new NVGCall.RenderFill(lNVGGLPaths, lTriangleOffset, lTriangleCount, lFillPaint.getImage() , lUniformArray1, lUniformArray2));
		/* Delegate the fill to the rendering environment. */
		this.getNanoVG().onNVGCall(lFillCall);
	}
	
	public final void onStroke() {
		/* Fetch Stroke render variables. */
		final NVGState lState       = this.getNanoVG().getCurrentState();
		final float    lScale       = NVGGlobal.onCalculateAverageScale(lState.getTransform(), 0);
		final NVGPaint lStrokePaint = lState.getStrokePaint();
		      float    lStrokeWidth = NVGGlobal.clamp(lState.getStrokeWidth() * lScale, 0.0f, 200.0f);
		/* Determine whether to emulate coverage. */
		if(lStrokeWidth < this.getNanoVG().getFringeWidth()) {
		/* If the stroke width is less than pixel size, use alpha to emulate coverage. Since coverage is area, scale by alpha*alpha. */
			float lAlpha = NVGGlobal.clamp(lStrokeWidth / this.getNanoVG().getFringeWidth(), 0.0f, 1.0f);
			lStrokePaint.getInnerColor()[3] *= lAlpha * lAlpha; /** TODO: Is this why premultiplication is unnecessary? **/
			lStrokePaint.getOuterColor()[3] *= lAlpha * lAlpha;
			lStrokeWidth               = this.getNanoVG().getFringeWidth();
		}
		/* Apply global alpha. */
		lStrokePaint.getInnerColor()[3] *= lState.getAlpha(); /** TODO: Is this why premultiplication is unnecessary? **/
		lStrokePaint.getOuterColor()[3] *= lState.getAlpha();
		
		/* If the current path has already been flattened, do not re-attempt. */
		if(this.getNVGPaths().size() == 0) {
			/* Flatten paths. */
			this.onFlattenPaths();
		}
		
		/** TODO: StrokeWidth is usually scaled also. **///*0.5?
		final NVGGLPath[] lStrokePath = this.onExpandStroke(lStrokeWidth * 0.5f + this.getNanoVG().getFringeWidth(), lState.getLineCap(), lState.getLineJoin(), lState.getMiterLimit(), this.getNanoVG().getTesselationTolerance());
		
		/** TODO: RenderStroke. **/
		final float[] lUniformArray1 = this.getNanoVG().onConvertPaint(lStrokePaint, lState.getScissor(), lStrokeWidth, this.getNanoVG().getFringeWidth(), -1.0f);
		final float[] lUniformArray2 = this.getNanoVG().onConvertPaint(lStrokePaint, lState.getScissor(), lStrokeWidth, this.getNanoVG().getFringeWidth(),  1.0f - 0.5f/255.0f);
		
		/* Init type is stroke. */
		final NVGCall lStrokeCall = new NVGCall.RenderStroke(lStrokePath, 0, 0, lStrokePaint.getImage(), lUniformArray1, lUniformArray2);
		/* Delegate the fill to the rendering environment. */
		this.getNanoVG().onNVGCall(lStrokeCall);
	}
	
	private final NVGGLPath[] onExpandStroke(float w, final ELineCap pLineCap, final ELineJoin pLineJoin, float pMiterLimit, final float pTesselationTolerance) {
		
		final float lAA   = this.getNanoVG().getFringeWidth();
		final int   lNCap = NVGGlobal.onCalculateCurveDivs(w, (float)Math.PI, pTesselationTolerance);
		
		this.onCalculateJoins((w != 0), w, pLineJoin, pMiterLimit);
		/* Declare the return NVGGLPath List. */
		final List<NVGGLPath> lNVGGLPaths = new ArrayList<NVGGLPath>();
		/* Iterate the flattened paths. */
		for(int i = 0; i < this.getNVGPaths().size(); i++) {
			/* Fetch the CurrentPath. */
			final NVGPath lNVGPath = this.getNVGPaths().get(i);
			final boolean lIsLoop  = lNVGPath.isClosed();
			/* Define NVGPoint iteration placeholders. */
			NVGPoint lNVGPoint0;
			NVGPoint lNVGPoint1;
			int lStart;
			int lEnd;
			/* Define entry vertices. */
			if(lIsLoop) {
				/* Loop vertices. */
				lNVGPoint0 = lNVGPath.getNVGPoints().get(lNVGPath.getNVGPoints().size() - 1);
				lNVGPoint1 = lNVGPath.getNVGPoints().get(0);
				/* Configure iteration parameters. */
				lStart = 0;
				lEnd = lNVGPath.getNVGPoints().size();
			}
			else {
				/* Cap vertices. */
				lNVGPoint0 = lNVGPath.getNVGPoints().get(0);
				lNVGPoint1 = lNVGPath.getNVGPoints().get(1);
				/* Configure iteration parameters. */
				lStart = 1;
				lEnd = lNVGPath.getNVGPoints().size() - 1;
			}

			final int lStrokeOffset = this.getNanoVG().getVerticesBufferOffset();
			
			/* Generate the StartCap. */
			if(!lIsLoop) {
				/* Add cap. */
				final float[] lD = new float[]{(lNVGPoint1.getX() - lNVGPoint0.getX()), (lNVGPoint1.getY() - lNVGPoint0.getY())};// dx, dy
				/* Normalize the delta parameters. */
				NVGGlobal.normalize(lD, 0, lD, 1, 1E-6f); /** TODO: Abstract. **/
				switch(pLineCap) {
					case BUTT : 
						NVGPathContext.onButtCapStart(this.getNanoVG(), lNVGPoint0, lD[0], lD[1], w, -lAA * 0.5f, lAA);
					break;
					case SQUARE : 
						NVGPathContext.onButtCapStart(this.getNanoVG(), lNVGPoint0, lD[0], lD[1], w, w - lAA, lAA);
					break;
					case ROUND : 
						NVGPathContext.onRoundCapStart(this.getNanoVG(), lNVGPoint0, lD[0], lD[1], w, lNCap);
					break;
				}
			}
			
			/* Stroke the path. */
			for(int j = lStart; j < lEnd; ++j) {
				if((lNVGPoint1.getFlags() & (NVGPoint.FLAG_POINT_BEVEL | NVGPoint.FLAG_POINT_INNERBEVEL)) != 0) {
					if (pLineJoin == ELineJoin.ROUND) {
						this.onRoundJoin(lNVGPoint0, lNVGPoint1, w, w, 0, 1, lNCap);
					} 
					else {
						this.onBevelJoin(lNVGPoint0, lNVGPoint1, w, w, 0, 1, lAA);
					}
				} 
				else {
					this.getNanoVG().onBufferVertices(new float[]{ lNVGPoint1.getX() + (lNVGPoint1.getDeltaM()[0] * w), lNVGPoint1.getY() + (lNVGPoint1.getDeltaM()[1] * w), 0.0f, 1.0f });
					this.getNanoVG().onBufferVertices(new float[]{ lNVGPoint1.getX() - (lNVGPoint1.getDeltaM()[0] * w), lNVGPoint1.getY() - (lNVGPoint1.getDeltaM()[1] * w), 1.0f, 1.0f });
				}
				/* Advance the search. */
				lNVGPoint0 = lNVGPoint1;
				lNVGPoint1 = lNVGPath.getNVGPoints().get((j + 1) % lNVGPath.getNVGPoints().size());
			}
			
			if(lIsLoop) {
				/* Loop the path. */
				this.getNanoVG().onBufferVertices(new float[]{ lStrokeOffset + 0, lStrokeOffset + 1, 0,1 });
				this.getNanoVG().onBufferVertices(new float[]{ lStrokeOffset + 4, lStrokeOffset + 5, 1,1 });
			} 
			else {
				/* Apply the EndCap. */
				final float[] lD = new float[2];
				lD[0] = lNVGPoint1.getX() - lNVGPoint0.getX();
				lD[1] = lNVGPoint1.getY() - lNVGPoint0.getY();
				/* Normalize the vertices. */
				NVGGlobal.normalize(lD, 0, lD, 1, 1E-6f); /** TODO: Abstract. **/
				switch(pLineCap) {
					case BUTT   : 
						NVGPathContext.onButtCapEnd(this.getNanoVG(), lNVGPoint1, lD[0], lD[1], w, -lAA * 0.5f, lAA);
					break;
					case SQUARE : 
						NVGPathContext.onButtCapEnd(this.getNanoVG(), lNVGPoint1, lD[0], lD[1], w, w-lAA, lAA);
					break;
					case ROUND  : 
						NVGPathContext.onRoundCapEnd(this.getNanoVG(), lNVGPoint1, lD[0], lD[1], w, lNCap);
					break;
				}
			}
			final int lStrokeCount = (this.getNanoVG().getVerticesBufferOffset() - lStrokeOffset) / 4;
			lNVGGLPaths.add(new NVGGLPath(lStrokeOffset, 0, lStrokeOffset, lStrokeCount));
		}
		/* Return the array of paths. */
		final NVGGLPath[] lNVGGLPathArray = new NVGGLPath[lNVGGLPaths.size()];
		return lNVGGLPaths.toArray(lNVGGLPathArray);
	}
	
	private final NVGGLPath[] onExpandFill(final float pFringeWidth, final ELineJoin pLineJoin, final float pMiterLimit) {
		/* Declare expansion constants. */
		final float   lAA       = pFringeWidth;
		final boolean lIsFringe = pFringeWidth > 0.0f;
		final boolean lIsConvex = (this.getNVGPaths().size() == 1) && this.getNVGPaths().get(0).isConvex();
		/* Calculate the join vertices for the path. */
		this.onCalculateJoins(lIsFringe, pFringeWidth, pLineJoin, pMiterLimit);
		/* Declare the return NVGGLPath List. */
		final List<NVGGLPath> lNVGGLPaths = new ArrayList<NVGGLPath>();
		/* Iterate the flattened paths. */
		for(int i = 0; i < this.getNVGPaths().size(); i++) {
			/* Grab a reference to the CurrentPath. */
			final NVGPath lCurrentPath = this.getNVGPaths().get(i);
			/* Calculate shape vertices. */
			final float lWOFF = 0.5f * lAA;
			/* Initialize fill constants. */
			final int lFillOffset = this.getNanoVG().getVerticesBufferOffset();
			/* Calculate fringe vertices? */
			if(lIsFringe) {
				/* Define placeholders for iterating the CurrentPath's NVGPoints. */
				NVGPoint lNVGPoint0 = lCurrentPath.getNVGPoints().get(lCurrentPath.getNVGPoints().size() - 1);
				NVGPoint lNVGPoint1 = lCurrentPath.getNVGPoints().get(0);
				/* Iterate the CurrentPath's Points. */
				for(int j = 0; j < lCurrentPath.getNVGPoints().size(); j++) {
					if((lNVGPoint0.getFlags() & NVGPoint.FLAG_POINT_BEVEL) != 0) {
						/* Fetch delta regions. */
						final float dlx0 =  lNVGPoint0.getDelta()[1];
						final float dly0 = -lNVGPoint0.getDelta()[0];
						final float dlx1 =  lNVGPoint1.getDelta()[1];
						final float dly1 = -lNVGPoint1.getDelta()[0];
						if((lNVGPoint1.getFlags() & NVGPoint.FLAG_POINT_LEFT) != 0) {
							float lx = lNVGPoint1.getX() + lNVGPoint1.getDeltaM()[0] * lWOFF;
							float ly = lNVGPoint1.getY() + lNVGPoint1.getDeltaM()[1] * lWOFF;
							this.getNanoVG().onBufferVertices(new float[]{ lx, ly, 0.5f, 1.0f });
						} 
						else {
							float lx0 = lNVGPoint1.getX() + dlx0 * lWOFF;
							float ly0 = lNVGPoint1.getY() + dly0 * lWOFF;
							float lx1 = lNVGPoint1.getX() + dlx1 * lWOFF;
							float ly1 = lNVGPoint1.getY() + dly1 * lWOFF;
							this.getNanoVG().onBufferVertices(new float[]{ lx0, ly0, 0.5f, 1.0f });
							this.getNanoVG().onBufferVertices(new float[]{ lx1, ly1, 0.5f, 1.0f });
						}
					}
					else {
						this.getNanoVG().onBufferVertices(new float[]{ lNVGPoint1.getX() + (lNVGPoint1.getDeltaM()[0] * lWOFF), lNVGPoint1.getY() + (lNVGPoint1.getDeltaM()[1] * lWOFF), 0.5f, 1.0f });
					}
					/* Advance the search. */
					lNVGPoint0 = lNVGPoint1;
					lNVGPoint1 = lCurrentPath.getNVGPoints().get((j + 1) % lCurrentPath.getNVGPoints().size());
				}
			}
			else {
				for(int j = 0; j < lCurrentPath.getNVGPoints().size(); j++) {
					/* Grab the CurrentPoint. */
					final NVGPoint lNVGPoint = lCurrentPath.getNVGPoints().get(j);
					/* Buffer the CurrentPoint. */
					this.getNanoVG().onBufferVertices(new float[]{ lNVGPoint.getX(), lNVGPoint.getY(), 0.5f, 1.0f });
				}
			}
			final int lFillCount = (this.getNanoVG().getVerticesBufferOffset() - lFillOffset) / 4;
			/* Initialize stroke constants. */
			final int lStrokeOffset = this.getNanoVG().getVerticesBufferOffset();
			/* Calculate Fringe. */
			if(lIsFringe) {
				final float lw = lIsConvex? lWOFF : (pFringeWidth + lWOFF);
				final float rw = pFringeWidth - lWOFF; /* Fill inset generation. */
				final float lu = lIsConvex? 0.5f  : 0; /* Set outline fade. */
				final float ru = 1;
				/* Define placeholders for iterating the CurrentPath's NVGPoints. */
				NVGPoint lNVGPoint0 = lCurrentPath.getNVGPoints().get(lCurrentPath.getNVGPoints().size() - 1);
				NVGPoint lNVGPoint1 = lCurrentPath.getNVGPoints().get(0);
				/* Iterate the CurrentPath's Points. */
				for(int j = 0; j < lCurrentPath.getNVGPoints().size(); j++) {
					if((lNVGPoint1.getFlags() & (NVGPoint.FLAG_POINT_BEVEL | NVGPoint.FLAG_POINT_INNERBEVEL)) != 0) {
						this.onBevelJoin(lNVGPoint0, lNVGPoint1, lw, rw, lu, ru, pFringeWidth); /** INCREASE OFFSET! **/
					} 
					else {
						this.getNanoVG().onBufferVertices(new float[]{ lNVGPoint1.getX() + (lNVGPoint1.getDeltaM()[0] * lw), lNVGPoint1.getY() + (lNVGPoint1.getDeltaM()[1] * lw), lu, 1.0f });
						this.getNanoVG().onBufferVertices(new float[]{ lNVGPoint1.getX() - (lNVGPoint1.getDeltaM()[0] * rw), lNVGPoint1.getY() - (lNVGPoint1.getDeltaM()[1] * rw), ru, 1.0f });
					}
					/* Advance the search. */
					lNVGPoint0 = lNVGPoint1;
					lNVGPoint1 = lCurrentPath.getNVGPoints().get((j + 1) % lCurrentPath.getNVGPoints().size());
				}
				/* Loop it. */
				this.getNanoVG().onBufferVertices(new float[]{ this.getNanoVG().getVertices()[lStrokeOffset + 0], this.getNanoVG().getVertices()[lStrokeOffset + 1], lu, 1.0f });
				this.getNanoVG().onBufferVertices(new float[]{ this.getNanoVG().getVertices()[lStrokeOffset + 4], this.getNanoVG().getVertices()[lStrokeOffset + 5], ru, 1.0f });
			}
			final int lStrokeCount = (this.getNanoVG().getVerticesBufferOffset() - lStrokeOffset) / 4;
			/* Add the new path to the list. */
			lNVGGLPaths.add(new NVGGLPath(lFillOffset, lFillCount, lStrokeOffset, lStrokeCount));
		}

		/* Return the array of paths. */
		final NVGGLPath[] lNVGGLPathArray = new NVGGLPath[lNVGGLPaths.size()];
		return lNVGGLPaths.toArray(lNVGGLPathArray);
		
	}
	
	private final void onChooseBevel(final int pBevelFlag, final NVGPoint p0, final NVGPoint p1, final float pW, final float[] pX0, final int pX0Offset, final float[] pY0, final int pY0Offset, final float[] pX1, final int pX1Offset, final float[] pY1, final int pY1Offset) { /** TODO: Should just return the float[4], rather than apply a callback. **/
		if(pBevelFlag != 0) {
			pX0[pX0Offset] = p1.getX() + p0.getDelta()[1] * pW;
			pY0[pY0Offset] = p1.getY() - p0.getDelta()[0] * pW;
			pX1[pX1Offset] = p1.getX() + p1.getDelta()[1] * pW;
			pY1[pY1Offset] = p1.getY() - p1.getDelta()[0] * pW;
		} else {
			pX0[pX0Offset] = p1.getX() + p1.getDeltaM()[0] * pW;
			pY0[pY0Offset] = p1.getY() + p1.getDeltaM()[1] * pW;
			pX1[pX1Offset] = p1.getX() + p1.getDeltaM()[0] * pW;
			pY1[pY1Offset] = p1.getY() + p1.getDeltaM()[1] * pW;
		}
	}
	
	private final void onBevelJoin(final NVGPoint p0, final NVGPoint p1, float lw, float rw, float lu, float ru, float fringe) { /** TODO: Should be static. **/
		final float[] r  =  new float[4];// rx0,ry0,rx1,ry1; (0,1,2,3)
		final float[] l  =  new float[4];// lx0,ly0,lx1,ly1; (0,1,2,3)
		final float dlx0 =  p0.getDelta()[1];
		final float dly0 = -p0.getDelta()[0];
		final float dlx1 =  p1.getDelta()[1];
		final float dly1 = -p1.getDelta()[0];
		if((p1.getFlags() & NVGPoint.FLAG_POINT_LEFT) != 0) {
			this.onChooseBevel(p1.getFlags() & NVGPoint.FLAG_POINT_INNERBEVEL, p0, p1, lw, l, 0, l, 1, l, 2, l, 3);
			this.getNanoVG().onBufferVertices(new float[]{l[0], l[1], lu,1});
			this.getNanoVG().onBufferVertices(new float[]{p1.getX() - dlx0*rw, p1.getY() - dly0*rw, ru,1});
			if((p1.getFlags() & NVGPoint.FLAG_POINT_BEVEL) != 0) {
				this.getNanoVG().onBufferVertices(new float[]{l[0], l[1], lu,1});
				this.getNanoVG().onBufferVertices(new float[]{p1.getX() - dlx0*rw, p1.getY() - dly0*rw, ru,1});
				this.getNanoVG().onBufferVertices(new float[]{l[2], l[3], lu,1});
				this.getNanoVG().onBufferVertices(new float[]{p1.getX() - dlx1*rw, p1.getY() - dly1*rw, ru,1});
			} else {
				r[0] = p1.getX() - p1.getDeltaM()[0] * rw;
				r[1] = p1.getY() - p1.getDeltaM()[1] * rw;
				this.getNanoVG().onBufferVertices(new float[]{p1.getX(), p1.getY(), 0.5f,1});
				this.getNanoVG().onBufferVertices(new float[]{p1.getX() - dlx0*rw, p1.getY() - dly0*rw, ru,1});
				this.getNanoVG().onBufferVertices(new float[]{r[0], r[1], ru,1});
				this.getNanoVG().onBufferVertices(new float[]{r[0], r[1], ru,1});
				this.getNanoVG().onBufferVertices(new float[]{p1.getX(), p1.getY(), 0.5f,1});
				this.getNanoVG().onBufferVertices(new float[]{p1.getX() - dlx1*rw, p1.getY() - dly1*rw, ru,1});
			}
			this.getNanoVG().onBufferVertices(new float[]{l[2], l[3], lu,1});
			this.getNanoVG().onBufferVertices(new float[]{p1.getX() - dlx1*rw, p1.getY() - dly1*rw, ru,1});
		} 
		else {
			this.onChooseBevel(p1.getFlags() & NVGPoint.FLAG_POINT_INNERBEVEL, p0, p1, -rw, r, 0, r, 1, r, 2, r, 3);
			this.getNanoVG().onBufferVertices(new float[]{p1.getX() + dlx0*lw, p1.getY() + dly0*lw, lu,1});
			this.getNanoVG().onBufferVertices(new float[]{r[0], r[1], ru,1});
			if((p1.getFlags() & NVGPoint.FLAG_POINT_BEVEL) != 0) {
				this.getNanoVG().onBufferVertices(new float[]{p1.getX() + dlx0*lw, p1.getY() + dly0*lw, lu,1});
				this.getNanoVG().onBufferVertices(new float[]{r[0], r[1], ru,1});
				this.getNanoVG().onBufferVertices(new float[]{p1.getX() + dlx1*lw, p1.getY() + dly1*lw, lu,1});
				this.getNanoVG().onBufferVertices(new float[]{r[2], r[3], ru,1});
			}
			else {
				l[0] = p1.getX() + p1.getDeltaM()[0] * lw;
				l[1] = p1.getY() + p1.getDeltaM()[1] * lw;
				this.getNanoVG().onBufferVertices(new float[]{p1.getX() + dlx0*lw, p1.getY() + dly0*lw, lu,1});
				this.getNanoVG().onBufferVertices(new float[]{p1.getX(), p1.getY(), 0.5f,1});
				this.getNanoVG().onBufferVertices(new float[]{l[0], l[1], lu,1});
				this.getNanoVG().onBufferVertices(new float[]{l[0], l[1], lu,1});
				this.getNanoVG().onBufferVertices(new float[]{p1.getX() + dlx1*lw, p1.getY() + dly1*lw, lu,1});
				this.getNanoVG().onBufferVertices(new float[]{p1.getX(), p1.getY(), 0.5f,1});
			}
			this.getNanoVG().onBufferVertices(new float[]{p1.getX() + dlx1*lw, p1.getY() + dly1*lw, lu,1});
			this.getNanoVG().onBufferVertices(new float[]{r[2], r[3], ru,1});
		}
		
	}
	
	private final void onRoundJoin(final NVGPoint p0, final NVGPoint p1, final float lw, final float rw, final float lu, final float ru, final int ncap) {
		final float dlx0 =  p0.getDelta()[1];
		final float dly0 = -p0.getDelta()[0];
		final float dlx1 =  p1.getDelta()[1];
		final float dly1 = -p1.getDelta()[0];
		if((p1.getFlags() & NVGPoint.FLAG_POINT_LEFT) != 0) {
			final float[] l = new float[4];//float lx0,ly0,lx1,ly1
			float a0, a1;
			this.onChooseBevel(p1.getFlags() & NVGPoint.FLAG_POINT_INNERBEVEL, p0, p1, lw, l, 0, l, 1, l, 2, l, 3);
			a0 = (float)Math.atan2(-dly0, -dlx0);
			a1 = (float)Math.atan2(-dly1, -dlx1);
			if (a1 > a0) a1 -= ((float)Math.PI * 2);
			this.getNanoVG().onBufferVertices(new float[]{ l[0], l[1], lu,1 });
			this.getNanoVG().onBufferVertices(new float[]{ p1.getX() - dlx0*rw, p1.getY() - dly0*rw, ru,1 });
			int n = NVGGlobal.clamp((int)Math.ceil(((a0 - a1) / (float)Math.PI) * ncap), 2, ncap);
			for(int i = 0; i < n; i++) {
				float u = i/(float)(n-1);
				float a = a0 + u*(a1-a0);
				float rx = p1.getX() + (float)Math.cos(a) * rw;
				float ry = p1.getY() + (float)Math.sin(a) * rw;
				this.getNanoVG().onBufferVertices(new float[]{ p1.getX(), p1.getY(), 0.5f,1 });
				this.getNanoVG().onBufferVertices(new float[]{ rx, ry, ru,1 });
			}
			this.getNanoVG().onBufferVertices(new float[]{ l[2], l[3], lu,1 });
			this.getNanoVG().onBufferVertices(new float[]{ p1.getX() - dlx1*rw, p1.getY() - dly1*rw, ru,1 });
		} 
		else {
			float[] r = new float[4];// rx0,ry0,rx1,ry1,
			float a0,a1;
			this.onChooseBevel(p1.getFlags() & NVGPoint.FLAG_POINT_INNERBEVEL, p0, p1, -rw, r, 0, r, 1, r, 2, r, 3);
			a0 = (float)Math.atan2(dly0, dlx0);
			a1 = (float)Math.atan2(dly1, dlx1);
			if (a1 < a0) a1 += (float)Math.PI * 2;
			this.getNanoVG().onBufferVertices(new float[]{ p1.getX() + dlx0*rw, p1.getY() + dly0*rw, lu,1 });
			this.getNanoVG().onBufferVertices(new float[]{ r[0], r[1], ru,1 });
			int n = NVGGlobal.clamp((int)Math.ceil(((a1 - a0) / (float)Math.PI) * ncap), 2, ncap);
			for(int i = 0; i < n; i++) {
				float u = i/(float)(n-1);
				float a = a0 + u*(a1-a0);
				float lx = p1.getX() + (float)Math.cos(a) * lw;
				float ly = p1.getY() + (float)Math.sin(a) * lw;
				this.getNanoVG().onBufferVertices(new float[]{ lx, ly, lu,1 });
				this.getNanoVG().onBufferVertices(new float[]{ p1.getX(), p1.getY(), 0.5f,1 });
			}
			this.getNanoVG().onBufferVertices(new float[]{ p1.getX() + dlx1*rw, p1.getY() + dly1*rw, lu,1 });
			this.getNanoVG().onBufferVertices(new float[]{ r[2], r[3], ru,1 });
		}
	}
	
	private final void onCalculateJoins(final boolean pIsFringe, final float pFringeWidth, final ELineJoin pLineJoin, final float pMiterLimit) {
		/* Declare fringe join. */
		final float lIW = pIsFringe ? (1.0f / pFringeWidth) : 0.0f;
		/* Calculate which joins needs extra vertices to append, and gather vertex count. */
		for(int i = 0; i < this.getNVGPaths().size(); i++) {
			/* Fetch a reference to the CurrentPath. */
			final NVGPath lCurrentPath = this.getNVGPaths().get(i);
			/* Define placeholders for iterating the CurrentPath's NVGPoints. */
			NVGPoint lNVGPoint0 = lCurrentPath.getNVGPoints().get(lCurrentPath.getNVGPoints().size() - 1);
			NVGPoint lNVGPoint1 = lCurrentPath.getNVGPoints().get(0);
			/* Track the number of left turns taken. */
			int lNumLeftTurns = 0;
			/* Calculate segment directions and length. */
			for(int j = 0; j < lCurrentPath.getNVGPoints().size(); j++) {
				/* Attain reference to the point's delta values. */
				final float dlx0 =  lNVGPoint0.getDelta()[1];
				final float dly0 = -lNVGPoint0.getDelta()[0];
				final float dlx1 =  lNVGPoint1.getDelta()[1];
				final float dly1 = -lNVGPoint1.getDelta()[0];
				/* Calculate extrusions. */
				lNVGPoint1.getDeltaM()[0] = (dlx0 + dlx1) * 0.5f;
				lNVGPoint1.getDeltaM()[1] = (dly0 + dly1) * 0.5f;
				final float dmr2 = (lNVGPoint1.getDeltaM()[0] * lNVGPoint1.getDeltaM()[0]) + (lNVGPoint1.getDeltaM()[1] * lNVGPoint1.getDeltaM()[1]);
				/** TODO: Beautify. **/
				if(dmr2 > 0.000001f) {
					float scale = 1.0f / dmr2;
					if (scale > 600.0f) {
						scale = 600.0f;
					}
					lNVGPoint1.getDeltaM()[0] *= scale;
					lNVGPoint1.getDeltaM()[1] *= scale;
				}
				/* Clear flags, but keep the corner. */
				lNVGPoint1.setFlags(lNVGPoint1.getFlags() & NVGPoint.FLAG_POINT_CORNER);
				/* Keep track of left turns. */
				final float lCross = (lNVGPoint1.getDelta()[0] * lNVGPoint0.getDelta()[1]) - (lNVGPoint0.getDelta()[0] * lNVGPoint1.getDelta()[1]);
				if(lCross > 0.0f) {
					lNumLeftTurns++;
					lNVGPoint1.setFlags(lNVGPoint1.getFlags() | NVGPoint.FLAG_POINT_LEFT);
				}
				/* Calculate if we should use bevel or miter for inner join. */
				final float limit = Math.max(1.01f, Math.min(lNVGPoint0.getLength(), lNVGPoint1.getLength()) * lIW);
				if((dmr2 * limit * limit) < 1.0f) {
					lNVGPoint1.setFlags(lNVGPoint1.getFlags() | NVGPoint.FLAG_POINT_INNERBEVEL);
				}
				/* Check to see if the corner needs to be beveled. */
				if((lNVGPoint1.getFlags() & NVGPoint.FLAG_POINT_CORNER) != 0) {
					if ((dmr2 * pMiterLimit * pMiterLimit) < 1.0f || pLineJoin == ELineJoin.BEVEL || pLineJoin == ELineJoin.ROUND) {
						lNVGPoint1.setFlags(lNVGPoint1.getFlags() | NVGPoint.FLAG_POINT_BEVEL);
					}
				}
				/* Maintain the BevelCount. */
				if((lNVGPoint1.getFlags() & (NVGPoint.FLAG_POINT_BEVEL | NVGPoint.FLAG_POINT_INNERBEVEL)) != 0) {
					lCurrentPath.setBevelCount(lCurrentPath.getBevelCount() + 1);
				}
				/* Advance the search. */
				lNVGPoint0 = lNVGPoint1;
				lNVGPoint1 = lCurrentPath.getNVGPoints().get((j + 1) % lCurrentPath.getNVGPoints().size());
			}
			/* Determine the path's convexity. */
			 /** TODO: Modified logic to match List iteration size. **/
			lCurrentPath.setConvex(lNumLeftTurns == (lCurrentPath.getNVGPoints().size() - 2)); // ?
		}
	}
	
	private final void onFlattenPaths() {
		/* Transform commands. */
		for(int i = 0; i < this.getCommandsBufferOffset(); i++) {
			/* Acquire the PathCommand. */
			final EPathCommand lPathCommand = EPathCommand.values()[(int)this.getCommandsBuffer()[i]];
			/* Process the PathCommand. */
			switch(lPathCommand) {
				case MOVETO   : 
					/* Add a new NVGPath. */
					this.getNVGPaths().add(new NVGPath(EWindingOrder.CCW));
					/* Add the current point to the path. */
					NVGPathContext.onAddPoint(this.getNVGPaths().get(this.getNVGPaths().size() - 1), this.getCommandsBuffer()[i + 1], this.getCommandsBuffer()[i + 2], NVGPoint.FLAG_POINT_CORNER, this.getNanoVG().getDistanceTolerance());
				break;
				case LINETO   : 
					/* Add the current point to the path. */
					NVGPathContext.onAddPoint(this.getNVGPaths().get(this.getNVGPaths().size() - 1), this.getCommandsBuffer()[i + 1], this.getCommandsBuffer()[i + 2], NVGPoint.FLAG_POINT_CORNER, this.getNanoVG().getDistanceTolerance());
				break;
				case BEZIERTO : 
					/* Fetch a reference to the LastPath. */
					final NVGPath      lLastPath    = this.getNVGPaths().get(this.getNVGPaths().size() - 1);
					/* Fetch a reference to the LastPoint. */
					final NVGPoint lLastPoint = lLastPath.getNVGPoints().get(lLastPath.getNVGPoints().size() - 1);
					/* Deploy a tesselated Bezier curve. */
					NVGPathContext.onTesselateBezier(lLastPath, lLastPoint.getX(), lLastPoint.getY(), this.getCommandsBuffer()[i + 1], this.getCommandsBuffer()[i + 2], this.getCommandsBuffer()[i + 3], this.getCommandsBuffer()[i + 4], this.getCommandsBuffer()[i + 5], this.getCommandsBuffer()[i + 6], 0, NVGPoint.FLAG_POINT_CORNER, this.getNanoVG().getDistanceTolerance(), this.getNanoVG().getTesselationTolerance());
				break;
				case CLOSE    : 
					/* Set the LastPath as closed. */
					this.getNVGPaths().get(this.getNVGPaths().size() - 1).setClosed(true);
				break;
				case WINDING  : 
					/* Set the LastPath winding. */
					this.getNVGPaths().get(this.getNVGPaths().size() - 1).setWindingOrder(EWindingOrder.values()[(int)this.getCommandsBuffer()[i + 1]]);
				break;
				default :
					throw new NVGException("Malformed path!");
			}
			/* Offset the search to the next PathCommand in the set. */
			i += lPathCommand.getNumberOfComponents() - 1;
		}
		/* Initialize bounds. */
		this.getBounds()[0] = +1.0E6f;
		this.getBounds()[1] = +1.0E6f;
		this.getBounds()[2] = -1.0E6f;
		this.getBounds()[3] = -1.0E6f;
		
		/* Calculate length and direction of line segments. */
		for(int i = 0; i < this.getNVGPaths().size(); i++) {
			/* Grab a reference to the CurrentPath. */
			final NVGPath lCurrentPath = this.getNVGPaths().get(i);
			/* If the first and last points are the same, remove the last, mark as closed path. */
			if(NVGGlobal.isPointEquals(lCurrentPath.getNVGPoints().get(0).getX(), lCurrentPath.getNVGPoints().get(0).getY(), lCurrentPath.getNVGPoints().get(lCurrentPath.getNVGPoints().size() - 1).getX(), lCurrentPath.getNVGPoints().get(lCurrentPath.getNVGPoints().size() - 1).getY(), this.getNanoVG().getDistanceTolerance())) {
				/* Remove the last vertex. */
				lCurrentPath.getNVGPoints().remove(lCurrentPath.getNVGPoints().size() - 1);
				/* Mark the path as closed. */
				lCurrentPath.setClosed(true);
			}
			/* Enforce winding. */
			if(lCurrentPath.getNVGPoints().size() > 2) {
				/* Calculate the polygon area. */
				final float lSignedArea = NVGPathContext.onCalculatePolygonArea(lCurrentPath.getNVGPoints());
				/* Determine if the vertices must be re-ordered. */
				if((lCurrentPath.getWindingOrder() == EWindingOrder.CCW && lSignedArea < 0.0f) || (lCurrentPath.getWindingOrder() == EWindingOrder.CW && lSignedArea > 0.0f)) {
					/* Reverse the polygon. */
					Collections.reverse(lCurrentPath.getNVGPoints());
				}
			}
			/* Define placeholders for iterating the CurrentPath's NVGPoints. */
			NVGPoint lNVGPoint0 = lCurrentPath.getNVGPoints().get(lCurrentPath.getNVGPoints().size() - 1);
			NVGPoint lNVGPoint1 = lCurrentPath.getNVGPoints().get(0);
			/* Calculate segment directions and length. */
			for(int j = 0; j < lCurrentPath.getNVGPoints().size(); j++) {
				/* Calculate segment direction and length. */
				lNVGPoint0.getDelta()[0] = (lNVGPoint1.getX() - lNVGPoint0.getX());
				lNVGPoint0.getDelta()[1] = (lNVGPoint1.getY() - lNVGPoint0.getY()); 
				lNVGPoint0.setLength(NVGGlobal.normalize(lNVGPoint0.getDelta(), 0, lNVGPoint0.getDelta(), 1, 1.0E-6f)); /** TODO: Abstract to constant. **/
				/* Update bounds. */
				this.getBounds()[0] = Math.min(this.getBounds()[0], lNVGPoint0.getX());
				this.getBounds()[1] = Math.min(this.getBounds()[1], lNVGPoint0.getY());
				this.getBounds()[2] = Math.max(this.getBounds()[2], lNVGPoint0.getX());
				this.getBounds()[3] = Math.max(this.getBounds()[3], lNVGPoint0.getY());
				/* Advance the search. */
				lNVGPoint0 = lNVGPoint1;
				lNVGPoint1 = lCurrentPath.getNVGPoints().get((j + 1) % lCurrentPath.getNVGPoints().size());
			}
		}
		
	}
	
	public final void onTranslate(final float pX, final float pY) {
		final float[] t = new float[6];
		NVGGlobal.setMatrixTranslate(t, 0, pX, pY);
		NVGGlobal.onTransformMultiply(this.getNanoVG().getCurrentState().getTransform(), 0, t, 0);
	}
	
	public final void onRotate(final double pAngle) {
		final float[] t = new float[6];
		NVGGlobal.setMatrixRotate(t, 0, pAngle);
		NVGGlobal.onTransformMultiply(this.getNanoVG().getCurrentState().getTransform(), 0, t, 0);
	}
	
	private final INanoVG getNanoVG() {
		return this.mNanoVG;
	}
	
	private final List<NVGPath> getNVGPaths() {
		return this.mNVGPaths;
	}
	
	private final float[] getCommandsBuffer() {
		return this.mCommandsBuffer;
	}
	
	private final float[] getBounds() {
		return this.mBounds;
	}
	
	private final int getCommandsBufferOffset() {
		return this.mCommandsBufferOffset;
	}
	
	private final float getCommandX() {
		return this.mCommandX;
	}
	
	private final float getCommandY() {
		return this.mCommandY;
	}

}
