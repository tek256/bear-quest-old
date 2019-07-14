package io.tek256.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Quad {
	private int vao,vbo,vboi,vto;
	private float width,height;
	
	public Quad(float width, float height){
		//set the quad's size
		this.width = width;
		this.height = height;
		
		//create the vertex array
		vao = glGenVertexArrays();
		
		//use the vertex array
		glBindVertexArray(vao);
		
		//create buffer objects
		vbo = glGenBuffers();
		vto = glGenBuffers();
		vboi = glGenBuffers();
		
		//the half sizes so the center is 0,0 in relative space
		float hw = width * 0.5f, hh = height * 0.5f;
		
		float[] verts = new float[]{
			-hw, -hh, 0, //bottom left
			-hw,  hh, 0, //top left
			 hw,  hh, 0, //top right
			 hw, -hh, 0, //bottom right
		};
		
		//the texture coords are flipped
		float[] texcoords = new float[]{
			0,1, //bottom left 
			0,0, //top left
			1,0, //top right
			1,1, //bottom right
		};
		
		//each triangle in draw order
		int[] indices = new int[]{
			0,1,2, //first triangle
			2,3,0 //second triangle
		};

		//create a buffer for the vertices to be sent to opengl
		FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(12);
		//add the verticies and make sure its prepared for reading
		vertBuffer.put(verts).flip();
		
		//do the same thing for the texture coords
		FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(8);
		texCoordBuffer.put(texcoords).flip();
		
		//the indices are a little different, we use integers here
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(6);
		indicesBuffer.put(indices).flip();
		
		//bind the buffer in opengl
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		//send the data to the opengl buffer and tell it that its static draw
		glBufferData(GL_ARRAY_BUFFER, vertBuffer, GL_STATIC_DRAW);
		//setup the attribute of the array (vertices) which have 3 elements (x,y,z)
		glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		
		//bind the texture coord array in opengl
		glBindBuffer(GL_ARRAY_BUFFER, vto);
		//send the texturecoord data to opengl
		glBufferData(GL_ARRAY_BUFFER, texCoordBuffer, GL_STATIC_DRAW);
		//tell opengl that there are 2 floats per vertex and its known as the 2nd attribute (1)
		glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
		
		//bind the index buffer (which vertices to draw)
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboi);
		//send opengl the indices
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
		
		//make sure no array buffers (position or texcoord) are still bound
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		//unbind the vertex array
		glBindVertexArray(0);
	}
	
	/**
	 * Bind the vertex array and enable the vertex attribs
	 */
	public void prepRender(){
		glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
	}
	
	/**
	 * Draw the elements of the Quad
	 */
	public void prepedRender(){
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
	}
	
	/**
	 * Disable the vertex attribs, and unbind the quad
	 */
	public void exitRender(){
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
	}
	
	/**
	 * prepRender -> prepedRender -> exitRender
	 */
	public void render(){
		prepRender();
		prepedRender();
		exitRender();
	}
	
	/** Get the width of the quad
	 * 
	 * @return the width of the quad
	 */
	public float getWidth(){
		return width;
	}
	
	/** Get the height of the quad
	 * 
	 * @return the height of the quad
	 */
	public float getHeight(){
		return height;
	}
	
	/**
	 * Destroy the buffers
	 */
	public void destroy(){
		//delete the positions
		glDeleteBuffers(vbo);
		//delete the indices
		glDeleteBuffers(vboi);
		//delete the texcoords
		glDeleteBuffers(vto);
		//delete the vertex array
		glDeleteVertexArrays(vao);
	}
}
