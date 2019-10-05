package com.foozenergy.notekeeper;

import android.app.LoaderManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.content.CursorLoader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.foozenergy.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.foozenergy.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.foozenergy.notekeeper.NoteKeeperProviderContract.Notes;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_NOTES = 0;
    public static final int NOTE_UPLOADER_JOB_ID = 1;
    private NoteRecyclerAdapter noteRecyclerAdapter;
    private RecyclerView recyclerItems;
    private RecyclerView.LayoutManager notesLayoutManager;
    private GridLayoutManager coursesLayoutManager;
    private CourseRecyclerAdapter courseRecyclerAdapter;
    private NoteKeeperOpenHelper dbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,NoteActivity.class));
            }
        });

        PreferenceManager.setDefaultValues(this,R.xml.pref_general,false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_notification,false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_data_sync,false);

        initializeList();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initializeList() {


        recyclerItems = findViewById(R.id.list_items);
        notesLayoutManager = new LinearLayoutManager(this);
        coursesLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.course_grid_span));
        recyclerItems.setHasFixedSize(true);

//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        noteRecyclerAdapter = new NoteRecyclerAdapter(this,null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        courseRecyclerAdapter = new CourseRecyclerAdapter(this,courses);

        DataManager.loadFromDatabase(dbOpenHelper);
        displayNotes();
    }

    private void displayNotes() {
        recyclerItems.setLayoutManager(notesLayoutManager);
        recyclerItems.setAdapter(noteRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    public void displayCourses(){
        recyclerItems.setLayoutManager(coursesLayoutManager);
        recyclerItems.setAdapter(courseRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_courses);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_NOTES,null,this);

        updateNavHeader();

        openDrawer();
    }

    private void openDrawer() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(Gravity.START);
            }
        }, 1000);
    }

    private void loadNotes() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String[] noteColumns = new String[]{NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_COURSE_ID, NoteInfoEntry._ID};
        String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
        Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null, null,
                null, noteOrderBy);

        noteRecyclerAdapter.changeCursor(noteCursor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbOpenHelper.close();
    }

    private void updateNavHeader() {
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        TextView textUserName = view.findViewById(R.id.text_user_name);
        TextView textEmailAddress = view.findViewById(R.id.text_email_address);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        textUserName.setText(pref.getString("user_display_name",""));
        textEmailAddress.setText(pref.getString("user_email_address",""));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }else if (id == R.id.action_backup_notes){
            backupNotes();
            return true;
        }else if (id == R.id.action_upload_notes) {
            scheduleNoteUpload();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scheduleNoteUpload() {

        PersistableBundle extras = new PersistableBundle();
        extras.putString(NoteUploaderJobService.EXTRA_DATA_URI, Notes.CONTENT_URI.toString());

        ComponentName componentName = new ComponentName(this, NoteUploaderJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(NOTE_UPLOADER_JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(extras)
                .build();
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    private void backupNotes() {
        Intent intent = new Intent(this, NoteBackupService.class);
        intent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_courses) {
            displayCourses();
        } else if (id == R.id.nav_share) {
            Snackbar.make(findViewById(R.id.list_items),"Share to "+
                    PreferenceManager.getDefaultSharedPreferences(this).getString("user_favourite_social",""),
                    Snackbar.LENGTH_SHORT).show();
        } else if (id == R.id.nav_send) {
            display(R.string.nav_send_message);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void display(int message_id) {
        Snackbar.make(findViewById(R.id.list_items),message_id,Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader loader = null;
        if (i == LOADER_NOTES){
            String[] noteColumns = new String[]{
                    Notes.COLUMN_NOTE_TITLE,
                    Notes._ID,
                    Notes.COLUMN_COURSE_TITLE
            };
            String noteOrderBy = Notes.COLUMN_COURSE_TITLE + "," + Notes.COLUMN_NOTE_TITLE;
            loader = new CursorLoader(this, Notes.CONTENT_EXPANDED_URI,noteColumns,
                    null, null, noteOrderBy);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == LOADER_NOTES){
            noteRecyclerAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES){
            noteRecyclerAdapter.changeCursor(null);
        }
    }
}
