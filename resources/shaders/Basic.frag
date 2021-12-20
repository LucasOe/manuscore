#version 430 core

// Color from vertex shader
in vec4 vColor;

// Fragment color
out vec4 FragColor;

void main()
{
	FragColor = vColor;
}
