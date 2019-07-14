package io.tek256.core;

public interface GameInterface {
	public void start();
	
	public void connect(Scene scene);
	
	public void input(long delta);
	public void update(long delta);
	public void render(long delta);
	
	public void end();
}
