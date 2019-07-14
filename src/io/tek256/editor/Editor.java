package io.tek256.editor;

import java.util.ArrayList;

import org.joml.Vector2f;

import io.tek256.core.Scene;
import io.tek256.core.runtime.GameObject;
import io.tek256.core.runtime.LevelObject;
import io.tek256.core.runtime.Transform;

public class Editor {
	public static enum SNAP_PRIORITY{
		ALL,
		LEVEL,
		NONE,
	}
	
	public SNAP_PRIORITY snapping = SNAP_PRIORITY.LEVEL;
	
	public Vector2f TILE_SIZE = new Vector2f(16f,16f);
	
	private Scene scene;
	
	public Editor(){
		
	}
	
	public void connect(Scene scene){
		this.scene = scene;
	}
	
	public void add(Transform transform){
		if(transform instanceof GameObject){
			//if snapping is enabled for game objects
			if(snapping == SNAP_PRIORITY.ALL){
				//snap the position to the nearest tile
				transform.setX(transform.getX() - (transform.getX() % TILE_SIZE.x));
				transform.setY(transform.getY() - (transform.getY() % TILE_SIZE.y));
			}
		}else if(transform instanceof LevelObject){
			//if snapping isn't specifically disabled for level objects
			if(snapping != SNAP_PRIORITY.NONE){
				//snap the position to the nearest tile
				transform.setX(transform.getX() - (transform.getX() % TILE_SIZE.x));
				transform.setY(transform.getY() - (transform.getY() % TILE_SIZE.y));
			}
		}
		
		if(scene != null)
			scene.add(transform);
	}
	
	public void add(Transform transform, Vector2f...instances){
		for(Vector2f instancePosition : instances){
			if(transform instanceof GameObject)
				add(new GameObject((GameObject)transform, instancePosition));
			else if(transform instanceof LevelObject)
				add(new LevelObject((LevelObject)transform, instancePosition));
		}
	}
	
	public void add(Transform transform, int x, int y, int width, int height){
		float startx = x * TILE_SIZE.x;
		float starty = y  * TILE_SIZE.y;
		for(int xtemp=0;x<width; x++){
			for(int ytemp=0;y<height;y++){
				if(transform instanceof GameObject)
					add(new GameObject((GameObject)transform,
							new Vector2f(startx + (TILE_SIZE.x * xtemp), starty + (TILE_SIZE.y * ytemp))));
				else if(transform instanceof LevelObject)
					add(new LevelObject((LevelObject)transform,
							new Vector2f(startx + (TILE_SIZE.x * xtemp), starty + (TILE_SIZE.y * ytemp))));
			}
		}
	}
	
	public void add(Transform transform, int x, int y, int width, int height, int... exceptions){
		Vector2f tileStart = new Vector2f(x * TILE_SIZE.x, y * TILE_SIZE.y);
		
		Vector2f[] instances = new Vector2f[width * height];
		for(int i=0;i<instances.length;i++){
			if(!containedWithinArray(i, exceptions))
				instances[i] = new Vector2f(i % width, i / height);
		}
		
		for(int i=0;i<instances.length;i++){
			if(instances[i] != null)
				if(transform instanceof GameObject)
					add(new GameObject((GameObject)transform, 
							instances[i].mul(TILE_SIZE).add(tileStart)));
				else if(transform instanceof LevelObject)
					add(new LevelObject((LevelObject)transform, 
							instances[i].mul(TILE_SIZE).add(tileStart)));
		}
	}
	
	public boolean containedWithinArray(int c, int[] array){
		for(int i=0;i<array.length;i++)
			if(i == c)
				return true;
		return false;
	}
	
	public void cleanUpScene(){
		ArrayList<LevelObject> levelObjects;
	}
	
	//TODO rescaling ?? 
	public void setTileSize(Vector2f tileSize){
		TILE_SIZE.set(tileSize);
	}
}
