package com.nanovgj;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.cawfree.boilerplate.IGLEventListener;
import com.github.cawfree.boilerplate.IGLES20;

import com.nanovgj.exception.NVGException;
import com.nanovgj.global.NVGGlobal;

public class NanoVG implements IGLEventListener<IGLES20>, INanoVG {
	
	/* GLSL 130+ uses 'in' and 'out' instead of attribute and varying. */
	private static final String HEADER_VERSION_MIGRATION = 
	      "#if __VERSION__ >= 130"
	+"\n"+"\t"+"#define attribute in"
	+"\n"+"\t"+"#define varying   out"
	+"\n"+"#endif"
	+"\n";
	
	private static final String HEADER_PLATFORM_PRECISION = 
		  "#if GL_ES >= 130"
	+"\n"+"\t"+"precision mediump float;"
	+"\n"+"\t"+"precision mediump int;"
	+"\n"+"#endif"
	+"\n";

	private static final String SHADER_VERTEX   =	NanoVG.HEADER_VERSION_MIGRATION +
	      "uniform   vec2 viewSize;"
	+"\n"+"attribute vec2 vertex;"
	+"\n"+"attribute vec2 tcoord;"
	+"\n"+"varying   vec2 ftcoord;"
	+"\n"+"varying   vec2 fpos;"
	+"\n"+"void main(void) {"
	+"\n"+"ftcoord     = tcoord;"
	+"\n"+"fpos        = vertex;"
	+"\n"+"gl_Position = vec4(2.0*vertex.x/viewSize.x - 1.0, 1.0 - 2.0*vertex.y/viewSize.y, 0, 1);"
	+"\n"+"}";

	public static final int UNIFORMARRAY_GLSL_LENGTH                    = 11; /* Number of vec4s. */
	public static final int UNIFORMARRAY_GLSL_INDEX_SCISSOR_MATRIX      = 0;
	public static final int UNIFORMARRAY_GLSL_INDEX_PAINT_MATRIX        = 3;
	public static final int UNIFORMARRAY_GLSL_INDEX_INNER_COLOR         = 6;
	public static final int UNIFORMARRAY_GLSL_INDEX_OUTER_COLOR         = 7;
	public static final int UNIFORMARRAY_GLSL_INDEX_SCISSOR_EXT         = 8;
	public static final int UNIFORMARRAY_GLSL_INDEX_SCISSOR_SCALE       = 8;
	public static final int UNIFORMARRAY_GLSL_INDEX_EXTENT              = 9;
	public static final int UNIFORMARRAY_GLSL_INDEX_RADIUS              = 9;
	public static final int UNIFORMARRAY_GLSL_INDEX_FEATHER             = 9;
	public static final int UNIFORMARRAY_GLSL_INDEX_STROKE_MULTIPLIER   = 10;
	public static final int UNIFORMARRAY_GLSL_INDEX_STROKE_THRESHOLD    = 10;
	public static final int UNIFORMARRAY_GLSL_INDEX_STROKE_TEXTURE_TYPE = 10;
	public static final int UNIFORMARRAY_GLSL_INDEX_STROKE_RENDER_TYPE  = 10;

	public  static final int UNIFORMARRAY_JAVA_LENGTH                    = UNIFORMARRAY_GLSL_LENGTH * NVGGlobal.OPENGL_FLOATS_PER_VEC4;
	public static final int UNIFORMARRAY_JAVA_INDEX_SCISSOR_MATRIX      = 0;
	public static final int UNIFORMARRAY_JAVA_INDEX_PAINT_MATRIX        = 12;
	public static final int UNIFORMARRAY_JAVA_INDEX_INNER_COLOR         = 24;
	public static final int UNIFORMARRAY_JAVA_INDEX_OUTER_COLOR         = 28;
	public static final int UNIFORMARRAY_JAVA_INDEX_SCISSOR_EXT         = 32;
	public static final int UNIFORMARRAY_JAVA_INDEX_SCISSOR_SCALE       = 34;
	public static final int UNIFORMARRAY_JAVA_INDEX_EXTENT              = 36;
	public static final int UNIFORMARRAY_JAVA_INDEX_RADIUS              = 38;
	public static final int UNIFORMARRAY_JAVA_INDEX_FEATHER             = 39;
	public static final int UNIFORMARRAY_JAVA_INDEX_STROKE_MULTIPLIER   = 40;
	public static final int UNIFORMARRAY_JAVA_INDEX_STROKE_THRESHOLD    = 41;
	public static final int UNIFORMARRAY_JAVA_INDEX_STROKE_TEXTURE_TYPE = 42;
	public static final int UNIFORMARRAY_JAVA_INDEX_STROKE_RENDER_TYPE  = 43;
	
	private static final String SHADER_FRAGMENT =	NanoVG.HEADER_PLATFORM_PRECISION + 
			
		      "uniform vec4 frag["+NanoVG.UNIFORMARRAY_GLSL_LENGTH+"];"
		+"\n"+"uniform sampler2D tex;"
		+"\n"+"varying vec2 ftcoord;"
		+"\n"+"varying vec2 fpos;"
		+"\n"+"#define scissorMat mat3(frag[" + UNIFORMARRAY_GLSL_INDEX_SCISSOR_MATRIX      + "].xyz, frag[" + (UNIFORMARRAY_GLSL_INDEX_SCISSOR_MATRIX + 1) +"].xyz, frag[" + (UNIFORMARRAY_GLSL_INDEX_SCISSOR_MATRIX + 2) +"].xyz)"
		+"\n"+"#define paintMat   mat3(frag[" + UNIFORMARRAY_GLSL_INDEX_PAINT_MATRIX        + "].xyz, frag[" + (UNIFORMARRAY_GLSL_INDEX_PAINT_MATRIX   + 1) +"].xyz, frag[" + (UNIFORMARRAY_GLSL_INDEX_PAINT_MATRIX   + 2) +"].xyz)"
		+"\n"+"#define innerCol        frag[" + UNIFORMARRAY_GLSL_INDEX_INNER_COLOR         + "]"
		+"\n"+"#define outerCol        frag[" + UNIFORMARRAY_GLSL_INDEX_OUTER_COLOR         + "]"
		+"\n"+"#define scissorExt      frag[" + UNIFORMARRAY_GLSL_INDEX_SCISSOR_EXT         + "].xy"
		+"\n"+"#define scissorScale    frag[" + UNIFORMARRAY_GLSL_INDEX_SCISSOR_SCALE       + "].zw"
		+"\n"+"#define extent          frag[" + UNIFORMARRAY_GLSL_INDEX_EXTENT              + "].xy"
		+"\n"+"#define radius          frag[" + UNIFORMARRAY_GLSL_INDEX_RADIUS              + "].z"
		+"\n"+"#define feather         frag[" + UNIFORMARRAY_GLSL_INDEX_FEATHER             + "].w"
		+"\n"+"#define strokeMult      frag[" + UNIFORMARRAY_GLSL_INDEX_STROKE_MULTIPLIER   + "].x"
		+"\n"+"#define strokeThr       frag[" + UNIFORMARRAY_GLSL_INDEX_STROKE_THRESHOLD    + "].y"
		+"\n"+"#define texType     int(frag[" + UNIFORMARRAY_GLSL_INDEX_STROKE_TEXTURE_TYPE +"].z)"
		+"\n"+"#define type        int(frag[" + UNIFORMARRAY_GLSL_INDEX_STROKE_RENDER_TYPE  +"].w)"
		
		+"\n"+"float sdroundrect(vec2 pt, vec2 ext, float rad) {"
		+"\n"+"\t"+"vec2 ext2 = ext - vec2(rad,rad);"
		+"\n"+"\t"+"vec2 d = abs(pt) - ext2;"
		+"\n"+"\t"+"return min(max(d.x,d.y),0.0) + length(max(d,0.0)) - rad;"
		+"\n"+"}"
			
		+"\n"+"float scissorMask(vec2 p) {"
		+"\n"+"\t"+"vec2 sc = (abs((scissorMat * vec3(p,1.0)).xy) - scissorExt);"
		+"\n"+"\t"+"sc = vec2(0.5,0.5) - sc * scissorScale;"
		+"\n"+"\t"+"return clamp(sc.x,0.0,1.0) * clamp(sc.y,0.0,1.0);"
		+"\n"+"}"
			
		+"\n"+"float strokeMask() {"
		+"\n"+"\t"+"return min(1.0, (1.0-abs(ftcoord.x*2.0-1.0))*strokeMult) * min(1.0, ftcoord.y);"
		+"\n"+"}"
					
		+"\n"+"void main(void) {"
		+"\n"+"\t"+"vec4 result;\n"
		+"\n"+"\t"+"float scissor = scissorMask(fpos);"
		+"\n"+"\t"+"float strokeAlpha = strokeMask();"
		+"\n"+"\t"+"if (type == "+(EShadingMode.FILL_GRADIENT.ordinal())+") {"    /* Gradient. */
		+"\n"+"\t"+"\t"+"vec2 pt = (paintMat * vec3(fpos,1.0)).xy;"
		+"\n"+"\t"+"\t"+"float d = clamp((sdroundrect(pt, extent, radius) + feather*0.5) / feather, 0.0, 1.0);"
		+"\n"+"\t"+"\t"+"vec4 color = mix(innerCol,outerCol,d);"
		+"\n"+"\t"+"\t"+"color *= strokeAlpha * scissor;"
		+"\n"+"\t"+"\t"+"result = color;"
		+"\n"+"\t"+"} else if (type == "+(EShadingMode.FILL_IMAGE.ordinal())+") {"/* Image. */
		+"\n"+"\t"+"\t"+"vec2 pt = (paintMat * vec3(fpos,1.0)).xy / extent;"
		+"\n"+"\t"+"\t"+"vec4 color = texture2D(tex, pt);"
		+"\n"+"\t"+"\t"+"if (texType == "+ETextureType.RGBA.ordinal()+")          { color = vec4(color.xyz*color.w,color.w); }"
		+"\n"+"\t"+"\t"+"if (texType == "+ETextureType.ALPHA_CHANNEL.ordinal()+") { color = vec4(color.x);                   }"
		+"\n"+"\t"+"\t"+"color *= innerCol;"
		+"\n"+"\t"+"\t"+"color *= strokeAlpha * scissor;"
		+"\n"+"\t"+"\t"+"result = color;"
		+"\n"+"\t"+"} else if (type == "+(EShadingMode.SIMPLE.ordinal())+") {"   /* Stencil fill. */
		+"\n"+"\t"+"\t"+"result = vec4(1,1,1,1);"
		+"\n"+"\t"+"} else if (type == "+(EShadingMode.IMAGE.ordinal())+") {"    /* Textured triangles. */
		+"\n"+"\t"+"\t"+"vec4 color = texture2D(tex, ftcoord);"
		+"\n"+"\t"+"\t"+"if (texType == "+ETextureType.RGBA.ordinal()+")          { color = vec4(color.xyz*color.w,color.w); }"
		+"\n"+"\t"+"\t"+"if (texType == "+ETextureType.ALPHA_CHANNEL.ordinal()+") { color = vec4(color.x);                   }"
		+"\n"+"\t"+"\t"+"color *= scissor;"
		+"\n"+"\t"+"\t"+"result = color * innerCol;"
		+"\n"+"\t"+"}"
		+"\n"+"\t"+"if (strokeAlpha < strokeThr) discard;"
		+"\n"+"\t"+"gl_FragColor = result;"
		+"\n"+"}";
			
	
/*	private static final String onFetchShaderError(final IGLES20 pGLES20, final int pShaderId) {
          byte[] lErrorLog = new byte[255];
          pGLES20.glGetShaderInfoLog(pShaderId, (lErrorLog.length), (int[])null, 0, lErrorLog, 0);
          return new String(lErrorLog);
	}
	
	private static final String onFetchProgramError(final IGLES20 pGLES20, final int pProgramId) {
        byte[] lErrorLog = new byte[255];
        pGLES20.glGetProgramInfoLog(pProgramId, (lErrorLog.length), (int[])null, 0, lErrorLog, 0);
        return new String(lErrorLog);
	}
*/	
	private static final void onFetchGLError(final IGLES20 pGLES20) {
		final int lError = pGLES20.glGetError();
		if(lError != IGLES20.GL_NO_ERROR) {
			System.err.println("OpenGL error " + Integer.toHexString(lError) + ".");
		}
	}
	
	private static final void onCompileShader(final IGLES20 pGLES20, final int pShaderId, final int[] pStatusBuffer) {
		/* Vertex Shader compilation. */
		pGLES20.glCompileShader(pShaderId);
		pGLES20.glGetShaderiv(pShaderId, IGLES20.GL_COMPILE_STATUS, pStatusBuffer, 0);
		if(pStatusBuffer[0] == IGLES20.GL_NONE) { 
			throw new NVGException("GL2ES2 compilation error.");// + NanoVG.onFetchShaderError(pGLES20, pShaderId)); 
		}
	}
	
	private static final void onLinkProgram(final IGLES20 pGLES20, final int pProgramId, final int[] pStatusBuffer) {
		/* Program linking. */
		pGLES20.glLinkProgram(pProgramId);
		pGLES20.glGetProgramiv(pProgramId, IGLES20.GL_LINK_STATUS, pStatusBuffer, 0);
		if(pStatusBuffer[0] == IGLES20.GL_NONE) { 
			throw new NVGException("GL2ES2 linker error. \n");// NanoVG.onFetchProgramError(pGLES20, pProgramId)); 
		}
	}
	
	@Override
	public final float[] onConvertPaint(final NVGPaint pNVGPaint, final NVGScissor pNVGScissor, final float pWidth, final float pFringe, final float pStrokeThreshold) {
		
		final float[] lPaintArray = new float[NanoVG.UNIFORMARRAY_JAVA_LENGTH];
		
		/* Create an InverseTransform reference. */
		final float[] lInverseTransform = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
		/* Define the PaintArray's color. */
		//NVGGlobal.onPremultiplyRGBA(lPaintArray, UNIFORMARRAY_JAVA_INDEX_INNER_COLOR, pNVGPaint.getInnerColor(), 0); /** TODO: Why doesn't this work? **/
		//NVGGlobal.onPremultiplyRGBA(lPaintArray, UNIFORMARRAY_JAVA_INDEX_OUTER_COLOR, pNVGPaint.getOuterColor(), 0);
		System.arraycopy(pNVGPaint.getInnerColor(), 0, lPaintArray, UNIFORMARRAY_JAVA_INDEX_INNER_COLOR, NVGGlobal.RGBA_LENGTH);
		System.arraycopy(pNVGPaint.getOuterColor(), 0, lPaintArray, UNIFORMARRAY_JAVA_INDEX_OUTER_COLOR, NVGGlobal.RGBA_LENGTH);
		
		if(pNVGScissor.getExtent()[0] < -0.5f || pNVGScissor.getExtent()[1] < -0.5f) {
			/* Re-initialize the PaintArray's Scissor parameters. */
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_SCISSOR_EXT   + 0] = 1.0f;
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_SCISSOR_EXT   + 1] = 1.0f;
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_SCISSOR_SCALE + 0] = 1.0f;
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_SCISSOR_SCALE + 1] = 1.0f;
		}
		else {
			/* Compute the inverse transform. */
			NVGGlobal.onTransformInverse(lInverseTransform, 0, pNVGScissor.getTransform(), 0); /** Check implementation. **/
			/* Convert the inverse transform to a Matrix 3x4 representation. */
			NVGGlobal.onTransformToMatrix3x4(lPaintArray, UNIFORMARRAY_JAVA_INDEX_SCISSOR_MATRIX, lInverseTransform, 0);
			/* Re-initialize the PaintArray's Scissor parameters. */
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_SCISSOR_EXT   + 0] = pNVGScissor.getExtent()[0];
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_SCISSOR_EXT   + 1] = pNVGScissor.getExtent()[1];
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_SCISSOR_SCALE + 0] = (float)Math.sqrt(pNVGScissor.getTransform()[0] * pNVGScissor.getTransform()[0] + pNVGScissor.getTransform()[2] * pNVGScissor.getTransform()[2]) / pFringe;
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_SCISSOR_SCALE + 1] = (float)Math.sqrt(pNVGScissor.getTransform()[1] * pNVGScissor.getTransform()[1] + pNVGScissor.getTransform()[3] * pNVGScissor.getTransform()[3]) / pFringe;
		}
		
		/* Copy the NVGPaint's gradient rendering extent. */
		System.arraycopy(pNVGPaint.getExtent(), 0, lPaintArray, UNIFORMARRAY_JAVA_INDEX_EXTENT, pNVGPaint.getExtent().length);
		/* Copy stroke constants. */
		lPaintArray[UNIFORMARRAY_JAVA_INDEX_STROKE_MULTIPLIER] = (pWidth * 0.5f + pFringe * 0.5f) / pFringe;
		lPaintArray[UNIFORMARRAY_JAVA_INDEX_STROKE_THRESHOLD]  = (pStrokeThreshold);
		
		if(pNVGPaint.getImage() != null) {
			throw new RuntimeException("NVGImage not yet implemented.");
		}
		else {
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_STROKE_RENDER_TYPE] = (float)EShadingMode.FILL_GRADIENT.ordinal();
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_RADIUS]             = pNVGPaint.getRadius();
			lPaintArray[UNIFORMARRAY_JAVA_INDEX_FEATHER]            = pNVGPaint.getFeather();
			/* Execute the inverse transform. */
			NVGGlobal.onTransformInverse(lInverseTransform, 0, pNVGPaint.getTransform(), 0);
		}
		/* Apply the PaintArray. */
		NVGGlobal.onTransformToMatrix3x4(lPaintArray, UNIFORMARRAY_JAVA_INDEX_PAINT_MATRIX, lInverseTransform, 0);
		
		return lPaintArray;
	}
	
	@Override
	public final void onSetUniforms(final IGLES20 pGLES20, final float[] pPaintArray, final NVGImage pNVGImage) {
		/* Buffer the uniform array. */
		pGLES20.glUniform4fv(this.getUniformArray(), UNIFORMARRAY_GLSL_LENGTH, pPaintArray, 0);
		/* Apply the image. */
		if(pNVGImage != null) {
			throw new RuntimeException("NVGImage not yet implemented.");
		}
		else {
			/* Bind a null texture. */
			pGLES20.glBindTexture(IGLES20.GL_TEXTURE_2D, IGLES20.GL_NONE);
		}
	}
	
	private static final float DEFAULT_TOLERANCE_TESSELATION = 0.125f;
	private static final float DEFAULT_TOLERANCE_DISTANCE    = 0.010f;
	private static final float DEFAULT_WIDTH_FRINGE          = 1.200f;
	
	/* Member Variables. */
	private final INVGListener   mNVGListener;
	private final List<NVGState> mNVGStates;
	private final List<NVGCall>  mNVGCalls;
	private final NVGPathContext mNVGPathBuilder;
	private       int            mVertexShader;
	private       int            mFragmentShader;
	private       int            mRenderProgram;
	private       int            mAttributeVertex;
	private       int            mAttributeTextureCoord;
	private       int            mUniformArray;
	private       int            mUniformView;
	private       int            mUniformTexture;
	private final float[]        mView;
	private final float          mPixelRatio;
	private final float          mTesselationTolerance;
	private final float          mDistanceTolerance;
	private final float          mFringeWidth;
	private       float[]        mVerticesBuffer;
	private       int            mVerticesBufferOffset;
	
	public NanoVG(final INVGListener pNVGListener, final float pPixelRatio) {
		/* Initialize Member Variables. */
		this.mNVGListener           = pNVGListener;
		this.mNVGStates             = new ArrayList<NVGState>();
		this.mNVGCalls              = new ArrayList<NVGCall>();
		this.mNVGPathBuilder        = new NVGPathContext(this);
		this.mVertexShader          = 0;
		this.mFragmentShader        = 0;
		this.mRenderProgram         = 0;
		this.mAttributeVertex       = 0;
		this.mAttributeTextureCoord = 0;
		this.mUniformArray          = 0;
		this.mUniformView           = 0;
		this.mUniformTexture        = 0;
		this.mView                  = new float[2];
		this.mPixelRatio            = pPixelRatio;
		this.mTesselationTolerance  = NanoVG.DEFAULT_TOLERANCE_TESSELATION / this.getPixelRatio();
		this.mDistanceTolerance     = NanoVG.DEFAULT_TOLERANCE_DISTANCE    / this.getPixelRatio();
		this.mFringeWidth           = NanoVG.DEFAULT_WIDTH_FRINGE          / this.getPixelRatio();
		this.mVerticesBuffer        = new float[]{};
		this.mVerticesBufferOffset  = 0;
		/** TODO: Remove this code. (Initializing with a default state.) **/
		this.getNVGStates().add(new NVGState());
	}

	@Override
	public final void onNVGCall(final NVGCall pNVGCall) {
		/* Add the call to the list. */
		this.getNVGCalls().add(pNVGCall);
	}

	@Override
	public void init(final IGLES20 pGLES20) {
		/* Declare method variables. */
		int[] lStatusBuffer = new int[1];
		/* Allocate shaders onboard the GPU. */
		this.mVertexShader   = pGLES20.glCreateShader(IGLES20.GL_VERTEX_SHADER);
		this.mFragmentShader = pGLES20.glCreateShader(IGLES20.GL_FRAGMENT_SHADER);
		this.mRenderProgram  = pGLES20.glCreateProgram();
		/* Delegate the shader source. */
		pGLES20.glShaderSource(this.getVertexShader(), NanoVG.SHADER_VERTEX);
		//pGLES20.glShaderSource(this.getVertexShader(),   1, new String[]{ NanoVG.SHADER_VERTEX },   new int[]{ NanoVG.SHADER_VERTEX.length() },   0);
		pGLES20.glShaderSource(this.getFragmentShader(), NanoVG.SHADER_FRAGMENT);
		//pGLES20.glShaderSource(this.getFragmentShader(), 1, new String[]{ NanoVG.SHADER_FRAGMENT }, new int[]{ NanoVG.SHADER_FRAGMENT.length() }, 0);
		/* Compile shaders. */
		NanoVG.onCompileShader(pGLES20, this.getVertexShader(),   lStatusBuffer);
		NanoVG.onCompileShader(pGLES20, this.getFragmentShader(), lStatusBuffer);
		/* Attach the compiled shaders. */
		pGLES20.glAttachShader(this.getRenderProgram(), this.getVertexShader());
		pGLES20.glAttachShader(this.getRenderProgram(), this.getFragmentShader());
		/* Link the program. */
		NanoVG.onLinkProgram(pGLES20, this.getRenderProgram(), lStatusBuffer);
		/* Attribute Locations. */
		this.mAttributeVertex       = pGLES20.glGetAttribLocation(this.getRenderProgram(), "vertex");
		this.mAttributeTextureCoord = pGLES20.glGetAttribLocation(this.getRenderProgram(), "tcoord");
		/* Uniform Locations. */
		this.mUniformArray          = pGLES20.glGetUniformLocation(this.getRenderProgram(), "frag");
		this.mUniformView           = pGLES20.glGetUniformLocation(this.getRenderProgram(), "viewSize");
		this.mUniformTexture        = pGLES20.glGetUniformLocation(this.getRenderProgram(), "tex");
		
	}

	@Override
	public void reshape(final IGLES20 pGLES20, final int pX, final int pY, final int pWidth, final int pHeight) {
		/* View Storage. */
		this.mView[0] = (float)pWidth;
		this.mView[1] = (float)pHeight;
		/* Configure the Viewport. */
		pGLES20.glViewport(0, 0, pWidth, pHeight);
		/* Enable a transparent display. */
		pGLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
	}
	
	@Override
	public void display(final IGLES20 pGLES20) {
		/* Clear the display. */
		pGLES20.glClear(IGLES20.GL_COLOR_BUFFER_BIT | IGLES20.GL_DEPTH_BUFFER_BIT | IGLES20.GL_STENCIL_BUFFER_BIT);
		/* Initialize broad graphical configuration. */
		pGLES20.glEnable(IGLES20.GL_BLEND);
		pGLES20.glBlendFunc(IGLES20.GL_SRC_ALPHA, IGLES20.GL_ONE_MINUS_SRC_ALPHA);
		pGLES20.glEnable(IGLES20.GL_CULL_FACE);
		pGLES20.glDisable(IGLES20.GL_DEPTH_TEST);
		/* Allow the NVGListener to render the frame. */
		this.getNVGListener().onRenderFrame(this.getNVGPathBuilder(), this.getView()[0], this.getView()[1]);
		/* Render flush. */
		pGLES20.glUseProgram(this.getRenderProgram());
		pGLES20.glCullFace(IGLES20.GL_BACK);
		pGLES20.glFrontFace(IGLES20.GL_CCW);
		pGLES20.glEnable(IGLES20.GL_BLEND);
		pGLES20.glDisable(IGLES20.GL_DEPTH_TEST);
		pGLES20.glDisable(IGLES20.GL_SCISSOR_TEST);
		pGLES20.glColorMask(true, true, true, true);
		pGLES20.glStencilMask(0xFFFFFFFF);
		pGLES20.glStencilOp(IGLES20.GL_KEEP, IGLES20.GL_KEEP, IGLES20.GL_KEEP);
		pGLES20.glStencilFunc(IGLES20.GL_ALWAYS, 0, 0xFFFFFFFF);
		pGLES20.glActiveTexture(IGLES20.GL_TEXTURE0);
		pGLES20.glBindTexture(IGLES20.GL_TEXTURE_2D, 0);
		/* Buffer the Texture Location. */
		pGLES20.glUniform1i(this.getUniformTexture(), 0);
		/* Buffer the View. */
		pGLES20.glUniform2fv(this.getUniformView(), 1, this.getView(), 0);
		/* Allocate a pointer towards a new VertexBuffer. */
		int[] lVertexBufferId = new int[1];
	    	/* Attempt to allocate a new buffer. */
		pGLES20.glGenBuffers(1, lVertexBufferId, 0);
		/* Buffer the Vertex data. */
		final ByteBuffer lVertexData = NVGGlobal.delegateNative(this.getVertices());
		/* Bind to the buffer. */
		pGLES20.glBindBuffer(IGLES20.GL_ARRAY_BUFFER, lVertexBufferId[0]);
		/* Transfer vertex from native memory to the GPU. */
		pGLES20.glBufferData(IGLES20.GL_ARRAY_BUFFER, (this.getVerticesBufferOffset() * NVGGlobal.BYTES_PER_FLOAT), lVertexData, IGLES20.GL_STREAM_DRAW);
		/* Enable vertex attributes. */
		pGLES20.glEnableVertexAttribArray(this.getAttributeVertex());
		pGLES20.glEnableVertexAttribArray(this.getAttributeTextureCoord());
		/* Initialize vertex stride. */
		pGLES20.glVertexAttribPointer(this.getAttributeVertex(),       2, IGLES20.GL_FLOAT, false, NVGGlobal.BYTES_PER_XYUV, 0);
		pGLES20.glVertexAttribPointer(this.getAttributeTextureCoord(), 2, IGLES20.GL_FLOAT, false, NVGGlobal.BYTES_PER_XYUV, 2 * NVGGlobal.BYTES_PER_FLOAT);
		/* Iterate through each NVGCall. */
		for(final NVGCall lNVGCall : this.getNVGCalls()) {
			/* Render the NVGCall. */
			lNVGCall.onRenderGraphics(this, pGLES20);
		}
		/* Disable vertex attributes. */
		pGLES20.glDisableVertexAttribArray(this.getAttributeVertex());
		pGLES20.glDisableVertexAttribArray(this.getAttributeTextureCoord());
		/* Unbind from the buffer. */
		pGLES20.glBindBuffer(IGLES20.GL_ARRAY_BUFFER, IGLES20.GL_NONE);
		/* Delete the vertex buffer. */
		pGLES20.glDeleteBuffers(1, lVertexBufferId, 0);
		/* Disable the depth test. */
		pGLES20.glEnable(IGLES20.GL_DEPTH_TEST);
		/* Unbind from the program. */
		pGLES20.glUseProgram(IGLES20.GL_NONE);
		/* Reset the VertexOffset. */
		this.mVerticesBufferOffset = 0;
		/* Clear the NVGCalls. */
		this.getNVGCalls().clear();
		/* Reset the CurrentState. */
		this.getNVGStates().clear();
		this.getNVGStates().add(new NVGState());
		/* Check for errors. */
		NanoVG.onFetchGLError(pGLES20);
	}

	@Override
	public void dispose(final IGLES20 pGLES20) {
		/* Delete dependencies. */
		pGLES20.glDeleteShader(this.getVertexShader());
		pGLES20.glDeleteShader(this.getFragmentShader());
		pGLES20.glDeleteProgram(this.getRenderProgram());
	}
	
	@Override
	public final void onBufferVertices(final float[] pVertices) {
		/* Determine if the current array of vertices can hold the new set. */
		if(this.getVerticesBufferOffset() + pVertices.length > this.getVertices().length) {
			/* Re-allocate the array. */
			this.mVerticesBuffer = Arrays.copyOf(this.getVertices(), NVGGlobal.toNearestPowerOfTwo(this.getVerticesBufferOffset() + pVertices.length));
		}
		/* Buffer the vertices. */
		System.arraycopy(pVertices, 0, this.getVertices(), this.getVerticesBufferOffset(), pVertices.length);
		/* Increase the offset. */
		this.mVerticesBufferOffset += pVertices.length;
	}
	
	public final INVGListener getNVGListener() {
		return this.mNVGListener;
	}
	
	private final List<NVGState> getNVGStates() {
		return this.mNVGStates;
	}
	
	private final List<NVGCall> getNVGCalls() {
		return this.mNVGCalls;
	}
	
	@Override
	public final NVGState getCurrentState() {
		return this.getNVGStates().get(this.getNVGStates().size() - 1);
	}
	
	private final NVGPathContext getNVGPathBuilder() {
		return this.mNVGPathBuilder;
	}
	
	private final int getVertexShader() {
		return this.mVertexShader;
	}
	
	private final int getFragmentShader() {
		return this.mFragmentShader;
	}
	
	private final int getRenderProgram() {
		return this.mRenderProgram;
	}
	
	private final int getAttributeVertex() {
		return this.mAttributeVertex;
	}
	
	private final int getAttributeTextureCoord() {
		return this.mAttributeTextureCoord;
	}
	
	private final int getUniformArray() {
		return this.mUniformArray;
	}
	
	private final int getUniformView() {
		return this.mUniformView;
	}
	
	private final int getUniformTexture() {
		return this.mUniformTexture;
	}
	
	private final float[] getView() {
		return this.mView;
	}
	
	private final float getPixelRatio() {
		return this.mPixelRatio;
	}
	
	@Override
	public final float getTesselationTolerance() {
		return this.mTesselationTolerance;
	}
	
	@Override
	public final float getDistanceTolerance() {
		return this.mDistanceTolerance;
	}
	
	@Override
	public final float getFringeWidth() {
		return this.mFringeWidth;
	}
	
	@Override
	public final float[] getVertices() {
		return this.mVerticesBuffer;
	}
	
	@Override
	public final int getVerticesBufferOffset() {
		return this.mVerticesBufferOffset;
	}

}
