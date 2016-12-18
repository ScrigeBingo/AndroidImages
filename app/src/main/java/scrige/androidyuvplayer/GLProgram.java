package scrige.androidyuvplayer;

import android.opengl.GLES20;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GLProgram {

    static float[] squareVertices = { -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, }; // fullscreen

    static float[] squareVertices1 = { -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, }; // left-top

    static float[] squareVertices2 = { 0.0f, -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, }; // right-bottom

    static float[] squareVertices3 = { -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, }; // left-bottom

    static float[] squareVertices4 = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, }; // right-top

    private static float[] coordVertices = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, };// whole-texture

    private static final String VERTEX_SHADER = "attribute vec4 vPosition;\n" + "attribute vec2 a_texCoord;\n"
            + "varying vec2 tc;\n" + "void main() {\n" + "gl_Position = vPosition;\n" + "tc = a_texCoord;\n" + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n" + "uniform sampler2D tex_y;\n"
            + "uniform sampler2D tex_u;\n" + "uniform sampler2D tex_v;\n" + "varying vec2 tc;\n" + "void main() {\n"
            + "vec4 c = vec4((texture2D(tex_y, tc).r - 16./255.) * 1.164);\n"
            + "vec4 U = vec4(texture2D(tex_u, tc).r - 128./255.);\n"
            + "vec4 V = vec4(texture2D(tex_v, tc).r - 128./255.);\n" + "c += V * vec4(1.596, -0.813, 0, 0);\n"
            + "c += U * vec4(0, -0.392, 2.017, 0);\n" + "c.a = 1.0;\n" + "gl_FragColor = c;\n" + "}\n";


    // program id
    private int program;
    // window position
    public final int mWinPosition;
    // texture id
    private int textureI;
    private int textureII;
    private int textureIII;
    // texture index in gles
    private int tIindex;
    private int tIIindex;
    private int tIIIindex;
    // vertices on screen
    private float[] vertices;
    // handles
    private int positionHandle = -1, coordHandle = -1;
    private int yhandle = -1, uhandle = -1, vhandle = -1;
    private int ytid = -1, utid = -1, vtid = -1;
    // vertices buffer
    private ByteBuffer vertice_buffer;
    private ByteBuffer coord_buffer;
    // video width and height
    private int video_width = -1;
    private int video_height = -1;
    // flow control
    private boolean isProgBuilt = false;


    public GLProgram(int position) {
        if (position < 0 || position > 4) {
            throw new RuntimeException("Index can only be 0 to 4");
        }
        mWinPosition = position;
        setup(mWinPosition);
    }

    /**
     * prepared for later use
     */
    public void setup(int position) {
        switch (mWinPosition) {
        case 1:
            vertices = squareVertices1;
            textureI = GLES20.GL_TEXTURE0;
            textureII = GLES20.GL_TEXTURE1;
            textureIII = GLES20.GL_TEXTURE2;
            tIindex = 0;
            tIIindex = 1;
            tIIIindex = 2;
            break;
        case 2:
            vertices = squareVertices2;
            textureI = GLES20.GL_TEXTURE3;
            textureII = GLES20.GL_TEXTURE4;
            textureIII = GLES20.GL_TEXTURE5;
            tIindex = 3;
            tIIindex = 4;
            tIIIindex = 5;
            break;
        case 3:
            vertices = squareVertices3;
            textureI = GLES20.GL_TEXTURE6;
            textureII = GLES20.GL_TEXTURE7;
            textureIII = GLES20.GL_TEXTURE8;
            tIindex = 6;
            tIIindex = 7;
            tIIIindex = 8;
            break;
        case 4:
            vertices = squareVertices4;
            textureI = GLES20.GL_TEXTURE9;
            textureII = GLES20.GL_TEXTURE10;
            textureIII = GLES20.GL_TEXTURE11;
            tIindex = 9;
            tIIindex = 10;
            tIIIindex = 11;
            break;
        case 0:
        default:
            vertices = squareVertices;
            textureI = GLES20.GL_TEXTURE0;
            textureII = GLES20.GL_TEXTURE1;
            textureIII = GLES20.GL_TEXTURE2;
            tIindex = 0;
            tIIindex = 1;
            tIIIindex = 2;
            break;
        }
    }

    public boolean isProgramBuilt() {
        return isProgBuilt;
    }

    public void buildProgram() {
        // TODO createBuffers(_vertices, coordVertices);
        if (program <= 0) {
            program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        }


        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        checkGlError("glGetAttribLocation vPosition");
        if (positionHandle == -1) {
            throw new RuntimeException("Could not get attribute location for vPosition");
        }
        coordHandle = GLES20.glGetAttribLocation(program, "a_texCoord");
        checkGlError("glGetAttribLocation a_texCoord");
        if (coordHandle == -1) {
            throw new RuntimeException("Could not get attribute location for a_texCoord");
        }

        /*
         * get uniform location for y/u/v, we pass data through these uniforms
         */
        yhandle = GLES20.glGetUniformLocation(program, "tex_y");
        checkGlError("glGetUniformLocation tex_y");
        if (yhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_y");
        }
        uhandle = GLES20.glGetUniformLocation(program, "tex_u");
        checkGlError("glGetUniformLocation tex_u");
        if (uhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_u");
        }
        vhandle = GLES20.glGetUniformLocation(program, "tex_v");
        checkGlError("glGetUniformLocation tex_v");
        if (vhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_v");
        }

        isProgBuilt = true;
    }

    /**
     * build a set of textures, one for R, one for G, and one for B.
     */
    public void buildTextures(Buffer y, Buffer u, Buffer v, int width, int height) {
        boolean videoSizeChanged = (width != video_width || height != video_height);
        if (videoSizeChanged) {
            video_width = width;
            video_height = height;
        }

        // building texture for Y data
        if (ytid < 0 || videoSizeChanged) {
            if (ytid >= 0) {
                GLES20.glDeleteTextures(1, new int[] { ytid }, 0);
                checkGlError("glDeleteTextures");
            }

            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            checkGlError("glGenTextures");
            ytid = textures[0];
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ytid);
        checkGlError("glBindTexture");
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, video_width, video_height, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y);
        checkGlError("glTexImage2D");
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // building texture for U data
        if (utid < 0 || videoSizeChanged) {
            if (utid >= 0) {

                GLES20.glDeleteTextures(1, new int[] { utid }, 0);
                checkGlError("glDeleteTextures");
            }
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            checkGlError("glGenTextures");
            utid = textures[0];
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, utid);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, video_width / 2, video_height / 2, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, u);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // building texture for V data
        if (vtid < 0 || videoSizeChanged) {
            if (vtid >= 0) {
                GLES20.glDeleteTextures(1, new int[] { vtid }, 0);
                checkGlError("glDeleteTextures");
            }
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            checkGlError("glGenTextures");
            vtid = textures[0];
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vtid);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, video_width / 2, video_height / 2, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, v);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    /**
     * render the frame
     * the YUV data will be converted to RGB by shader.
     */
    public void drawFrame() {
        GLES20.glUseProgram(program);
        checkGlError("glUseProgram");

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertice_buffer);
        checkGlError("glVertexAttribPointer mPositionHandle");
        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glVertexAttribPointer(coordHandle, 2, GLES20.GL_FLOAT, false, 8, coord_buffer);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(coordHandle);

        // bind textures
        GLES20.glActiveTexture(textureI);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ytid);
        GLES20.glUniform1i(yhandle, tIindex);

        GLES20.glActiveTexture(textureII);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, utid);
        GLES20.glUniform1i(uhandle, tIIindex);

        GLES20.glActiveTexture(textureIII);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vtid);
        GLES20.glUniform1i(vhandle, tIIIindex);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFinish();

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(coordHandle);
    }

    /**
     * create program and load shaders, fragment shader is very important.
     */
    public int createProgram(String vertexSource, String fragmentSource) {
        // create shaders
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        // just check

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    /**
     * create shader with given source.
     */
    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * these two buffers are used for holding vertices, screen vertices and texture vertices.
     */
    void createBuffers(float[] vert) {
        vertice_buffer = ByteBuffer.allocateDirect(vert.length * 4);
        vertice_buffer.order(ByteOrder.nativeOrder());
        vertice_buffer.asFloatBuffer().put(vert);
        vertice_buffer.position(0);

        if (coord_buffer == null) {
            coord_buffer = ByteBuffer.allocateDirect(coordVertices.length * 4);
            coord_buffer.order(ByteOrder.nativeOrder());
            coord_buffer.asFloatBuffer().put(coordVertices);
            coord_buffer.position(0);
        }
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(op + ": glError " + error);
        }
    }


}