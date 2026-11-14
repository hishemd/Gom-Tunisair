package com.newhtcdridi.weather

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(12, 14, 18)
        window.navigationBarColor = Color.rgb(12, 14, 18)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 40, 24, 40)
            setBackgroundColor(Color.rgb(12, 14, 18))
        }

        root.addView(
            text("HTC DIAMOND", 18f, Color.LTGRAY, true)
        )

        root.addView(
            text("3D WEATHER", 12f, Color.GRAY)
        )

        val clock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 25)
        }

        val hour = text("10", 76f, Color.WHITE, true)
        val minute = text("28", 76f, Color.WHITE, true)

        clock.addView(hour)
        clock.addView(
            text(" : ", 48f, Color.GRAY)
        )
        clock.addView(minute)

        root.addView(clock)

        root.addView(
            text("SATURDAY  |  NOVEMBER 14", 14f, Color.GRAY)
        )

        root.addView(
            text("TUNIS", 24f, Color.WHITE, true).apply {
                setPadding(0, 35, 0, 10)
            }
        )

        root.addView(
            text("☀", 82f, Color.rgb(224, 183, 103))
        )

        root.addView(
            text("24°", 48f, Color.WHITE, true)
        )

        root.addView(
            text("CLEAR SKY", 15f, Color.LTGRAY)
        )

        root.addView(
            text(
                "HTC DIAMOND WEATHER EXPERIENCE",
                11f,
                Color.GRAY
            ).apply {
                setPadding(0, 55, 0, 0)
            }
        )

        setContentView(
            root,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }
}
