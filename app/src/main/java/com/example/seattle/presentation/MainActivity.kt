package com.example.seattle.presentation

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.seattle.R
import com.example.seattle.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS)

        if (savedInstanceState == null) {
            val profileFragment = ProfileFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, profileFragment)
                .commit()
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Fragment.toast(msg: String) {
    requireContext().toast(msg)
}