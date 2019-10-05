package com.foozenergy.notekeeper;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;

import static android.support.test.espresso.Espresso.pressBack;
import static org.hamcrest.Matchers.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {

    static DataManager dm;
    @BeforeClass
    public static void classSetUp() throws Exception{
        dm = DataManager.getInstance();
    }
    @Rule
    public ActivityTestRule<MainActivity> noteListActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void createNewNote(){

        final CourseInfo course  = dm.getCourse("java_lang");
        final String title = "note title";
        final String text = "note text";

        onView(withId(R.id.fab)).perform(click());

        onView(withId(R.id.spinner_course)).perform(click());
        onData(allOf(instanceOf(CourseInfo.class),equalTo(course))).perform(click());
        onView(withId(R.id.spinner_course)).check(matches(withSpinnerText(containsString(course.getTitle()))));

        onView(withId(R.id.note_title)).perform(typeText(title)).check(matches(withText(containsString(title))));

        onView(withId(R.id.note_text)).perform(typeText(text),closeSoftKeyboard());
        onView(withId(R.id.note_text)).check(matches(withText(containsString(text))));

        pressBack();

        int index = dm.getNotes().size() - 1;
        NoteInfo note = dm.getNotes().get(index);

        assertEquals(course,note.getCourse());
        assertEquals(title,note.getTitle());
        assertEquals(text,note.getText());
    }
}