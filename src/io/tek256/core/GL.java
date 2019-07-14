package io.tek256.core;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;

public class GL {
	private static float r = 0f,g = 0f,b = 0f,a = 0f;
	
	/** Set the clear color values
	 *  Note: If a value is passed greater than 1, 
	 *  the value will be divided by 255 for normalization.
	 * 
	 * @param r red value of the clear color
	 * @param g green value of the clear color
	 * @param b blue value of the clear color
	 */
	public static void clearColor(float r, float g, float b){
		GL.r = (r > 1) ? r / 255 : r;
		GL.g = (g > 1) ? g / 255 : g;
		GL.b = (b > 1) ? b / 255 : b;
		GL11.glClearColor(GL.r, GL.g, GL.b, GL.a);
	}
	
	/** Set the clear color values
	 *  Note: If a value is passed greater than 1, 
	 *  the value will be divided by 255 for normalization.
	 * 
	 * @param r red value of the clear color
	 * @param g green value of the clear color
	 * @param b blue value of the clear color
	 * @param a alpha value of the clear color
	 */
	public static void clearColor(float r, float g, float b, float a){
		GL.r = (r > 1) ? r / 255 : r;
		GL.g = (g > 1) ? g / 255 : g;
		GL.b = (b > 1) ? b / 255 : b;
		GL.a = (a > 1) ? a / 255 : a;
		GL11.glClearColor(GL.r, GL.g, GL.b, GL.a);
	}
	
	/** Enable an OpenGL Function
	 * 
	 * @param func OpenGL Function
	 */
	public static void enable(int func){
		glEnable(func);
	}
	
	/** disable an OpenGL Function
	 * 
	 * @param func OpenGL Function
	 */
	public static void disable(int func){
		glDisable(func);
	}
	
	/** Set an OpenGL Depth Function
	 *  
	 * @param func OpenGL Depth Function
	 */
	public static void depthFunc(int func){
		glDepthFunc(func);
	}
	
	/** Set the blend function for OpenGL
	 * 
	 * @param src source value
	 * @param dst destination value
	 */
	public static void blendFunc(int src, int dst){
		glBlendFunc(src, dst);
	}
	
	/**
	 * Enable GL_CULL_FACE and set culling to
	 * GL_BACK (back face)
	 */
	public static void cullFace(){
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
	}
}
