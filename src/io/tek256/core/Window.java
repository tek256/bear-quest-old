package io.tek256.core;

import static org.lwjgl.glfw.GLFW.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import io.tek256.Util;
import io.tek256.core.input.Joystick;
import io.tek256.core.input.Keyboard;
import io.tek256.core.input.Mouse;

import static org.lwjgl.opengl.GL11.*;

/**
 * This class exists to handle all of our GLFW/Window functions for us
 * This does not include OpenGL (although it does do some OpenGL work)
 * 
 * The main attributes of this class are the values `x`,`y`,`width`,`height`,`fullscreen`,
 * `refreshRate`, and `vsync`.
 * 
 * For proper usage of the window you'll want to pollEvents at the start of a frame, 
 * and swap buffers at the end of a frame (or after calling render)
 * 
 * @author tek256
 * @date August 19 2016
 */
public class Window {
	public static final String DEFAULT_TITLE = "Bear Quest 0.02";

	private static Window instance = null;
	
	private int lastX = -1,lastY = -1,lastWidth = -1,lastHeight = -1;
	private long handle;
	private int x = -1,y = -1,width = -1,height = -1,refreshRate = -1;
	private boolean fullscreen = false, allowRender = false, vsync = true;
	private String title;
	
	private GameEngine gameEngine;
	
	public Window(int width, int height){
		this(width, height, DEFAULT_TITLE);
	}
	
	public Window(int width, int height, String title){
		this.width = width;
		this.height = height;
		this.title = title;
		initialize();
	}
	
	public Window(int width, int height, String title, boolean fullscreen){
		this.width = width;
		this.height = height;
		this.title = title;
		this.fullscreen = fullscreen;
		initialize();
	}
	
	public Window(int width, int height, String title, boolean fullscreen, int refreshRate){
		this.width = width;
		this.height = height;
		this.title = title;
		this.fullscreen = fullscreen;
		this.refreshRate = refreshRate;
		initialize();
	}
	
	public Window(int x, int y, int width, int height, String title, boolean fullscreen, int refreshRate, boolean vsync){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.title = title;
		this.fullscreen = fullscreen;
		this.refreshRate = refreshRate;
		this.vsync = vsync;
		initialize();
	}
	
	protected void connect(GameEngine gameEngine){
		this.gameEngine = gameEngine;
	}
	
	private void initialize(){
		//setup a pipe for GLFW to shoot error messages
		GLFWErrorCallback.createPrint(System.err).set();
		
		//initialize GLFW and if it doesn't want to work, then we can't run the game
		if(!glfwInit()){
			throw new RuntimeException("Could not initialize GLFW");
		}
		
		//setup some default guidelines for the window to be created with
		glfwDefaultWindowHints(); 
		
		//while we're creating the window we don't want it to be visible
		setHint(GLFW_VISIBLE, false);
		
		//we also don't want the window resizable so that we can handle proper sizes for 
		//render scaling on our own
		setHint(GLFW_RESIZABLE, false);
		
		//get the desktops current videomode
		GLFWVidMode primaryVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		//if no refresh rate is set then use the monitor's default refresh rate
		if(refreshRate == -1){
			refreshRate = primaryVidMode.refreshRate();
		}
		
		//setup refresh rate
		setHint(GLFW_REFRESH_RATE, refreshRate);
		
		//if the x & y values aren't set (-1) then we center them
		//note: not using center() to conserve on glfw calls for vidmode
		if(x == -1 && y == -1){
			x = (primaryVidMode.width() - width) / 2;
			y = (primaryVidMode.height() - height) / 2;
		}

		//create a window respective to if its fullscreen or not (ref window modes)
		if(fullscreen){
			handle = glfwCreateWindow(width, height, title, glfwGetPrimaryMonitor(), 0L);
		}else{
			handle = glfwCreateWindow(width, height, title, 0L, 0L);
			glfwSetWindowPos(handle, x,y);
		}
		
		//if the window == NULL (c) then the game can't run
		if(handle == 0L)
			throw new RuntimeException("Unable to create window");
		
		//connect the glfw window size to our window class and resize the viewport if available
		glfwSetWindowSizeCallback(handle, (window, width, height) ->{
			this.width = width;
			this.height = height;
			if(GL.getCapabilities() != null)
				GL11.glViewport(0, 0, width, height);
		});
		
		//connect the glfw window position to our window class
		glfwSetWindowPosCallback(handle, (window, x, y)->{
			this.x = x;
			this.y = y;
		});
		
		//connect the keyboard callback to our keyboard class
		glfwSetKeyCallback(handle, (window, key, scancode, action, mods)->{
			Keyboard.setKey(key, (action == GLFW_PRESS) ? true : (action == GLFW_REPEAT) ? true : false);
		});
		
		//connect the mouse button callback to our mouse class
		glfwSetMouseButtonCallback(handle, (handle, button, action, mods)->{
			Mouse.setButton(button, (action == GLFW_PRESS) ? true : false);
		});
		
		//connect the cursor position to our mouse class
		glfwSetCursorPosCallback(handle, (handle, x, y)->{
			Mouse.setPosition(x, y);
		});
		
		//connect the joystick callback to our joystick class (controllers)
		glfwSetJoystickCallback((joy, action)->{
			if(action == GLFW_CONNECTED){
				Joystick.setupJoystick(joy);
			}else if(action == GLFW_DISCONNECTED){
				Joystick.removeJoystick(joy);
			}
		});
		
		//connect the 'x' button to closing out the game
		glfwSetWindowCloseCallback(handle, (window)->{
			gameEngine.exit();
		});
		
		//setup all joysticks connected currently
		Joystick.setup();
		
		//make sure that glfw doesn't multisample our window
		setHint(GLFW_SAMPLES, 0);
		
		//make glfw recognize this window as the current window
		glfwMakeContextCurrent(handle);
		
		//create opengl context
		GL.createCapabilities();
		
		//setup vsync (2 = double buffering, 1 = single buffer, 0 = none)
		if(vsync){
			//enables single buffer vertical sync
			glfwSwapInterval(1);
		}else{
			//disables vertical sync buffers
			glfwSwapInterval(0);
		}
		
		//show the window
		setHint(GLFW_VISIBLE, true);
		glfwShowWindow(handle);
		
		//setup the window viewport correctly
		GL11.glViewport(0, 0, width, height);
		
		//enable textures
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		//enable alphas in textures
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		//enable proper depth testing
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		
		//enable point smoothing
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GL11.glHint(GL11.GL_POINT_SMOOTH, GL11.GL_NICEST);
		
		//enable line smoothing
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH, GL11.GL_NICEST);
		
		//enable perspective correction
		GL11.glEnable(GL11.GL_PERSPECTIVE_CORRECTION_HINT);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		
		//allow frame rendering, since everything is setup
		allowRender = true;
		
		instance = this;
	}
	
	/**
	 * IMPORTANT: use this at the start of an update/input cycle
	 * poll events calls for changes in the mouse, keyboard, and joysticks (controllers)
	 */
	
	public void pollEvents(){
		glfwPollEvents();
	}
	
	/**
	 * IMPORTANT: use this at the end of a render cycle
	 * swap buffers is used to show the current rendered frame
	 */
	public void swapBuffers(){
		glfwSwapBuffers(handle);
	}
	
	public void clear(int buffer){
		glClear(buffer);
	}
	
	public void clearColorAndDepth(){
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	/**
	 * set the window position to the center of the 
	 * desktop's current monitor video mode
	 */
	public void center(){
		//don't change fullscreen x & y
		if(fullscreen)
			return;
		
		//set the backup for `revertPosition()` 
		lastX = x;
		lastY = y;
		
		//get the monitor's current video mode
		GLFWVidMode primaryVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		//calculate the center of the monitor relative to the size of the window
		x = (primaryVidMode.width() - width) / 2;
		y = (primaryVidMode.height() - height) / 2;
		
		//tell glfw to resize the window
		glfwSetWindowPos(handle, x, y); 
	}
	
	public void destroy(){
		glfwDestroyWindow(handle);
	}
	
	/** Load Icons from file with STB and set the glfw window
	 * 	icon as the respective image files
	 * 
	 * @param icon16Path the 16x16 icon path
	 * @param icon32Path the 32x32 icon path
	 */
	public void setIcon(String icon16Path, String icon32Path){
		//setup buffers to work with stb
		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);
		
		//these will be the data buffers for the textures
		ByteBuffer icon16,icon32;
		try{
			//populate the buffers with the raw image data
			icon16 = Util.ioResourceToByteBuffer(icon16Path, 2048);
			icon32 = Util.ioResourceToByteBuffer(icon32Path, 4096);
			
			//setup image buffers for the images to be processed
			try(GLFWImage.Buffer icons = GLFWImage.malloc(2)){
				//process both images with stb
				//16x16 icon
				ByteBuffer p16 = STBImage.stbi_load_from_memory(icon16, w, h, comp, 4);
				icons.position(0).width(w.get(0)).height(h.get(0)).pixels(p16);
				
				//32x32 icon
				ByteBuffer p32 = STBImage.stbi_load_from_memory(icon32, w, h, comp, 4);
				icons.position(1).width(w.get(0)).height(h.get(0)).pixels(p32);
				
				//reset the icons buffer position
				icons.position(0);
				glfwSetWindowIcon(handle, icons);
				
				//free the stb resources
				STBImage.stbi_image_free(p16);
				STBImage.stbi_image_free(p32);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/** Change the input mode (ie mouse grabbed)
	 * 
	 * @param key
	 * @param mode
	 */
	public void setInputMode(int key, int mode){
		glfwSetInputMode(handle, key, mode);
	}
	
	/** Change a window hint in glfw
	 * 
	 * @param key attribute/variable
	 * @param value value
	 */
	public void setHint(int key, int value){
		glfwWindowHint(key, value);
	}
	
	/** Change a true/false hint in glfw
	 * 
	 * @param key attribute/variable
	 * @param value true/false -> (1/0)
	 */
	public void setHint(int key, boolean value){
		glfwWindowHint(key, (value) ? 1 : 0);
	}
	
	/** Change the size of the window
	 * 
	 * @param width width of the window (x)
	 * @param height height of the window (y)
	 */
	public void setSize(int width, int height){
		//if there isn't any real change, don't update
		if(this.width == width && this.height == height)
			return;
		if(!fullscreen){
			//setup backup for `revertSize()`
			this.lastWidth = this.width;
			this.lastHeight = this.height;
		}
		
		//set the size
		this.width = width;
		this.height = height;
		
		//call glfw to update the window size
		glfwSetWindowSize(handle, width, height);
	}
	
	/**
	 * 
	 * @param x x position of the window
	 * @param y y position of the window
	 */
	public void setPosition(int x, int y){
		//don't change the position of a fullscreen window
		if(fullscreen)
			return;
		
		//if there are no real changes, don't update
		if(this.x == x && this.y == y)
			return;
		//setup the backup for `revertPosition()`
		this.lastX = this.x;
		this.lastY = this.y;
		
		//set the position
		this.x = x;
		this.y = y;
		
		//call glfw to update the window position
		glfwSetWindowPos(handle, x, y);
	}
	
	/** Change the title of the window
	 * 
	 * @param title the title of the window
	 */
	public void setTitle(String title){
		//if the title isn't different, don't update
		if(this.title.equals(title))
			return;
		//set the title in the class
		this.title = title;
		
		//call glfw to change it in the OS
		glfwSetWindowTitle(handle, title);
	}
	
	/**
	 * Reverts changes in position,size, and fullscreen 
	 */
	public void revertChanges(){
		if(lastX == -1 && lastY == -1 && lastWidth == -1 && lastHeight == -1)
			return;
		//temporary position variables
		int tX = x;
		int tY = y;
		
		//temporary size variables
		int tW = width;
		int tH = height;
		
		//revert the position
		x = lastX;
		y = lastY;
		
		//revert the size
		width = lastWidth;
		height = lastHeight;
			
		//exit fullscreen
		if(fullscreen){
			setFullscreen(false);
		}else{ 
			//if already windowed we have to update manually
			glfwSetWindowPos(handle, x, y);
			glfwSetWindowSize(handle, width, height);
		}
		
		//backup position changes
		lastX = tX;
		lastY = tY;
		
		//backup size changes
		lastWidth = tW;
		lastHeight = tH;
		
		//update the window's fullscreen status
		setFullscreen(fullscreen);
	}
	
	/**
	 * revert to the backed up size values
	 */
	public void revertSize(){
		//if there are no backups, don't change
		if(lastWidth == -1 && lastHeight == -1)
			return;
		//temporarily backup current sizes
		int tW = width;
		int tH = height;
		
		//revert the sizes
		width = lastWidth;
		height = lastHeight;
		
		//setup backups with temporary backups
		lastWidth = tW;
		lastHeight = tH;
		
		//call glfw to update window size
		glfwSetWindowSize(handle, width, height);
	}
	
	/**
	 * revert to the backed up position values
	 */
	public void revertPosition(){
		//if there are no backups, don't change
		if(lastX == -1 && lastY == -1)
			return;
		
		//temporarily backup current positions
		int tX = x;
		int tY = y;
		
		//revert positions
		x = lastX;
		y = lastY;
		
		//set backups to temporary backups
		lastX = tX;
		lastY = tY;
		
		//call glfw to update the window position
		glfwSetWindowPos(handle, x, y);
	}
	
	public void setFullscreen(boolean fullscreen){
		if(this.fullscreen == fullscreen)
			return;
		
		//disable rendering so no frames are thrown away
		allowRender = false;
		
		if(this.fullscreen){ //exiting fullscreen
			if(x == -1 && y == -1 && lastX == -1 && lastY == -1){
				center();
			}else if(lastX != -1 && lastY != -1){
				revertPosition();
			}
			
			//switch to the windowed size
			revertSize();
			
			destroy();
			
			handle = glfwCreateWindow(width, height, title, 0L, 0L);
			
			this.fullscreen = false;
		}else{ //entering fullscreen
			
			//destroy the instance of the window
			destroy();
			
			//the array of video modes glfw fetches
			GLFWVidMode.Buffer vidBuffer = glfwGetVideoModes(glfwGetPrimaryMonitor());
			//video mode we'll set
			GLFWVidMode selection = null;
			//go thru each vidmode
			for(int i=0;i<vidBuffer.capacity();i++){
				//move the vidmode buffer's cursor
				vidBuffer.position(i);
				//if the selection isn't set, select the first one for reference
				if(selection == null)
					selection = vidBuffer.get();
				
				//if the difference to target is less than selected (width -> height -> refreshRate)
				if(width - vidBuffer.width() <= width - selection.width()){
					if(height - vidBuffer.height() <= height - selection.height()){
						if(refreshRate - vidBuffer.refreshRate() <= refreshRate - selection.refreshRate()){
							//update the selection
							selection = vidBuffer.get();
						}
					}
				}
			}
			
			//update the refresh rate variable
			refreshRate = selection.refreshRate();
			
			//send glfw the refreshrate
			setHint(GLFW_REFRESH_RATE, refreshRate);
			
			//set the RGB values of the mode
			setHint(GLFW_RED_BITS, selection.redBits());
			setHint(GLFW_GREEN_BITS, selection.greenBits());
			setHint(GLFW_BLUE_BITS, selection.blueBits());
			
			//update the size variables
			width = selection.width();
			height = selection.height();
			
			//create the window
			handle = glfwCreateWindow(width, height, title, glfwGetPrimaryMonitor(), 0L);
		}
		//allow rendering again
		allowRender = true;
	}
	
	public String getTitle(){
		return title;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public long getHandle(){
		return handle;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getRefreshRate(){
		return refreshRate;
	}
	
	public boolean isFullscreen(){
		return fullscreen;
	}
	
	public boolean isRenderAllowed(){
		return allowRender;
	}
	
	public boolean isCloseRequested(){
		return glfwWindowShouldClose(handle);
	}
	
	public static Window getCurrent(){
		return instance;
	}
	
	public static void exit(){
		glfwTerminate();
	}
	
	@Override
	public String toString(){
		return "Window{x:"+x+", y:"+y+", width:"+width+", height:"+
					height+", fullscreen:"+fullscreen+", vsync:"+vsync+",refreshRate:"+refreshRate+"}";
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null) return false;
		if(!o.getClass().equals(this.getClass())) return false;
		Window w = (Window)o;
		return w.x == x && w.y == y && w.lastX == lastX && w.lastY == lastY
				&& w.width == width && w.height == height &&
				w.lastWidth == lastWidth && w.lastHeight == lastHeight &&
				w.fullscreen == fullscreen && w.vsync == vsync && w.title == title;
	}
}	
