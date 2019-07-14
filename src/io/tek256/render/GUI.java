package io.tek256.render;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;

import io.tek256.Util;

import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;


public class GUI {
	public ArrayList<GUIElement> elements; 
	public ArrayList<GUIText> text;
	
	public GUI(){
		elements = new ArrayList<>();
		text = new ArrayList<>();
	}
	
	public void renderText(){
		for(GUIText textElement : text){
			
		}
	}
	
	public static class GUIElement {
		public Vector2f position, size;
		
		public Texture texture;
		
		public Matrix4f mat;
		
		private int layer = 0;
		
		public GUIElement(){
			position = new Vector2f();
			size = new Vector2f();
			updateMat();
		}
		
		public GUIElement(Vector2f size){
			position = new Vector2f();
			this.size = new Vector2f(size);
			updateMat();
		}
		
		public GUIElement(Vector2f size, Vector2f position){
			this.size = new Vector2f(size);
			this.position = new Vector2f(position);
			updateMat();
		}
		
		public GUIElement(Texture texture){
			this.texture = texture;
			position = new Vector2f();
			size = new Vector2f();
			updateMat();
		}
		
		public GUIElement(Texture texture, Vector2f size){
			this.texture = texture;
			this.size = new Vector2f(size);
			position = new Vector2f();
			updateMat();
		}
		
		public GUIElement(Texture texture, Vector2f size, Vector2f position){
			this.texture = texture;
			this.size = new Vector2f(size);
			this.position = new Vector2f(position);
			updateMat();
		}
		
		public void setPosition(Vector2f vec){
			position.set(vec);
			updateMat();
		}
		
		public void setSize(Vector2f vec){
			size.set(vec);
			updateMat();
		}
		
		public void setLayer(int layer){
			this.layer = layer;
			updateMat();
		}
		
		private void updateMat(){
			if(mat == null)
				mat = new Matrix4f();
			
			mat.identity();
			mat.translate(position.x, position.y, layer * 0.01f);
			mat.scale(size.x, size.y, 1f);
		}
		
		public Vector2f getSize(){
			return size;
		}
		
		public Vector2f getPosition(){
			return position;
		}
		
		public int getLayer(){
			return layer;
		}
	}

	public static class GUIFont {
		public static GUIFont defaultFont;
		public static int BITMAP_WIDTH = 1024;
		public static int BITMAP_HEIGHT = 1024;
		
		private static HashMap<String, GUIFont> fontMap;
		
		static{
			fontMap = new HashMap<>();
		}
		
		private int id;
		private int width = -1,height = -1;
		
		private String name,path;
		
		private float[] scale = new float[]{
			12f,
			24f,
		};
		
		private STBTTAlignedQuad quad;
		private STBTTPackedchar.Buffer chardata;
		private FloatBuffer xb, yb;
		
		private String compatible;
		
		public GUIFont(String name, String path){
			this.name = name;
			this.path = path;
			
			load();
		}
		
		public GUIFont(String path){
			this.path = path;
			
			load();
		}
		
		public GUIFont(String path, float[] scale){
			this.path = path;
			
			load();
		}
		
		private void load(){
			if(width == -1)
				width = BITMAP_WIDTH;
			if(height == -1)
				height = BITMAP_HEIGHT;
			
			id = GL11.glGenTextures();
			
			quad = STBTTAlignedQuad.malloc();
			xb = memAllocFloat(1);
			yb = memAllocFloat(1);
			
			try{
				ByteBuffer ttfFile = Util.ioResourceToByteBuffer(path, 160 * 1024);
				
				ByteBuffer bitmap = BufferUtils.createByteBuffer(width * height);
				
				STBTTPackContext packContext = STBTTPackContext.malloc();
				
				stbtt_PackBegin(packContext, bitmap, width, height, 0, 1, null);
				
				for(int i=0;i<scale.length;i++){
					chardata.position((i * 3) * 128 + 32);
					stbtt_PackSetOversampling(packContext, 1, 1);
					stbtt_PackFontRange(packContext, ttfFile, i, scale[i], 32, chardata);
					
					chardata.position((i * 4) * 128 + 32);
					stbtt_PackSetOversampling(packContext, 2, 2);
					stbtt_PackFontRange(packContext, ttfFile, i, scale[i], 32, chardata);
					
					chardata.position((i * 5) * 128 + 32);
					stbtt_PackSetOversampling(packContext, 3, 3);
					stbtt_PackFontRange(packContext, ttfFile, i, scale[i], 32, chardata);
				}
				
				stbtt_PackEnd(packContext);
				
				packContext.free();
				
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
				
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_ALPHA, width, height, 0, GL11.GL_ALPHA, GL11.GL_UNSIGNED_BYTE, bitmap);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
				
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		public void drawString(float x, float y, float z, String message){
		}
		
		public String getName(){
			return name;
		}
		
		public String getPath(){
			return path;
		}
		
		public int getId(){
			return id;
		}
		
		public static GUIFont getFont(String name){
			if(fontMap.containsKey(name)){
				return fontMap.get(name);
			}
			return null;
		}
	}
	
	public static class GUIText {
		private String text;
		private Vector2f position;
		private int layer;
		
		public GUIText(String text){
			this.text = text;
			position = new Vector2f();
		}
		
		public void setText(String text){
			this.text = text;
		}
		
		public String getText(){
			return text;
		}
		
		public void setPosition(Vector2f position){
			this.position.set(position);
		}
		
		public Vector2f getPosition(){
			return position;
		}
		
		public void setLayer(int layer){
			this.layer = layer;
		}
		
		public int getLayer(){
			return layer;
		}
		
		public float getZ(){
			return layer * 0.01f;
		}
	}
}
