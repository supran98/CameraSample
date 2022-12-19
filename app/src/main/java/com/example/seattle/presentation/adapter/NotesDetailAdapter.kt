package com.example.seattle.presentation.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.seattle.R
import com.example.seattle.databinding.NoteDeatailItemBinding

class NotesDetailAdapter : ListAdapter<NotesAdapterItem, NotesDetailAdapter.NotesDetailViewHolder>(
    NotesAdapterDiffCallback()
) {
    inner class NotesDetailViewHolder(private val binding: NoteDeatailItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotesAdapterItem) {
            binding.detailNoteEditText.setText(item.content())
            binding.detailNoteEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null) {
                        item.update(s.toString())
                    }
                }

                override fun afterTextChanged(s: Editable?) = Unit
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesDetailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_deatail_item, parent, false)
        return NotesDetailViewHolder(NoteDeatailItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: NotesDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getNoteText(): String {
        return currentList.joinToString(separator = "") { it.content() }
    }
}

data class NoteDetailModel(
    var text: String
) : NotesAdapterItem {
    override fun id(): Int = hashCode()

    override fun content(): String = text

    override fun update(upd: String) {
        text = upd
    }
}