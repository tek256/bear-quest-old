package io.tek256.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

import org.joml.Vector2f;

import io.tek256.core.runtime.*;
import io.tek256.render.*;

public class Scene {
	//snapping
	public static  enum SnapState{
		NONE, //nothing
		SNAP_LEVELOBJECTS, //level objects, but not gameobjects
		ALL, //everything
	}
	
	//current state for snapping
	public SnapState snapping = SnapState.NONE;

	private float tileSize = 16f;
	
	//texture mapped lists of the objects
	private HashMap<Texture, ArrayList<GameObject>> gameObjects;
	private HashMap<Texture, ArrayList<LevelObject>> levelObjects;
	
	//shader mapped lists of the objects
	private HashMap<Shader, ArrayList<GameObject>> shaderGameObjects;
	private HashMap<Shader, ArrayList<LevelObject>> shaderLevelObjects;
	
	//free objects (no shader / texture)
	private ArrayList<Transform> transforms;
	
	//the scene's camera
	private Camera camera;
	
	//the primary quad to be rendered
	private Quad quad;
	
	public Scene(){
		//setup the texture based lists for the objects
		gameObjects = new HashMap<>();
		levelObjects = new HashMap<>();
		
		//setup the shader based lists for objects
		shaderGameObjects = new HashMap<>();
		shaderLevelObjects = new HashMap<>();
		
		//setup the no-render list
		transforms = new ArrayList<Transform>();
		
		//setup the default quad of 1x1 becuse we'll scale everything by size
		quad = new Quad(1f,1f);

		//create the scene's camera
		camera = new Camera();
		
		//load all o the default shaders
		Shader.loadAll("res/shaders/");
	}
	
	/** Update the scene's gameObjects' input
	 * 
	 * @param delta time since last input call
	 */
	public void input(long delta){
		//for each gameobject list
		for(ArrayList<GameObject> gameObjects : this.gameObjects.values()){
			//for each game object in the list
			for(GameObject gameObject : gameObjects){
				//update the input
				gameObject.input(delta);
			}
		}
		
	}
	
	/** Call the scene's gameObjects' update calls
	 * 
	 * @param delta time since last update call
	 */
	public void update(long delta){
		//for each gameObject list
		for(ArrayList<GameObject> gameObjects : this.gameObjects.values()){
			//for each gameObject in the list
			for(GameObject gameObject : gameObjects){
				//update the gameObject
				gameObject.update(delta);
			}
		}
	}
	
	public void render(long delta){
		//check each list 
		Consumer<ArrayList<GameObject>> gameObjectIteration = (arraylist) -> {
			arraylist.forEach((gameObject) -> { //and for each game object in the list
				//if the position has changed more than 0.05f units
				if(gameObject.getPosChange() != 0.05f){
					//update the model view matrix
					gameObject.updateMatrix();
				}
				//update the renderable as well
				gameObject.renderable.update(delta);
			});
		};
		
		//for each level object, update the renderable
		Consumer<ArrayList<LevelObject>> levelObjectIteration = (arraylist) -> {
			arraylist.forEach((levelObject) -> levelObject.renderable.update(delta));
		};
		
		//we only have to update 1 side of a list, since both are complete
		//lists of the game objects
		gameObjects.values().forEach(gameObjectIteration);
		levelObjects.values().forEach(levelObjectIteration);
		
		//render the level
		renderLevel();
		
		//render the game objects
		renderObjects();
		
		//apply post processing effects
		postEffects();
	}

	public void load(String jsonPath){
		
	}
	
	/**
	 * sort each level object then render it
	 */
	private void renderLevel(){
		//in the render process we sort objects by shader, then by texture
		for(Shader shader : shaderLevelObjects.keySet()){
			//get the objects listed for the shader
			ArrayList<LevelObject> shaderList = shaderLevelObjects.get(shader);
			
			//Mapping of textures & game objects that use this shader
			HashMap<Texture, ArrayList<LevelObject>> toRender = new HashMap<Texture, ArrayList<LevelObject>>();
			
			//go through each object in the shader's object list
			for(LevelObject levelObject : shaderList){
				//get the texture of the object
				Texture texture = levelObject.renderable.getTexture();
				//escape any children (sub textures)
				if(texture.isSubTexture())
					texture = texture.getSheet().getTexture();
				
				//if the texture isn't mapped, add it to the map
				if(!toRender.containsKey(texture)){
					//create the list of objects with the same texture
					ArrayList<LevelObject> textureList = new ArrayList<LevelObject>();
					//add the object to the list
					textureList.add(levelObject);
					//add the new list
					toRender.put(texture, textureList);
				}else{
					//if the list already exists, add the object to it
					toRender.get(texture).add(levelObject);
				}
			}
			
			//bind the shader that's been sorted
			shader.bind();
			
			//setup the camera variables
			shader.set("PROJECTION_MAT", camera.getProjection());
			shader.set("VIEW_MAT", camera.getView());
			
			//Go through each texture that uses the shader
			for(Texture texture : toRender.keySet()){
				//make sure the texture is bound
				texture.bind();
				
				//get the list of objects using the texture
				ArrayList<LevelObject> objects = toRender.get(texture);
								
				//if the texture is a sheet, or sub texture
				if(texture.isSheet() || texture.isSubTexture()){
					//setup the size variables in the shader
					shader.set("TEXTURE_SIZE", texture.getSheet().getTexture().getSize());;
					shader.set("SUB_SIZE", texture.getSheet().getSubSize());
				}
				
				//for each object that uses the texture
				for(LevelObject levelObject : objects){
					//get the reference of the object's renderable
					Renderable renderable = levelObject.renderable;
					//get the texture
					Texture tex = renderable.getTexture();
					
					//if the texture is a sub texture
					if(tex.isSubTexture()){
						//tell the shader that the texture is a sub texture
						shader.set("SUB_TEXTURE", 1);
						//tell the sahder the texture offset
						shader.set("TEXTURE_OFFSET", tex.getOffset());
					}else{
						shader.set("SUB_TEXTURE", 0);
						shader.set("TEXTURE_OFFSET", Vector2f.ZERO);
					}
					
					//set the model matrix
					shader.set("MODEL_MAT", levelObject.getMat());
					
					//set the texture repeat
					shader.set("TEXTURE_REPEAT", renderable.getTextureRepeat());
					
					//render the quad
					quad.render();
				}
				//unbind the texture
				Texture.unbind();
			}
			//unbind the shader
			Shader.unbind();
			
			//empty out the list
			toRender.clear();
		}
	}
	
	private void renderObjects(){
		//in the render process we sort objects by shader, then by texture
		for(Shader shader : shaderGameObjects.keySet()){
			//get the objects listed for the shader
			ArrayList<GameObject> shaderList = shaderGameObjects.get(shader);
			
			//Mapping of textures & game objects that use this shader
			HashMap<Texture, ArrayList<GameObject>> toRender = new HashMap<Texture, ArrayList<GameObject>>();
			
			//go through each object in the shader's object list
			for(GameObject gameObject : shaderList){
				//get the texture of the object
				Texture texture = gameObject.renderable.getTexture();
				//escape any children (sub textures)
				if(texture.isSubTexture())
					texture = texture.getSheet().getTexture();
				
				//if the texture isn't mapped, add it to the map
				if(!toRender.containsKey(texture)){
					//create the list of objects with the same texture
					ArrayList<GameObject> textureList = new ArrayList<GameObject>();
					//add the object to the list
					textureList.add(gameObject);
					//add the new list
					toRender.put(texture, textureList);
				}else{
					//if the list already exists, add the object to it
					toRender.get(texture).add(gameObject);
				}
			}
			
			//bind the shader that's been sorted
			shader.bind();
			
			//setup the camera variables
			shader.set("PROJECTION_MAT", camera.getProjection());
			shader.set("VIEW_MAT", camera.getView());
			
			//Go through each texture that uses the shader
			for(Texture texture : toRender.keySet()){
				//make sure the texture is bound
				texture.bind();
				
				//get the list of objects using the texture
				ArrayList<GameObject> objects = toRender.get(texture);
				
				//if the texture is a sheet, or sub texture
				if(texture.isSheet() || texture.isSubTexture()){
					//setup the size variables in the shader
					shader.set("TEXTURE_SIZE", texture.getSheet().getTexture().getSize());
					shader.set("SUB_SIZE", texture.getSheet().getSubSize());
					
					shader.set("SUB_TEXTURE", 1);
				}else{
					shader.set("SUB_TEXTURE", 0);
					shader.set("TEXTURE_OFFSET", Vector2f.ZERO);
				}
				
				//for each object that uses the texture
				for(GameObject gameObject : objects){
					//get the reference of the object's renderable
					Renderable renderable = gameObject.renderable;
					
					shader.set("FLIP_X", renderable.isFlipX() ? 1 : 0);
					shader.set("FLIP_Y", renderable.isFlipY() ? 1 : 0);
					
					//get the texture
					Texture tex = renderable.getTexture();
					
					//if the texture is a sub texture
					if(tex.isSubTexture()){
						//tell the shader the texture offset
						shader.set("TEXTURE_OFFSET", tex.getOffset());
					}

					//set the texture repeat
					shader.set("TEXTURE_REPEAT", renderable.getTextureRepeat());
					
					//set the model matrix
					shader.set("MODEL_MAT", gameObject.getMat());
					
					//render the quad
					quad.render();
				}
				//unbind the texture
				Texture.unbind();
			}
			//unbind the shader
			Shader.unbind();
			
			//empty out the list
			toRender.clear();
		}
	}
	
	//TODO implement post effects: framebuffer usage
	private void postEffects(){
		//Note implementation of framebuffers will be soon, its just not a priority curently
	}

	/** Add a transform to the scene
	 * 
	 * 	Process: Check for type -> Add to Texture's Render List -> Add to Shader's Render List
	 * 	If no renderable, add to no-render list
	 * 
	 * @param transform transform to be added to the scene
	 */
	public void add(Transform transform){
		//reference texture for adding to correct list
		Texture src =  null;
		
		if(transform instanceof GameObject){ //If the transform is a `GameObject`
			//cast the transform to `GameObject` for functionality
			GameObject gameObject = (GameObject)transform;
			
			if(snapping == SnapState.ALL){
				//snap position to tile size
				float newX = transform.getX() - (transform.getX() % tileSize);
				float newY = transform.getY() - (transform.getY() % tileSize);
				
				transform.setPosition(newX, newY);
			}
			
			//if the gameobject isn't renderable, then we'll want to
			//add it to a no-render list and just back out
			if(gameObject.renderable == null){
				//add the gameobject to the no-render list
				transforms.add(gameObject);
				//back out of the call
				return;
			}
			
			//get the texture from the renderable
			src = gameObject.renderable.getTexture();
			
			//if the source is a sub texture, use the sheet's texture for sorting
			if(src.isSubTexture()){
				//this just insures that we're using 1 image sorting instead of sub image
				src = src.getSheet().getTexture();
			}
			
			//if the gameObject map already contains the texture
			if(gameObjects.containsKey(src)){
				//add the object to the list for it!
				gameObjects.get(src).add(gameObject);
			}else{
				//if not, then make a new list
				ArrayList<GameObject> list = new ArrayList<GameObject>();
				//add the object to the list
				list.add(gameObject);
				
				//and add the list to the map
				gameObjects.put(src, list);
			}
			
			//the shader of the gameObject
			Shader shader = gameObject.renderable.getShader();
			
			//if the shader list already contains a list for the shader
			if(shaderGameObjects.containsKey(shader)){
				//add the game object to the list in the map
				shaderGameObjects.get(shader).add(gameObject);
			}else{
				//otherwise, make a list
				ArrayList<GameObject> list = new ArrayList<GameObject>();
				//add the object to it
				list.add(gameObject);
				
				//and add the list to the map
				shaderGameObjects.put(shader, list);
			}
		}else if(transform instanceof LevelObject){ //If the transform is a `LevelObject` 
			//cast the transform to a `LevelObject` for functionality
			
			//if the scene snapping isn't off
			if(snapping != SnapState.NONE){
				//snap position to tile size
				float newX = transform.getX() - (transform.getX() % tileSize);
				float newY = transform.getY() - (transform.getY() % tileSize);
				
				transform.setPosition(newX, newY);
			}
			
			LevelObject levelObject = (LevelObject)transform;
			
			//if the level object isn't renderable, add it to a no-render
			//list and exit the call
			if(levelObject.renderable == null){
				//add to a no-render list
				transforms.add(levelObject);
				//back out of the call
				return;
			}
			
			//the texture of the object
			src = levelObject.renderable.getTexture();
			
			//if the texture is a sub texture, use the parent for sorting's sake
			if(src.isSubTexture())
				src = src.getSheet().getTexture();
			
			//if the levelObjects already contain the texture, then add it to the list
			if(levelObjects.containsKey(src)){
				levelObjects.get(src).add(levelObject);
			}else{
				//make a new list
				ArrayList<LevelObject> list = new ArrayList<LevelObject>();
				//add the object to it
				list.add(levelObject);
				
				//and place it in the map
				levelObjects.put(src, list);
			}

			//shader of the levelObject
			Shader shader = levelObject.renderable.getShader();
			
			//check to see if there is a list of objects using the same shader
			if(shaderLevelObjects.containsKey(shader)){
				//add the level object to the list of objects
				shaderLevelObjects.get(shader).add(levelObject);
			}else{
				//since there isn't a list already made, make one
				ArrayList<LevelObject> list = new ArrayList<LevelObject>();
				//add the object to the list
				list.add(levelObject);
				
				//put the list in the map
				shaderLevelObjects.put(shader, list);
			}
			
		}else{
			//we only directly handle gameobjects and levelobjects for now, so
			//we'll add any other abstraction of transform to a generic list
			//and won't render anything for it. (good for script objects)
			transforms.add(transform);
		}
	}

	public void add(Transform...transforms){
		for(Transform transform : transforms)
			add(transform);
	}
	
	/** Remove a transform from the scene
	 * 
	 *  Process: Check for type -> Remove from Texture's Render List -> Remove from Shader's Render List
	 *  If no renderable, remove from the no-render list
	 * 
	 * @param transform the object to be removed from the scene
	 */
	public void remove(Transform transform){
		//s & t will be used for shader & texture references respectively
		//both references are used to sort & arrange objects
		Shader s = null;
		Texture t = null;
		
		if(transform instanceof GameObject){
			//cast the transform to gameobject
			GameObject g = (GameObject)transform;
			
			if(g.renderable != null){
				//setup the comparable (texture & shader) references
				t = g.renderable.getTexture();
				
				//escape any sub textures for comparing
				if(t.isSubTexture()){
					t = t.getSheet().getTexture();
				}
				
				//get the shader of the object for reference
				s = g.renderable.getShader();
				
				//check if the texture is indexed
				if(gameObjects.containsKey(t)){
					//remove the gameobject from the texture's render list
					gameObjects.get(t).remove(g);
				}
				
				//check if the shader is indexed
				if(shaderGameObjects.containsKey(s)){
					//remove the gameobject from the shader's render list
					shaderGameObjects.get(s).remove(g);
				}
			}else{
				//if there is no renderable its in the no-render list
				if(transforms.contains(transform))
					transforms.remove(transform);
			}
		}else if(transform instanceof LevelObject){
			//cast the transform to levelobject 
			LevelObject l = (LevelObject)transform;
			//if the object isn't renderable, then take it out of the no-render list
			if(l.renderable != null){
				//setup comparable references
				t = l.renderable.getTexture();
				s = l.renderable.getShader();
				
				//check if the texture is indexed
				if(levelObjects.containsKey(t)){
					levelObjects.get(t).remove(l);
				}
				
				//check if the shader is indexed
				if(shaderLevelObjects.containsKey(s)){
					//remove the object from the indexed shader list
					shaderLevelObjects.get(s).remove(l);
				}
				
			}else{
				//if its not renderable, its stored in the no-render list
				if(transforms.contains(transform))
					transforms.remove(transform);
			}
			
		}else{ //we haven't defined what abstraction this would be, so we handle
			//it as such.
			//remove the transform from the no-render list
			if(transforms.contains(transform))
				transforms.remove(transform);
		}
	}
	
	
	/** Set the snapping state
	 * 
	 * @param state state of snapping
	 */
	public void setSnapping(SnapState state){
		snapping = state;
	}
	
	/** Get the snapping state
	 * 
	 * @return the snapping state
	 */
	public SnapState getSnapping(){
		return snapping;
	}
	
	/** There are 2 main operations of this call.
	 *  1: To reset the scene for a new level
	 *  	-destroy shaders = false (load time)
	 *  2: To exit the game
	 *  	-destroy shaders = true (exiting, not reloading)
	 *  
	 *  
	 * 
	 * @param destroyShaders if we should destroy the shaders as well
	 */
	public void destroy(boolean destroyShaders){
		//setup the lambda to clear for us
		/*
		 * this function is the same as defining 
		 * void clear(ArrayList<?> list){
		 * 	list.clear();
		 * }
		 * 
		 * but the purpose for this lambda is to keep it
		 * inline and within scope.
		 * There's not really any other use for submap cleaning
		 * outside of this function
		 */
		Consumer<? super ArrayList<?>> clear = (list)->{ 
				list.clear(); //clear the list passed into it
		};
		
		//clear out the texture arrays
		gameObjects.values().forEach(clear);
		levelObjects.values().forEach(clear);
		
		//clear out the texture maps
		gameObjects.clear();
		levelObjects.clear();
		
		//clear out the shader arrays (transforms)
		shaderGameObjects.values().forEach(clear);
		shaderLevelObjects.values().forEach(clear);
		
		if(destroyShaders){
			shaderGameObjects.clear();
			shaderLevelObjects.clear();
			
			//get the collection of shaders
			Collection<Shader> shaders = Shader.getShaders();
			
			//list of shaders to remove
			ArrayList<Shader> toRemove = new ArrayList<Shader>();

			//go through each shader
			for(Shader shader : shaders){
				//filter out duplicates
				if(!toRemove.contains(shader)){
					//destroy the shader itself
					shader.destroy();
					//add the shader to be removed
					toRemove.add(shader);
				}
			}
			
			//remove all of the entries affected
			shaderGameObjects.keySet().removeAll(toRemove);
			shaderLevelObjects.keySet().removeAll(toRemove);
			
			//update the global shader store
			Shader.updateCheck();
		}
		
		//clear out the no-render list
		transforms.clear();
	}
	
	public Camera getCamera(){
		return camera;
	}
	
	public void outputGameObjects(){
		for(Shader shader : shaderGameObjects.keySet()){
			for(GameObject gameObject : shaderGameObjects.get(shader)){
				System.out.println(gameObject.renderable.getTexture().getPath());
			}
		}
	}
}
