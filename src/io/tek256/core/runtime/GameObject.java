package io.tek256.core.runtime;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class GameObject extends Transform{
	public Renderable renderable;
	
	public GameObject(Renderable renderable){
		super();
		this.renderable = renderable;
	}
	
	public GameObject(Vector3f position, Renderable renderable){
		super(position);
		this.renderable = renderable;
	}
	
	public GameObject(Vector2f position, Renderable renderable){
		super();
		setPosition(position);
		this.renderable = renderable;
	}
	
	public GameObject(GameObject prefab, Vector2f instancePosition){
		super(prefab, instancePosition);
		this.renderable = new Renderable(prefab.renderable);
	}
	
	@Override
	public void Update(long delta) {
		
	}
	
	public void input(long delta){
		
	}

	@Override
	public void collidedWith(Transform transform) {
		System.out.println("WOW");
	}

}
