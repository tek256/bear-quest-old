package io.tek256.render;

import static org.lwjgl.opengl.GL20.*;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

//we're going to be using a lot of math variables in this class
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import io.tek256.ResourceLoader;
import io.tek256.core.GameEngine;

public class Shader {
	private static HashMap<String,Shader> shaders;
	
	static{
		shaders = new HashMap<>();
	}
	
	private int id;
	
	private String vertex, fragment, name;
	private boolean bound = false, destroyed = false;
	
	private HashMap<String,Integer> uniforms;
	
	private FloatBuffer buffer;
	
	
	public Shader(String name, String vertex, String fragment){
		this.name = name;
		this.vertex = vertex;
		this.fragment = fragment;
		
		//create each sub shader
		int vid = createShader(GL_VERTEX_SHADER, vertex);
		int fid = createShader(GL_FRAGMENT_SHADER, fragment);
		
		//create the program id
		id = glCreateProgram();
		
		//attach each individual shader
		glAttachShader(id, vid);
		glAttachShader(id, fid);
		
		//link the shaders together to make the program
		glLinkProgram(id);

		//if there is an error with linking, output the message
		if(glGetProgrami(id, GL_LINK_STATUS) == GL11.GL_FALSE){
			System.err.println(glGetProgramInfoLog(id, 1024));
			glDeleteProgram(id);
		}
		
		//remove the sub shader instances (already compiled into a whole)
		glDetachShader(id, vid);
		glDetachShader(id, fid);
		
		//add the shader program to the shader list by both paths of the shaders
		//note: collision will occur when reusing sub shaders
		shaders.put(name, this);
		shaders.put(vertex, this);
		shaders.put(fragment, this);

		//setup a uniform map (string uniform name, integer uniform location)
		uniforms = new HashMap<>();
		
		//create a buffer for 4x4 matrices
		buffer = BufferUtils.createFloatBuffer(16);
	}
	
	public Shader(String vertex, String fragment){
		this.vertex = vertex;
		this.fragment = fragment;
		
		//create each sub shader
		int vid = createShader(GL_VERTEX_SHADER, vertex);
		int fid = createShader(GL_FRAGMENT_SHADER, fragment);
		
		//create the program id
		id = glCreateProgram();
		
		//attach each individual shader
		glAttachShader(id, vid);
		glAttachShader(id, fid);
		
		//link the shaders together to make the program
		glLinkProgram(id);

		//if there is an error with linking, output the message
		if(glGetProgrami(id, GL_LINK_STATUS) == GL11.GL_FALSE){
			System.err.println(glGetProgramInfoLog(id, 1024));
			glDeleteProgram(id);
		}
		
		//remove the sub shader instances (already compiled into a whole)
		glDetachShader(id, vid);
		glDetachShader(id, fid);
		
		//add the shader program to the shader list by both paths of the shaders
		//note: collision will occur when reusing sub shaders
		shaders.put(vertex, this);
		shaders.put(fragment, this);
		
		//setup a uniform map (string uniform name, integer uniform location)
		uniforms = new HashMap<>();
		
		//create a buffer for 4x4 matrices
		buffer = BufferUtils.createFloatBuffer(16);
		
	}
	
	/** Create a shader by type and file
	 *  
	 * @param type type of shader (vertex or fragment)
	 * @param path path to shader file
	 * @return a shader id
	 */
	private int createShader(int type, String path){
		//create the shader id
		int id = glCreateShader(type);
		
		//get and place the source to the shader
		glShaderSource(id, ResourceLoader.getString(path));
		
		//compile the shader
		glCompileShader(id);
		
		//check for compilation issues, if present, output them
		if(glGetShaderi(id, GL_COMPILE_STATUS) == GL11.GL_FALSE){
			System.err.println(glGetShaderInfoLog(id, 1024));
			//don't leak the shader
			glDeleteShader(id);
		}
		
		return id;
	}
	
	/** Set a matrix uniform
	 * 
	 * @param uniform name of a uniform
	 * @param mat matrix to set
	 */
	public void set(String uniform, Matrix4f mat){
		GL20.glUniformMatrix4fv(getUniform(uniform), false, mat.get(buffer));
	}
	
	/** Set a vec3 uniform
	 * 
	 * @param uniform name of a uniform
	 * @param vec vec3 to set
	 */
	public void set(String uniform, Vector3f vec){
		GL20.glUniform3f(getUniform(uniform), vec.x, vec.y, vec.z);
	}
	
	/** Set a vec2 uniform
	 *
	 * @param uniform name of a uniform
	 * @param vec vec2 to set
	 */
	public void set(String uniform, Vector2f vec){
		GL20.glUniform2f(getUniform(uniform), vec.x, vec.y);
	}
	
	/** Set an array or individual integer(s)
	 * 
	 * @param uniform name of a uniform
	 * @param ints integer(s) to set
	 */
	public void set(String uniform, int... ints){
		if(ints.length == 1){
			GL20.glUniform1i(getUniform(uniform), ints[0]);
		}else if(ints.length == 2){
			GL20.glUniform2i(getUniform(uniform), ints[0], ints[1]);
		}else if(ints.length == 3){
			GL20.glUniform3i(getUniform(uniform), ints[0], ints[1], ints[2]);
		}else if(ints.length >= 4){
			GL20.glUniform4i(getUniform(uniform), ints[0], ints[1], ints[2], ints[3]);
		}
	}
	
	/** Set an array or individual float(s)
	 * 
	 * @param uniform name of a uniform
	 * @param floats float(s) to set
	 */
	public void set(String uniform, float...floats){
		if(floats.length == 1){
			GL20.glUniform1f(getUniform(uniform), floats[0]);
		}else if(floats.length == 2){
			GL20.glUniform2f(getUniform(uniform), floats[0], floats[1]);
		}else if(floats.length == 3){
			GL20.glUniform3f(getUniform(uniform), floats[0], floats[1], floats[2]);
		}else if(floats.length >= 4){
			GL20.glUniform4f(getUniform(uniform), floats[0], floats[1], floats[2], floats[3]);
		}
	}
	
	/** Get the location of a uniform by name
	 * 
	 * @param uniform name of a uniform
	 * @return location of the uniform
	 */
	public int getUniform(String uniform){
		if(uniforms.containsKey(uniform))
			return uniforms.get(uniform);
		
		int loc = glGetUniformLocation(id, uniform);
		uniforms.put(uniform, loc);
		return loc;
	}
	
	/** Name the shader
	 * 
	 * @param name name of the shader
	 */
	public void name(String name){
		if(this.name != null){
			shaders.remove(name);
		}
		shaders.put(name, this);
		this.name = name;
	}
	
	/**
	 * tell opengl to use this shader program
	 */
	public void bind(){
		//if this shader is bound, then do nothing
		if(bound)
			return;
		//make sure all of the other `bound` variables aren't set
		unbindOthers();
		//bind the shader in opengl
		glUseProgram(id);
		//update the binding check
		bound = true;
	}
	
	/** Get if the shader is destroyed
	 * 
	 * @return if the shader is destroyed
	 */
	public boolean isDestroyed(){
		return destroyed;
	}
	
	/** Get the name of the shader
	 * 
	 * @return name of the shader
	 */
	public String getName(){
		return name;
	}
	
	/** Get the path of the vertex shader
	 * 
	 * @return path of the vertex shader
	 */
	public String getVertex(){
		return vertex;
	}
	
	/** Get the path of the fragment shader
	 * 
	 * @return path of the fragment shader
	 */
	public String getFragment(){
		return fragment;
	}
	
	/** Get if the shader is bound
	 * 
	 * @return if the shader is bound
	 */
	public boolean isBound(){
		return bound;
	}
	
	/** Get the id of the shader
	 * 
	 * @return the id of the shader
	 */
	public int getId(){
		return id;
	}
	
	/**
	 * Delete the shader in opengl
	 */
	public void destroy(){
		//prevent from any repeat calls
		if(destroyed)
			return;
		destroyed = true;
		glDeleteProgram(id);
	}
	
	/**
	 * Set the shader to not bound
	 */
	protected void setUnbound(){
		bound = false;
	}
	
	/**
	 * Unbind other shaders
	 */
	protected void unbindOthers(){
		for(Shader shader : shaders.values()){
			if(shader != this)
				shader.setUnbound();
		}
	}
	
	/**
	 * Unbind all shaders
	 */
	public static void unbind(){
		glUseProgram(0);
		for(Shader shader : shaders.values()){
			shader.setUnbound();
		}
	}
	
	/** Get a shader by name or accessible string
	 * 
	 * @param shader name or accessible string of shader
	 * @return the shader
	 */
	public static Shader get(String shader){
		return shaders.get(shader);
	}
	
	/** Get a shader by id
	 * 
	 * @param id Id of the shader
	 * @return the shader
	 */
	public static Shader get(int id){
		for(Shader shader : shaders.values()){
			if(shader.id == id)
				return shader;
		}
		return null;
	}
	
	/** Load all of matching name shader files in a directory 
	 * 
	 * @param path directory to shaders
	 */
	public static void loadAll(String path){
		File root = new File(path);
		
		if(!root.isDirectory() || !root.exists()){
			System.out.println("Directory of: "+path+" does not exist");
			return;
		}
		
		//attribs: string[] [0] = name [1-2] = file path (extension incl)
		ArrayList<String[]> matches = new ArrayList<String[]>();
		
		for(File file : root.listFiles()){
			boolean matchFound = false;
			//get name (strip file extension)
			String name = file.getName().substring(0, file.getName().lastIndexOf("."));
			
			for(String[] attribs : matches){
				if(attribs[0].toLowerCase().equals(name.toLowerCase())){
					//if the included shader isn't a duplicate
					if(!attribs[1].toLowerCase().equals(file.getPath())){
						matchFound = true;
						String[] temp = new String[3];
						temp[0] = attribs[0];
						
						//if the normalized string is a vertex shader
						if(attribs[1].toLowerCase().endsWith("vs") || attribs[1].toLowerCase().endsWith("vert")){
							//file already loaded is a fragment shader
							temp[1] = attribs[1];
							temp[2] = file.getPath();
						}else if(attribs[1].toLowerCase().endsWith("fs") || attribs[1].toLowerCase().endsWith("frag")){
							//file already loaded is a vertex shader
							temp[1] = file.getPath();
							temp[2] = attribs[1];
						}
						
						//remove old shader attributes
						matches.remove(attribs);
						//update the array
						attribs = temp;
						//add the complete attributes
						matches.add(attribs);
					}
				}
			}
			
			//if no matches found, add one to the array
			if(!matchFound){
				matches.add(new String[]{name, file.getPath()});
			}
		}
		
		for(String[] match : matches){
			if(match.length == 3){
				//create a new shader with the attributed features of the match
				new Shader(match[0], match[1], match[2]);
				
				//if we're debugging
				if(GameEngine.DEBUG) 
					//output the addressable shader
					System.out.println(match.length);
			}
		}
	}
	
	public static void outputMap(){
		//map of shaders with addressable strings
		HashMap<Shader, ArrayList<String>> map = new HashMap<>();
		
		//for each entry in the shader map
		for(Entry<String, Shader> entry : shaders.entrySet()){
			//if the shader has already been listed, add the key to the 
			//accessible array that exists
			if(map.containsKey(entry.getValue())){
				map.get(entry.getValue()).add(entry.getKey());
			}else{
				//otherwise, make a list for other strings to be added to
				//as needed
				ArrayList<String> list = new ArrayList<String>();
				list.add(entry.getKey());
				map.put(entry.getValue(), list);
			}
		}
		
		for(Entry<Shader, ArrayList<String>> entry : map.entrySet()){
			StringBuilder ln = new StringBuilder();
			entry.getValue().forEach((str)->ln.append(str +","));
			System.out.println("{ "+ln.toString()+" }");
		}
	}
	
	public static Collection<Shader> getShaders(){
		return shaders.values();
	}
	
	public static void destroyAll(){
		ArrayList<Shader> checks = new ArrayList<Shader>();
		//clear the objects
		for(Shader shader : shaders.values()){
			if(!checks.contains(shader)){
				shader.destroy();
				checks.add(shader);
			}
		}
		
		//clear out all of the objects
		checks.clear();
		shaders.clear();
	}
	
	public static void updateCheck(){
		ArrayList<Shader> checks = new ArrayList<Shader>();
		//filter out dead shaders
		for(Shader shader : shaders.values()){
			if(shader.isDestroyed() && !checks.contains(shader)){
				checks.add(shader);
			}
		}
		//remove all of the dead shaders
		shaders.values().removeAll(checks);
		
		//make sure there aren't any stray references
		checks.clear();
	}
	
	public static String getAliases(Shader shader){
		StringBuilder aliases = new StringBuilder();
		for(Entry<String, Shader> entry : shaders.entrySet()){
			if(entry.getValue() == shader)
				aliases.append(entry.getKey()+",");
		}
		//remove trailing comma
		return aliases.substring(0, aliases.length() -1);
	}
	
	public static HashMap<String, Shader> getMap(){
		return shaders;
	}
}
