package io.tek256.core;

import io.tek256.GamePreferences;
import io.tek256.core.audio.Audio;
import io.tek256.core.input.*;
import io.tek256.render.Texture;

public class GameEngine {
	public static final boolean DEBUG = false;
	
	//static second variable, 1000ms / 1 second
	private static final long SECOND = 1000;
	
	public static boolean autoSavePreferences = false;
	
	private static GameEngine instance = null;
	
	private boolean running = false;
	
	//runtime variables
	public int target_fps = 60;
	private int fps_check = target_fps;
	public int target_ups = 60;
	private int ups_check = target_ups;
	
	private int updates = 0, frames = 0;
	private int fps = 0, ups = 0;
	
	//the game preferences
	private GamePreferences gamePreferences;
	
	//the game interface
	private GameInterface gameInterface;
	
	//the game scene
	private Scene scene;
	
	//the game window
	private Window window;
	
	//the game's audio
	private Audio audio;
	
	public GameEngine(GamePreferences gamePreferences, GameInterface gameInterface){
		//set the game preferences variable
		this.gamePreferences = gamePreferences;
		
		//set the game interface
		this.gameInterface = gameInterface;

		instance = this;
		
		//create the window from game preferences
		window = new Window(gamePreferences.x, gamePreferences.y, gamePreferences.width, gamePreferences.height, 
				"Test Window", gamePreferences.fullscreen,	gamePreferences.refreshRate, gamePreferences.vsync);
		
		//connect the window to this instance for functionality
		window.connect(this);

		//setup the audio manager
		audio = new Audio();
		
		//setup the keyboard
		new Keyboard();
		//setup the mouse
		new Mouse();
		
		//setup the default scene
		scene = new Scene();
		
		//set the scene for the game interface
		gameInterface.connect(scene);
		
		//call start in the game interface
		gameInterface.start();
		
		//enter the game loop
		loop();
	}
	
	private void loop(){
		//make sure the game starts running
		running = true;
		
		//update the check counters
		fps_check = target_fps;
		ups_check = target_ups;
		
		//calculate the time lines
		long update_frame = SECOND / target_ups;
		long render_frame = SECOND / target_fps;
		
		//loop frame variables
		long delta = 0; //time between frames
		long accumulation = 0; //time left
		long last = 0; //last loop time
		long current = 0; //current loop time
		
		//the timer runs on a 1 second timer to update FPS & UPS
		long timer = 0;
		
		//allow the loop to run
		running = true;
		
		//loop the thread
		while(running){
			//update the current time
			current = System.currentTimeMillis();
			//update the delta
			delta = current - last;
			
			//update the last variable with the current update time
			last = current;
			
			//true last update = last - delta
			
			//update the timer
			timer -= delta;
			
			//if the target updates per second has changed
			if(ups_check != target_ups){
				//recalculate the time frame for updates
				update_frame = SECOND / target_ups;
				//reset the check variable
				ups_check = target_ups;
			}
			
			//if the target frames per second has changed
			if(fps_check != target_fps){
				//recalculate the time for frames
				render_frame = SECOND / target_fps;
				//reset the check variable
				fps_check = target_fps;
			}
			
			//update the accumulator with render time frame (new frame in time)
			accumulation += render_frame;
			
			//if time left in the frame to update, do so
			if(accumulation >= update_frame){
				//call for input to be polled
				input(update_frame);
				
				//call for the game to update
				update(update_frame);
				
				//add to the amount of updates in this second
				updates++;
				
				//update the accumulation timer 
				accumulation -= update_frame;
			}
			
			//render the frame
			render(render_frame);
				
			//update frame counter
			frames++;
			
			//if the timer is ready to be reset
			if(timer <= 0){
				//show the last second's stats
				fps = frames;
				ups = updates;
				
				//reset the counters
				updates = 0;
				frames = 0;
				
				//reset the timer
				timer = SECOND;
				
				//only output the stats if we're debugging
				if(DEBUG)
					System.out.println(fps+":"+ups);
			}
		}
		//close out the game
		exit();
	}
	
	/** Update input based on the time since the last input update
	 * 
	 * @param delta time since the last input update
	 */
	private void input(long delta){
		//if rendering isn't allowed, input isn't going
		//to change anything
		if(!window.isRenderAllowed())
			return;
		
		//have glfw poll events
		window.pollEvents();
		
		//update all input classes
		Keyboard.update();
		Mouse.update();
		Joystick.updateAll();
		
		//call for any input methods to be used
		scene.input(delta);
		gameInterface.input(delta);
	}
	
	/** Update everything based on the time since the last update
	 * 
	 * @param delta time between last update
	 */
	private void update(long delta){
		//update the scene
		scene.update(delta);
		//update the game
		gameInterface.update(delta);
	}
	
	/** Render everything based on the tiem since the last frame
	 * 
	 * @param delta time between the last frame
	 */
	private void render(long delta){
		//if rendering is disabled, just don't render 
		if(!window.isRenderAllowed())
			return;
		
		//clear the color
		window.clearColorAndDepth();
		
		//render the frame
		scene.render(delta);
		gameInterface.render(delta);
		
		//show new frame
		window.swapBuffers();
	}

	public void exit(){
		//if already running allow the loop to close
		if(running){
			running = false;
			return;
		}
		
		if(autoSavePreferences){
			//update the game preferences with the window attributes
			gamePreferences.fullscreen = window.isFullscreen();
			
			gamePreferences.width = window.getWidth();
			gamePreferences.height = window.getHeight();
			
			gamePreferences.refreshRate = window.getRefreshRate();
			
			gamePreferences.x = window.getX();
			gamePreferences.y = window.getY();
			
			//save the updated game preferences 
			gamePreferences.save();
		}
		
		//end the game instance
		gameInterface.end();
		
		//destroy the scene assets
		scene.destroy(true);
		
		//clear out any IO buffers
		Keyboard.destroy();
		Mouse.destroy();
		Joystick.destroy();
		
		//exit the window
		window.destroy();
		
		audio.destroy();
		
		//terminate glfw
		Window.exit();
		//exit java
		System.exit(0);
	}
	
	/** Get the frames per second
	 * 
	 * @return the frames per second
	 */
	public int getFPS(){
		return fps;
	}
	
	/**
	 * Get the updates per second
	 * @return the updates per second
	 */
	public int getUPS(){
		return ups;
	}
	
	
	/** Get the default game preferences set
	 * 
	 * @return the default game preferences set
	 */
	public GamePreferences getPreferences(){
		return gamePreferences;
	}
	
	
	public static void close(){
		instance.exit();
	}
	
}
