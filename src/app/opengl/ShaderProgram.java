package app.opengl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.jogamp.opengl.GL2ES2;

public class ShaderProgram {

	static int shaderProgramID;
	static GL2ES2 gl;

	public ShaderProgram(GL2ES2 gl) {
		ShaderProgram.gl = gl;
	}

	public int getShaderProgramID() {
		return shaderProgramID;
	}

	public void deleteShaderProgram() {
		gl.glDeleteProgram(shaderProgramID);
	}

	/**
	 * Loads a vertex and a fragment shader from two files,
	 * creates a shader program object on the GPU and links the shaders
	 * with this shader program.
	 * The shader program ID is stored in an instance variable of this program object.
	 *
	 * @param path						Directory path of the shader files
	 * @param vertexShaderFileName		File name (including file name extension) of the vertex shader
	 * @param fragmentShaderFileName	File name (including file name extension) of the fragment shader
	 */
	public static void loadShaderAndCreateProgram(String path, String vertexShaderFileName,
			String fragmentShaderFileName) {
		// Load shader from file and compile vertex shader
		String vertexShaderString;
		int vertexShader;
		String vertexPathAndFileName = path + vertexShaderFileName;
		//System.out.println("Loading vertex shader from file: " + vertexPathAndFileName);
		vertexShaderString = loadFileToString(vertexPathAndFileName);
		vertexShader = createAndCompileShader(GL2ES2.GL_VERTEX_SHADER, vertexShaderString);

		// Load shader from file and compile fragment shader
		String fragmentShaderString;
		int fragmentShader;
		String fragmentPathAndFileName = path + fragmentShaderFileName;
		//System.out.println("Loading fragment shader from file: " + fragmentPathAndFileName);
		fragmentShaderString = loadFileToString(fragmentPathAndFileName);
		fragmentShader = createAndCompileShader(GL2ES2.GL_FRAGMENT_SHADER, fragmentShaderString);

		// Create the shader program object on the GPU and attach the shader objects to it.
		// Stores the GPU's program ID in the instance variable shaderProgramID.
		shaderProgramID = gl.glCreateProgram();
		gl.glAttachShader(shaderProgramID, vertexShader);
		gl.glAttachShader(shaderProgramID, fragmentShader);

		// Link the program.
		gl.glLinkProgram(shaderProgramID);

		// The shader objects (on the GPU) can be deleted because they are linked with the program.
		gl.glDeleteShader(vertexShader);
		gl.glDeleteShader(fragmentShader);
	}

	/**
	 * Creates a shader object on the GPU, compiles a shader
	 * of a given shader type from a character string array.
	 * Checks compile status and outputs the error log.
	 *
	 * @param shaderType	OpenGL-Shader type (eg. GL2ES2.GL_VERTEX_SHADER)
	 * @param shaderString	Character string containing the shader source code
	 * @return 				OpenGL shader ID on the GPU
	 */
	private static int createAndCompileShader(int shaderType, String shaderString) {
		int shader;

		// Create shader object on GPU, associate the source code with it
		// and compile the shader string.
		shader = gl.glCreateShader(shaderType);
		String[] shaderLines = new String[] { shaderString };
		int[] shaderLengths = new int[] { shaderLines[0].length() };
		gl.glShaderSource(shader, shaderLines.length, shaderLines, shaderLengths, 0);
		gl.glCompileShader(shader);

		// Check the compile status and output an error log.
		int[] compiled = new int[1];
		gl.glGetShaderiv(shader, GL2ES2.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] != 0) {
			//System.out.println("Shader compiled successfully.");
		} else {
			// Compilation with error: Read and print compiler log to console
			int[] logLength = new int[1];
			gl.glGetShaderiv(shader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

			byte[] log = new byte[logLength[0]];
			gl.glGetShaderInfoLog(shader, logLength[0], (int[]) null, 0, log, 0);

			System.err.println("Error compiling shader: " + new String(log));
			System.exit(1);
		}
		return shader;
	}

	/**
	 * Loads the contents of a (text) file into a string variable.
	 * @param fileName name of the file including the (relative) path
	 * @return contents of the (text) file
	 *
	 */
	private static String loadFileToString(String fileName) {
		String fileContent = "";

		try {
			StringBuffer buffer = new StringBuffer();
			FileReader charStream = new FileReader(fileName);

			int bufferItem = charStream.read();
			while (bufferItem != -1) {
				buffer.append((char) bufferItem);
				bufferItem = charStream.read();
			}
			charStream.close();
			fileContent = buffer.toString();
		} catch (FileNotFoundException e) {
			System.err.println("File \"" + fileName + "\" not found!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("IO Expection encountered when reading file!");
			System.exit(1);
		}
		return fileContent;
	}
}
