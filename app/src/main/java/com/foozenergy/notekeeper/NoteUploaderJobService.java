package com.foozenergy.notekeeper;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

public class NoteUploaderJobService extends JobService {

    public static final String EXTRA_DATA_URI = "com.foozenergy.notekeeper.extra.NOTE_URI";
    private NoteUploader noteUploader;

    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        noteUploader = new NoteUploader(this);

        AsyncTask<JobParameters, Void, Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... backgroundParams) {
                JobParameters jobParams = backgroundParams[0];
                String stringDataUri = jobParams.getExtras().getString(EXTRA_DATA_URI);
                Uri dataUri = Uri.parse(stringDataUri);

                noteUploader.doUpload(dataUri);

                if (!noteUploader.isCancelled()) {
                    jobFinished(jobParams, false);
                }

                return null;
            }
        };

        task.execute(jobParameters);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        noteUploader.cancel();
        return true;
    }


}
