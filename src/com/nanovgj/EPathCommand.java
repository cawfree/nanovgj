package com.nanovgj;

public enum EPathCommand {
	
	MOVETO   { @Override public final float toFloat() { return 0.0f; } @Override public final int getNumberOfComponents() { return 3; } },
	LINETO   { @Override public final float toFloat() { return 1.0f; } @Override public final int getNumberOfComponents() { return 3; } },
	BEZIERTO { @Override public final float toFloat() { return 2.0f; } @Override public final int getNumberOfComponents() { return 7; } },
	CLOSE    { @Override public final float toFloat() { return 3.0f; } @Override public final int getNumberOfComponents() { return 1; } },
	WINDING  { @Override public final float toFloat() { return 4.0f; } @Override public final int getNumberOfComponents() { return 2; } };

	public abstract float toFloat();
	public abstract int   getNumberOfComponents();
}
