package com.example.seattle.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.seattle.R
import com.example.seattle.data.MainViewModel
import com.example.seattle.data.Note
import com.example.seattle.presentation.adapter.NoteDetailModel
import com.example.seattle.presentation.adapter.NotesDetailAdapter
import com.example.seattle.databinding.FragmentNoteDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteDetailFragment : Fragment() {
    private var _noteFragmentBinding: FragmentNoteDetailBinding? = null
    val noteFragmentBinding: FragmentNoteDetailBinding get() = _noteFragmentBinding!!

    private val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    private val notesDetailAdapter by lazy {
        val defaultBlocks = listOf(
            NoteDetailModel(""),
            NoteDetailModel("")
        )
        NotesDetailAdapter().apply {
            submitList(defaultBlocks)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _noteFragmentBinding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return noteFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentNoteId = arguments?.getInt(NOTE_ID_KEY)
        if (currentNoteId != null && currentNoteId != 0) {

            viewModel.getNote(currentNoteId)
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.currentNoteFlow.collect { note ->
                    val noteBlocks = note.chunkText(MAX_BLOCK_LENGTH).map { block ->
                        NoteDetailModel(block)
                    }
                    notesDetailAdapter.submitList(noteBlocks)
                }
            }

            noteFragmentBinding.deleteNoteButton.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.deleteNote(currentNoteId)
                    navigateToProfile()
                }
            }
        }

        noteFragmentBinding.noteDetailItemsRecycler.apply {
            adapter = notesDetailAdapter
            setVerticalLayout(requireContext())
        }
    }

    override fun onStop() {
        super.onStop()
        saveNote()
    }

    private fun navigateToProfile() {
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
            .remove(this)
            .commit()
        fragmentManager.popBackStack()
    }

    private fun saveNote() {
        val noteId = arguments?.getInt(NOTE_ID_KEY)
        val content = notesDetailAdapter.getNoteText()
        if (noteId != null && noteId != 0) {
            viewModel.saveNote(noteId, content)
        } else {
            viewModel.addNote(Note(null, content))
        }
    }

    companion object {
        const val NOTE_ID_KEY = "noteId"
        const val MAX_BLOCK_LENGTH = 510

        fun newInstance(noteId: Int?) = NoteDetailFragment().apply {
            arguments = Bundle().apply {
                noteId?.let { putInt(NOTE_ID_KEY, it) }
            }
        }
    }
}