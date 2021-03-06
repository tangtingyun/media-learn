#extension GL_OES_EGL_image_external : require
//必须 写的 固定的  意思   用采样器
//所有float类型数据的精度是lowp
varying vec2 vTexCoord;
//采样器  uniform static
uniform sampler2D vTexture;
void main(){
    //Opengl 自带函数
    vec4 rgba = texture2D(vTexture, vec2(vTexCoord.y, vTexCoord.x));
    gl_FragColor=vec4(rgba.r, rgba.g, rgba.b, rgba.a);
}