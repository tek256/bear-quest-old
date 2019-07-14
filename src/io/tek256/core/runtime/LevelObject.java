package io.tek256.core.runtime;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class LevelObject extends Transform{
	public Renderable renderable;

	public LevelObject(Renderable renderable){
		super();
		this.renderable = renderable;
		updateMatrix();
	}
	
	public LevelObject(Vector3f position, Renderable renderable){
		super(position);
		this.renderable = renderable;
		updateMatrix();
	}
	
	public LevelObject(Vector2f position, Renderable renderable){
		super();
		setPosition(position);
		this.renderable = renderable;
		updateMatrix();
	}
	
	public LevelObject(Vector3f position, Vector2f size, Renderable renderable){
		super(position, size);
		this.renderable = renderable;
		updateMatrix();
	}
	
	public LevelObject(Vector2f position, Vector2f size, Renderable renderable){
		super();
		setPosition(position);
		setSize(size);
		this.renderable = renderable;
		updateMatrix();
	}
	
	public LevelObject(LevelObject prefab, Vector2f instancePosition){
		super(prefab, instancePosition);
		this.renderable = new Renderable(prefab.renderable);
		updateMatrix();
	}
	
	@Override
	public void Update(long delta) {

	}
	
	@Override
	public void collidedWith(Transform transform){
		System.out.println("WOAH");
	}
}
