package com.nanovgj.global;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class NVGGlobal {
	
	public static final int OPENGL_FLOATS_PER_VEC4 = 4;
	public static final int BYTES_PER_FLOAT        = Float.SIZE     / Byte.SIZE;
	public static final int BYTES_PER_XYUV         = 4 * NVGGlobal.BYTES_PER_FLOAT;
	public static final int RGBA_LENGTH			   = 4;
	
	public static final int onCalculateCurveDivs(final float pR, final float pArc, final float pTolerance) {
		final float lDA = (float)Math.acos(pR / (pR + pTolerance)) * 2.0f;
		return Math.max(2, (int)Math.ceil(pArc / lDA));
		
	}
	
	public static final float onCalculateAverageScale(final float[] pT, final int pOffset) {
		float lSX = (float)Math.sqrt(pT[pOffset + 0] * pT[pOffset + 0] + pT[pOffset + 2] * pT[pOffset + 2]);
		float lSY = (float)Math.sqrt(pT[pOffset + 1] * pT[pOffset + 1] + pT[pOffset + 3] * pT[pOffset + 3]);
		return (lSX + lSY) * 0.5f;
	}

	public static final float clamp(float a, float mn, float mx) { 
		return a < mn ? mn : (a > mx ? mx : a); 
	}

	public static final int clamp(int a, int mn, int mx) {
		return a < mn ? mn : (a > mx ? mx : a); 
	}
	
	public static final float normalize(final float[] pX, final int pXOffset, final float[] pY, final int pYOffset, final float pMinimumMagnitude) {
		final float lD = (float)Math.sqrt((pX[pXOffset]*pX[pXOffset]) + (pY[pYOffset] * pY[pYOffset]));
		if(lD > pMinimumMagnitude) { /** TODO: Minimum magnitude abstraction. **/
			final float lID = 1.0f / lD;
			pX[pXOffset] *= lID;
			pY[pYOffset] *= lID;
		}
		return lD;
	}
	
	public static final boolean isPointEquals(final float pX1, final float pY1, final float pX2, final float pY2, final float pTolerance) {
		final float lDX = pX2 - pX1;
		final float lDY = pY2 - pY1;
		return (lDX * lDX) + (lDY * lDY) < (pTolerance * pTolerance);
	}
	
	public static final void setMatrixTranslate(final float[] pT, final int pOffset, final float pX, final float pY) {
		pT[pOffset + 0] = 1.0f; 
		pT[pOffset + 1] = 0.0f;
		pT[pOffset + 2] = 0.0f; 
		pT[pOffset + 3] = 1.0f;
		pT[pOffset + 4] = pX;   
		pT[pOffset + 5] = pY;
	}
	
	public static final void setMatrixScale(final float[] pT, final int pOffset, final float pX, final float pY) {
		pT[pOffset + 0] = pX; 
		pT[pOffset + 1] = 0.0f;
		pT[pOffset + 2] = 0.0f; 
		pT[pOffset + 3] = pY;
		pT[pOffset + 4] = 0.0f; 
		pT[pOffset + 5] = 0.0f;
	}
	
	public static final void setMatrixRotate(final float[] pT, final int pOffset, final double pA) {
		final float lCosine = (float)Math.cos(pA);
		final float lSine   = (float)Math.sin(pA);
		pT[pOffset + 0] =      lCosine; 
		pT[pOffset + 1] =      lSine;
		pT[pOffset + 2] = -1 * lSine; 
		pT[pOffset + 3] =      lCosine;
		pT[pOffset + 4] = 0.0f; 
		pT[pOffset + 5] = 0.0f;
	}
	
	public static final void setMatrixSkewX(final float[] pT, final int pOffset, final double pA) {
		pT[pOffset + 0] = 1.0f; 
		pT[pOffset + 1] = 0.0f;
		pT[pOffset + 2] = (float)Math.tan(pA);
		pT[pOffset + 3] = 1.0f;
		pT[pOffset + 4] = 0.0f; 
		pT[pOffset + 5] = 0.0f;
	}
	
	public static final void setMatrixSkewY(final float[] pT, final int pOffset, final double pA) {
		pT[0] = 1.0f; 
		pT[1] = (float)Math.tan(pA);
		pT[2] = 0.0f; 
		pT[3] = 1.0f;
		pT[4] = 0.0f; 
		pT[5] = 0.0f;
	}
	
	public static final void onTransformMultiply(final float[] pT, final int pOffsetT, final float[] pS, final int pOffsetS) {
		float lT0        = pT[0 + pOffsetT] * pS[0 + pOffsetS] + pT[1 + pOffsetT] * pS[2 + pOffsetS];
		float lT2        = pT[2 + pOffsetT] * pS[0 + pOffsetS] + pT[3 + pOffsetT] * pS[2 + pOffsetS];
		float lT4        = pT[4 + pOffsetT] * pS[0 + pOffsetS] + pT[5 + pOffsetT] * pS[2 + pOffsetS] + pS[4 + pOffsetS];
		pT[1 + pOffsetT] = pT[0 + pOffsetT] * pS[1 + pOffsetS] + pT[1 + pOffsetT] * pS[3 + pOffsetS];
		pT[3 + pOffsetT] = pT[2 + pOffsetT] * pS[1 + pOffsetS] + pT[3 + pOffsetT] * pS[3 + pOffsetS];
		pT[5 + pOffsetT] = pT[4 + pOffsetT] * pS[1 + pOffsetS] + pT[5 + pOffsetT] * pS[3 + pOffsetS] + pS[5 + pOffsetS];
		pT[0 + pOffsetT] = lT0;
		pT[2 + pOffsetT] = lT2;
		pT[4 + pOffsetT] = lT4;
	}
	
	public static final void setMatrixIdentity(final float[] pT, final int pOffset) {
		pT[pOffset + 0] = 1.0f; 
		pT[pOffset + 1] = 0.0f;
		pT[pOffset + 2] = 0.0f; 
		pT[pOffset + 3] = 1.0f;
		pT[pOffset + 4] = 0.0f; 
		pT[pOffset + 5] = 0.0f;
	}
	
//	public static final void onTransformPremultiply(final float[] pT, final int pTOffset, final float[] pS, final int pSOffset) {
//		final float[] lS = Arrays.copyOfRange(pS, pSOffset, pSOffset + 6);
//		NVGGlobal.onTransformMultiply(lS, 0, pT, pTOffset);
//		System.arraycopy(lS, 0, pT, pTOffset, lS.length);
//	}
	
	public static final double TRANSFORM_MINIMUM_DETERMINANT = 1E-6;
	
	public static final boolean onTransformInverse(final float[] pInverse, final int pInverseOffset, final float[] pT, final int pTOffset) {
		double lInverseDet, lDet = (double)pT[0 + pTOffset] * pT[3 + pTOffset] - (double)pT[2 + pTOffset] * pT[1 + pTOffset];
		if(lDet > (-1 * NVGGlobal.TRANSFORM_MINIMUM_DETERMINANT) && lDet < NVGGlobal.TRANSFORM_MINIMUM_DETERMINANT) {
			NVGGlobal.setMatrixIdentity(pInverse, pInverseOffset);
			return false;
		}
		lInverseDet = 1.0 / lDet;
		pInverse[0 + pInverseOffset] = (float)( pT[3 + pTOffset] * lInverseDet);
		pInverse[2 + pInverseOffset] = (float)(-pT[2 + pTOffset] * lInverseDet);
		pInverse[4 + pInverseOffset] = (float)(((double)pT[2 + pTOffset] * pT[5 + pTOffset] - (double)pT[3 + pTOffset] * pT[4 + pTOffset]) * lInverseDet);
		pInverse[1 + pInverseOffset] = (float)(-pT[1 + pTOffset] * lInverseDet);
		pInverse[3 + pInverseOffset] = (float)( pT[0 + pTOffset] * lInverseDet);
		pInverse[5 + pInverseOffset] = (float)(((double)pT[1 + pTOffset] * pT[4 + pTOffset] - (double)pT[0 + pTOffset] * pT[5 + pTOffset]) * lInverseDet);
		return true;
	}
	
	public static final void onTransformPoint(final float[] pX, final int pXOffset, final float[] pY, final int pYOffset, final float[] pT, final int pTOffset, final float pSX, final float pSY) {
		pX[pXOffset] = pSX * pT[0 + pTOffset] + pSY * pT[2 + pTOffset] + pT[4 + pTOffset];
		pY[pYOffset] = pSX * pT[1 + pTOffset] + pSY * pT[3 + pTOffset] + pT[5 + pTOffset];
	}
	
	public static final void setRGBA(final float[] pColor, final int pColorOffset, final float pRed, final float pGreen, final float pBlue, final float pAlpha) {
		pColor[pColorOffset + 0] = pRed;
		pColor[pColorOffset + 1] = pGreen;
		pColor[pColorOffset + 2] = pBlue;
		pColor[pColorOffset + 3] = pAlpha;
	}
	
	public static final ByteBuffer delegateNative(final float[] pFloats) {
		/* Allocate memory for the ByteBuffer (with a backing array). */
		final ByteBuffer lReturnBuffer = ByteBuffer.allocate(pFloats.length * NVGGlobal.BYTES_PER_FLOAT).order(ByteOrder.nativeOrder());
		/* Load float data. */
		lReturnBuffer.asFloatBuffer().put(pFloats);
		/* Reposition the ByteBuffer pointer. */
		lReturnBuffer.position(0);
		/* Return the created ByteBuffer. */
		return lReturnBuffer;
	}
	
	public static final void onPremultiplyRGBA(final float[] pColorDestination, final int pColorDestinationOffset, final float[] pColorSource, final int pColorSourceOffset) {
		pColorDestination[pColorDestinationOffset + 0] = (pColorSource[pColorSourceOffset + 0] * pColorSource[pColorSourceOffset + 3]);
		pColorDestination[pColorDestinationOffset + 1] = (pColorSource[pColorSourceOffset + 1] * pColorSource[pColorSourceOffset + 3]);
		pColorDestination[pColorDestinationOffset + 2] = (pColorSource[pColorSourceOffset + 2] * pColorSource[pColorSourceOffset + 3]);
		pColorDestination[pColorDestinationOffset + 3] =  pColorSource[pColorSourceOffset + 3];
	}
	
	public static final void onTransformToMatrix3x4(final float[] pMat3, final int pMat3Index, final float[] pT, final int pTOffset) {
		pMat3[pMat3Index + 0]  = pT[pTOffset + 0];
		pMat3[pMat3Index + 1]  = pT[pTOffset + 1];
		pMat3[pMat3Index + 2]  = 0.0f;
		pMat3[pMat3Index + 3]  = 0.0f;
		pMat3[pMat3Index + 4]  = pT[pTOffset + 2];
		pMat3[pMat3Index + 5]  = pT[pTOffset + 3];
		pMat3[pMat3Index + 6]  = 0.0f;
		pMat3[pMat3Index + 7]  = 0.0f;
		pMat3[pMat3Index + 8]  = pT[pTOffset + 4];
		pMat3[pMat3Index + 9]  = pT[pTOffset + 5];
		pMat3[pMat3Index + 10] = 1.0f;
		pMat3[pMat3Index + 11] = 0.0f;
	}
	
	public static final int toNearestPowerOfTwo(final int pValue) {
		return 1 << (Integer.SIZE - (Integer.numberOfLeadingZeros(pValue - 1)));
	}
	
	/* Prevent instantiation of this class. */
	private NVGGlobal() {}
	
}
