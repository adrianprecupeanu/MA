package com.example.ma.sm.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.example.ma.sm.jobs.TestJobActivity;

import java.util.LinkedList;

import timber.log.Timber;


/**
 * Service to handle callbacks from the JobScheduler. Requests scheduled with the JobScheduler
 * ultimately land on this service's "onStartJob" method. Currently all this does is post a message
 * to the app's main activity to change the state of the UI.
 */
public class TestJobService extends JobService {

  TestJobActivity mActivity;
  private final LinkedList<JobParameters> jobParamsMap = new LinkedList<>();

  @Override
  public void onCreate() {
    super.onCreate();
    Timber.v("Service created");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Timber.v("Service destroyed");
  }

  /**
   * When the app's MainActivity is created, it starts this service. This is so that the
   * activity and this service can communicate back and forth. See "setUiCalback()"
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.v("onStartCommand");
    Messenger callback = intent.getParcelableExtra("messenger");
    Message message = Message.obtain();
    message.what = TestJobActivity.MSG_SERVICE_OBJ;
    message.obj = this;
    try {
      callback.send(message);
    } catch (RemoteException e) {
      Timber.e(e, "Error passing service object back to activity.");
    }
    return START_NOT_STICKY;
  }

  @Override
  public boolean onStartJob(JobParameters params) {
    // We don't do any real 'work' in this sample app. All we'll
    // do is track which jobs have landed on our service, and
    // update the UI accordingly.
    jobParamsMap.add(params);
    if (mActivity != null) {
      mActivity.onReceivedStartJob(params);
    }
    Timber.v("on start job: %d", params.getJobId());
    return true;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    // Stop tracking these job parameters, as we've 'finished' executing.
    jobParamsMap.remove(params);
    if (mActivity != null) {
      mActivity.onReceivedStopJob();
    }
    Timber.v("on stop job: %d", params.getJobId());
    return true;
  }

  public void setUiCallback(TestJobActivity activity) {
    mActivity = activity;
  }

  /**
   * Send job to the JobScheduler.
   */
  public void scheduleJob(JobInfo t) {
    Timber.v("Scheduling job");
    JobScheduler tm =
        (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    if (tm != null) {
      tm.schedule(t);
    }
  }

  /**
   * Not currently used, but as an exercise you can hook this
   * up to a button in the UI to finish a job that has landed
   * in onStartJob().
   */
  public boolean callJobFinished() {
    JobParameters params = jobParamsMap.poll();
    if (params == null) {
      return false;
    } else {
      jobFinished(params, false);
      return true;
    }
  }

}