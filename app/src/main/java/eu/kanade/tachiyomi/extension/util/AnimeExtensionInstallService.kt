package eu.kanade.tachiyomi.extension.util

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.data.preference.PreferenceValues
import eu.kanade.tachiyomi.extension.installer.InstallerAnime
import eu.kanade.tachiyomi.extension.installer.PackageInstallerInstallerAnime
import eu.kanade.tachiyomi.extension.installer.ShizukuInstallerAnime
import eu.kanade.tachiyomi.extension.util.AnimeExtensionInstaller.Companion.EXTRA_DOWNLOAD_ID
import eu.kanade.tachiyomi.util.system.logcat
import eu.kanade.tachiyomi.util.system.notificationBuilder
import logcat.LogPriority

class AnimeExtensionInstallService : Service() {

    private var installer: InstallerAnime? = null

    override fun onCreate() {
        super.onCreate()
        val notification = notificationBuilder(Notifications.CHANNEL_EXTENSIONS_UPDATE) {
            setSmallIcon(R.drawable.ic_ani)
            setAutoCancel(false)
            setOngoing(true)
            setShowWhen(false)
            setContentTitle(getString(R.string.ext_install_service_notif))
            setProgress(100, 100, true)
        }.build()
        startForeground(Notifications.ID_EXTENSION_INSTALLER, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uri = intent?.data
        val id = intent?.getLongExtra(EXTRA_DOWNLOAD_ID, -1)?.takeIf { it != -1L }
        val installerUsed = intent?.getSerializableExtra(EXTRA_INSTALLER) as? PreferenceValues.ExtensionInstaller
        if (uri == null || id == null || installerUsed == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (installer == null) {
            installer = when (installerUsed) {
                PreferenceValues.ExtensionInstaller.PACKAGEINSTALLER -> PackageInstallerInstallerAnime(this)
                PreferenceValues.ExtensionInstaller.SHIZUKU -> ShizukuInstallerAnime(this)
                else -> {
                    logcat(LogPriority.ERROR) { "Not implemented for installer $installerUsed" }
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }
        installer!!.addToQueue(id, uri)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        installer?.onDestroy()
        installer = null
    }

    override fun onBind(i: Intent?): IBinder? = null

    companion object {
        private const val EXTRA_INSTALLER = "EXTRA_INSTALLER"

        fun getIntent(
            context: Context,
            downloadId: Long,
            uri: Uri,
            installer: PreferenceValues.ExtensionInstaller,
        ): Intent {
            return Intent(context, AnimeExtensionInstallService::class.java)
                .setDataAndType(uri, AnimeExtensionInstaller.APK_MIME)
                .putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                .putExtra(EXTRA_INSTALLER, installer)
        }
    }
}
