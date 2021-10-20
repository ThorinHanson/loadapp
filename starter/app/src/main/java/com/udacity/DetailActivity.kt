package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        if(intent?.extras != null){
            file_name_text_view.text = intent.getStringExtra("fileName")
            status_text_view.text = intent.getStringExtra("status")
        }

        ok_button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}
