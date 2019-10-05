package com.foozenergy.notekeeper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.CursorLoader;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.foozenergy.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.foozenergy.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.foozenergy.notekeeper.NoteKeeperProviderContract.Courses;
import com.foozenergy.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = NoteActivity.class.getName();
    public static final String ORIGINAL_NOTE_COURSE_ID = "NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "NOTE_TEXT";
    public static final String NOTE_URI = "NOTE_URI";

    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private NoteInfo note = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    public static final String NOTE_ID = "NotePosition";
    private boolean isNewNote;
    private Spinner spinnerCourses;
    private EditText textNoteTitle;
    private EditText textNoteText;
    private int noteId;
    private boolean isCancelling;
    private String originalCourseId;
    private String originalNoteTitle;
    private String originalNoteText;
    private NoteKeeperOpenHelper dbOpenHelper;
    private Cursor noteCursor;
    private int courseIdPos;
    private int noteTitlePos;
    private int noteTextPos;
    private SimpleCursorAdapter courseAdapter;
    private boolean coursesQueryFinished;
    private boolean noteQueryFinished;
    private Uri noteUri;
    private ModuleStatusView moduleStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableStrictMode();

        dbOpenHelper = new NoteKeeperOpenHelper(this);

        spinnerCourses = findViewById(R.id.spinner_course);

//        List<CourseInfo> courses = DataManager.getInstance().getCourses();

        courseAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_spinner_item,
                null, new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1}, 0);

        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(courseAdapter);

        getLoaderManager().initLoader(LOADER_COURSES, null, this);

        readDisplayStateValues();
        if (savedInstanceState == null)
            saveOriginalValue();
        else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        textNoteTitle = findViewById(R.id.note_title);
        textNoteText = findViewById(R.id.note_text);

        if(!isNewNote) {

//            loadNoteData();
            getLoaderManager().initLoader(LOADER_NOTES,null, this);
        }

        moduleStatusView = findViewById(R.id.module_status);
        loadModuleStatusValues();

        Log.d(TAG,"onCreate");

    }

    private void loadModuleStatusValues() {
        // In real life we'd look up for the selected course's module statuses from the content provider
        int totalNumberOfModules = 11;
        int completedNumberOfModules = 7;
        boolean[] moduleStatus = new boolean[totalNumberOfModules];
        for (int moduleIndex=0; moduleIndex < completedNumberOfModules; moduleIndex++){
            moduleStatus[moduleIndex] = true;
        }

        moduleStatusView.setModuleStatus(moduleStatus);
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void loadCourseData() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };

        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME,courseColumns,
                null,null,null,null,CourseInfoEntry.COLUMN_COURSE_TITLE);

        courseAdapter.changeCursor(cursor);
    }

    private void loadNoteData() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoEntry._ID + " = ?" ;
        String[] selectionArgs = {Integer.toString(noteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        noteCursor = db.query(NoteInfoEntry.TABLE_NAME,noteColumns,selection,
                selectionArgs,null,null,null);

        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        noteCursor.moveToNext();

        displayNote();
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        originalCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        originalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        originalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID,originalCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE,originalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT,originalNoteText);

    }

    private void saveOriginalValue() {
        if(isNewNote)
            return;
        originalCourseId = note.getCourse().getCourseId();
        originalNoteTitle = note.getTitle();
        originalNoteText = note.getText();
    }

    private void displayNote() {
        String courseId = noteCursor.getString(courseIdPos);
        String noteTitle = noteCursor.getString(noteTitlePos);
        String noteText = noteCursor.getString(noteTextPos);

        int index = getIndexOfCourseId(courseId);

        spinnerCourses.setSelection(index);
        textNoteTitle.setText(noteTitle);
        textNoteText.setText(noteText);

        CourseEventBroadCastHelper.sendEventBroadcast(this, courseId, "Editing Note");
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = courseAdapter.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more){
            String cursorCourseId = cursor.getString(courseIdPos);

            if (courseId.equals(cursorCourseId))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }

        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        noteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);

        isNewNote = this.noteId == ID_NOT_SET;
        if (isNewNote){
            createNewNote();
        }

//        note = DataManager.getInstance().getNotes().get(this.noteId);
    }

    private void createNewNote() {

        AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {
            private ProgressBar progressBar;

            @Override
            protected void onPreExecute() {
                progressBar = findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(1);
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                ContentValues insertValues = contentValues[0];
                Uri rowUri = getContentResolver().insert(Notes.CONTENT_URI, insertValues);

                simulateLongRunningWork(); //simulates slow database work
                publishProgress(2);

                simulateLongRunningWork(); //simulates slow database work
                publishProgress(3);

                return rowUri;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                progressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                noteUri = uri;
                progressBar.setVisibility(View.GONE);
            }
        };
       ContentValues values = new ContentValues();
       values.put(Notes.COLUMN_COURSE_ID, "");
       values.put(Notes.COLUMN_NOTE_TITLE, "");
       values.put(Notes.COLUMN_NOTE_TEXT, "");

       task.execute(values);
    }

    private void simulateLongRunningWork() {
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_email) {
            sendEmail();
            return true;
        }else if(id == R.id.action_cancel){
            isCancelling = true;
            finish();
            return true;
        }else if (id == R.id.action_next){
            moveNext();
        }else if (id == R.id.action_set_reminder){
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
        String noteTitle = textNoteTitle.getText().toString();
        String noteText = textNoteText.getText().toString();
        int noteId = (int) ContentUris.parseId(noteUri);

        Intent intent = new Intent(this, NoteReminderReceiver.class);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long currentTimeInMilliseconds = SystemClock.elapsedRealtime();
        long ONE_HOUR = 60 * 60 * 1000;
        long TEN_SECONDS = 10 * 1000;
        long alarmTime = currentTimeInMilliseconds + TEN_SECONDS;

        alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;

        item.setEnabled(noteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++noteId;
        note = DataManager.getInstance().getNotes().get(noteId);
        saveOriginalValue();
        displayNote();

        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isCancelling){
            Log.i(TAG,"cancelling");
            if (isNewNote)
                deleteNoteFromDatabase();
            else
                storePreviousNoteValue();
        }else
            saveNote();

        Log.d(TAG,"onPause");
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ? ";
        final String[] selectionArgs = {Integer.toString(noteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME,selection, selectionArgs);
                return null;
            }
        };

        task.execute();

    }

    private void storePreviousNoteValue() {
        CourseInfo course = DataManager.getInstance().getCourse(originalCourseId);
        note.setCourse(course);
        note.setTitle(originalNoteTitle);
        note.setText(originalNoteText);
    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = textNoteTitle.getText().toString();
        String noteText = textNoteText.getText().toString();
        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = spinnerCourses.getSelectedItemPosition();
        Cursor cursor = courseAdapter.getCursor();
        cursor.moveToPosition(selectedPosition);
        String courseId = cursor.getString(cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID));
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText){
        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(noteId)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) spinnerCourses.getSelectedItem();
        String subject = textNoteTitle.getText().toString();
        String text = "Checkout what I learnt in the courseTitle \""+course.getTitle()+"\"\n"+textNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");  //this type indicate that we want to send an email
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);

        //resolve intent with android package manager
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        CursorLoader loader = null;
        if (i == LOADER_NOTES){
            loader = createLoaderNotes();
        }else if (i == LOADER_COURSES){
            loader = createLoaderCourses();
        }
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        coursesQueryFinished = false;
        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };

        return new CursorLoader(this, uri, courseColumns,null,null,Courses.COLUMN_COURSE_TITLE);
    }

    private CursorLoader createLoaderNotes() {
        noteQueryFinished = false;
        String[] noteColumns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };

        noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, noteId);
        return new CursorLoader(this, noteUri, noteColumns, null, null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == LOADER_NOTES){
            loadFinishedNotes(cursor);
        }else if (loader.getId() == LOADER_COURSES){
            courseAdapter.changeCursor(cursor);
            coursesQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }

    }

    private void loadFinishedNotes(Cursor cursor) {
        noteCursor = cursor;
        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        noteCursor.moveToNext();
        noteQueryFinished = true;

        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
        if (noteQueryFinished && coursesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES){
            if (noteCursor != null){
                noteCursor.close();
            }
        }else if (loader.getId() == LOADER_COURSES){
            courseAdapter.changeCursor(null);
        }

    }

//    @Override
//    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
//
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
//
//    }
}
