#version 330

// About the file:
// The vertex shader is the first program in our shaders
// that will handle any form of the rendering pipeline
// so in the vertex shader we have to setup the coordinate systems
// and get things sorted for the fragment shader to draw

// In this specific shader, we setup the position (lines 33 & 36)
// and calculate then pass the texture coordinates (lines 39 - 48)


uniform mat4 PROJECTION_MAT; //projection is the rendering style for the camera
uniform mat4 VIEW_MAT; //view is the camera position, rotation, and scale
uniform mat4 MODEL_MAT; //model is the individual model position, rotation, and scale.

uniform int SUB_TEXTURE; //needed to calculate for sub textures
uniform int FLIP_X; //if the texture should be flipped horizontally
uniform int FLIP_Y; //if the texture should be flipped vertically

uniform vec2 SUB_SIZE; //the sub size of the texture
uniform vec2 TEXTURE_SIZE; //the size of the whole texture
uniform vec2 TEXTURE_OFFSET; //the offset of the sub texture
uniform vec2 TEXTURE_REPEAT; //the amount of times a texture wants to repeat

//vertex attribs
layout(location = 0) in vec3 IN_POSITION; //the vertex position
layout(location = 1) in vec2 IN_TEXCOORD; //the texture coordinate

//the texture coord needs to be passed to the fragment shader
out vec2 OUT_TEXCOORD;

void main(){
	//model position = model matrix * vertex position
	vec4 mvpos = MODEL_MAT * vec4(IN_POSITION, 1.0); //just converts the vector to a point
	
	//output the vertex position as a product of the projection, view, and model vertex position
	gl_Position = PROJECTION_MAT * VIEW_MAT * mvpos;
	
	//modifiable texture coordinates
	vec2 MOD_TEXCOORD = IN_TEXCOORD;
		
	//flip x texcoord if requested
	if(FLIP_X == 1){
		//then mirror the texture coord from 1 - X
		//(texture coords are between 0 - 1)
		MOD_TEXCOORD.x = 1 - MOD_TEXCOORD.x;
	}
	
	//flip the y texcoord if requested
	if(FLIP_Y == 1){
		//then mirror the texture coord from 1 - Y
		//(texture coords are between 0 - 1)
		MOD_TEXCOORD.y = 1 - MOD_TEXCOORD.y;
	}
	
	//if the texture is a sub texture, we need to update the texture coord
	if(SUB_TEXTURE == 1){
		//we do this by getting the offset / whole texture size to get the top left corner 
		//where the sub texture starts.
		//then add the sub size relative to the whole image (subSize / texSize)
		//then multiply it by the texcoord value so we get the correct corner  
		OUT_TEXCOORD = ((TEXTURE_OFFSET / TEXTURE_SIZE) + (SUB_SIZE / TEXTURE_SIZE) * MOD_TEXCOORD);
	}else{ //if the texture isn't a sub texture
		//modify by the repeating amount
		MOD_TEXCOORD /= TEXTURE_REPEAT;
		
		//pass the modified texture coordinates
		OUT_TEXCOORD = MOD_TEXCOORD;
	}
}