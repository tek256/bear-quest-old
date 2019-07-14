package io.tek256.core.runtime;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public abstract class Transform {
	private static int UNIQUE_TRANSFORMS = 0;
	
	//prefab id
	private int id = 0;
	
	//for rendering (model matrix)
	private Matrix4f mat;

	//current state
	private Vector3f position;
	private Vector2f size, velocity; //scale / motion /s ec
	private float angle;
	
	//tracking variables for the matrix
	private Vector3f lastPosition;
	private Vector2f lastSize;
	private float lastAngle;
	
	public Transform(){
		position = new Vector3f();
		lastPosition = new Vector3f();
		
		size = new Vector2f();
		lastSize = new Vector2f();
		
		velocity = new Vector2f();
		
		angle = 0f;
		lastAngle = 0f;
		
		UNIQUE_TRANSFORMS ++;
		id = UNIQUE_TRANSFORMS;
		
		mat = new Matrix4f();
	}
	
	public Transform(Vector3f position){
		this.position = new Vector3f(position);
		this.lastPosition = new Vector3f(position);
		
		this.size = new Vector2f();
		this.lastSize = new Vector2f();
		
		velocity = new Vector2f();
		
		angle = 0f;
		lastAngle = 0f;

		UNIQUE_TRANSFORMS ++;
		id = UNIQUE_TRANSFORMS;
		
		mat = new Matrix4f();
	}
	
	public Transform(Vector3f position, Vector2f size){
		this.position = new Vector3f(position);
		this.lastPosition = new Vector3f(position);
		
		this.size = new Vector2f(size);
		this.lastSize = new Vector2f(lastSize);
		
		velocity = new Vector2f();
		
		angle = 0f;
		lastAngle = 0f;

		UNIQUE_TRANSFORMS ++;
		id = UNIQUE_TRANSFORMS;
		
		mat = new Matrix4f();
	}
	
	public Transform(Vector3f position, Vector2f size, float angle){
		this.position = new Vector3f(position);
		this.lastPosition = new Vector3f(position);
		
		this.size = new Vector2f(size);
		this.lastSize = new Vector2f(size);
		
		velocity = new Vector2f();
		
		this.angle = angle;
		this.lastAngle = angle;

		UNIQUE_TRANSFORMS ++;
		id = UNIQUE_TRANSFORMS;
		
		mat = new Matrix4f();
	}
	
	public Transform(Transform transform, Vector2f vector2f){
		this.position = new Vector3f(transform.position);
		this.lastPosition = new Vector3f(transform.lastPosition);
		
		this.size = new Vector2f(transform.size);
		this.lastSize = new Vector2f(transform.lastSize);
		
		velocity = new Vector2f();
		
		this.angle = transform.angle;
		this.lastAngle = transform.lastAngle;
		
		this.position.x = vector2f.x;
		this.position.y = vector2f.y;
		
		id = transform.id;
		
		mat = new Matrix4f();
	}
	
	public void updatePosition(long delta){
		lastPosition.set(position);
		
		position.x += velocity.x * delta;
		position.y += velocity.y * delta;
	}
	
	public void update(long delta){
		if(!position.equals(lastPosition) || 
				!size.equals(lastSize) || angle != lastAngle){
			//something changed
			lastPosition.set(position);
			lastSize.set(size);
		}
		
		Update(delta);
	}
	
	public abstract void collidedWith(Transform transform);
	
	public abstract void Update(long delta);
	
	public void setVelocity(float x, float y){
		velocity.set(x,y);
	}
	
	public void setVelocity(Vector2f velocity){
		this.velocity.set(velocity);
	}
	
	public void setVelocityX(float x){
		velocity.x = x;
	}
	
	public void setVelocityY(float y){
		velocity.y = y;
	}
	
	public Vector2f getVelocity(){
		return velocity;
	}
	
	public float getVelocityX(){
		return velocity.x;
	}
	
	public float getVelocityY(){
		return velocity.y;
	}
	
	public void setPosition(float x, float y){
		lastPosition.x = position.x;
		lastPosition.y = position.y;
		
		position.x = x;
		position.y = y;
	}
	
	public void setPosition(Vector2f vec){
		lastPosition.x = position.x;
		lastPosition.y = position.y;
		
		position.x = vec.x;
		position.y = vec.y;
	}
	
	public void setPosition(float x, float y, float z){
		lastPosition.set(position);
		
		position.set(x,y,z);
	}
	
	public void setPosition(Vector3f vec){
		lastPosition.set(position);
		
		position.set(vec);
	}
	
	public void setX(float x){
		lastPosition.x = position.x;
		position.x = x;
	}
	
	public void setY(float y){
		lastPosition.y = position.y;
		position.y = y;
	}
	
	public void setZ(float z){
		lastPosition.z = position.z;
		position.z = z;
	}
	
	public void move(Vector2f vec){
		move(vec.x, vec.y);
	}
	
	public void move(float x, float y){
		lastPosition.x = position.x;
		lastPosition.y = position.y;
		
		position.x += x;
		position.y += y;
	}
	
	public float getX(){
		return position.x;
	}
	
	public float getY(){
		return position.y;
	}
	
	public float getZ(){
		return position.z;
	}
	
	public float getLeft(){
		return position.x - (size.x / 2f);
	}
	
	public float getRight(){
		return position.x + (size.x / 2f);
	}
	
	public float getBottom(){
		return position.y - (size.y / 2f);
	}
	
	public float getTop(){
		return position.y + (size.y / 2f);
	}
	
	public Vector3f getPosition(){
		return position;
	}
	
	public float getWidth(){
		return size.x;
	}
	
	public float getHeight(){
		return size.y;
	}
	
	public void setSize(float width, float height){
		lastSize.set(size);
		size.set(width, height);
	}
	
	public void setSize(Vector2f vec){
		lastSize.set(size);
		size.set(vec);
	}
	
	public void setWidth(float width){
		lastSize.x = size.x;
		size.x = width;
	}
	
	public void setHeight(float height){
		lastSize.y = size.y;
		size.y = height;
	}
	
	public Vector2f getSize(){
		return size;
	}
	
	public void setAngle(float angle){
		lastAngle = this.angle;
		this.angle = angle;
	}
	
	public float getAngle(){
		return angle;
	}
	
	public void updateMatrix(){
		mat.identity();
		
		mat.translate(position.x, position.y, -position.z);
		mat.rotateZ(angle);
		mat.scale(size.x, size.y, 1f);
		
		lastPosition.set(position);
		lastSize.set(size);
		lastAngle = angle;
	}
	
	public Matrix4f getMat(){
		return mat;
	}
	
	public float getPosChange(){
		return position.distance(lastPosition);
	}
	
	public float getSizeChange(){
		return size.distance(lastSize);
	}
	
	public float getAngleChange(){
		return Math.abs(angle - lastAngle);
	}
}
