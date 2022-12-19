package com.example.seattle.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.seattle.R
import com.example.seattle.databinding.NoteItemBinding

class NotesPreviewAdapter(private val onClickItem: (id: Int) -> Unit) :
    ListAdapter<NotesAdapterItem, NotesPreviewAdapter.NotesViewHolder>(NotesAdapterDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NotesViewHolder(NoteItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotesViewHolder(private val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: NotesAdapterItem) {
            binding.textView.apply {
                text = note.content()
                setOnClickListener {
                    onClickItem.invoke(note.id())
                }
            }
        }
    }
}

data class NotePreviewModel(var id: Int, var text: String) : NotesAdapterItem {
    override fun id(): Int = id

    override fun content(): String = text

    override fun update(upd: String) = Unit
}