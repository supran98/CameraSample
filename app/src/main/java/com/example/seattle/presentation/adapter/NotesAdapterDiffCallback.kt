package com.example.seattle.presentation.adapter

import androidx.recyclerview.widget.DiffUtil

internal class NotesAdapterDiffCallback : DiffUtil.ItemCallback<NotesAdapterItem>() {
    override fun areItemsTheSame(oldItem: NotesAdapterItem, newItem: NotesAdapterItem): Boolean {
        return oldItem.id() == newItem.id()
    }

    override fun areContentsTheSame(oldItem: NotesAdapterItem, newItem: NotesAdapterItem): Boolean {
        return oldItem.content() == newItem.content()
    }
}