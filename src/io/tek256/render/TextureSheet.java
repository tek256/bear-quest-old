package io.tek256.render;

import java.util.HashMap;
import java.util.Map.Entry;

import org.joml.Vector2f;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.tek256.ResourceLoader;
import io.tek256.Util;

public class TextureSheet {
	private static HashMap<String, TextureSheet> sheets;
	
	//statically initialize the sheets
	static{
		sheets = new HashMap<>();
	}
	
	//source texture image
	private Texture src;
	//name of the sheet
	private String name;
	//sub texture sizes
	private int subWidth, subHeight;
	//amount of sub textures
	private int perWidth, perHeight;
	//vector2f representation of size
	private Vector2f subSize;
	//texture instances of sub textures
	private Texture[] subTextures;
	//private name map for sub texture instances
	private HashMap<String,Integer> subMap;
	//map of all of the named animations
	private HashMap<String, Animation> animationMap;
	
	
	public TextureSheet(Texture texture, int subWidth, int subHeight){
		//setup the custom map for sub textures
		subMap = new HashMap<>();
		
		src = texture;
		
		if(src == null)
			System.out.println("null");
		
		src.setTextureSheet(this);
		
		this.subWidth = subWidth;
		this.subHeight = subHeight;
		
		subSize = new Vector2f(subWidth, subHeight);
		
		//calculate the amount of sub textures
		perWidth = src.getWidth() / subWidth;
		perHeight = src.getHeight() / subHeight;

		//create the sub textures array
		subTextures = new Texture[perWidth * perHeight];
		
		//create each sub texture
		for(int i=0;i<subTextures.length;i++)
			subTextures[i] = new Texture(this, i);
		
		//setup the map for the texture mappings
		subMap = new HashMap<>();
		
		//setup the map for animations
		animationMap = new HashMap<>();
		
		perWidth = texture.getWidth() / subWidth;
		
		//put the sheet in the sheet map
		if(name != null) //names are optional
			sheets.put(name, this);
		sheets.put(src.getPath(), this); //list it by the texture as well
	}
	
	/** Create a texture sheet from json file
	 * 
	 * @param path path to json file for texture sheet
	 */
	public TextureSheet(String path){
		//setup the custom map for sub textures
		subMap = new HashMap<>();
		
		//setup the json parser
		JsonParser parser = new JsonParser();
		//get the json element from the parser
		JsonElement rootElement = parser.parse(ResourceLoader.getString(path));
		//cast the element to a usable json element
		JsonObject root = rootElement.getAsJsonObject();
		
		//get the path of the source texture from the json attribute `path`
		src = Texture.getTexture(root.get("path").getAsString());
		
		if(src == null)
			System.out.println("null");
		
		src.setTextureSheet(this);
		
		//get the name of the texture sheet from the json attribute `name`
		name = root.get("name").getAsString();

		//get the sub width of the sheet from the json attribute `width`
		subWidth = root.get("width").getAsInt();
		//get the sub height of the sheet from the json attribute `height`
		subHeight = root.get("height").getAsInt();
		
		subSize = new Vector2f(subWidth, subHeight);
		
		//calculate the amount of sub textures
		perWidth = src.getWidth() / subWidth;
		perHeight = src.getHeight() / subHeight;

		//create the sub textures array
		subTextures = new Texture[perWidth * perHeight];
		
		//create each sub texture
		for(int i=0;i<subTextures.length;i++)
			subTextures[i] = new Texture(this, i);
		
		//setup the map for the texture mappings
		subMap = new HashMap<>();
		
		//get the mappings arrays
		JsonArray mappings = root.get("mappings").getAsJsonArray();
		//go thru each mapping
		for(int i=0;i<mappings.size();i++){
			//get the json object in the array
			JsonObject mapping = mappings.get(i).getAsJsonObject();
			//get the id listed in the mapping
			int id = mapping.get("id").getAsInt();
			//get the name listed in the mapping
			String name = mapping.get("name").getAsString();
			
			//name the sub texture
			subTextures[id].name(name);
			//put the sub texture in the sub map
			subMap.put(name, id);
		}
		

		//setup the map for animations
		animationMap = new HashMap<>();
		
		//if the sheet has an animation array
		if(root.has("animations")){
			//get the animations array
			JsonArray animations = root.get("animations").getAsJsonArray();
			
			//for each animation in the json array
			for(int i=0;i<animations.size();i++){
				//get the listing in the array
				JsonObject animation = animations.get(i).getAsJsonObject();
				//get the name of the animation
				String name = animation.get("name").getAsString();
				//get the frames as string
				String frames = animation.get("frames").getAsString();
				//get the frame length listed 
				float frameLength = animation.get("frameLength").getAsFloat();
				//get the frames converted
				int[] framesConv = Util.getInts(frames, ",");
				
				//create the new array
				Animation anim = new Animation(this, name, frameLength, framesConv);
				
				//add the animation to the map
				animationMap.put(name, anim);
			}
		}
		
		//put the sheet in the sheet map
		if(name != null) //names are optional
			sheets.put(name, this);
		sheets.put(path, this); //but the sheet by path as well
		sheets.put(src.getPath(), this); //list it by the texture as well
	}
	
	/** Get sub texture by index
	 * 
	 * @param index sub texture index
	 * @return sub texture by index
	 */
	public Texture get(int index){
		return subTextures[index];
	}
	
	/** get sub texture by 2d mapping
	 * 
	 * @param x x axis mapping
	 * @param y y axis mapping
	 * @return sub texture by 2d mapping
	 */
	public Texture get(int x, int y){
		return get(x + (y * perWidth));
	}
	
	/** Get sub texture by 2d coordinate
	 * 
	 * @param x x coordinate 
	 * @param y y coordinate
	 * @return sub texture by 2d coordinate
	 */
	public Texture getAt(int x, int y){
		x -= (x % subWidth);
		y -= (y % subHeight);
		x /= subWidth;
		y /= subHeight;
		return subTextures[x + (y * perWidth)];
	}
	
	/** Get the amount of sub textures along the x axis
	 * 
	 * @return the amount of sub textures along the x axis
	 */
	public int getPerWidth(){
		return perWidth;
	}
	
	/** Get the amount of sub textures along the y axis
	 * 
	 * @return the amount of sub textures along the y axis
	 */
	public int getPerHeight(){
		return perHeight;
	}
	
	/** Get the width of sub textures
	 * 
	 * @return the width of sub textures
	 */
	public int getSubWidth(){
		return subWidth;
	}
	
	/** Get the height of sub textures
	 * 
	 * @return the height of sub textures
	 */
	public int getSubHeight(){
		return subHeight;
	}
	
	/** Get the sub size in Vector2f form
	 * 
	 * @return the sub size in Vector2f form
	 */
	public Vector2f getSubSize(){
		return subSize;
	}
	
	/** Get the width of the source texture
	 * 
	 * @return the width of the source texture
	 */
	public int getWidth(){
		return src.getWidth();
	}
	
	/** Get the height of the source texture
	 * 
	 * @return the height of the source texture
	 */
	public int getHeight(){
		return src.getHeight();
	}
	
	/** Get the source texture
	 * 
	 * @return the source texture
	 */
	public Texture getTexture(){
		return src;
	}
	
	/** Set the new name of the texture sheet
	 * 
	 * @param name new name of the texture sheet
	 */
	public void setName(String name){
		if(this.name == null){
			this.name = name;
			return;
		}
		
		//if there is no change in the name don't update
		if(this.name.equals(name))
			return;
		//remove previous name of sheet
		sheets.remove(this.name);
		//put updated listing
		sheets.put(name, this);
		//update the local variable
		this.name = name;
	}
	
	/** Set the new name of a sub texture
	 * 
	 * @param id sub texture index
	 * @param name new name of the sub texture
	 */
	public void setSubName(int id, String name){
		//if the name isn't changed, don't waste time
		if(name == subTextures[id].getName())
			return;
		//remove the exusting name for the sub texture
		subMap.remove(subTextures[id].getName());
		//update the sub map with the new name
		subMap.put(name, id);
		//update the sub texture object with the new name
		subTextures[id].name(name);
	}
	
	/** Get the name of the texture sheet
	 * 
	 * @return name of the texture sheeet
	 */
	public String getName(){
		return name;
	}
	
	public boolean hasAnimation(String name){
		return animationMap.containsKey(name);
	}
	
	public Animation getAnimation(String name){
		return animationMap.get(name);
	}
	
	public int getAnimationIndex(Animation animation){
		int i = 0;
		for(Entry<String, Animation> entry : animationMap.entrySet()){
			if(entry.getValue() == animation)
				return i;
			i++;
		}
		return -1;
	}
	
	/** set the list of animations
	 * 
	 * @param animations list of animations
	 */
	public void setAnimations(Animation[] animations){
		animationMap.clear();
		for(Animation animation : animations)
			animationMap.put(animation.getName(), animation);
	}
	
	/** Add an animation to the map
	 * 
	 * @param animation animation to add to the map
	 */
	public void addAnimation(Animation animation){
		animationMap.put(animation.getName(), animation);
	}
	
	public HashMap<String, Integer> getNames(){
		return subMap;
	}
	
	public HashMap<String, Animation> getAnimations(){
		return animationMap;
	}
	
	/**
	 * output mappings of sub textures by key:id
	 */
	public void outputMappings(){
		System.out.println("MAPPINGS:");
		//go thru each key
		for(String k : subMap.keySet())
			//output the key and its value
			System.out.println(k+":"+subMap.get(k));
		
		System.out.println("ANIMATIONS:");
		for(String k : animationMap.keySet())
			System.out.println(k + ":"+animationMap.get(k).getFramesAsString());
	}
	
	/** Get the map of sub textures
	 * 
	 * @return the map of sub textures
	 */
	public HashMap<String,Integer> getSubMap(){
		return subMap;
	}
	
	/** Get a sub mapping value by name
	 * 
	 * @param name name of a mapping
	 * @return value of the mapping
	 */
	public int getIndex(String name){
		return subMap.get(name);
	}
	
	/** Save the texture mappings to file
	 * 
	 * @param path path of file to save
	 */
	public void saveMappings(String path){
		//sort the sub map by id
		subMap = Util.sortByValue(subMap);
		
		//setup the json writter with pretty printing
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		//create the root object for all of the mappings
		JsonObject root = new JsonObject();
		
		//set the name
		root.addProperty("name", name);
		//set the parse type
		root.addProperty("type", "spritesheet");
		//set the path of the image
		root.addProperty("path", src.getPath());
		//set the width
		root.addProperty("width", subWidth);;
		//set the height
		root.addProperty("height", subHeight);
		
		//create the json mappings array
		JsonArray mappings = new JsonArray();
		for(Entry<String,Integer> mapping : subMap.entrySet()){
			//create the mapping in json
			JsonObject jsonMapping = new JsonObject();
			//set the id of the mapping in json
			jsonMapping.addProperty("id", mapping.getValue());
			//set the name of the mapping in json
			jsonMapping.addProperty("name", mapping.getKey());
			//add the json mapping to the mappings array
			mappings.add(jsonMapping);
		}

		//add the mappings array to the root
		root.add("mappings", mappings);
		
		
		//sort the animation map by name
		animationMap = Util.sortByKey(animationMap);
		
		JsonArray animations = new JsonArray();
		for(Entry<String, Animation> mapping : animationMap.entrySet()){
			//create the json mapping
			JsonObject jsonMapping = new JsonObject();
			//add the name
			jsonMapping.addProperty("name", mapping.getKey());
			//add the frame length
			jsonMapping.addProperty("frameLength", mapping.getValue().getFrameLength());
			//add the frames
			jsonMapping.addProperty("frames", mapping.getValue().getFramesAsString());
			//add the texturesheet name
			jsonMapping.addProperty("textureSheet", mapping.getValue().getSource().name);
			//add the listing to the array
			animations.add(jsonMapping);
		}
		//add the animation array to the root
		root.add("animations", animations);
		
		//convert the json root to text and write it
		ResourceLoader.writeString(path, gson.toJson(root));
	}
	
	/** Get the texture sheet map
	 * 
	 * @return the texture sheet map
	 */
	public static HashMap<String, TextureSheet> getSheetMap(){
		return sheets;
	}
	
	/** Check if a base texture is contained by any of the defined sheets
	 * 
	 * @param texture base texture to check
	 * @return if the base texture is contained by any of the sheets
	 */
	public static boolean isSheet(Texture texture){
		for(TextureSheet sheet : sheets.values()){
			if(sheet.getTexture() == texture){
				return true;
			}
		}
		return false;
	}
	
	/** Get the texture sheet by base texture
	 * 
	 * @param texture base texture to check
	 * @return the texture sheet with the same base texture
	 */
	public static TextureSheet getSheet(Texture texture){
		for(TextureSheet sheet : sheets.values()){
			if(sheet.getTexture() == texture){
				return sheet;
			}
		}
		return null;
	}

	/** Get the sheet in the sheet map by name
	 * 
	 * @param sheet sheet name 
	 * @return the sheet in the sheet map by name
	 */
	public static TextureSheet getSheet(String sheet){
		return sheets.get(sheet);
	}
}
