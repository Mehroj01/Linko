package com.neb.linko.ui.registerscreens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neb.linko.databinding.ActivityRegisterBinding
import com.neb.linko.ui.registerscreens.experience.EperienceFragment

class RegisterActivity : AppCompatActivity(), EperienceFragment.StopSpeakerClick {
    lateinit var registerBinding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(registerBinding.root)
    }

    override fun click() {
        finish()
    }
}