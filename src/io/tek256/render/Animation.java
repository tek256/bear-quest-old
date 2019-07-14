package io.tek256.render;

public class Animation {
	//attribute
	private String name;

	//playback source image
	private TextureSheet src;
	
	//playback values
	private int[] frames;
	private int currentFrame = 0;
	
	//playback timers
	private float frameTimer = 0f;
	public float frameLength = 100f;
	
	//playback booleans
	private boolean reverse = false, 
			pause = false,
			play = false,
			loop = true,
			next = false;
	
	/** Create an animation
	 * 
	 * @param source source texture sheet
	 * @param name name of the animation
	 * @param frameLength length in ms of the frame
	 * @param frames list of the frames to be used
	 */
	public Animation(TextureSheet source, String name, float frameLength, int[] frames){
		this.src = source;
		this.name = name;
		this.frameLength = frameLength;
		this.frames = frames;
	}
	
	/** Update the animation based on the time between frames (delta)
	 * 
	 * @param delta the time between frames
	 */
	public void update(float delta){
		if(play && !pause){
			//remove time remaining from the frame timer
			frameTimer -= delta;
			
			//if the timer has gone off for the next frame
			if(frameTimer <= 0){
				//if the current frame is at the end of the animation
				if(currentFrame == lastFrame()){
					//reset the current frame to the start of the animation
					currentFrame = firstFrame();
					//signal that there has been a frame change
					next = true;
					//if not looping stop and signal that no frames changed
					if(!loop){
						stop();
						//signal that there has been a frame change
						next = false;
					}
				}else{
					//decrement or increment the frame based on if its in reverse or not
					currentFrame += (reverse) ? -1 : 1;
					//signal that there has been a frame change
					next = true;
				}
				//reset the frame timer and apply any lay over
				frameTimer = frameLength - frameTimer;
			}
		}
	}
	
	/** Get the last frame to be shown, based on if the animation
	 * is in reverse or not.
	 * 
	 * @return the last frame index in the frames[] array
	 */
	public int lastFrame(){
		return (reverse) ? 0 : frames.length - 1;
	}
	
	/** Get the first frame to be shown, based on if the animation
	 * is in reverse or not.
	 * 
	 * @return the first frame index in the frames[] array
	 */
	public int firstFrame(){
		return (reverse) ? frames.length - 1 : 0;
	}
	
	/** set the frame timer to the length of fps
	 *  (1000 (ms/s) / fps) 
	 * @param fps frames per second
	 */
	public void setFPS(int fps){
		//1000ms / sec / frames per second
		this.frameLength = 1000 / fps;
	}
	
	/** Get the current frame timer in MS
	 * 
	 * @return the current time left on this frame
	 */
	public float getFrameTimer(){
		return frameTimer;
	}
	
	/** Get the frame length in MS
	 * 
	 * @return the frame length in MS
	 */
	public float getFrameLength(){
		return frameLength;
	}
	
	/** Get the current frame in the frame index array
	 * 
	 * @return the current frame in the frame index array
	 */
	public int getCurrentFrame(){
		return currentFrame;
	}
	
	/** Set the current frame in the frame index array
	 * 	Clamped between 0 and max frame length
	 * 
	 * @param frame set the current frame of the animation
	 */
	public void setCurrentFrame(int frame){
		if(frame < 0){
			currentFrame = 0;
		}else if(frame >= frames.length){
			currentFrame = frames.length;
		}else{
			currentFrame = frame;
		}
	}
	
	/** Get the animation name
	 * 
	 * @return the name of the animation
	 */
	public String getName(){
		return name;
	}
	
	/** Get the index array of frames
	 * 
	 * @return the index array of the frames
	 */
	public int[] getFrames(){
		return frames;
	}
	
	/** Get the source (texture sheet) of the
	 * animation
	 * 
	 * @return the source texture sheet
	 */
	public TextureSheet getSource(){
		return this.src;
	}
	
	/** Get the current frame texture
	 * 
	 * @return the current frame texture
	 */
	public Texture getTextureFrame(){
		return src.get(frames[currentFrame]);
	}
	
	/** Get if the animation is paused
	 *
	 * @return if the animation is paused
	 */
	public boolean isPaused(){
		return pause;
	}
	
	/** Get if the animation is stopped
	 *  true = !play && !pause
	 * 
	 * @return if the animation is stopped
	 */
	public boolean isStopped(){
		return !play && !pause;
	}
	
	/** Get if the animation is playing
	 *  true = play && !pause
	 * @return if the animation is playing
	 */
	public boolean isPlaying(){
		return play && !pause;
	}
	
	/** Get if the animation is playing in reverse
	 * 
	 * @return if the animation is playing in reverse
	 */
	public boolean isReverse(){
		return reverse;
	}
	
	/** Get if the animation is looping
	 * 
	 * @return if the animation is looping
	 */
	public boolean isLooping(){
		return loop;
	}
	
	/** Set if the animation should play in reverse
	 *
	 * @param reverse if the animation should play in reverse
	 */
	public void setReverse(boolean reverse){
		this.reverse = reverse;
	}
	
	/** Set if the animation should loop
	 * 
	 * @param loop if the animation should loop
	 */
	public void setLoop(boolean loop){
		this.loop = loop;
	}
	
	/** Set if the animation should paused
	 * 
	 * @param pause if the animation should pause
	 */
	public void setPause(boolean pause){
		this.pause = pause;
	}
	
	/** Set if the animation should play
	 * 
	 * @param play if the animation should play
	 */
	public void setPlay(boolean play){
		if(!play)
			pause = false;
		this.play = play;
	}
	
	/**
	 * Play the animation (removes pause)
	 * Logic: play = true, pause = false
	 */
	public void play(){
		play = true;
		pause = false;
	}
	
	/**
	 * Switch pause state
	 * Logic: pause = !pause
	 */
	public void pause(){
		pause = !pause;
	}
	
	/**
	 * Stop the animation
	 * Logic: play = false, pause = false
	 */
	public void stop(){
		play = false;
		pause = false;
	}
	
	/**
	 * Swap reverse state
	 * Logic: reverse = !reverse
	 */
	public void reverse(){
		reverse = !reverse;
	}
	
	/** Get the frame array as a string 
	 *  Example: "0,1,13,14"
	 * @return the frame array as a string
	 */
	public String getFramesAsString(){
		//create a string builder to add the frames to
		StringBuilder ln = new StringBuilder();
		//for each frame
		for(int frame : frames){
			//add the number to the line
			ln.append(frame+",");
		}
		//return the string
		return ln.toString();
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null) return false;
		if(!o.getClass().equals(getClass())) return false;
		
		Animation oa = (Animation)o;
		return oa.loop == loop && oa.play == play
				&& oa.next == next && oa.src == src
				&& oa.reverse == reverse;
	}
}
