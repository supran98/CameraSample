package com.example.seattle.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seattle.R
import com.example.seattle.data.MainViewModel
import com.example.seattle.databinding.FragmentProfileBinding
import com.example.seattle.presentation.adapter.NotesPreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class ProfileFragment : Fragment(), DefaultLifecycleObserver {
    private var _profileFragmentBinding: FragmentProfileBinding? = null
    val profileFragmentBinding: FragmentProfileBinding get() = _profileFragmentBinding!!

    private val viewModel: MainViewModel by viewModels()

    private var resultLauncher: ActivityResultLauncher<String>? = null

    private var prefsPhotoUri: String? = null

    private val sharedPrefs by lazy {
        requireActivity().getPreferences(Context.MODE_PRIVATE)
    }

    private val notesPreviewAdapter by lazy {
        NotesPreviewAdapter(onClickItem = { id -> navigateToNote(id) })
    }

    override fun onCreate(owner: LifecycleOwner) {
        val registry = requireActivity().activityResultRegistry
        resultLauncher = registry.register(LAUNCHER_KEY, owner, ActivityResultContracts.GetContent()) { uri ->
            uri?.let { it ->
                val src = ImageDecoder.createSource(requireActivity().contentResolver, it)
                val bitmap = ImageDecoder.decodeBitmap(src)

                setProfilePhoto(bitmap)

                sharedPrefs.edit {
                    putString(CameraFragment.PREFS_PHOTO_URI_KEY, getImageUriFromBitmap(bitmap).toString())
                }
            }
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream)
        val path = MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap,"File",null)
        return Uri.parse(path.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        onCreate(this) // DefaultLifecycleObserver.onCreate

        viewModel.getNotes()
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.notesFlow.collect {
                notesPreviewAdapter.submitList(it.reversed().map { note -> note.toPreviewItem() })
            }
        }

        _profileFragmentBinding = FragmentProfileBinding.inflate(inflater, container, false)
        return profileFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPhoto()

        profileFragmentBinding.notesRecyclerView.apply {
            adapter = notesPreviewAdapter
            setVerticalLayout(requireContext())
        }

        profileFragmentBinding.addNoteBtn.setOnClickListener {
            navigateToNote(null)
        }

        profileFragmentBinding.editPhotoBtn.setOnClickListener {
            resultLauncher?.launch(MIME_TYPE)
        }

        profileFragmentBinding.takePhotoBtn.setOnClickListener {
            val cameraFragment = CameraFragment()
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
                .replace(R.id.fragment_container, cameraFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadPhoto() {
        prefsPhotoUri = sharedPrefs.getString(CameraFragment.PREFS_PHOTO_URI_KEY, null)

        if (prefsPhotoUri != null) {
            setProfilePhoto(Uri.parse(prefsPhotoUri))
        }
    }

    private fun setProfilePhoto(photoUri: Uri) {
        try {
            val src = ImageDecoder.createSource(requireActivity().contentResolver, photoUri)
            val bitmap = ImageDecoder.decodeBitmap(src)
            profileFragmentBinding.profilePhoto.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("LoadImageError", e.message ?: "Could not load image")
            prefsPhotoUri = null
            setProfilePhoto(R.drawable.profile_photo_placeholder)
        }
    }

    private fun setProfilePhoto(bitmap: Bitmap) {
        profileFragmentBinding.profilePhoto.setImageBitmap(bitmap)
    }

    private fun setProfilePhoto(@DrawableRes resource: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, resource)
        profileFragmentBinding.profilePhoto.setImageBitmap(bitmap)
    }

    private fun navigateToNote(noteId: Int?) {
        val noteFragment = NoteDetailFragment.newInstance(noteId)
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
            .replace(R.id.fragment_container, noteFragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        private const val LAUNCHER_KEY = "default"
        private const val MIME_TYPE = "image/*"
    }
}

fun RecyclerView.setVerticalLayout(context: Context) {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
}