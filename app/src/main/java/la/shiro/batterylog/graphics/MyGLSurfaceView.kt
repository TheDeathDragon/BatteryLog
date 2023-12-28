package la.shiro.batterylog.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MyGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {
    private val renderer: MyGLRenderer

    init {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = MyGLRenderer()

        // Render the view only when there is a change in the drawing data
        // This setting prevents the GLSurfaceView frame from being redrawn until you call requestRender(),
        // which is more efficient for this sample app.
        //renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)
    }
}
