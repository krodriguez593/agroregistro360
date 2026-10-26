package ec.dx3os.launcher.data

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.LruCache
import ec.dx3os.core.launcher.ShortcutTarget

data class LaunchableApp(val label: String, val component: ComponentName) {
    val target get() = ShortcutTarget(component.packageName, component.className)
}

/**
 * Catálogo de apps instaladas. La lista se calcula bajo demanda y se invalida cuando se
 * instala/desinstala un paquete. Los iconos se cargan perezosamente en una caché LRU acotada.
 */
class AppCatalog(private val context: Context) : Observable<List<LaunchableApp>>(emptyList()) {
    private val pm: PackageManager = context.packageManager
    private val icons = LruCache<ComponentName, Drawable>(48)

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            icons.evictAll()
            reload()
        }
    }

    fun reload() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val list = pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { LaunchableApp(it.loadLabel(pm).toString(), it.component()) }
            .sortedBy { it.label.lowercase() }
        publish(list)
    }

    fun icon(component: ComponentName): Drawable? = icons.get(component) ?: try {
        pm.getActivityIcon(component).also { icons.put(component, it) }
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    fun trimIcons() = icons.evictAll()

    fun isAvailable(t: ShortcutTarget): Boolean = try {
        pm.getActivityInfo(ComponentName(t.packageName, t.activityName), 0); true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    fun find(t: ShortcutTarget): LaunchableApp? = value.firstOrNull { it.target == t }

    fun launch(component: ComponentName): Boolean = start(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
    )

    /** Apps capaces de abrir un destino geográfico (navegadores GPS). */
    fun navigationApps(): List<LaunchableApp> = resolveToLaunchable(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")))

    /** Apps que publican un MediaBrowserService (reproductores de música). */
    fun mediaApps(): List<LaunchableApp> =
        pm.queryIntentServices(Intent("android.media.browse.MediaBrowserService"), 0)
            .map { it.serviceInfo.packageName }.distinct()
            .mapNotNull { pkg -> pm.getLaunchIntentForPackage(pkg)?.component }
            .map { LaunchableApp(labelOf(it), it) }
            .sortedBy { it.label.lowercase() }

    fun openDialer(): Boolean = start(Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

    fun start(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true
    } catch (_: android.content.ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }

    private fun resolveToLaunchable(intent: Intent): List<LaunchableApp> =
        pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName }.distinct()
            .mapNotNull { pkg -> pm.getLaunchIntentForPackage(pkg)?.component }
            .map { LaunchableApp(labelOf(it), it) }

    private fun labelOf(c: ComponentName): String = try {
        pm.getActivityInfo(c, 0).loadLabel(pm).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        c.packageName
    }

    private fun ResolveInfo.component() = ComponentName(activityInfo.packageName, activityInfo.name)

    override fun onActive() {
        context.registerReceiver(packageReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        })
        reload()
    }

    override fun onInactive() {
        context.unregisterReceiver(packageReceiver)
    }
}
