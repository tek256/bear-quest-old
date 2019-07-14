package io.tek256.core.runtime;

import java.util.Arrays;

//Vector2f is fairly standard for any
//classes handling 2D Math
import org.joml.Vector2f;

//in this class, we really only need the 
//render classes, so we call it fairly simple
import io.tek256.render.Animation;
import io.tek256.render.Shader;
import io.tek256.render.Texture;

/**  
 * USAGE: With animations: pass all used animations on creation,
 * 		  or use `setAnimations` to pass the animations array
 * 		  Without animations: Pass the texture to use on creation
 * 
 * NOTES: Renderable is setup to use both individual textures
 * 		  and animations. You can switch between with the `useAnimations`
 * 		  variable. 
 * 		  Animation swapping is meant to be simple, you can pick
 * 		  an animation by name or index. 
 * @author tek256
 * @date 30 August 2016
 */

public class Renderable {
	//the default shader `default` in the shader map
	public static Shader DEFAULT_SHADER;
	
	//the shader to use for the renderable
	private Shader shader = DEFAULT_SHADER;

	//array of animations used
	private Animation[] animations;
	
	//the current animation
	private int currentAnimation = 0;
	
	//animation playback
	private boolean play = false,
			pause = false,autoPlay = false;
		
	//if we want to use the animations
	private boolean useAnimations = true;
	
	//the base texture of the renderable
	private Texture texture;
	
	//the amount of times a texture should be repeated across a plane
	private Vector2f textureRepeat;
	
	private boolean flipX = false, flipY = false;
	
	/** Create a renderable with the list of animations the object will use
	 * 
	 * @param animations list of animations the renderable will use 
	 */
	public Renderable(Animation... animations){
		useAnimations = true;
		//set the animations list
		this.animations = animations;
		//set the default texture repeat to 1,1
		textureRepeat = new Vector2f(Vector2f.ONE);
	}
	
	/** Create a renderable with the base texture 
	 * 
	 * @param texture the base texture of the renderable
	 */
	public Renderable(Texture texture){
		//set the default texture
		this.texture = texture;
		//single image
		this.useAnimations = false;
		//set the default texture repeat at 1,1
		textureRepeat = new Vector2f(Vector2f.ONE);
	}
	
	public Renderable(Texture texture, Animation... animations){
		//set the default texture
		this.texture = texture;
		//set the animation list
		this.animations = animations;
		//set the default texture repeat at 1,1
		textureRepeat = new Vector2f(Vector2f.ONE);
	}
	
	public Renderable(Renderable renderable) {
		this.useAnimations = renderable.useAnimations;
		this.animations = Arrays.copyOf(renderable.animations, renderable.animations.length);
		this.texture = renderable.texture;
		this.autoPlay = renderable.autoPlay;
		this.flipX = renderable.flipX;
		this.flipY = renderable.flipY;
		this.textureRepeat = renderable.textureRepeat;
		this.shader = renderable.shader;
		this.currentAnimation = renderable.currentAnimation;
	}

	/** Update the object with the time between frames
	 * 
	 * @param delta time between frames
	 */
	public void update(long delta){
		if(useAnimations){
			if(play){ 
				if(animations != null){
					animations[currentAnimation].update(delta);
				}
			}
		}
	}
	
	/** Set the current animation to the animation in the list
	 * 
	 * @param animation the animation in the list to use
	 */
	public void setAnimation(int animation){
		//if the animation is out of range then correct it
		currentAnimation = (animation < 0) ? 0 : 
			//otherwise set it to the animation value
			(animation > animations.length) ? animations.length : animation;
		
		//if auto play enabled, start playing
		if(autoPlay){
			playAnimation();
		}
	}
	
	/** Seet the current animation from the name of the animation
	 * 
	 * @param name name of the animation 
	 * @return if the animation was set correctly
	 */
	public boolean setAnimation(String name){
		//check the animation list for the index of the 
		//matching animation
		int index = getIndex(name);
		//if the index is -1 (not in bounds)
		if(index == -1)
			return false; //return false (unable to set)
		
		//stop the animation we're exiting
		animations[currentAnimation].stop();
		
		//otherwise, set the current animation
		currentAnimation = index;
		
		//we we want to auto play the animation
		if(autoPlay){
			//auto play the new current animation
			animations[currentAnimation].play();
		}
		
		//return setting was successful
		return true;
	}
	
	/** Get an animation in the list by name
	 * 
	 * @param name the name of the animation
	 * @return an animation in the list by name
	 */
	public Animation get(String name){
		//for each animation
		for(Animation animation : animations){
			//if the name (normalized to lowercase)
			//equals the name parameter (normalized to lowercase)
			if(animation.getName().toLowerCase().equals(name.toLowerCase())){
				//return the animation as a match
				return animation;
			}
		}
		//otherwise, we didn't find a match so return nothing
		return null;
	}
	
	/** Get the index of an animation in the `animations` list
	 *  
	 *  NOTE: NOT_FOUND = -1
	 *  
	 * @param name the name of the animation
	 * @return the index of an animation in the `animations` list
	 */
	public int getIndex(String name){
		//for each animation
		for(int i=0;i<animations.length;i++){
			//check if the name of the animation(normalized to lowercase)
			//and the name parameter (normalized to lowercase) are equal
			if(animations[i].getName().toLowerCase().equals(name.toLowerCase()))
				//if so, return the index
				return i;
		}
		//otherwise return -1 (not found)
		return -1;
	}
	
	/** Check if an animation is contained in the `animations` list by name
	 * 
	 * @param name name of the animation
	 * @return check if an animation is contained in the list
	 */
	public boolean hasAnimation(String name){
		//for each animation in the list
		for(Animation animation : animations){
			//if the name (normalized to lowercase)
			//equals the name parameter (normalized to lowercase)
			if(animation.getName().toLowerCase().equals(name.toLowerCase()))
				//return that its a match
				return true;
		}
		//otherwise return there was no match found
		return false;
	}
	
	/** Get if animations are present 
	 * 
	 * @return if animations are present
	 */
	public boolean hasAnimations(){
		return animations != null;
	}
	
	/** Get the animations
	 * 
	 * @return the animations
	 */
	public Animation[] getAnimations(){
		return animations;
	}
	
	/** Get the current animation being used
	 * 
	 * @return the current animation being used
	 */
	public Animation getCurrentAnimation(){
		//if the animations list exists
		if(animations != null)
			//get the current listed animation
			return animations[currentAnimation];
		//otherwise, return nothing
		return null;
	}

	/**
	 * Play the `currentAnimation`
	 */
	public void playAnimation(){
		play = true; //make sure the play variable is set
		pause = false; //we're not paused
		animations[currentAnimation].setPlay(true); //tell the animation to play
	}
	
	/**
	 * Pause the `currentAnimation`
	 */
	public void pauseAnimation(){
		pause = true; //make sure the pause variable is set
		animations[currentAnimation].setPause(true); //pause the animation itself
	}
	
	/**
	 * Stop the `currentAnimation`
	 */
	public void stopAnimation(){
		play = false; //we're no longer playing
		pause = false; //and we're not pausing
		if(animations != null)
			animations[currentAnimation].stop(); //tell the animation to stop
	}
	
	/** Check if auto play is enabled
	 * 
	 * @return if auto play is enabled
	 */
	public boolean isAutoPlay(){
		return autoPlay;
	}
	
	/** Check if the `currentAnimation` is playing
	 *  
	 *  Note: When paused, this returns false
	 *  
	 * @return if the `currentAnimation` is playing
	 */
	public boolean isPlaying(){
		return play && !pause;
	}
	
	/** Check if the `currentAnimation` is paused
	 * 
	 * @return if the `currentAnimation` is paused
	 */
	public boolean isPaused(){
		return pause;
	}
	
	/** Check if the `currentAnimation` is stopped
	 * 
	 *  Note: pausing will make this come back false
	 * 
	 * @return if the `currentAnimation` is stopped
	 */
	public boolean isStopped(){
		return !play && !pause;
	}
	
	/** Get the current texture
	 * 
	 * @return the current texture
	 */
	public Texture getTexture(){
		//if the animations are set
		if(animations != null && useAnimations)
			return animations[currentAnimation].getTextureFrame();
		return texture;
	}
	
	/** Check if the renderable is usign animation frames
	 * 
	 * @return if the renderable is using animation frames
	 */
	public boolean isUsingAnimations(){
		return useAnimations;
	}
	
	/** Set if to use the animation frames
	 * 
	 * @param useAnimations if to use the animation frames
	 */
	public void setUseAnimations(boolean useAnimations){
		//change the local `useAnimations` variable
		this.useAnimations = useAnimations;
	}
	
	/** Get the amount the texture should repeat across the x (s) and y (t) axes
	 * 
	 * @return the amount the texture should repeat across the x (s) and y (t) axes
	 */
	public Vector2f getTextureRepeat(){
		return textureRepeat;
	}
	
	/** Set the repeating size of the texture
	 * 
	 * @param textureRepeat the repeating size of the texture
	 */
	public void setTextureRepeat(Vector2f textureRepeat){
		this.textureRepeat.set(textureRepeat);
	}
	
	/** Set the amount of texture repeat across the x & y axis
	 * 
	 * @param x amount of repeat across the x axis (s)
	 * @param y amount of repeat across the y axis (t)
	 */
	public void setTextureRepeat(float x, float y){
		this.textureRepeat.set(x,y);
	}
	
	/** Get the final texture used
	 * 
	 * @return the final texture used
	 */
	public Texture getEscapeTexture(){
		return (texture.isSheet()) ? texture : (texture.isSubTexture())
				? texture.getSheet().getTexture() : texture;
	}
	
	/**
	 * 
	 * @return the shader used by the renderable
	 */
	public Shader getShader(){
		return shader;
	}
	
	public void setFlipX(boolean flipX){
		this.flipX = flipX;
	}
	
	public void setFlipY(boolean flipY){
		this.flipY = flipY;
	}
	
	public boolean isFlipX(){
		return flipX;
	}
	
	public boolean isFlipY(){
		return flipY;
	}
}
