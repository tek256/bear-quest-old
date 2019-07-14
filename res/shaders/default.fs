#version 330

uniform sampler2D texture_sampler; //the texture representation

in vec2 OUT_TEXCOORD; //the pass texture coord

out vec4 out_color; //the output color of the fragment 

void main(){
	//sample the texture at the texture coord
	vec4 tex_color = texture(texture_sampler, OUT_TEXCOORD);
	
	if(tex_color.a == 0)
		discard;
	
	//output the texture sample
	out_color = tex_color;
}