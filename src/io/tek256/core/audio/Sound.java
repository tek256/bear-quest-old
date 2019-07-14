package io.tek256.core.audio;

import java.io.File;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;

import io.tek256.Util;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;

public class Sound {
	private String path;
	
	private ShortBuffer buffer;
	private int id,channels,sampleRate,length;
	
	public Sound(String sound){
		this.path = sound;
		load();
	}
	
	private void load(){
		//create an openal buffer for the sound
		id = alGenBuffers();
		
		//try and create the stbvorbis loading info
		try(STBVorbisInfo info = STBVorbisInfo.malloc()){
			//size of the file in bytes
			long size = new File(path).length();
			//bytebuffer of the file
			ByteBuffer vorbis = Util.ioResourceToByteBuffer(path, (int)size);
			
			//error buffer (c compatibility)
			IntBuffer error = BufferUtils.createIntBuffer(1);
			//decoder handle
			long decoder = stb_vorbis_open_memory(vorbis, error, null);
			
			//if the decoder was unable to be created 
			if(decoder == 0L) 
				//throw the most generic error ever just to piss anyone else off 
				throw new RuntimeException("Decoder error");
			
			//get the info of the buffer
			stb_vorbis_get_info(decoder, info);
			
			//set the channel count
			channels = info.channels();
			//length in samples
			length = stb_vorbis_stream_length_in_samples(decoder);
			//sample rate of the file 
			sampleRate = info.sample_rate();
			
			//short buffer representation of the sound
			buffer = BufferUtils.createShortBuffer(length);
			
			//set the limit of the buffer
			buffer.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, buffer) * channels);
			
			//close stb vorbis
			stb_vorbis_close(decoder);
			
			//buffer the data of the file into OpenAL based on the channel count
			alBufferData(id, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, buffer, info.sample_rate());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/** Get the rate the sound plays sample per second
	 * 
	 * @return the rate samples play per second
	 */
	public int getSampleRate(){
		return sampleRate;
	}
	
	/** Get the length of the sound in samples
	 * 
	 * @return the length of the sound in samples
	 */
	public int getLength(){
		return length;
	}
	
	/** Get the amount of channels the sound has
	 * 
	 * @return the amount of channels the sound has
	 */
	public int getChannels(){
		return channels;
	}
	
	/** Get the OpenAL ID of the sound
	 * 
	 * @return the OpenAL ID of the sound
	 */
	public int getId(){
		return id;
	}
	
	/** Get the path of the sound file
	 * 
	 * @return the path of the sound file
	 */
	public String getPath(){
		return path;
	}
	
	/**
	 * Destroy the OpenAL buffer of the sound
	 */
	public void destroy(){
		alDeleteBuffers(id);
	}
}
