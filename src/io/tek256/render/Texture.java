package io.tek256.render;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.joml.Vector2f;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import io.tek256.ResourceLoader;
import io.tek256.core.GameEngine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class Texture {
	/*
	 * TEXTURE MAP USAGE:
	 * Textures maintain 2 aliases (keys):
	 * 		* Texture Path (unchangable)
	 * 		* Texture Name (changable)
	 * 	For ease of access, texture name has been added
	 *  however the policy is that a texture cannot maintain
	 *  more than 1 name. This is to remove any confusion and 
	 *  keep texture namespaces clean
	 */
	private static HashMap<String,Texture> textureMap;

	//staticially initialize the texture map
	static{
		textureMap = new HashMap<>();
	}
	
	//important variables
	private int id,width,height;
	
	private String path,name;
	
	//for memory efficiency when calling getSize()
	private Vector2f size;
	
	//parent texture sheet
	private TextureSheet sheet;
	
	private boolean isSubTexture = false, isSheet = false;
	
	//for shader usage
	private Vector2f offset;
	
	public Texture(String path){
		try{
			//get the raw file as an input stream
			InputStream in = ResourceLoader.getFileStream(path);
			
			//send the inputstream to pngdecoder
			PNGDecoder decoder = new PNGDecoder(in);
			
			//set the texture path
			this.path = path;
			
			//set the size variables
			width = decoder.getWidth();
			height = decoder.getHeight();
			
			size = new Vector2f(width, height);
			
			//allocate the byte buffer for decoding
			ByteBuffer buf = ByteBuffer.allocateDirect(4* width * height);
			
			//decode the byte buffer
			decoder.decode(buf, width * 4, Format.RGBA);
			
			//flip the buffer for reading
			buf.flip();
			//close the input stream for memory management
			in.close();
			
			//buffer the decoded texture data
			buffer(buf);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void buffer(ByteBuffer buffer){
		//ensure that textures are enabled
		glEnable(GL_TEXTURE_2D);
		
		//generate an id for the texture
		id = glGenTextures();
		
		//bind the texture in opengl
		glBindTexture(GL_TEXTURE_2D, id);
		
		//set the texture to clamp to a given mesh
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		//set the texture filter to nearest (pixel art)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		//buffer the data from the buffer into opengl buffers
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		//release the texture from focus in opengl
		glBindTexture(GL_TEXTURE_2D, 0);
		
		//place the texture in the map for ease of access
		textureMap.put(path, this);
	}
	
	public Texture(TextureSheet sheet, int id){
		//set the sheet variables
		this.sheet = sheet;
		this.id = id;
		
		isSubTexture = true;
	
		width = sheet.getSubWidth();
		height = sheet.getSubHeight();
		
		//calculate the offset
		offset = new Vector2f(width * (id % sheet.getPerWidth()),
				height * (id / sheet.getPerWidth()));
	}
	
	/** Set the texture as a reference to a texture sheet
	 * 
	 * @param sheet sheet the texture is a reference to
	 */
	public void setTextureSheet(TextureSheet sheet){
		this.sheet = sheet;
		isSheet = true;
	}
	
	/** Get the offset of a sub texture
	 *  WARNING: Value is Vector2f.ZERO if not a sub texture
	 * @return the offset of the sub texture (Vector2f.ZERO if not a sub texture)
	 */
	public Vector2f getOffset(){
		if(!isSubTexture)
			return Vector2f.ZERO;
		return offset;
	}
	 
	/** Assign a name to the texture in the textureMap
	 * 
	 * @param name name of the texture
	 */
	public void name(String name){
		if(this.name == null){
			//set the name
			this.name = name;
			//insert updated name
			textureMap.put(name, this);
			return;
		}else if(this.name.equals(name)){
			return;
		}
		//remove past name
		textureMap.remove(this.name);
		//insert updated name
		textureMap.put(name, this);
		//rename local string
		this.name = name;
	}
	
	/** Get the name of the texture as seen in the static textureMap
	 * 
	 * @return name of the texture as seen in the static textureMap
	 */
	public String getName(){
		return name;
	}
	
	/** Get the information of the texture as string
	 *  WARNING: Only for debugging/monitor usage
	 * @return
	 */
	public String getInfo(){
		return "id="+this.id+"width="+this.width+" height="+this.height;
	}
	
	/** Get the path of the texture
	 *  WARNING: the path is set to the texture map parent if 
	 *  	the texture is a sub texture.
	 * @return the path of the texture
	 */
	public String getPath(){
		return path;
	}
	
	/** Bind the texture to opengl
	 *  WARNING: If the texture is a sub texture the parent
	 *  texture sheet will be bound.
	 */
	public void bind(){
		glBindTexture(GL_TEXTURE_2D, (isSubTexture) ? sheet.getTexture().getId() : id);
	}

	/**
	 * Delete the texture in OpenGL
	 */
	public void destroy(){
		if(!isSubTexture) //if a sub texture there is no inherent texture present
			glDeleteTextures(this.id);
	}
	
	/** Get the width of the texture
	 * 
	 * @return the width of the texture
	 */
	public int getWidth(){
		return (isSubTexture) ? sheet.getSubWidth() : width;
	}
	
	/** Get the height of the texture
	 * 
	 * @return the height of the texture
	 */
	public int getHeight(){
		return (isSubTexture) ? sheet.getSubHeight() : height;
	}
	
	/** Get if the texture is a sub texture
	 * 
	 * @return if the texture is a sub texture
	 */
	public boolean isSubTexture(){
		return isSubTexture;
	}
	
	/** Get if the texture is a texturesheet
	 * 
	 * @return if the texture is a texturesheet
	 */
	public boolean isSheet(){
		return isSheet;
	}
	
	/** Get the parent texture sheet
	 *  WARNING: If the texture is not a sub texture
	 *  the return value will be null
	 * 
	 * @return the parent texture sheet
	 */
	public TextureSheet getSheet(){
		return (isSubTexture || isSheet) ? sheet : null;
	}
	
	/** Get the size of the texture in vector2f form
	 * 
	 * @return the size of the texture in vector2f form
	 */
	public Vector2f getSize(){
		return (isSubTexture) ? sheet.getSubSize() : size;
	}
	
	/** Get the id of the texture in OpenGL
	 *  WARNING: If the texture is a sub texture then 
	 *  it will return the relative id to the parent 
	 *  texture.
	 * @return the id of the texture in OpenGL
	 */
	public int getId(){
		return id;
	}
	
	@Override
	public int hashCode(){
		int result = this.id;
		result = 31 * result + (this.width != 0f ? Float.floatToIntBits(this.width) : 0);
		result = 31 * result + (this.height != 0f ? Float.floatToIntBits(this.height) : 0);
		result = 31 * result + (this.path.length() != 0 ? this.path.hashCode() : 0);
		if(this.offset != null)
			result = 31 * result + this.offset.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(!o.getClass().equals(Texture.class))
			return false;
		
		Texture ot = (Texture)o;
		return ot.id == this.id && ot.isSubTexture == ot.isSubTexture
				&& ot.width == this.width
				&& ot.height == this.height && ot.path == this.path 
				&& ot.offset == this.offset;
	}

	
	/**
	 * Unbind the OpenGL Texture
	 */
	public static void unbind(){
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	/** Get the static texture map
	 *  TextureMap usage <K,V> -> <path/name, texture>
	 * 
	 * @return the whole texture map
	 */
	public static HashMap<String,Texture> getMap(){
		return textureMap;
	}
	
	/** Get a texture by path/name
	 * 	Note: Path can be substituted for name
	 *  WARNING: If name is used, no new texture will be created to
	 *  return a non-null texture
	 * @param path string key value to get texture by (can be name)
	 * @return Texture object
	 */
	public static Texture getTexture(String path){
		//if the texture map contains a texture by the key
		if(textureMap.containsKey(path)){
			//return the texture from the map
			return textureMap.get(path);
		}else if(new File(path).exists()){ //otherwise, if it exists as a file
			//return a new texture from the file
			return new Texture(path);
		}
		//if nothing else, return null
		return null;
	}
	
	/** Place a texture in the static map
	 * 
	 * @param path Key to place texture as (usually path)
	 * @param texture Texture object
	 */
	public static void put(String path, Texture texture){
		textureMap.put(path, texture);
	}

	public static void outputMap(){
		HashMap<Texture, ArrayList<String>> formatting = new HashMap<Texture, ArrayList<String>>();
		
		for(Entry<String, Texture> entry : textureMap.entrySet()){
			if(formatting.containsKey(entry.getValue())){
				formatting.get(entry.getValue()).add(entry.getKey());
			}else{
				ArrayList<String> list = new ArrayList<String>();
				list.add(entry.getKey());
				formatting.put(entry.getValue(), list);
			}
		}
		
		for(Entry<Texture, ArrayList<String>> entry : formatting.entrySet()){
			StringBuilder out = new StringBuilder();
			//add each string to the builder
			entry.getValue().forEach((str)-> out.append(str + ", "));
			System.out.println(entry.getKey().getPath()+" : {"+out.toString()+"}");
		}
	}
	
	/** Load all from a directory to a list of textures (??)
	 * 
	 * @param path directory to a list of textures 
	 */
	public static void loadAll(String path){
		File root = new File(path);
		if(!root.isDirectory()){
			System.out.println("directory: "+path+" does not exist");
			return;
		}
		
		//create a waitlist with file attributes 
		HashMap<File, String[]> waitlist = new HashMap<File, String[]>();
		
		//for each file in the directory
		for(File file : root.listFiles()){
			//set the name (typically "test.file")
			String unfiltered = file.getName();
			//split after the period
			String extension = unfiltered.substring(unfiltered.lastIndexOf("."), unfiltered.length());
			//split before the period
			String name = unfiltered.substring(0, unfiltered.lastIndexOf("."));
			
			if(extension.toLowerCase().contains("png")){ //individual texture
				if(!textureMap.containsKey(file.getPath())){
					//create the texture 
					Texture tex = new Texture(file.getPath());
					
					//set the name in the texture map
					tex.name(name);
					
					//place the file in the texture map
					textureMap.put(file.getPath(), tex);
				}
			}else if(extension.toLowerCase().equals(".json")){//texture sheet
				//load the texture sheet
				String[] info = processJson(file.getPath());
				
				//if the texture map has a listing with the same addresses
				if(textureMap.containsKey(info[0]) || textureMap.containsKey(info[1])){
					new TextureSheet(file.getPath());
				}else{ //if not, place the file in the waitlist for final processing
					waitlist.put(file, info);
				}
			}else{
				//output so we know if there are unknown files
				System.out.println("unknown file: "+file.getPath());
			}
		}
		
		//for each entry in the waitlist
		for(Entry<File, String[]> entry : waitlist.entrySet()){
			//get the stored info
			String[] info = entry.getValue();
			//check to see if a texture of the sort has been loaded
			if(textureMap.containsKey(info[0]) || textureMap.containsKey(info[1])){
				//if so, process the json file
				new TextureSheet(entry.getKey().getPath());
			}
		}
		
		//clean out the waitlist
		waitlist.clear();
		
		if(GameEngine.DEBUG)
			//output the map
			outputMap();
	}
	
	private static String[] processJson(String path){
		String[] attribs = new String[2];
		
		//setup the json parser
		JsonParser parser = new JsonParser();
		
		//get the json element from the parser
		JsonElement rootElement = parser.parse(ResourceLoader.getString(path));
		
		//cast the element to a usable json element
		JsonObject root = rootElement.getAsJsonObject();
		
		//add the attributes to the array
		attribs[0] = root.get("name").getAsString();
		attribs[1] = root.get("path").getAsString();
		
		
		//send back the attributes
		return attribs;
	}
	
	public static String getAliases(Texture texture){
		StringBuilder aliases = new StringBuilder();
		for(Entry<String, Texture> entry : textureMap.entrySet()){
			if(entry.getValue() == texture)
				aliases.append(entry.getKey()+",");
		}
		//get rid of the stray comma
		return aliases.substring(0, aliases.length()-1);
	}
	
}