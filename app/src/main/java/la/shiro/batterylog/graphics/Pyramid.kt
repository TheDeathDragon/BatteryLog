package la.shiro.batterylog.graphics;


import android.opengl.GLES20
import la.shiro.batterylog.config.COORDINATES_PER_VERTEX
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Pyramid {

    private val pyramidCoordinates = floatArrayOf(
        // in counterclockwise order:
        //front
        -0.5f, -0.5f,  0.5f, //bottom left
        0.5f, -0.5f,  0.5f, //bottom right
        0.0f,  0.5f,  0.0f, //top

        //left side
        -0.5f, -0.5f,  0.5f,
        -0.5f, -0.5f, -0.5f,
        0.0f,  0.5f,  0.0f, //top

        //back
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.0f,  0.5f,  0.0f, //top

        //right side
        0.5f, -0.5f,  0.5f,
        0.5f, -0.5f, -0.5f,
        0.0f,  0.5f,  0.0f, //top

        //bottom of the pyramid (front)
        0.5f, -0.5f, -0.5f, //top
        -0.5f, -0.5f,  0.5f,
        0.5f, -0.5f,  0.5f,

        //bottom of the pyramid (behind)
        -0.5f, -0.5f,  0.5f,
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f, //top
    )

    // monochrome
    private val vertexColors: FloatArray
    init {
        val list = arrayListOf<Float>()
        for(i in 0 until pyramidCoordinates.size / COORDINATES_PER_VERTEX) {
            list.add(0.56f)
            list.add(0.56f)
            list.add(0.56f)
            list.add(1.0f)
        }
        vertexColors = list.toFloatArray()
    }
    private val drawingMode = GLES20.GL_TRIANGLE_STRIP

    private val vertexShaderCode =
        """
        /* This matrix member variable provides a hook to manipulate the coordinates of the objects that use this vertex shader */
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec4 vColor;
        
        varying vec4 uColor;
        
        void main() {
            // The matrix must be included as a modifier of gl_Position. Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            gl_Position = uMVPMatrix * vPosition;
            uColor = vColor;
        }
        """.trimIndent()

    private val fragmentShaderCode =
        """
        precision mediump float;
        varying vec4 uColor;
        void main() {
            gl_FragColor = uColor;
        }    
        """.trimIndent()

    private val vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(pyramidCoordinates.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(pyramidCoordinates)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private val colorBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(vertexColors.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vertexColors)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private val mProgram: Int

    init {
        val vertexShader: Int = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram() // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader) // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(mProgram) // creates OpenGL ES program executables
    }



    //Specify how to draw the pyramid
    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var vPMatrixHandle: Int = 0


    private val vertexCount: Int = pyramidCoordinates.size / COORDINATES_PER_VERTEX
    private val vertexStride: Int = COORDINATES_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition") // get handle to vertex shader's vPosition member
        GLES20.glEnableVertexAttribArray(positionHandle) // Enable a handle to the pyramid vertices

        GLES20.glVertexAttribPointer( // Prepare the pyramid coordinate data
            positionHandle, COORDINATES_PER_VERTEX, GLES20.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor")   // get handle to vertex shader's vColor member
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix") // get handle to shape's transformation matrix
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0) //Pass the MVP data into the shader

        GLES20.glDrawArrays(drawingMode, 0, vertexCount) // Draw the pyramid.
        GLES20.glDisableVertexAttribArray(positionHandle) // Disable vertex array
    }
}
