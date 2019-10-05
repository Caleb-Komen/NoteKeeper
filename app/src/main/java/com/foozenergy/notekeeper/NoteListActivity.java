package com.foozenergy.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    //private ArrayAdapter<NoteInfo> notesAdapter;
    private NoteRecyclerAdapter noteRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeList();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteListActivity.this,NoteActivity.class));
            }
        });
    }

    private void initializeList() {


        RecyclerView recyclerView = findViewById(R.id.list_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//        noteRecyclerAdapter = new NoteRecyclerAdapter(this,notes);
        recyclerView.setAdapter(noteRecyclerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //notesAdapter.notifyDataSetChanged();
        noteRecyclerAdapter.notifyDataSetChanged();
    }
}
