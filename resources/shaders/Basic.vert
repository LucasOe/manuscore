#version 430 core

// Position and color of vertex
layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec3 color;
// Projection and model-view matrix
layout (location = 0) uniform mat4 projectionMatrix;
layout (location = 1) uniform mat4 modelViewMatrix;

// Color of the vertex
out vec4 vColor;

void main() {
	// Calculation of the model-view-perspective transform
	gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);

	vColor = vec4(normal, 1.0);
}
