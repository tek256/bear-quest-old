package io.tek256;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

public class ResourceLoader {
	private static OS os;
	
	public static enum OS{
		Windows,
		UNIX,
	}
	
	static{
		String osString = System.getProperty("os.name").toLowerCase();
		if(osString.contains("window")){
			os = OS.Windows;
		}else{
			os = OS.UNIX;
		}
	}
	
	public static String getString(String path){
		String str = null;
		StringBuilder strBuilder = new StringBuilder();
		
		try{
			FileInputStream in = new FileInputStream(path);
			//create an inputstream reader
			InputStreamReader sr = new InputStreamReader(in);
			BufferedReader read = new BufferedReader(sr);
			//use string as a placeholder for each line
			while((str = read.readLine()) != null){
				strBuilder.append(str+"\n");
			}
			in.close();
			sr.close();
			//close all resources
			read.close();
			//set the string to return
			str = strBuilder.toString();
			strBuilder = null;
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return str;
	}
	
	public static InputStream getStream(String path){
		return ResourceLoader.class.getResourceAsStream(path);
	}
	
	public static FileInputStream getFileStream(String path){
		try{
			return new FileInputStream(path);
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getPath(String path){
		URL u = ResourceLoader.class.getResource(path);
		
		if(u == null)
			u = ResourceLoader.class.getResource("/"+path);
		
		return u.getPath();
	}
	
	public static  void writeString(String path, String str){
		//ensure that the file exists
		File f = new File(path);
		if(!f.exists()){
			f.getParentFile().mkdirs();
		}
		
		if(os == OS.Windows){
			str.replaceAll("\n", "\r\n");
		}else{
			str.replaceAll("\r\n", "\n");
		}
		
		//write the file
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
			out.write(str);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
