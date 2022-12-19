package com.example.seattle.presentation.adapter

interface NotesAdapterItem {
    fun id(): Int

    fun content(): String

    fun update(upd: String)
}