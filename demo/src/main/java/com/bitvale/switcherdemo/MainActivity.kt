package com.bitvale.switcherdemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bitvale.switcherdemo.databinding.ActivityMainBinding

/**
 * Created by Alexander Kolpakov (jquickapp@gmail.com) on 11-Jul-18
 * https://github.com/bitvale
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val defaultColor = ContextCompat.getColor(this, R.color.text_color_default)
        val disabledColor = ContextCompat.getColor(this, R.color.text_color_disabled)

        with(binding) {
            switcherX.setOnCheckedChangeListener { checked ->
                switcherC.setChecked(checked)
                if (checked) tvSwitcherX.setTextColor(defaultColor)
                else tvSwitcherX.setTextColor(disabledColor)
            }

            switcherC.setOnCheckedChangeListener { checked ->
                switcherX.setChecked(checked)
                if (checked) tvSwitcherC.setTextColor(defaultColor)
                else tvSwitcherC.setTextColor(disabledColor)
            }

            actionDribbble.setOnClickListener {
                openDribbble()
            }
        }
    }

    private fun openDribbble() {
        val uri = Uri.parse(getString(R.string.dribbble_link))
        Intent(Intent.ACTION_VIEW, uri).apply {
            if (resolveActivity(packageManager) != null) {
                startActivity(this)
            }
        }
    }
}
