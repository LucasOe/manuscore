#version 430 core

in vec3 ObjectColor;
in vec3 Normal;
in vec3 FragPos;

// Fragment color
out vec4 FragColor;

void main()
{
	float ambientStrength = 0.1;
	vec3 lightColor = vec3(1, 1, 1);
	vec3 lightPos = vec3(0, 0, 0);

	vec3 ambient = ambientStrength * lightColor;
	vec3 norm = normalize(Normal);
	vec3 lightDir = normalize(lightPos - FragPos);
	float diff = max(dot(norm, lightDir), 0.0);
	vec3 diffuse = diff * lightColor;

	vec3 result = (ambient + diffuse) * ObjectColor;
	FragColor = vec4(result, 1.0);
}
