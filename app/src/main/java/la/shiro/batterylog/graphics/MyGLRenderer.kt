package la.shiro.batterylog.graphics;

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import la.shiro.batterylog.config.TAG

class MyGLRenderer : GLSurfaceView.Renderer {
    private lateinit var mTriangle: Triangle
    private lateinit var mPyramid: Pyramid
    private lateinit var mSquare: Square

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private var width: Int = 0
    private var height: Int = 0


    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        //GLES20.glClearColor(153.0f, 51.0f, 0.0f, 1.0f)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Unless the structure (the original coordinates) of the shapes you use in your program change during the course of execution,
        // you should initialize them in the onSurfaceCreated() method of your renderer for memory and processing efficiency.
        mTriangle = Triangle(
            Triple(0.0f, 0.622008459f, 0.0f),      // top
            Triple(-0.5f, -0.311004243f, 0.0f),    // bottom left
            Triple(0.5f, -0.311004243f, 0.0f),      // bottom right
            Color(1.0f, 0.0f, 0.0f),    //Color(0.63671875f, 0.76953125f, 0.22265625f)
            Color(0.0f, 1.0f, 0.0f),
            Color(0.0f, 0.0f, 1.0f),
        )
        mPyramid = Pyramid()
        mSquare = Square()
    }

    // create a model matrix for the triangle
    private val mModelMatrix = FloatArray(16)

    // create a temporary matrix for calculation purposes,
    // to avoid the same matrix on the right and left side of multiplyMM later
    // see https://stackoverflow.com/questions/13480043/opengl-es-android-matrix-transformations#comment18443759_13480364
    private var mTempMatrix = FloatArray(16)

    override fun onDrawFrame(unused: GL10) {
        //Timber.d("Rendering!")

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT); // Draw background color

        // Set the camera position (View matrix). eye = camera position. center = target position
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Draw a gazillion of tiny rotating pyramids
        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        val dx = 1.2f
        val dy = 1.3f
        val (rows, cols) = arrayOf(75, 81)
        val (rowsExcess, colsExcess) = arrayOf(
            0,
            0
        ) // How many rows,cols will be drawn outside of the display area. These still consume resources.

        for (i in 0 until rows + rowsExcess) {
            for (j in 0 until cols + colsExcess) {
                Matrix.setIdentityM(mModelMatrix, 0);
                Matrix.scaleM(mModelMatrix, 0, 0.02f, 0.02f, 0.02f) //make it smaller
                Matrix.translateM(mModelMatrix, 0, 48.0f, 48.0f, 0.0f)  //move to top-left corner

                Matrix.translateM(mModelMatrix, 0, -j * dx, -i * dy, 0.0f)

                Matrix.rotateM(mModelMatrix, 0, angle + (i + j) * 5, 0.0f, 1.0f, 0.0f)
                Matrix.rotateM(mModelMatrix, 0, angle + (i + j) * 5, 1.0f, 0.0f, 0.0f)

                // combine the model with the view matrix
                Matrix.multiplyMM(vPMatrix, 0, viewMatrix, 0, mModelMatrix, 0);

                // combine the model-view with the projection matrix
                mTempMatrix = vPMatrix.clone();
                Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, mTempMatrix, 0);

                // Draw pyramid
                mPyramid.draw(vPMatrix); //HINT! I can reuse mPyramid everywhere!
            }
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        Log.d(TAG, "GPU rendering surface: $width x $height")
        this.width = width
        this.height = height

        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)

    }


    companion object {
        fun loadShader(type: Int, shaderCode: String): Int {

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            return GLES20.glCreateShader(type).also { shader ->

                // add the source code to the shader and compile it
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }
}
