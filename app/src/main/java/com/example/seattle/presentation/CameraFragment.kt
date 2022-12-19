package com.example.seattle.presentation

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.core.ImageCapture.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.seattle.R
import com.example.seattle.databinding.FragmentCameraBinding
import java.text.SimpleDateFormat
import java.util.*

class CameraFragment : Fragment() {
    private var _cameraFragmentBinding: FragmentCameraBinding? = null
    val cameraFragmentBinding: FragmentCameraBinding get() = _cameraFragmentBinding!!
    private val sharedPrefs by lazy {
        requireActivity().getPreferences(Context.MODE_PRIVATE)
    }

    private var preview: Preview? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _cameraFragmentBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return cameraFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraFragmentBinding.apply {
            cameraPreview.post {
                setupCamera()
            }
            goToProfileBtn.setOnClickListener {
                navigateToProfileFragment()
                bindCameraUseCases()
            }
            takePhotoBtn.setOnClickListener {
                takePicture {
                    sharedPrefs.edit {
                        putString(PREFS_PHOTO_URI_KEY, "content://media${it?.path}")
                    }
                    navigateToProfileFragment()
                }
            }
            switchCameraBtn.setOnClickListener {
                switchLensFacing()
                bindCameraUseCases()
            }
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val rotation = cameraFragmentBinding.cameraPreview.display.rotation
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProvider = cameraProvider ?: throw IllegalArgumentException()

        preview = Preview.Builder()
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(rotation)
            .build()

        cameraProvider.unbindAll()
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        preview?.setSurfaceProvider(cameraFragmentBinding.cameraPreview.surfaceProvider)
    }

    private fun takePicture(onImageSaved: (savedUri: Uri?) -> Unit = {  }) {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraSample")
        }

        val outputOptions = OutputFileOptions.Builder(
            requireActivity().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri = outputFileResults.savedUri
                    toast("Successfully saved: $uri")
                    onImageSaved.invoke(uri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX (take picture)", "Failed to save image: ${exception.message}")
                }
            }
        )
    }

    private fun navigateToProfileFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
            .remove(this)
            .commit()
        fragmentManager.popBackStack()
    }

    private fun switchLensFacing() {
        lensFacing = if (lensFacing == LENS_FACING_FRONT) {
            LENS_FACING_BACK
        } else {
            LENS_FACING_FRONT
        }
    }

    companion object {
        const val PREFS_PHOTO_URI_KEY = "profilePhotoUri"
    }
}