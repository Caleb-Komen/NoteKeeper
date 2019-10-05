package com.foozenergy.notekeeper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foozenergy.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.foozenergy.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>{

    Context context;
    Cursor cursor;
    private final LayoutInflater layoutInflater;
    private int coursePos;
    private int noteTitlePos;
    private int idPos;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        layoutInflater = LayoutInflater.from(context);

        populateColumnPosition();
    }

    private void populateColumnPosition() {
        if (cursor == null)
            return;

        //get column indexes from cursor
        coursePos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        noteTitlePos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        idPos = cursor.getColumnIndex(NoteInfoEntry._ID);

    }

    public void changeCursor(Cursor mCursor){
        if (cursor != null){
            cursor.close();
        }

        cursor = mCursor;
        populateColumnPosition();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.item_note_list,viewGroup,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        cursor.moveToPosition(i);
        String course = cursor.getString(coursePos);
        String noteTitle = cursor.getString(noteTitlePos);
        int id = cursor.getInt(idPos);

        viewHolder.courseTitle.setText(course);
        viewHolder.noteTitle.setText(noteTitle);
        viewHolder.id = id;

    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0: cursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseTitle;
        TextView noteTitle;
        int id;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            courseTitle = itemView.findViewById(R.id.txt_courses);
            noteTitle = itemView.findViewById(R.id.txt_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context,NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, id);
                    context.startActivity(intent);
                }
            });
        }
    }
}
