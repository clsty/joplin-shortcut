package io.github.clsty.joplinshortcut.shortcut

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

object ShortcutHelper {
    fun createShortcut(context: Context, id: String, title: String, joplinId: String, type: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("joplin://$type/$joplinId"))
            .setPackage(context.packageName)
        val shortcut = ShortcutInfoCompat.Builder(context, "joplin_${type}_$joplinId")
            .setShortLabel(title.take(25))
            .setLongLabel(title)
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }
}
