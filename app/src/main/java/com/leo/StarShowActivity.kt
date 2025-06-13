package com.leo

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

/**
 * 动态设置阴影页
 */
class StarShowActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var shadowLayout: com.lihang.ShadowLayout
    private lateinit var skbarX: SeekBar
    private lateinit var skbarY: SeekBar
    private lateinit var skbarLimit: SeekBar
    private lateinit var skbarCorner: SeekBar
    private var alpha: Int = 0
    private lateinit var skbarAlpha: SeekBar
    private var red: Int = 0
    private lateinit var skbarRed: SeekBar
    private var green: Int = 0
    private lateinit var skbarGreen: SeekBar
    private var blue: Int = 0
    private lateinit var skbarBlue: SeekBar
    private lateinit var tabTopShow: ImageView
    private lateinit var tabBottomShow: ImageView
    private lateinit var tabRightShow: ImageView
    private lateinit var tabLeftShow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starshow)
        shadowLayout = findViewById(R.id.ShadowLayout)
        skbarX = findViewById(R.id.skbar_x)
        skbarY = findViewById(R.id.skbar_y)
        skbarLimit = findViewById(R.id.skbar_limit)
        skbarCorner = findViewById(R.id.skbar_corner)
        skbarAlpha = findViewById(R.id.skbar_alpha)
        skbarRed = findViewById(R.id.skbar_red)
        skbarGreen = findViewById(R.id.skbar_green)
        skbarBlue = findViewById(R.id.skbar_blue)
        tabTopShow = findViewById(R.id.tab_topShow)
        tabTopShow.setOnClickListener(this)
        tabBottomShow = findViewById(R.id.tab_bottomShow)
        tabBottomShow.setOnClickListener(this)
        tabRightShow = findViewById(R.id.tab_rightShow)
        tabRightShow.setOnClickListener(this)
        tabLeftShow = findViewById(R.id.tab_leftShow)
        tabLeftShow.setOnClickListener(this)

        skbarCorner.max = (shadowLayout.cornerRadius * 3).toInt()
        skbarCorner.progress = shadowLayout.cornerRadius.toInt()
        skbarCorner.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                shadowLayout.cornerRadius = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        skbarLimit.max = (shadowLayout.shadowLimit * 3).toInt()
        skbarLimit.progress = shadowLayout.shadowLimit.toInt()
        skbarLimit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                shadowLayout.shadowLimit = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        skbarX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                shadowLayout.shadowOffsetX = (progress - 100).toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        skbarY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                shadowLayout.shadowOffsetY = (progress - 100).toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        skbarAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                alpha = progress
                shadowLayout.shadowColor = Color.argb(alpha, red, green, blue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        skbarRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                red = progress
                shadowLayout.shadowColor = Color.argb(alpha, red, green, blue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        skbarGreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                green = progress
                shadowLayout.shadowColor = Color.argb(alpha, red, green, blue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        skbarBlue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                blue = progress
                shadowLayout.shadowColor = Color.argb(alpha, red, green, blue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tab_topShow -> shadowLayout.setShadowHiddenTop(select(tabTopShow))
            R.id.tab_bottomShow -> shadowLayout.setShadowHiddenBottom(select(tabBottomShow))
            R.id.tab_leftShow -> shadowLayout.setShadowHiddenLeft(select(tabLeftShow))
            R.id.tab_rightShow -> shadowLayout.setShadowHiddenRight(select(tabRightShow))
        }
    }

    private fun select(imageView: ImageView): Boolean {
        return if (imageView.isSelected) {
            imageView.isSelected = false
            false
        } else {
            imageView.isSelected = true
            true
        }
    }
}