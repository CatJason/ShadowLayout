package com.leo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.leo.databinding.ActivityMainBinding

/**
 * 首页展示
 */
class MainActivity : AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding?.run {

            //ShadowLayout的使用
            ShadowLayoutShadow.setOnClickListener {
                startActivity(Intent(this@MainActivity, ShadowActivity::class.java))
            }

            ShadowLayoutStart.setOnClickListener {
                startActivity(Intent(this@MainActivity, ShapeActivity::class.java))
            }
        }
    }
}