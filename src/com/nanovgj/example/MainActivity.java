package com.nanovgj.example;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.nanovgj.INVGListener;
import com.nanovgj.NVGPaint;
import com.nanovgj.NVGPathContext;
import com.nanovgj.NanoVG;

public final class MainActivity {
	static int i = 1;
	private static final void drawEyes(final NVGPathContext pNVGPathBuilder, float x, float y, float w, float h, float mx, float my, float pT) {
		NVGPaint gloss, bg;
		float ex = w *0.23f;
		float ey = h * 0.5f;
		float lx = x + ex;
		float ly = y + ey;
		float rx = x + w - ex;
		float ry = y + ey;
		float dx,dy,d;
		float br = (ex < ey ? ex : ey) * 0.5f;
		float blink = 1 - (float)Math.pow((float)Math.sin((float)pT*0.5f),200)*0.8f; /** TODO: Get this to work. **/
		i+=1.5f;

		
		bg = pNVGPathBuilder.onCreateLinearGradient(x,y+h*0.5f,x+w*0.1f,y+h, new float[]{(float)0/255,(float)0/255,(float)0/255,(float)32/255}, new float[]{(float)0/255,(float)0/255,(float)0/255,(float)16/255});
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onEllipse(lx+3.0f,ly+16.0f, ex,ey);
		pNVGPathBuilder.onEllipse(rx+3.0f,ry+16.0f, ex,ey);
		pNVGPathBuilder.onFillPaint(bg);
		pNVGPathBuilder.onFill();
		
		bg = pNVGPathBuilder.onCreateLinearGradient(x,y+h*0.25f,x+w*0.1f,y+h, new float[]{(float)220/255,(float)220/255,(float)220/255,(float)255/255}, new float[]{(float)128/255,(float)128/255,(float)128/255,(float)255/255});
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onEllipse(lx,ly, ex,ey);
		pNVGPathBuilder.onEllipse(rx,ry, ex,ey);
		pNVGPathBuilder.onFillPaint(bg);
		pNVGPathBuilder.onFill();
		
		dx = (mx - rx) / (ex * 10);
		dy = (my - ry) / (ey * 10);
		d = (float)Math.sqrt(dx*dx+dy*dy);
		if (d > 1.0f) {
		dx /= d; dy /= d;
		}
		dx *= ex*0.4f;
		dy *= ey*0.5f;
		
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onEllipse(lx+dx,ly+dy+ey*0.25f*(1-blink), br,br*blink);
		pNVGPathBuilder.onFillColor(new float[]{(float)32/255,(float)32/255,(float)32/255,(float)255/255});
		pNVGPathBuilder.onFill();
		dx = (mx - rx) / (ex * 10);
		dy = (my - ry) / (ey * 10);
		d = (float)Math.sqrt(dx*dx+dy*dy);
		if (d > 1.0f) {
		dx /= d; dy /= d;
		}
		dx *= ex*0.4f;
		dy *= ey*0.5f;
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onEllipse(rx+dx,ry+dy+ey*0.25f*(1-blink), br,br*blink);
		pNVGPathBuilder.onFillColor(new float[]{(float)32/255,(float)32/255,(float)32/255,(float)255/255});
		pNVGPathBuilder.onFill();
		
		gloss = pNVGPathBuilder.onCreateRadialGradient(lx-ex*0.25f,ly-ey*0.5f, ex*0.1f,ex*0.75f, new float[]{(float)255/255,(float)255/255,(float)255/255,(float)128/255}, new float[]{(float)255/255,(float)255/255,(float)255/255,(float)0/255});
		
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onEllipse(lx,ly, ex,ey);
		pNVGPathBuilder.onFillPaint(gloss);
		pNVGPathBuilder.onFill();
		
		gloss = pNVGPathBuilder.onCreateRadialGradient(rx-ex*0.25f,ry-ey*0.5f, ex*0.1f,ex*0.75f,  new float[]{(float)255/255,(float)255/255,(float)255/255,(float)128/255}, new float[]{(float)255/255,(float)255/255,(float)255/255,(float)0/255});
		
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onEllipse(rx,ry, ex,ey);
		pNVGPathBuilder.onFillPaint(gloss);
		pNVGPathBuilder.onFill();
		
	}
	
	private static final void drawGraph(final NVGPathContext pNVGPathBuilder, float x, float y, float w, float h, float t) {
		NVGPaint bg;
		float[] samples = new float[6];
		float[] sx = new float[6], sy = new float[6];
		float dx = w/5.0f;
		int i;
		samples[0] = (1+(float)Math.sin(t*1.2345f+(float)Math.cos(t*0.33457f)*0.44f))*0.5f;
		samples[1] = (1+(float)Math.sin(t*0.68363f+(float)Math.cos(t*1.3f)*1.55f))*0.5f;
		samples[2] = (1+(float)Math.sin(t*1.1642f+(float)Math.cos(t*0.33457)*1.24f))*0.5f;
		samples[3] = (1+(float)Math.sin(t*0.56345f+(float)Math.cos(t*1.63f)*0.14f))*0.5f;
		samples[4] = (1+(float)Math.sin(t*1.6245f+(float)Math.cos(t*0.254f)*0.3f))*0.5f;
		samples[5] = (1+(float)Math.sin(t*0.345f+(float)Math.cos(t*0.03f)*0.6f))*0.5f;
		for (i = 0; i < 6; i++) {
		sx[i] = x+i*dx;
		sy[i] = y+h*samples[i]*0.8f;
		}
		// Graph background
		bg = pNVGPathBuilder.onCreateLinearGradient(x,y,x,y+h, new float[]{(float)0/255, (float)160/255,(float)192/255, (float)255/255}, new float[]{(float)0/255, (float)160/255,(float)192/255, (float)0/255});
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onMoveTo(sx[0], sy[0]);
		for (i = 1; i < 6; i++)
		pNVGPathBuilder.onBezierTo(sx[i-1]+dx*0.5f,sy[i-1], sx[i]-dx*0.5f,sy[i], sx[i],sy[i]);
		pNVGPathBuilder.onLineTo(x+w, y+h);
		pNVGPathBuilder.onLineTo(x, y+h);
		pNVGPathBuilder.onFillPaint(bg);
		pNVGPathBuilder.onFill();
		// Graph line
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onMoveTo(sx[0], sy[0]+2);
		for (i = 1; i < 6; i++)
			pNVGPathBuilder.onBezierTo(sx[i-1]+dx*0.5f,sy[i-1]+2, sx[i]-dx*0.5f,sy[i]+2, sx[i],sy[i]+2);
		pNVGPathBuilder.onStrokeColor(new float[]{(float)0/255, (float)0/255,(float)0/255, (float)32/255});
		pNVGPathBuilder.onStrokeWidth(3);
		pNVGPathBuilder.onStroke();
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onMoveTo(sx[0], sy[0]);
		for (i = 1; i < 6; i++)
			pNVGPathBuilder.onBezierTo(sx[i-1]+dx*0.5f,sy[i-1], sx[i]-dx*0.5f,sy[i], sx[i],sy[i]);
		pNVGPathBuilder.onStrokeColor(new float[]{(float)0/255, (float)160/255,(float)192/255, (float)255/255});
		pNVGPathBuilder.onStrokeWidth(3);
		pNVGPathBuilder.onStroke();
		// Graph sample pos
		for (i = 0; i < 6; i++) {
		bg = pNVGPathBuilder.onCreateRadialGradient(sx[i],sy[i]+2, 3.0f,8.0f, new float[]{(float)0/255, (float)0/255,(float)0/255, (float)32/255}, new float[]{(float)0/255, (float)0/255,(float)0/255, (float)0/255});
		pNVGPathBuilder.onBeginPath();
		pNVGPathBuilder.onRect(sx[i]-10, sy[i]-10+2, 20,20);
		pNVGPathBuilder.onFillPaint(bg);
		pNVGPathBuilder.onFill();
		}
		pNVGPathBuilder.onBeginPath();
		for (i = 0; i < 6; i++)
			pNVGPathBuilder.onCircle(sx[i], sy[i], 4.0f);
		pNVGPathBuilder.onFillColor(new float[]{(float)0/255, (float)160/255,(float)192/255, (float)255/255});
		pNVGPathBuilder.onFill();
		pNVGPathBuilder.onBeginPath();
		for (i = 0; i < 6; i++)
			pNVGPathBuilder.onCircle(sx[i], sy[i], 2.0f);
		pNVGPathBuilder.onFillColor(new float[]{(float)220/255, (float)220/255,(float)220/255, (float)255/255});
		pNVGPathBuilder.onFill();
		pNVGPathBuilder.onStrokeWidth(1);
	}
	
	public static final void main(final String[] pArgs) {
		final GLCapabilities lGLCapabilities = new GLCapabilities(GLProfile.get(GLProfile.GL2ES2));
	    lGLCapabilities.setBackgroundOpaque(true);
	    final GLWindow lGLWindow = GLWindow.create(lGLCapabilities);
        final FPSAnimator lFPSAnimator = new FPSAnimator(30);
        lGLWindow.setPosition(50, 50);
        lGLWindow.setSize(500, 500);
        lGLWindow.setTitle("NanoVGJ");
        lGLWindow.setUndecorated(false);
        lGLWindow.setPointerVisible(true);
        lGLWindow.addGLEventListener(new NanoVG(new INVGListener() {
        	float i;
			@Override public final void onRenderFrame(final NVGPathContext pNVGPathBuilder, final float pWidth, final float pHeight) {
				MainActivity.drawEyes(pNVGPathBuilder, pWidth - 250, 50, 150, 100, 0, 0, (long)(i+=0.01));
				MainActivity.drawGraph(pNVGPathBuilder, 0, pHeight/2, pWidth, pHeight/2, (i+=0.01));
			}
			
        }, 0.8f));
        lFPSAnimator.add(lGLWindow);
        lFPSAnimator.start();
        lGLWindow.setVisible(true);
	}

}