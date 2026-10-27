package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import java.util.Locale

class UpgradeHubStreakWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val PREFS_NAME = "upgrade_hub_widget_prefs"
        private const val KEY_STREAK = "streak_days"
        private const val KEY_HOURS = "today_hours"
        private const val KEY_TARGET = "target_hours"
        private const val KEY_SLOT_TITLE = "next_slot_title"
        private const val KEY_SLOT_SUBTITLE = "next_slot_subtitle"

        fun syncAndBroadcastWidget(
            context: Context,
            streakDays: Int,
            todayHours: Float,
            targetHours: Float,
            nextSlotTitle: String,
            nextSlotSubtitle: String
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_STREAK, streakDays)
                .putFloat(KEY_HOURS, todayHours)
                .putFloat(KEY_TARGET, targetHours)
                .putString(KEY_SLOT_TITLE, nextSlotTitle)
                .putString(KEY_SLOT_SUBTITLE, nextSlotSubtitle)
                .apply()

            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, UpgradeHubStreakWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            for (id in ids) {
                updateAppWidget(context, manager, id)
            }
        }

        fun requestPinWidget(context: Context): Boolean {
            val manager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, UpgradeHubStreakWidgetProvider::class.java)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported) {
                manager.requestPinAppWidget(provider, null, null)
            } else {
                false
            }
        }

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val streak = prefs.getInt(KEY_STREAK, 5).coerceAtLeast(1)
            val hours = prefs.getFloat(KEY_HOURS, 6.0f)
            val target = prefs.getFloat(KEY_TARGET, 12.0f)
            val slotTitle = prefs.getString(KEY_SLOT_TITLE, "Next Slot: Physics • Electrostatics & PYQs")
                ?: "Next Slot: Physics • Electrostatics & PYQs"
            val slotSubtitle = prefs.getString(KEY_SLOT_SUBTITLE, "3-Hour Deep Work Block • Tap to Launch")
                ?: "3-Hour Deep Work Block • Tap to Launch"

            val views = RemoteViews(context.packageName, R.layout.widget_streak_next_slot)
            views.setTextViewText(R.id.widget_streak_text, "🔥 $streak Day Streak")
            views.setTextViewText(
                R.id.widget_hours_text,
                "${String.format(Locale.ENGLISH, "%.1f", hours)}h / ${target.toInt()}h"
            )
            views.setTextViewText(R.id.widget_slot_title, slotTitle)
            views.setTextViewText(R.id.widget_slot_subtitle, slotSubtitle)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
