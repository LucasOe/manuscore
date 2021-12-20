#version 430 core

// Position and color of vertex
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec3 aColor;
// Projection and model-view matrix
layout (location = 0) uniform mat4 projectionMatrix;
layout (location = 1) uniform mat4 modelViewMatrix;

// Color of the vertex
out vec3 Normal;
out vec3 FragPos;
out vec3 ObjectColor;

void main() {
	// Calculation of the model-view-perspective transform
	FragPos = vec3(modelViewMatrix * vec4(aPos, 1.0));
	//Normal = mat3(transpose(inverse(modelViewMatrix))) * aNormal;
	Normal = aNormal;
	ObjectColor = aColor;

	gl_Position = projectionMatrix * modelViewMatrix * vec4(aPos, 1.0);
}
