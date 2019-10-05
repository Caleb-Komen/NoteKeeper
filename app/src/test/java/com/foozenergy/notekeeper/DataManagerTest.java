package com.foozenergy.notekeeper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {

    static DataManager dataManager;
    @BeforeClass
    public static void classSetup() throws  Exception{
        dataManager = DataManager.getInstance();
    }

    @Before
    public void setUp() throws Exception{
        dataManager = DataManager.getInstance();
        dataManager.getNotes().clear();
        dataManager.initializeExampleNotes();

    }
    @Test
    public void createNewNote() {

        final CourseInfo course = dataManager.getCourse("android_async");
        final String title = "note title";
        final String text = "note text";

        int noteIndex = dataManager.createNewNote();
        NoteInfo newNote = dataManager.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(title);
        newNote.setText(text);

        NoteInfo compareNote = dataManager.getNotes().get(noteIndex);

        assertEquals(course,compareNote.getCourse());
        assertEquals(title,compareNote.getTitle());
        assertEquals(text,compareNote.getText());
    }

    @Test
    public void findSimilarNote() {
        final CourseInfo course = dataManager.getCourse("android_async");
        final String noteTitle = "note title";
        final String noteText1 = "note text";
        final String noteText2 = "note text2";

        int noteIndex1 = dataManager.createNewNote();
        NoteInfo newNote1 = dataManager.getNotes().get(noteIndex1);
        newNote1.setCourse(course);
        newNote1.setTitle(noteTitle);
        newNote1.setText(noteText1);

        int noteIndex2 = dataManager.createNewNote();
        NoteInfo newNote2 = dataManager.getNotes().get(noteIndex2);
        newNote2.setCourse(course);
        newNote2.setTitle(noteTitle);
        newNote2.setText(noteText2);

        int findIndex1 = dataManager.findNote(newNote1);
        assertEquals(noteIndex1,findIndex1);

        int findIndex2 = dataManager.findNote(newNote2);
        assertEquals(noteIndex2,findIndex2);
    }

    @Test
    public void createNewNoteOneStepCreation() throws Exception{
        final CourseInfo course = dataManager.getCourse("android_async");
        final String title = "note title";
        final String text = "note text";

        int noteIndex = dataManager.createNewNote(course,title,text);
        NoteInfo note = dataManager.getNotes().get(noteIndex);

        assertEquals(course,note.getCourse());
        assertEquals(title,note.getTitle());
        assertEquals(text,note.getText());
    }

    @Test
    public void getNoteCount() {
        final CourseInfo course = dataManager.getCourse("android_async");
        final String title1 = "note title";
        final String text1 = "note text";
        final String title2 = "my note title";
        final String text2 = "my note text";

        int index1 = dataManager.createNewNote(course,title1,text1);
        NoteInfo note1 = dataManager.getNotes().get(index1);

        int index2 = dataManager.createNewNote(course,title2,text2);
        NoteInfo note2 = dataManager.getNotes().get(index2);

        int count = dataManager.getNoteCount(course);

        assertEquals(4,count);
    }
}