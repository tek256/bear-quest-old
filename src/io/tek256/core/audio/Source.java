package io.tek256.core.audio;

import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class Source {
	//the OpenAL ID of the source 
	private int id;
	//position of the source in 3D Space
	private Vector3f position;
	
	//playback variables
	private boolean play = false, pause = false;
	private float gain = 0.8f;
	private int sampleTime; //basically unused for now
	
	//max distance of the source
	private float distance = 10f;
	
	//the sound to play at the source
	private Sound sound;
	
	public Source(){
		this.position = new Vector3f();
		initialize();
	}
	
	public Source(Sound sound){
		this.sound = sound;
		position = new Vector3f();
		initialize();
	}
	
	public Source(Vector3f position){
		this.position = new Vector3f(position);
		initialize();
	}
	
	public Source(Sound sound, Vector3f position){
		this.position = new Vector3f(position);
		this.sound = sound;
		initialize();
	}
	
	private void initialize(){
		//create the OpenAL Id for the source
		id = alGenSources();
		
		//if the sound is set, attach the sound to the source
		if(sound != null)
			alSourcei(id, AL_BUFFER, sound.getId());
		
		//set the position of the source
		alSource3f(id, AL_POSITION, position.x, position.y, position.z);
		
		//set the base gain of the source
		alSourcef(id, AL_GAIN, gain);
		//set the point where its loudest
		alSourcef(id, AL_REFERENCE_DISTANCE, 1.0f);
		//set the max distance
		alSourcef(id, AL_MAX_DISTANCE, distance);
		//set the rolloff factor
		alSourcef(id, AL_ROLLOFF_FACTOR, 1.0f);
	}	
	
	/** Set the max distance for fall off of the source's volume
	 * 
	 * @param distance max distance for fall off of the source's volume
	 */
	public void setMaxDistance(float distance){
		this.distance = distance;
		//buffer the distance to OpenAL
		alSourcef(id, AL_MAX_DISTANCE, distance);
	}
	
	/** Get the max distance for fall off of the source's volume
	 * 
	 * @return the max distance for fall off of the source's volume
	 */
	public float getMaxDistance(){
		return distance;
	}
	
	/** Set the sound for the source to play
	 * 
	 * @param sound the sound for the source to play
	 */
	public void setSound(Sound sound){
		this.sound = sound;
		//buffer in OpenAL the buffer (sound) of the source
		alSourcei(id, AL_BUFFER, sound.getId());
	}

	/** Get the sound the source is playing
	 * 
	 * @return the sound the source is playing
	 */
	public Sound getSound(){
		return sound;
	}
	
	/**
	 * Play the source's sound
	 */
	public void play(){
		//if already playing, do nothing
		if(play)
			return;
		//set the playback variables
		play = true;
		pause = false;
		
		//play the source in OpenAL
		alSourcePlay(id);
	}
	
	/**
	 * Stop the source's sound
	 */
	public void stop(){
		//if the source is already stopped, do nothing
		if(!play && !pause)
			return;
		//set the playback variables
		play = false;
		pause = false;
		//stop the source in OpenAL
		alSourceStop(id);
	}
	
	/**
	 * Pause the source's playback
	 */
	public void pause(){
		//if already paused, do nothing
		if(pause)
			return;
		//set the playback variable
		pause = true;
		//pause the source in OpenAL
		alSourcePause(id);
	}
	
	/** Get if the source is playing and not paused or stopped
	 * 
	 * @return if the source is playing and not paused or stopped
	 */
	public boolean isPlaying(){
		return play && !pause;
	}
	
	/** Get if the source is paused
	 * 
	 * @return if the source is paused
	 */
	public boolean isPaused(){
		return pause;
	}
	
	/** Get if the source is stopped
	 * 
	 * @return if the source is stopped
	 */
	public boolean isStopped(){
		return !play && !pause;
	}
	
	/** Set the gain for the source to play at
	 * 
	 * @param gain the gain for the source to play at
	 */
	public void setGain(float gain){
		this.gain = gain;
		//buffer the gain in OpenAL
		alSourcef(id, AL_GAIN, gain);
	}
	
	/** Get the gain of the source
	 * 
	 * @return the gain of the source
	 */
	public float getGain(){
		return gain;
	}
	
	/** Get the position of the source 
	 * 
	 * @return the position of the souce
	 */
	public Vector3f getPosition(){
		return position;
	}
	
	/** Set the position of the source
	 * 
	 * @param position the position of the source
	 */
	public void setPositon(Vector3f position){
		this.position.set(position);
		alSource3f(id, AL_POSITION, position.x, position.y, position.z);
	}
	
	/** Set the x and y position of the source
	 * 
	 * @param x x position of the source
	 * @param y y position of the source
	 */
	public void setPosition(float x, float y){
		this.position.x = x;
		this.position.y = y;
		alSource3f(id, AL_POSITION, position.x, position.y, position.z);

	}
	
	/** Set the x, y, and z position of the source
	 * 
	 * @param x x position of the source
	 * @param y y position of the source
	 * @param z z position of the source
	 */
	public void setPosition(float x, float y, float z){
		this.position.set(x,y,z);
		alSource3f(id, AL_POSITION, position.x, position.y, position.z);
	}
	
	/** Get the sample time of the source
	 * 
	 * @return the sample time of the source
	 */
	public int getSampleTime(){
		return sampleTime;
	}
	
	/** Get the OpenAL `id` of the source
	 * 
	 * @return the OpenAL `id` of the source
	 */
	public int getId(){
		return id;
	}
	
	/**
	 * Remove any reference of sound and delete the source in OpenAL
	 */
	public void destroy(){
		sound = null;
		alDeleteSources(id);
	}
}

