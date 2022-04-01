package com.example.health_app.activities

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import com.example.health_app.R
import com.yellowcode.roomsample.activities.NewNoteActivity
import com.yellowcode.roomsample.adapters.NoteListAdapter
import com.yellowcode.roomsample.database.NoteRoomDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope, View.OnClickListener {

    private var noteDB: NoteRoomDatabase? = null
    private var adapter: NoteListAdapter? = null

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mJob = Job()

        noteDB = NoteRoomDatabase.getDatabase(this)
        adapter = NoteListAdapter(MainActivity@this, noteDB!!)

        recycler_notes.adapter = adapter
        recycler_notes.layoutManager = LinearLayoutManager(this)

        button_new_note.setOnClickListener(this)
        button_find.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

        getAllNotes()
    }

    override fun onDestroy() {
        super.onDestroy()

        mJob.cancel()
    }

    override fun onClick(v: View?) {
        when(v) {
            button_new_note -> {
                val newNoteIntent = Intent(this, NewNoteActivity::class.java)
                startActivity(newNoteIntent)
            }

            button_find -> {
                findNote()
            }
        }
    }

    // Get all notes
    fun getAllNotes() {
        launch {
            val notes: List<ContactsContract.CommonDataKinds.Note>? = noteDB?.noteDao()?.getAllNotes()
            if (notes != null) {
                adapter?.setNotes(notes)
            }
        }
    }

    // Find note
    fun findNote() = launch {
        val strFind = edittext_find.text.toString()
        if (!TextUtils.isEmpty(strFind)) {
            // Find if the text is not empty
            val note: ContactsContract.CommonDataKinds.Note? = noteDB?.noteDao()?.findNoteByTitle(strFind)
            if (note != null) {
                val notes: List<ContactsContract.CommonDataKinds.Note> = mutableListOf(note)
                adapter?.setNotes(notes)
            }
        } else {
            // Else get all notes
            getAllNotes()
        }
    }
}
