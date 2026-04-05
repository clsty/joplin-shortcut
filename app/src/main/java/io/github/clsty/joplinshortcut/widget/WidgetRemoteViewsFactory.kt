package io.github.clsty.joplinshortcut.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import io.github.clsty.joplinshortcut.R

class WidgetRemoteViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    private val items = mutableListOf<Pair<String, String>>() // id to title

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // Data loading is handled by the database; for simplicity items remain empty until Room integration is added
    }

    override fun onDestroy() { items.clear() }

    override fun getCount() = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val (id, title) = items.getOrElse(position) { "" to "Item $position" }
        return RemoteViews(context.packageName, R.layout.widget_list_item).apply {
            setTextViewText(R.id.widget_item_title, title)
            val fillIntent = Intent(Intent.ACTION_VIEW, Uri.parse("joplin://note/$id"))
            setOnClickFillInIntent(R.id.widget_item_root, fillIntent)
        }
    }

    override fun getLoadingView() = null
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = position.toLong()
    override fun hasStableIds() = true
}
