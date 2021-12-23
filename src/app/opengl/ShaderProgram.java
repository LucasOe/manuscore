package app.opengl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.jogamp.opengl.GL2ES2;

/**
 * ShaderProgram erstellt einen Shader mit den angegebenen Vertx und Fragment Shader Dateien und speichert die ShaderProgramID.
 */
public class ShaderProgram {

	static int shaderProgramID;
	static GL2ES2 gl;

	/**
	* Konstruktor intialisiert die Instanzvariable.
	* @param gl	OpenGL Graphics Context
	*/
	public ShaderProgram(GL2ES2 gl) {
		ShaderProgram.gl = gl;
	}

	public int getShaderProgramID() {
		return shaderProgramID;
	}

	/**
	 * Löscht das ShaderProgram
	 */
	public void deleteShaderProgram() {
		gl.glDeleteProgram(shaderProgramID);
	}

	/**
	 * The shader program ID is stored in an instance variable of this program object.
	 * Lädt einen Vertex und Fragment Shader aus zwei Dateien und erstellt das shader program
	 * object auf der GPU.
	 * @param path						Verzeischnispfad zu den Shader Dateien
	 * @param vertexShaderFileName		Dateiname für den Vertex Shader
	 * @param fragmentShaderFileName	Dateiname für den Fragment Shader
	 */
	public static void loadShaderAndCreateProgram(String path, String vertexShaderFileName,
			String fragmentShaderFileName) {
		// Lädt den Vertex Shader aus der Datei
		String vertexShaderString;
		int vertexShader;
		String vertexPathAndFileName = path + vertexShaderFileName;
		vertexShaderString = loadFileToString(vertexPathAndFileName);
		vertexShader = createAndCompileShader(GL2ES2.GL_VERTEX_SHADER, vertexShaderString);

		// Lädt den Fragment Shader aus der Datei
		String fragmentShaderString;
		int fragmentShader;
		String fragmentPathAndFileName = path + fragmentShaderFileName;
		fragmentShaderString = loadFileToString(fragmentPathAndFileName);
		fragmentShader = createAndCompileShader(GL2ES2.GL_FRAGMENT_SHADER, fragmentShaderString);

		// Erstellt das shader program object auf der GPU und verbindet das Shader Objekt.
		// Die Program ID von der GPU wird in shaderProgramID gespeichert.
		shaderProgramID = gl.glCreateProgram();
		gl.glAttachShader(shaderProgramID, vertexShader);
		gl.glAttachShader(shaderProgramID, fragmentShader);

		// Linked das Program
		gl.glLinkProgram(shaderProgramID);

		// Die Shader Objekte auf der GPU können gelöscht werden
		gl.glDeleteShader(vertexShader);
		gl.glDeleteShader(fragmentShader);
	}

	/**
	 * Erstellt ein Shader Objekt auf der GPU und kompiliert einen Shader mit
	 * dem gegebenen Shader Typen. Überprüft den Kompilierungsstatus und gibt
	 * den Error log aus.
	 *
	 * @param shaderType	OpenGL-Shader Typ (z.B. GL2ES2.GL_VERTEX_SHADER)
	 * @param shaderString	String mit dem Shader Source Code
	 * @return 				OpenGL Shader ID auf der GPU
	 */
	private static int createAndCompileShader(int shaderType, String shaderString) {
		int shader;

		shader = gl.glCreateShader(shaderType);
		String[] shaderLines = new String[] { shaderString };
		int[] shaderLengths = new int[] { shaderLines[0].length() };
		gl.glShaderSource(shader, shaderLines.length, shaderLines, shaderLengths, 0);
		gl.glCompileShader(shader);

		// Überprüft den Kompilierungsstatus und gibt den Error log aus
		int[] compiled = new int[1];
		gl.glGetShaderiv(shader, GL2ES2.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] != 0) {
			//System.out.println("Shader compiled successfully.");
		} else {
			// Error: Log in der Konsole
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
	 * Lädt den Textinhalt in einem String
	 * @param fileName	Name der Textdatei mit dem relativen Pfad
	 * @return			Inhalt der Textdatei
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
