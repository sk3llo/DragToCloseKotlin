package com.topset.dragtoclosekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (supportFragmentManager.findFragmentByTag("DOWN_FRAGMENT") == null) {

        }

        up.setOnClickListener {
            val intent = Intent(this@MainActivity, Up::class.java)
            startActivity(intent)
        }

        down.setOnClickListener {
            up.visibility = View.GONE
            down.visibility = View.GONE
            upAndDown.visibility = View.GONE

            val ft = supportFragmentManager.beginTransaction()
            val fragment = Down()
            ft.add(R.id.fragmentLayout, fragment, "DOWN_FRAGMENT")
            ft.commit()
        }

        upAndDown.setOnClickListener {
            val intent = Intent(this@MainActivity, UpAndDown::class.java)
            startActivity(intent)
        }
    }
}
