package io.tek256;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.tek256.core.runtime.Renderable;
import io.tek256.render.Animation;
import io.tek256.render.Texture;

public class Util {
	private static String lineSeparator = "";
	
	static{
		lineSeparator = System.getProperty("line.separator");
	}
	
	public static JsonObject toJson(Renderable renderable){
		JsonObject object = new JsonObject();
		
		object.add("texture", toJson(renderable.getEscapeTexture(), true));
		
		if(renderable.hasAnimations()){
			object.add("animations", toJson(renderable.getAnimations(), true));
		}
		
		object.addProperty("autoPlay", renderable.isAutoPlay());
		
		return object;
	}
	
	public static JsonArray toJson(Animation[] animations, boolean reference){
		JsonArray array = new JsonArray();
		for(Animation animation : animations)
			array.add(toJson(animation, reference));
		return array;
	}
	
	public static JsonObject toJson(Texture texture, boolean reference){
		JsonObject object = new JsonObject();
		
		if(reference){
			object.addProperty("path", texture.getPath());
			return object;
		}else{
			if(texture.getName() != null)
				object.addProperty("name", texture.getName());
			else
				object.addProperty("path", texture.getPath());
			
			if(texture.isSubTexture()){
				//sub textures tend to be named
				if(!object.has("path"))
					object.addProperty("path", texture.getSheet().getTexture().getPath());
				
				//show that this is a sub texture
				object.addProperty("subTexture", true);
				//show its sub id
				object.addProperty("subId", texture.getId());
			}
			
		}
		
		return object;
	}
	
	public static JsonObject toJson(Animation animation, boolean reference){
		JsonObject object = new JsonObject();
		
		object.addProperty("name", animation.getName());
		object.addProperty("texture", animation.getSource().getTexture().getPath());
		
		if(reference){
			return object;
		}else{
			object.addProperty("frameLength", animation.getFrameLength());
			object.addProperty("frames", animation.getFramesAsString());
			return object;
		}
	}
	
	public static String toString(Vector3f vec){
		return ""+vec.x+","+vec.y+","+vec.z;
	}
	
	public static String toString(Vector2f vec){
		return ""+vec.x+","+vec.y;
	}
	
	public static Vector3f getVec3(String string){
		String[] split = string.split(",");
			
		for(int i=0;i<2;i++){
			split[i] = split[i].trim();
			split[i] = split[i].replaceAll(",", "");
		}
			
		return new Vector3f(getFloat(split[0]), getFloat(split[1]), getFloat(split[2]));
	}
	
	public static Vector2f getVec2(String string){
		String[] split = string.split(",");
		
		for(int i=0;i<2;i++){
			split[i] = split[i].trim();
			split[i] = split[i].replaceAll(",", "");
		}
		
		return new Vector2f(getFloat(split[0]), getFloat(split[1]));
	}
	
	/**
	 * 
	 * @param map
	 * @return
	 */
	public static <K extends Comparable<? super K>, V> HashMap<K,V> sortByKey(Map<K,V> map){
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K,V>>(map.entrySet());
		list.sort((a,b)->{
			return (a.getKey().compareTo(b.getKey()));
		});
		
		HashMap<K,V> retMap = new HashMap<K,V>();
		for(Entry<K,V> entry : list){
			retMap.put(entry.getKey(), entry.getValue());
		}
		return retMap;
	}
	
	/** sort a map by the value of an entry
	 * 
	 * K = Key
	 * V = value (extends Comparable<? super V> = comparable to self class)
	 * @param map = generic map value, K & V are set by the map's entry classes
	 */
	public static <K, V extends Comparable<? super V>> HashMap<K,V> sortByValue(Map<K, V> map){
		//convert the entryset to a linked list (for lambda usage)
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		//lambda to compare each value to eachother
		list.sort((Map.Entry<K,V> e1, Map.Entry<K, V> e2)->{
			//compare each cast value to eachother and sort off of that
			return (e1.getValue()).compareTo(e2.getValue());
		});
		
		//construct a new hashmap for the sorted values
		HashMap<K,V> retMap = new HashMap<K,V>();
		//place the sorted values in the linkedlist back into the new hashmap
		for(Entry<K,V> entry : list){
			retMap.put(entry.getKey(), entry.getValue());
		}
		
		//return the new sorted hashmap
		return retMap;
	}
	
	public static int[] getInts(String str, String split){
		//the split up string
		String[] strSplit = str.split(split);
		//the list of interpreted integers 
		int[] ints = new int[strSplit.length];
		
		//for each split string
		for(int i=0;i<strSplit.length;i++){
			//trim any spaces from the ints
			strSplit[i].trim();
			//replace all of the regex characters
			strSplit[i] = strSplit[i].replaceAll(split, "");
			//get the int and put it in the array
			ints[i] = getInt(strSplit[i]);
		}
		//return the interpritations
		return ints;
	}
	
	public static <T> int firstIndexOf(T[] array, T object){
		for(int i=0;i<array.length;i++)
			if(array[i] == object)
				return i;
		return -1;
	}
	
	public static <T> int lastIndexOf(T[] array, T object){
		for(int i=array.length;i>0;i--)
			if(array[i] == object)
				return i;
		return -1;
	}
	
	public static <T> int occuranceIn(T[] array, T object){
		int c = 0;
		for(int i=0;i<array.length;i++)
			if(array[i] == object)
				c++;
		return c;
	}
	
	public static <T> T[] add(T[] ta, int index, T to){
		@SuppressWarnings("unchecked")
		T[] ncopy = (T[]) Array.newInstance(ta.getClass(), ta.length + 1);
		for(int i=0;i<ncopy.length;i++){
			if(i == index)
				ncopy[i] = to;
			else
				 ncopy[i] = ta[(i > index) ? i - 1 : i];
		}
		return ncopy;
	}
	
	public static <T> T[] remove(T[] ta, int index){
		@SuppressWarnings("unchecked")
		T[] ncopy = (T[]) Array.newInstance(ta.getClass(), ta.length - 1);
		for(int i=0;i<ta.length;i++){
			if(i != index)
				ncopy[(i > index) ? i - 1: i] = ta[i];
		}
		return ncopy;
	}
	
	public static <T>T[] merge(T[] t0, T[] t1){
		@SuppressWarnings("unchecked")
		T[] ncopy = (T[]) Array.newInstance(t0.getClass(), t0.length + t1.length);
		for(int i=0;i<ncopy.length;i++){
			if(i<t0.length)
				ncopy[i] = t0[i];
			else
				ncopy[i] = t1[i - t0.length];
		}
		return ncopy;
	}
	
	public static <T>T[] flip(T[] array){
		T[] tmp = array.clone();
		for(int i=0;i<array.length;i++){
			tmp[i] = array[array.length - i];
		}
		return tmp;
	}
	
	public static byte[] flip(byte[] array){
		byte[] tmp = array.clone();
		for(int i=0;i<array.length;i++)
			tmp[i] = array[array.length - 1];
		return tmp;
	}
	
	public static <T>T[] truncate(T[] array, int start, int end){
		@SuppressWarnings("unchecked")
		T[] narray = (T[]) Array.newInstance(array.getClass(), end - start);
		for(int i=start;i<Math.min(array.length, end);i++){
			narray[i] = array[i];
		}
		return narray;
	}
	
	public static byte[] truncate(byte[] array, int start, int end){
		byte[] narray = new byte[end - start];
		for(int i=start;i<Math.min(array.length, end);i++)
			narray[i] = array[i];
		return narray;
	}
	
	public static <T>T[] resize(T[] array, int newlen){
		return truncate(array, 0, newlen);
	}
	
	public static byte[] resize(byte[] array, int newlen){
		return truncate(array, 0, newlen);
	}
	
	public static <T>T[] copy(T[] array){
		return array.clone();
	}
	
	public static byte[] copy(byte[] array){
		return array.clone();
	}
	
	public static byte[] append(byte[] array, byte[] appendage){
		byte[] tmp = new byte[array.length + appendage.length];
		for(int i=0;i<array.length;i++)
			tmp[i] = array[i];
		for(int i=0;i<appendage.length;i++)
			tmp[i+array.length] = appendage[i];
		return tmp;
	}
	
	public static byte[] append(byte[] array, byte[] appendage, byte[] dest){
		dest = new byte[array.length + appendage.length];
		for(int i=0;i<array.length;i++)
			dest[i] = array[i];
		for(int i=0;i<appendage.length;i++)
			dest[i+array.length] = appendage[i];
		return dest;
	}
	
	public static String getExtension(String fileStr){
		return fileStr.substring(fileStr.lastIndexOf("."), fileStr.length());
	}
	
	public static String getNL(){
		return lineSeparator;
	}
	
	public static int getInt(String str){
		try{
			return Integer.parseInt(str);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		return -1;
	}
	
	public static float getFloat(String str){
		try{
			return Float.parseFloat(str);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		return -1f;
	}
	
	public static boolean getBool(String str){
		try{
			return Boolean.parseBoolean(str);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		return false;
	}
	
	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
	    ByteBuffer buffer;

	    Path path = Paths.get(resource);
	    if(Files.isReadable(path)) {
	        try(SeekableByteChannel fc = Files.newByteChannel(path)){
	            buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
	            while(fc.read(buffer) != -1);
	        }
	    }else{
	        try(InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
	            ReadableByteChannel rbc = Channels.newChannel(source)){
	            buffer = BufferUtils.createByteBuffer(bufferSize);
	            while(true){
	                int bytes = rbc.read(buffer);
	                if(bytes == -1)
	                    break;
	                if (buffer.remaining() == 0)
	                    buffer = resizeBuffer(buffer, buffer.capacity() * 2);
	            }
	        }
	    }
	    
	    buffer.flip();
	    return buffer;
	}
	
	public static ByteBuffer resizeBuffer(ByteBuffer buffer, int size){
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(size);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}
	
	public static <T> ArrayList<T> getSame(ArrayList<T> a, ArrayList<T> b){
		ArrayList<T> c = new ArrayList<>();
		
		if(a.size() > b.size()){
			for(int i=0;i<a.size();i++){
				if(b.contains(a.get(i)))
					c.add(a.get(i));
			}
		}else if(a.size() <= b.size()){
			for(int i=0;i<b.size();i++){
				if(a.contains(b.get(i)))
					c.add(b.get(i));
			}
		}
		
		return c;
	}
	
	public static <T> ArrayList<T> getSameSet(ArrayList<T> a, ArrayList<ArrayList<T>> b){
		ArrayList<T> c = new ArrayList<>();
		for(ArrayList<T> bb : b){
			if(a.size() > bb.size()){
				for(int i=0;i<a.size();i++)
					if(bb.contains(a.get(i)))
						c.add(a.get(i));
			}else if(a.size() <= bb.size()){
				for(int i=0;i<bb.size();i++)
					if(a.contains(bb.get(i)))
						c.add(bb.get(i));
			}
		}
		return c;
	}
}
