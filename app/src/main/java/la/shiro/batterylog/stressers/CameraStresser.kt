
package la.shiro.batterylog.stressers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.add
import androidx.fragment.app.commit
import la.shiro.batterylog.R
import la.shiro.batterylog.fragment.CameraFragment

class CameraStresser(context: Context, private val fragmentManager: FragmentManager) : Stresser(context) {

    override fun permissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    override fun start() {
        super.start()
        fragmentManager.commit {
            add<CameraFragment>(R.id.camera_preview_frag_container_view)
        }
    }

    override fun stop() {
        super.stop()
        val fragment = fragmentManager.findFragmentById(R.id.camera_preview_frag_container_view)!!
        fragmentManager.commit {
            remove(fragment)
        }
    }
}
