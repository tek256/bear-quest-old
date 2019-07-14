package io.tek256.core.input;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

public class Joystick {
	private static ArrayList<Joystick> connected;
	
	static{
		connected = new ArrayList<>();
	}

	//glfw id of the controller
	private int id;
	
	//type (easier find of axes/buttons)
	private JoystickType type;
	
	//axes 
	private float[] axes;
	private float[] axesLastFrame;
	//buttons 
	private byte[] buttons;
	private byte[] buttonsLastFrame;
	
	public Joystick(int id, JoystickType type, int axisCount, int buttonCount){
		this.id = id;
		this.type = type;
		this.axes = new float[axisCount];
		this.axesLastFrame = new float[axisCount];
		this.buttons = new byte[buttonCount];
		this.buttonsLastFrame = new byte[buttonCount];
	}
	
	/** Get the ID of the joystick
	 * 
	 * @return the id of the joystick
	 */
	public int getId(){
		return id;
	}

	/**
	 * Update the values of the joystick
	 */
	public void update(){
		this.buttonsLastFrame = this.buttons.clone();
		this.axesLastFrame = this.axes.clone();
		
		FloatBuffer abuf = GLFW.glfwGetJoystickAxes(this.id);
		ByteBuffer bbuf = GLFW.glfwGetJoystickButtons(this.id);
		for(int i=0;i<abuf.capacity();i++)
			this.axes[i] = abuf.get(i);
		for(int i=0;i<bbuf.capacity();i++)
			this.buttons[i] = bbuf.get(i);
	}
	
	/** 
	 * 
	 * @param button the button on the controller
	 * @return if the button is down this frame and not last
	 */
	public boolean isClicked(int button){
		return this.buttons[button] == 1 && this.buttonsLastFrame[button] == 0;
	}
	
	public boolean isClicked(String buttonName){
		return isClicked(type.getButton(buttonName));
	}
	
	/**
	 * 
	 * @param button the button on the controller
	 * @return if the button is down
	 */
	
	public boolean isPressed(int button){
		return this.buttons[button] == 1;
	}
	
	/**
	 * 
	 * @param button the button on the controller
	 * @return if the button is not down
	 */
	public boolean isReleased(int button){
		return this.buttons[button] == 0;
	}
	
	/**
	 * 
	 * @param axis axis on the joystick/controller
	 * @return the axis value
	 */
	public float getAxis(int axis){
		return this.axes[axis];
	}
	
	/**
	 * 
	 * @param axis axis on the joystick/controller
	 * @return if the axis value is higher than the last frame
	 */
	public boolean isAxisIncreasing(int axis){
		return this.axes[axis] > this.axesLastFrame[axis];
	}
	
	public boolean isAxisIncreasing(String axis){
		int axisIndex = getAxisIndex(axis);
		return this.axes[axisIndex] > this.axesLastFrame[axisIndex];
	}
	
	public boolean isAxisPositiveIncreasing(String axis){
		int axisIndex = getAxisIndex(axis);
		return axes[axisIndex] > 0 && isAxisIncreasing(axisIndex);
	}
	
	public boolean isAxisPositiveIncreasing(int axis){
		return axes[axis] > 0 && isAxisIncreasing(axis);
	}

	public boolean isAxisMax(int axis){
		return axes[axis] >= 0.9;
	}
	
	public boolean isAxisMax(String axis){
		return axes[getAxisIndex(axis)] >= 0.9;
	}
	
	public boolean isAxisMin(int axis){
		return axes[axis] <=  -0.9;
	}
	
	public boolean isAxisMin(String axis){
		return axes[getAxisIndex(axis)] <= -0.9;
	}
	
	/**
	 * 
	 * @param axis axis on the joystick/controller
	 * @return if the axis value is lower than the last frame
	 */
	public boolean isAxisDecreasing(int axis){
		return this.axes[axis] < this.axesLastFrame[axis];
	}
	
	public boolean isAxisDecreasing(String name){
		int axisIndex = getAxisIndex(name);
		return this.axes[axisIndex] < this.axesLastFrame[axisIndex];
	}
	
	public boolean isAxisNegativeDecreasing(String name){
		int axisIndex = getAxisIndex(name);
		return axes[axisIndex] < 0 && isAxisDecreasing(axisIndex); 
	}
	
	public boolean isAxisNegativeDecreasing(int axis){
		return axes[axis] < 0 && isAxisDecreasing(axis);
	}
	
	/** WARNING: USE ONLY FOR DEBUGGING/MONITORING
	 * 
	 * @return all buttons values in a string
	 */
	public String getButtons(){
		StringBuilder but = new StringBuilder();
		ByteBuffer b = GLFW.glfwGetJoystickButtons(this.id);
		for(int i=0;i<b.capacity();i++)
			but.append(b.get(i)+" ");
		return but.toString();
	}
	
	/** WARNING: USE ONLY FOR DEBUGGING/MONITORING
	 * 
	 * @return axes values in a string (for debugging)
	 */
	public String getAxes(){
		StringBuilder but = new StringBuilder();
		for(float axis : this.axes)
			but.append(axis+" ");
		return but.toString();
	}
	
	/** Get the type of the controller
	 * 
	 * @return the type of controller (i.e Xbox or playstation)
	 */
	public JoystickType getType(){
		return this.type;
	}
	
	public boolean isAxisNonZero(String axisName){
		return axes[type.getAxis(axisName)] != 0;
	}
	
	public boolean isAxisNonZero(int axis){
		return axes[axis] != 0;
	}
	
	public float getAxis(String axisName){
		return axes[type.getAxis(axisName)];
	}
	
	public boolean getButton(String buttonName){
		return buttons[type.getButton(buttonName)] == 1;
	}
	
	public int getAxisIndex(String axisName){
		return type.getAxis(axisName);
	}
	
	public int getButtonIndex(String buttonName){
		return type.getButton(buttonName);
	}
	
	/** Setup a specific joystick that is connected
	 * 
	 * @param joy the joystick id to setup
	 */
	public static void setupJoystick(int joy){
		//make sure the joystick is connected
		if(!GLFW.glfwJoystickPresent(joy))
			return;
		
		//get the name of the joystick and normalize the casing
		String name = GLFW.glfwGetJoystickName(joy).toLowerCase();
		JoystickType type = JoystickType.OTHER;
		
		//check the name for the type
		if(name.contains("xbox"))
			type = JoystickType.XBOX360;
		else if(name.contains("playstation"))
			type = JoystickType.PLAYSTATION;
		
		//setup the axis size for the controller
		FloatBuffer buf  = GLFW.glfwGetJoystickAxes(joy);
		int axisCount = buf.capacity();
		
		//setup the button size for the controller
		ByteBuffer bbuf = GLFW.glfwGetJoystickButtons(joy);
		int buttonCount = bbuf.capacity();
		
		//add the controller to the static array for usage
		connected.add(new Joystick(joy, type, axisCount, buttonCount));
	}
	
	/**
	 * setup all connected joysticks
	 */
	public static void setup(){
		int[] joys = new int[]{
				GLFW.GLFW_JOYSTICK_1,
				GLFW.GLFW_JOYSTICK_2,
				GLFW.GLFW_JOYSTICK_3,
				GLFW.GLFW_JOYSTICK_4,
				GLFW.GLFW_JOYSTICK_5,
				GLFW.GLFW_JOYSTICK_6,
				GLFW.GLFW_JOYSTICK_7,
				GLFW.GLFW_JOYSTICK_8,
				GLFW.GLFW_JOYSTICK_9,
				GLFW.GLFW_JOYSTICK_10,
				GLFW.GLFW_JOYSTICK_11,
				GLFW.GLFW_JOYSTICK_12,
				GLFW.GLFW_JOYSTICK_13,
				GLFW.GLFW_JOYSTICK_14,
				GLFW.GLFW_JOYSTICK_15,
				GLFW.GLFW_JOYSTICK_16,
		};
		for(int joy : joys){
			if(GLFW.glfwJoystickPresent(joy)){
				setupJoystick(joy);
			}
		}
	}
	
	/**
	 * 
	 * @return the amount of joysticks/controllers connected 
	 */
	public static int size(){
		return connected.size();
	}
	
	
	/** Get a specific Joystick/Controller
	 * 
	 * @param index joystick connection ID
	 * @return jostick at index
	 */
	public static Joystick get(int index){
		return connected.get(index);
	}
	
	/**
	 * Update all connected joysticks
	 */
	public static void updateAll(){
		for(Joystick joy : connected)
			joy.update();
	}
	
	/** Add a joystick to the connected devices
	 * 
	 * @param joystick add an already created joystick
	 */
	public static void addJoystick(Joystick joystick){
		connected.add(joystick);
	}
	
	/** Remove a connected joystick by object reference
	 * 
	 * @param joystick remove a joystick by object
	 */
	public static void removeJoystick(Joystick joystick){
		if(!connected.contains(joystick))
			return;
		connected.remove(joystick);
	}
	
	/** Remove a connected joystick by integer id
	 * 
	 * @param id id reference of the joystick
	 */
	public static void removeJoystick(int id){
		connected.forEach((joy) -> {
			if(joy.id == id) 
				connected.remove(joy);
		});
	}
	
	public static void destroy(){
		for(Joystick joystick : connected){
			joystick.axes = null;
			joystick.axesLastFrame = null;
			
			joystick.buttons = null;
			joystick.buttonsLastFrame = null;
		}
		
		connected.clear();
		connected = null;
	}
	
	public static  enum JoystickType{
		XBOX360(0,1,2,3,4,5,6,7,8,9,10,11,12,13, 0,1,2,3,4,5, "Xbox"),
		PLAYSTATION(), //TODO map PS4 controller
 		OTHER;
		
		public String controllerName;
		public int a,b,x,y,lb,rb,select,start,lc,rc,dup,dright,ddown,dleft;
		public int lefthorizontal,leftvertical,righthorizontal,rightvertical;
		public int lefttrigger,righttrigger;
		
		JoystickType(int a, int b, int x, int y, int lb, int rb, int select, int start, int lc, int rc, 
				int dup, int dright, int ddown, int dleft, int lefthorizontal, int leftvertical, 
				int righthorizontal, int rightvertical, int lefttrigger,int righttrigger, String controllerName){
			this.a = a;
			this.b = b;
			this.x = x;
			this.y = y;
			this.lb = lb;
			this.rb = rb;
			this.select = select;
			this.start = start;
			this.lc = lc;
			this.rc = rc;
			this.dup = dup;
			this.dright = dright;
			this.ddown = ddown;
			this.dleft = dleft;
			this.lefthorizontal = lefthorizontal;
			this.leftvertical = leftvertical;
			this.righthorizontal = righthorizontal;
			this.rightvertical = rightvertical;
			this.lefttrigger = lefttrigger;
			this.righttrigger = righttrigger;
			this.controllerName = "Xbox";
		}
		
		JoystickType(){
			this.a = 0;
			this.a = 1;
			this.b = 2;
			this.x = 3;
			this.y = 4;
			this.lb = 5;
			this.rb = 6;
			this.select = 7;
			this.start = 8;
			this.lc = 9;
			this.rc = 10;
			this.dup = 11;
			this.dright = 12;
			this.ddown = 13;
			this.dleft = 14;
			
			this.lefthorizontal = 0;
			this.leftvertical = 1;
			this.righthorizontal = 2;
			this.rightvertical = 3;
			this.lefttrigger = 4;
			this.righttrigger = 5;
			
			controllerName = "Xbox";
		}
		
		public int getAxis(String axisName){
			switch(axisName.toLowerCase()){
			case "left vertical":
			case "left y":
				return leftvertical;
				
			case "left horizontal":
			case "left x":
				return lefthorizontal;
				
			case "right vertical":
			case "right y":
				return rightvertical;
				
			case "right horizontal":
			case "right x":
				
			default:
				return -1;
			}
		}
		
		public int getButton(String buttonName){
			if(controllerName.toLowerCase().equals("xbox")){
				switch(buttonName.toLowerCase()){
				case "a":
					return a;
					
				case "x":
					return x;
					
				case "y":
					return y;
					
				case "b":
					return b;
					
				case "left trigger":
				case "l2":
				case "lt":
					return lefttrigger;
					
				case "left bumper":
				case "lb":
				case "l1":
					return lb;
					
				case "right trigger":
				case "rt":
				case "r2":
					return righttrigger;
				
				case "right bumper":
				case "rb":
				case "r1":
					return rb;
				
				case "select":
					return select;
				
				case "start":
					return start;
					
				case "dpad down":
				case "pad down":
				case "down":
					return ddown;
				
				case "dpad left":
				case "pad left":
				case "left":
					return dleft;
					
					
				case "dpad right":
				case "pad right":
				case "right":
					return dright;
					
				case "dpad up":
				case "pad up":
				case "up":
					return dup;
				}
			}else if(controllerName.toLowerCase().equals("playstation")){ //TODO add playstation & generics support
				switch(buttonName.toLowerCase()){
				
				}
			}else{
				
			}
			return -1;
		}
	}
	
}
