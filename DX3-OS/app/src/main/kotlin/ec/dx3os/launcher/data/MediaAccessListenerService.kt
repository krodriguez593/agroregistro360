package ec.dx3os.launcher.data

import android.service.notification.NotificationListenerService

/**
 * Requisito de Android para que una app no privilegiada lea las sesiones multimedia activas
 * (MediaSessionManager.getActiveSessions). No sobrescribe onNotificationPosted: no lee,
 * procesa ni guarda notificaciones.
 */
class MediaAccessListenerService : NotificationListenerService()
