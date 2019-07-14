package io.tek256.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.CallbackI.V;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.tek256.ResourceLoader;
import io.tek256.Util;
import io.tek256.core.runtime.GameObject;
import io.tek256.core.runtime.LevelObject;
import io.tek256.core.runtime.Renderable;
import io.tek256.core.runtime.Transform;
import io.tek256.render.Animation;
import io.tek256.render.Shader;
import io.tek256.render.Texture;
import io.tek256.render.TextureSheet;

public class EditorSpace {
	public static final float tileSize = 16f;
	
	//runtime only
	public ArrayList<GameObject> gameObjects;
	public ArrayList<LevelObject> levelObjects;
	
	//tile size
	private int width,height;
	private int tileWidth,tileHeight;
	
	public EditorSpace(int width, int height){
		//set sizes
		this.width = width; //px 
		this.height = height;
		
		//runtime
		gameObjects = new ArrayList<GameObject>();
		levelObjects = new ArrayList<LevelObject>();
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getTileWidth(){
		return tileWidth;
	}
	
	public int getHeight(){
		return height;
	}
	
	public int getTileHeight(){
		return tileHeight;
	}

	public void load(String path){
		JsonParser parser = new JsonParser();
		JsonObject root = parser.parse(ResourceLoader.getString(path)).getAsJsonObject();
		
		JsonArray textures = root.get("textures").getAsJsonArray();
		ArrayList<Texture> texturesLoaded = new ArrayList<>();
		ArrayList<TextureSheet> textureSheetsLoaded = new ArrayList<>();
		
		for(int i=0;i<textures.size();i++){
			JsonObject textureListing = textures.get(i).getAsJsonObject();
			
			String texturePath = textureListing.get("path").getAsString();
			
			boolean isSheet = textureListing.get("sheet").getAsBoolean();
			
			Texture texture = new Texture(texturePath);
			
			if(textureListing.has("name")){
				texture.name(textureListing.get("name").getAsString());
			}
			
			texturesLoaded.add(texture);
			
			if(isSheet){
				int subWidth = textureListing.get("subWidth").getAsInt();
				int subHeight = textureListing.get("subHeight").getAsInt();
				
				textureSheetsLoaded.add(new TextureSheet(texture, subWidth, subHeight));
			}
		}
		
		JsonArray textureSheets = root.get("textureSheets").getAsJsonArray();
		
		for(int i=0;i<textureSheets.size();i++){
			JsonObject textureSheetListing = textureSheets.get(i).getAsJsonObject();
			
			//texture path
			String texture = textureSheetListing.get("texture").getAsString();
			
			TextureSheet textureSheet = null;
			
			for(int j=0;j<textureSheetsLoaded.size();j++){
				if(textureSheetsLoaded.get(i).getTexture().getPath().toLowerCase().equals(texture.toLowerCase()))
					textureSheet = textureSheetsLoaded.get(i);
			}
			
			JsonArray animations = textureSheetListing.get("animations").getAsJsonArray();
			JsonArray names = textureSheetListing.get("names").getAsJsonArray();

			ArrayList<Animation> animationList = new ArrayList<Animation>();
			
			for(int a=0;a<animations.size();a++){
				JsonObject animationListing = animations.get(a).getAsJsonObject();
				
				String name = animationListing.get("name").getAsString();
				float frameLength = animationListing.get("frameLength").getAsFloat();
				int[] frames = Util.getInts(animationListing.get("frames").getAsString(), ",");
				
				animationList.add(new Animation(textureSheet, name, frameLength, frames));
			}
			
			for(int n=0;n<names.size();n++){
				JsonObject nameListing = names.get(n).getAsJsonObject();
				
				int id = nameListing.get("id").getAsInt();
				String name = nameListing.get("name").getAsString();
				
				textureSheet.setSubName(id, name);
			}
			
			Animation[] animationArray = new Animation[animationList.size()];
			for(int aa=0;aa<animationArray.length;aa++){
				animationArray[aa] = animationList.get(aa);
			}
			
			textureSheet.setAnimations(animationArray);
		}
		
		JsonArray levelObjects = root.get("levelObjects").getAsJsonArray(); 
		JsonArray gameObjects = root.get("gameObjects").getAsJsonArray();
		
		for(int i=0;i<levelObjects.size();i++){
			JsonObject levelObjectListing = levelObjects.get(i).getAsJsonObject();
			
			Vector3f position = Util.getVec3(levelObjectListing.get("position").getAsString());
			Vector2f size = Util.getVec2(levelObjectListing.get("size").getAsString());
			
			ArrayList<Animation> animationsUsed = new ArrayList<>();
			
			if(levelObjectListing.has("animations")){
				JsonArray animationArray = levelObjectListing.get("animations").getAsJsonArray();
				for(int a=0;a<animationArray.size();a++){
					JsonObject animObject = animationArray.get(a).getAsJsonObject();
					
					//animation attributes
					String name = animObject.get("name").getAsString();
					String texturePath = animObject.get("texture").getAsString();
					
					//find the animation by attributes
					for(TextureSheet sheet : textureSheetsLoaded){
						//find the texture sheet first
						if(sheet.getTexture().getPath().toLowerCase().equals(texturePath.toLowerCase())){
							//then get the animation by name from the texture sheet
							Animation anim = sheet.getAnimation(name);
							//if its not null
							if(anim != null){
								//then if its not already in the list
								if(!animationsUsed.contains(anim))
									//add it to the list
									animationsUsed.add(anim);
							}
							//and exit the loop of texturesheets
							break;
						}
					} //end of animation find
				} //end of animations
			}
		}
	}
	
	private <T> int getIndex(T object, ArrayList<T> list){
		for(int i=0;i<list.size();i++)
			if(object == list.get(i))
				return i;
		return -1;
	}
	
	private <T> int getIndex(String key, HashMap<? super String,V> map){
		int index = 0;
		for(Entry<? super String, V> entry : map.entrySet()){
			if(entry.getKey().equals(key))
				return index;
			index ++;
		}
		return -1;
	}
	
	public void newSave(String path){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject root = new JsonObject();
		
		root.addProperty("width", width);
		root.addProperty("height", height);
		root.addProperty("size", tileSize);
		
		//check arrays
		ArrayList<Shader>       shaders      = new ArrayList<>();
		ArrayList<TextureSheet> sheets       = new ArrayList<>();
		ArrayList<Texture>      textures     = new ArrayList<>();
		ArrayList<Renderable>   renderables  = new ArrayList<>();
		ArrayList<GameObject>   gameObjects  = new ArrayList<>();
		ArrayList<LevelObject>  levelObjects = new ArrayList<>();
		
		//json arrays
		JsonArray textureList     = new JsonArray();
		JsonArray shaderList      = new JsonArray();
		JsonArray gameObjectList  = new JsonArray();
		JsonArray levelObjectList = new JsonArray();
		JsonArray renderableList  = new JsonArray();
		
		for(Entry<String, Texture> entry : Texture.getMap().entrySet()){
			if(!textures.contains(entry.getValue())){
				//texture we're listing
				Texture texture = entry.getValue();
				
				//texture in json
				JsonObject textureListing = new JsonObject();
				
				//if the name is availible 
				if(texture.getName() != null)
					textureListing.addProperty("name", texture.getName());
				
				textureListing.addProperty("path", texture.getPath());
				
				textureListing.addProperty("aliases", Texture.getAliases(texture));
				
				if(texture.isSheet()){
					textureListing.addProperty("usage", 1);
					
					TextureSheet sheet = texture.getSheet();
					
					textureListing.addProperty("subWidth", sheet.getSubWidth());
					textureListing.addProperty("subHeight", sheet.getSubHeight());
					
					JsonArray mappings = new JsonArray();
					
					for(Entry<String, Integer> mapping : Util.sortByValue(sheet.getSubMap()).entrySet()){
						JsonObject mappingObject = new JsonObject();
						
						mappingObject.addProperty("name", mapping.getKey());
						mappingObject.addProperty("id", mapping.getValue());
						
						mappings.add(mappingObject);
					}
					
					textureListing.add("mappings", mappings);
					
					JsonArray animations = new JsonArray();
					
					//sorted by name
					for(Entry<String, Animation> mapping : Util.sortByKey(sheet.getAnimations()).entrySet()){
						JsonObject mappingObject = new JsonObject();
						
						mappingObject.addProperty("name", mapping.getKey());
						mappingObject.addProperty("frames", mapping.getValue().getFramesAsString());
						mappingObject.addProperty("frameLength", mapping.getValue().getFrameLength());
						
						animations.add(mappingObject);
					}
					
					textureListing.add("animations", animations);
					
					sheets.add(sheet);
				}else{
					textureListing.addProperty("usage", 0);
				}
				
				textureList.add(textureListing);
				textures.add(texture);
			}
		}
		
		for(Shader shader : Shader.getShaders()){
			if(!shaders.contains(shader)){
				JsonObject shaderListing = new JsonObject();
				
				shaderListing.addProperty("vertex", shader.getVertex());
				shaderListing.addProperty("fragment", shader.getFragment());
				
				if(shader.getName() != null)
					shaderListing.addProperty("name", shader.getName());
				
				shaderListing.addProperty("aliases", Shader.getAliases(shader));
				
				//add the json version to the json array
				shaderList.add(shaderListing);
				
				//add the shader to the check array
				shaders.add(shader);
			}
		}
		
		for(LevelObject levelObject : levelObjects){
			JsonObject levelObjectListing = new JsonObject();
			
			if(levelObject.renderable != null){
				Renderable renderable = levelObject.renderable;
				
				if(renderable.hasAnimations()){
					JsonArray animationListings = new JsonArray();
					for(Animation animation : renderable.getAnimations()){
						JsonObject animationListing = new JsonObject();
						
						TextureSheet source = animation.getSource();
						animationListing.addProperty("name", animation.getName());
						//texture reference id
						animationListing.addProperty("texture", getIndex(animation.getSource().getTexture(), textures));
						
						animationListings.add(animationListing);
					}
				}
				
				//add the shader index
				if(renderable.getShader() != null){
					levelObjectListing.addProperty("shader", getIndex(renderable.getShader(), shaders));
				}else{
					levelObjectListing.addProperty("shader", "-1");
				}
				
				levelObjectListing.addProperty("texRepeat", renderable.getTextureRepeat().toString());
			}
			
			levelObjectListing.addProperty("position", levelObject.getPosition().toString());
			levelObjectListing.addProperty("size", levelObject.getSize().toString());
			levelObjectListing.addProperty("rotation", levelObject.getAngle());
			
			levelObjects.add(levelObject);
		}
		
		
		for(GameObject gameObject : gameObjects){
			JsonObject gameObjectListing = new JsonObject();
			
			if(gameObject.renderable != null){
				Renderable renderable = gameObject.renderable;
				
				if(renderable.hasAnimations()){
					JsonArray animationListings = new JsonArray();
					
					for(Animation animation : renderable.getAnimations()){
						JsonObject animationListing = new JsonObject();

						TextureSheet source = animation.getSource();
						int texRefId = getIndex(animation.getSource().getTexture(), textures);
						int sourceRefId = getIndex(source, sheets);
						
						animationListing.addProperty("name", animation.getName());
						animationListing.addProperty("texture", texRefId);
					}
				}
			}
			
		}
	}
	
	public void save(String path){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		JsonObject root = new JsonObject();
		
		//add properties
		root.addProperty("width", width);
		root.addProperty("height", height);
		root.addProperty("size", tileSize);
		
		//TODO add script locations
		
		JsonArray textureList = new JsonArray();
		
		ArrayList<TextureSheet> sheets = new ArrayList<>();
		
		ArrayList<Texture> textures = new ArrayList<>();
		
		for(LevelObject object : levelObjects)
			if(!textures.contains(object.renderable.getEscapeTexture()))
				textures.add(object.renderable.getEscapeTexture());
		
		for(GameObject gameObject : gameObjects)
			if(!textures.contains(gameObject.renderable.getEscapeTexture()))
				textures.add(gameObject.renderable.getEscapeTexture());
		
		
		for(Texture texture : textures){
			JsonObject textureListing = new JsonObject();
			
			//path & name
			if(texture.getName() != null)
				textureListing.addProperty("name", texture.getName());
			textureListing.addProperty("path", texture.getPath());
			
			//is a texture sheet
			textureListing.addProperty("sheet", texture.isSheet());
			
			//texturesheet info
			if(texture.isSheet()){
				TextureSheet sheet = texture.getSheet();
				textureListing.addProperty("subWidth", sheet.getSubWidth());
				textureListing.addProperty("subHeight", sheet.getSubHeight());
				//add to the sheets
				sheets.add(sheet);
			}
			
			//add to the array
			textureList.add(textureListing);
		}
		
		JsonArray sheetArray = new JsonArray();
		
		for(TextureSheet sheet : sheets){
			JsonObject sheetListing = new JsonObject();
			
			JsonArray names = new JsonArray();
			JsonArray animations = new JsonArray();
			
			//get the names
			HashMap<String, Integer> nameMap = Util.sortByValue(sheet.getNames());
			//get the animations
			HashMap<String, Animation> animationMap = Util.sortByKey(sheet.getAnimations());
			
			for(Entry<String, Integer> entry : nameMap.entrySet()){
				JsonObject nameListing = new JsonObject();
				nameListing.addProperty("id", entry.getValue());
				nameListing.addProperty("name", entry.getKey());
				names.add(nameListing);
			}
			
			for(Entry<String, Animation> entry : animationMap.entrySet()){
				JsonObject animationListing = new JsonObject();
				
				Animation animation = entry.getValue();
				
				animationListing.addProperty("name", entry.getKey());
				animationListing.addProperty("frames", animation.getFramesAsString());
				animationListing.addProperty("frameLength", animation.getFrameLength());
				
				animations.add(animationListing);
			}
			
			sheetListing.addProperty("texture", sheet.getTexture().getPath());
			sheetListing.add("names", names);
			sheetListing.add("animations", animations);
		}
		
		root.add("textureSheets", sheetArray);
		
		JsonArray levelObjects = new JsonArray();
		
		for(LevelObject object : this.levelObjects){
			JsonObject objectListing = new JsonObject();
			
			if(object.renderable.hasAnimations()){
				JsonArray animationsUsed = new JsonArray();
				
				for(Animation animation : object.renderable.getAnimations()){
					JsonObject animationListing = new JsonObject();
					animationListing.addProperty("name", animation.getName());
					animationListing.addProperty("texture", animation.getSource().getTexture().getPath());
					animationsUsed.add(animationListing);
				}
				
				objectListing.add("animations", animationsUsed);
			}
			
			objectListing.addProperty("position", object.getPosition().toString());
			objectListing.addProperty("size", object.getSize().toString());
			
			levelObjects.add(objectListing);
		}
		
		root.add("levelObjects", levelObjects);
		
		JsonArray gameObjects = new JsonArray();
		
		for(GameObject object : this.gameObjects){
			JsonObject objectListing = new JsonObject();
			
			if(object.renderable.hasAnimations()){
				JsonArray animationsUsed = new JsonArray();
				
				for(Animation animation : object.renderable.getAnimations()){
					JsonObject animationListing = new JsonObject();
					animationListing.addProperty("name", animation.getName());
					animationsUsed.add(animationListing);
				}
				
				objectListing.add("animations", animationsUsed);
			}
			
			objectListing.addProperty("position", object.getPosition().toString());
			objectListing.addProperty("size", object.getSize().toString());
			
			gameObjects.add(objectListing);
		}
		
		root.add("gameObjects", gameObjects);
		
		ResourceLoader.writeString(path, gson.toJson(root));
	}

}
