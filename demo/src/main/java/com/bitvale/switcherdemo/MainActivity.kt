package com.bitvale.switcherdemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val defaultColor = ContextCompat.getColor(this, R.color.text_color_default)
        val disabledColor = ContextCompat.getColor(this, R.color.text_color_disabled)

        switcher_x.setOnCheckedChangeListener { checked ->
            if (checked) tv_switcher_x.setTextColor(defaultColor)
            else tv_switcher_x.setTextColor(disabledColor)
        }

        switcher_c.setOnCheckedChangeListener { checked ->
            if (checked) tv_switcher_c.setTextColor(defaultColor)
            else tv_switcher_c.setTextColor(disabledColor)
        }

        dribbble.setOnClickListener {
            openDribbble()
        }
    }

    private fun openDribbble() {
        val uri = Uri.parse(getString(R.string.dribbble_link))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}
