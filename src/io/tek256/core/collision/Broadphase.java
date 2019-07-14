package io.tek256.core.collision;

import java.util.ArrayList;

import io.tek256.core.runtime.Transform;

public class Broadphase {
	public static final int CELL_WIDTH  = 64;
	public static final int CELL_HEIGHT = 64;
	
	public Cell[] cells;
	
	private float width,height;
	
	//amount of cells per axis
	private int perWidth,perHeight;
	
	private ArrayList<AABB> boxes;
	
	public Broadphase(float width, float height){
		this.width  = width;
		this.height = height;
		
		perWidth    = Math.round(width) / CELL_WIDTH;
		perHeight   = Math.round(height) / CELL_HEIGHT;
		
		boxes = new ArrayList<AABB>();
		
		cells = new Cell[perWidth * perHeight];
		for(int i=0; i<cells.length; i++){
			cells[i] = new Cell(i % perWidth, i / perWidth);
		}
	}
	
	public void add(Transform transform){
		for(AABB box : boxes)
			if(box.transform == transform)
				return;
		
		AABB box = new AABB(transform);
		
		boxes.add(box);
		
		int left  = Math.round(box.getLeft())  / CELL_WIDTH;
		int right = Math.round(box.getRight()) / CELL_HEIGHT;
		
		int bottom = Math.round(box.getBottom()) / CELL_HEIGHT;
		int top    = Math.round(box.getTop())    / CELL_HEIGHT;
		
		System.out.println(left +","+bottom+" : "+right+","+top);
		
		for(int x = left; x < right; x++){
			for(int y = bottom; y < top; y++){
				cells[x + perWidth * y].add(box);
			}
		}
	}
	
	public void update(long delta){
		ArrayList<AABB> changes = new ArrayList<>();
		
		for(AABB box : boxes){
			if(box.transform.getVelocityX() != 0 ||
					box.transform.getVelocityY() != 0){
				box.transform.updatePosition(delta);
				clamp(box);
				changes.add(box);
			}
		}
		
		changes.forEach((box)->{
			updateBox(box);
		});
		
		for(Cell cell : cells){
			cell.update();
		}
	}
	
	public void updateBox(AABB box){
		int left  = Math.round(box.getLeft())  / CELL_WIDTH;
		int right = Math.round(box.getRight()) / CELL_HEIGHT;
		
		int bottom = Math.round(box.getBottom()) / CELL_HEIGHT;
		int top    = Math.round(box.getTop())    / CELL_HEIGHT;
		
		for(int x = left; x < right; x++){
			for(int y = bottom; y < top; y++){
				cells[x + perWidth * y].add(box);
			}
		}
	}
	
	public void clamp(AABB box){
		float offsetx = 0;
		float offsety = 0;
		
		if(box.getLeft() < 0){
			offsetx = -box.getLeft();
		}else if(box.getRight() >= getWidth()){
			offsetx = getWidth() - box.getRight();
		}
		
		if(box.getBottom() < 0){
			offsety = -box.getBottom();
		}else if(box.getTop() >= getHeight()){
			offsety = getHeight() - box.getTop();
		}
		
		if(offsetx != 0 || offsety != 0){
			box.transform.move(offsetx, offsety);
		}
	}
	
	public float getWidth(){
		return width;
	}
	
	public float getHeight(){
		return height;
	}
	
	public int getPerWidth(){
		return perWidth;
	}
	
	public int getPerHeight(){
		return perHeight;
	}
	
	public void outputCells(){
		StringBuilder map = new StringBuilder();
		for(int y=0;y<perHeight;y++){
			StringBuilder row = new StringBuilder();
			for(int x=0;x<perWidth;x++){
				row.append(cells[x + (perWidth * y)].contents.size());
			}
			row.append("\n");
			map.append(row);
		}
		System.out.println(map);
	}
	
	public static class Cell {
		public ArrayList<AABB> contents;
		
		private float x,y,left,right,bottom,top;
		
		public Cell(int x, int y){
			this.x = x * Broadphase.CELL_WIDTH;
			this.y = y * Broadphase.CELL_HEIGHT;
			
			left   = x - (Broadphase.CELL_WIDTH  / 2f);
			right  = x + (Broadphase.CELL_WIDTH  / 2f);
			
			bottom = y - (Broadphase.CELL_HEIGHT / 2f);
			top    = y + (Broadphase.CELL_HEIGHT / 2f);
			
			contents = new ArrayList<>();
		}
		
		public void update(){
			clean();
			
			contents.forEach((a)->{
				contents.forEach((b)->{
					if(a != b){
						System.out.println("test");
						if(intersects(a, b)){
							solve(a, b);
						}
					}
				});
			});
		}
		
		public void add(AABB aabb){
			if(!contents.contains(aabb)){
				contents.add(aabb);
			}
		}
		
		public void clean(){
			ArrayList<AABB> toRemove = new ArrayList<>();
			
			for(AABB aabb : contents){
				if(!withinBounds(aabb))
					toRemove.add(aabb);
			}
			
			contents.removeAll(toRemove);
		}
		
		public boolean intersects(AABB a, AABB b){
			if(Math.abs(a.getX() - b.getX()) > ((a.getWidth() / 2f) + (b.getWidth() / 2f)))
				return false;
			if(Math.abs(a.getY() - b.getY()) > ((a.getHeight() / 2f) + (b.getHeight() / 2f)))
				return false;
			return true;
		}
		
		public void solve(AABB a, AABB b){
			System.out.println("SOLVING");
		}
		
		public boolean withinBounds(AABB box){
			if(Math.abs(x - box.getX()) > ((Broadphase.CELL_WIDTH / 2f) + (box.getWidth() / 2f)))
				return false;
			if(Math.abs(y - box.getY()) > ((Broadphase.CELL_HEIGHT / 2f) + (box.getHeight() / 2f)))
				return false;
			return true;
		}
		
		public float getX(){
			return x;
		}
		
		public float getY(){
			return y;
		}
		
		public float getLeft(){
			return left;
		}
		
		public float getRight(){
			return right;
		}
		
		public float getBottom(){
			return bottom;
		}
		
		public float getTop(){
			return top;
		}
	}
	
	public static class AABB{
		public Transform transform;
		
		public AABB(Transform transform){
			this.transform = transform;
		}
		
		public float getX(){
			return transform.getX();
		}
		
		public float getY(){
			return transform.getY();
		}
		
		public float getLeft(){
			return transform.getLeft();
		}
		
		public float getRight(){
			return transform.getRight();
		}
		
		public float getBottom(){
			return transform.getBottom();
		}
		
		public float getTop(){
			return transform.getTop();
		}
		
		public float getWidth(){
			return transform.getWidth();
		}
		
		public float getHeight(){
			return transform.getHeight();
		}
	}
}
