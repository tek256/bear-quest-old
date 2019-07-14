package io.tek256.core.audio;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.tek256.ResourceLoader;
import io.tek256.Util;

public class Audio {
	private static Audio instance = null;
	
	private long context,device;
	
	private ALCCapabilities deviceCaps;
	
	private HashMap<String, Sound> sounds;
	
	private FloatBuffer posBuffer,oriBuffer;
	private Vector3f listenerPosition,listenerOrientation;
	private float listenerGain = 0.8f;
	
	public Audio(){
		initialize();
	}
	
	private void initialize(){
		//get the default audio device
		device = alcOpenDevice((ByteBuffer) null);
		
		if(device == 0L)
			throw new RuntimeException("Unable to open default audio device");
		
		//get the capabilities of the default audio device
		deviceCaps = ALC.createCapabilities(device);
		
		//setup the openal context
		context = alcCreateContext(device, (IntBuffer)null);
		if(context == 0L)
			return;
		
		//make the context the primary focus
		alcMakeContextCurrent(context);
		//create OpenAL capabilities for the device 
		AL.createCapabilities(deviceCaps);

		//setup the distance model so the sounds get attenuated based on the distance
		alDistanceModel(AL11.AL_LINEAR_DISTANCE);
	
		
		//create the position buffer to send to openal
		posBuffer = BufferUtils.createFloatBuffer(3);
		
		//create the orientation buffer to send to openal
		oriBuffer = BufferUtils.createFloatBuffer(6);
		
		//i should probably set some range on the sources lol
		
		//setup the listener's position
		listenerPosition = new Vector3f();
		
		//setup the listener's orientation
		listenerOrientation = new Vector3f(0f,0f,-1f);
		
		//update the listener values in openal
		updateListener();
		
		//create the map for sounds
		sounds = new HashMap<String, Sound>();
		
		//reference this as the single instance of audio
		instance = this;
	}
	
	/**
	 * Update the listener values in OpenAL
	 */
	private void updateListener(){
		//clear the buffer before putting more elemnets in it
		posBuffer.clear();
		
		//setup the position buffer
		posBuffer.put(new float[]{
			listenerPosition.x,	
			listenerPosition.y,	
			listenerPosition.z,	
		});
		
		posBuffer.flip();

		//clear the buffer before adding more elements to it
		oriBuffer.clear();
		
		//add the orientation, and then up
		oriBuffer.put(new float[]{ 
				listenerOrientation.x,
				listenerOrientation.y,
				listenerOrientation.z,
				0f, 1f, 0f}); //up
		oriBuffer.flip();
		
		//send the orientation to openal
		alListenerfv(AL_ORIENTATION, oriBuffer);
		
		//tell openal the position
		alListenerfv(AL_POSITION, posBuffer);
		
	}
	
	/** Set the position of the listener in OpenAL
	 * 
	 * @param vector the position of the listener
	 */
	public void setListenerPosition(Vector3f vector){
		listenerPosition.set(vector);
		updateListener();
	}
	
	/** Get the position of the listener
	 * 
	 * @return the position of the listener
	 */
	public Vector3f getListenerPosition(){
		return listenerPosition;
	}
	
	/** Set the gain value in OpenAL
	 * 
	 * @param gain the gain value
	 */
	public void setGain(float gain){
		listenerGain = gain;
		alListenerf(AL_GAIN, listenerGain);
	}
	
	/** Get the gain value in OpenAL
	 * 
	 * @return the gain in OpenAL
	 */
	public float getGain(){
		return listenerGain;
	}
	
	
	public void update(Sound sound){
		//remove all instances of the sound from the map
		sounds.values().removeAll(Collections.singleton(sound));
	}
	
	private void add(String path){
		sounds.put(path, new Sound(path));
	}
	 
	/**
	 * Destroy the OpenAL Context
	 * This includes the device and OpenAL handles
	 */
	public void destroy(){
		alcCloseDevice(device);
		alcDestroyContext(context);
		ALC.destroy();
	}
	
	
	/** Load audio library from path to json library file
	 * 
	 * @param path path to json library file
	 */
	public static void loadLibrary(String path){
		//if the audio instance doesn't exist, do nothing
		if(instance == null)
			return;
		
		//open a json parser 
		JsonParser parser = new JsonParser();
		//get the root element for json (parse)
		JsonElement rootElement = parser.parse(ResourceLoader.getString(path));
		//make the root an accessible object
		JsonObject root = rootElement.getAsJsonObject();
		
		//get the array of mappings in json
		JsonArray mappings = root.get("sounds").getAsJsonArray();
		//for each mapping in the array
		for(int i=0;i<mappings.size();i++){
			//get the mapping as an individual object
			JsonObject mapping = mappings.get(i).getAsJsonObject();
			//get the 2 strings we need (path & name)
			String mappingPath = mapping.get("path").getAsString();
			String mappingName = mapping.get("name").getAsString(); 
			//add the sound to the instance
			if(!mappingName.equals("") && mappingName != null){
				//instance.add(mappingPath, mappingName);
			}else{
				instance.add(mappingPath);
			}
			
		}
	}
	
	/** Load a directory of audio files
	 * 
	 * @param path directory of audio files 
	 */
	public static void loadAll(String path){
		//if there is no instance, then do nothing
		if(instance == null)
			return;
		
		//source of the files
		File root = new File(path);
		//if the file doesn't exist, do return
		if(!root.exists() || root.isFile())
			return;
		
		//go thru each file listed
		for(File listing : root.listFiles()){
			if(isFileCompatible(Util.getExtension(listing.getPath()))){
				instance.sounds.put(listing.getPath(), new Sound(listing.getPath()));
			}
		}
	}
	 
	/** check if the extension string of a file is compatible with the 
	 *  files we want to load
	 *  
	 * @param extension the extension string of a file
	 * @return
	 */
	public static boolean isFileCompatible(String extension){
		//TODO add real audio support
		return extension.toLowerCase().equals("ogg");
	}
	
	/** Get a sound by `name` or `path`
	 * 
	 * @param name `name` or `path` of a sound
	 * @return Sound by name or path
	 */
	public static Sound getSound(String name){
		//if there is no audio instance, nothing is loaded
		if(instance == null) 
			return null;
		//if the instance has a sound by the name
		if(instance.sounds.containsKey(name))
			//get the instance's sound
			return instance.sounds.get(name);
		//otherwise, return nothing
		return null;
	}
	
	/** Get the audio instance
	 * 
	 * @return the audio instance
	 */
	public static Audio getInstance(){
		return instance;
	}
}
