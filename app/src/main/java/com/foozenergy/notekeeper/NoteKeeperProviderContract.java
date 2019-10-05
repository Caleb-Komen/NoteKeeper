package com.foozenergy.notekeeper;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NoteKeeperProviderContract {

    private NoteKeeperProviderContract(){};

    public static final String AUTHORITY = "com.foozenergy.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    protected interface CourseIdColumns{
        public static final String COLUMN_COURSE_ID = "course_id";
    }

    protected interface CourseColumns{
        public static final String COLUMN_COURSE_TITLE = "course_title";
    }

    protected interface NoteColumns{
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
    }

    public static final class Courses implements BaseColumns, CourseColumns, CourseIdColumns {
        public static final String PATH = "courses";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }

    public static final class Notes implements BaseColumns, NoteColumns, CourseIdColumns, CourseColumns{
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
        public static final String PATH_EXPANDED = "notes_expanded";
        public static final Uri CONTENT_EXPANDED_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED);
    }
}
