package com.foozenergy.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class NoteUploader {

    private final String TAG = getClass().getSimpleName();
    private final Context context;
    private boolean cancelled;

    public NoteUploader(Context context) {
        this.context = context;
    }

    public boolean isCancelled(){
        return cancelled;
    }

    public void cancel(){
        Log.i(TAG, ">>>*** UPLOAD CANCELLED ***<<<");
        cancelled = true;
    }

    public void doUpload(Uri dataUri){

        String[] columns = {
                NoteKeeperProviderContract.Notes.COLUMN_COURSE_ID,
                NoteKeeperProviderContract.Notes.COLUMN_NOTE_TITLE,
                NoteKeeperProviderContract.Notes.COLUMN_NOTE_TEXT
        };

        Cursor cursor = context.getContentResolver().query(dataUri, columns, null, null, null);
        int courseIdPos = cursor.getColumnIndex(NoteKeeperProviderContract.Notes.COLUMN_COURSE_ID);
        int noteTitlePos = cursor.getColumnIndex(NoteKeeperProviderContract.Notes.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(NoteKeeperProviderContract.Notes.COLUMN_NOTE_TEXT);

        Log.i(TAG, ">>>*** UPLOAD START - " + dataUri + " ***<<<");
        cancelled = false;

        while (!cancelled && cursor.moveToNext()){
            String courseId = cursor.getString(courseIdPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);

            if (!noteTitle.equals("")){
                Log.i(TAG, ">>> Uploading note <<< " + courseId + "|" + noteTitle + "|" + noteText);
                simulateLongRunningWork();
            }
        }

        Log.i(TAG, ">>>*** UPLOAD COMPLETE ***<<<");
        cursor.close();
    }

    private static void simulateLongRunningWork() {
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
