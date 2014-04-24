/*
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 */


package com.learnit.LearnIt.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.learnit.LearnIt.R;
import com.learnit.LearnIt.async_tasks.GetDictTask;
import com.learnit.LearnIt.fragments.LoadStarDictUiFragment;
import com.learnit.LearnIt.fragments.WorkerFragment;
import com.learnit.LearnIt.interfaces.IWorkerEventListener;
import com.learnit.LearnIt.interfaces.IWorkerJobInput;

import java.util.List;
import java.util.Map;


public class LoadStarDictActivity extends Activity implements IWorkerEventListener {
    protected static final String LOG_TAG = "my_logs";
    LoadStarDictUiFragment _uiFragment;
	IWorkerJobInput _jobStarter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "onCreate LoadStarDictActivity");
        FragmentManager fragmentManager = getFragmentManager();

	    // add a ui fragment to stack
	    _uiFragment = (LoadStarDictUiFragment) fragmentManager
			    .findFragmentByTag(LoadStarDictUiFragment.TAG);
	    if (_uiFragment == null) {
		    _uiFragment = new LoadStarDictUiFragment();
		    fragmentManager.beginTransaction()
				    .add(android.R.id.content, _uiFragment, LoadStarDictUiFragment.TAG)
				    .commit();
	    }

//	    add a headless worker fragment to stack if not yet there
        Fragment worker = fragmentManager
                .findFragmentByTag(WorkerFragment.TAG);
        if (worker == null)
        {
            worker = new WorkerFragment();
            fragmentManager.beginTransaction()
		            .add(worker, WorkerFragment.TAG)
                    .commit();
        }

//		set _jobStarter from the worker fragment if possible
//	    throw exception if the fragment is wrong
	    if (worker instanceof IWorkerJobInput) {
		    _jobStarter = (IWorkerJobInput) worker;
	    } else {
		    throw new ClassCastException(worker.getClass().getSimpleName() + "should implement" + IWorkerJobInput.class.getSimpleName());
	    }
    }

	@Override
	protected void onResume() {
		super.onResume();
		if (!_jobStarter.taskRunning()) {
			_jobStarter.addTask(new GetDictTask(), this);
		} else {
			_jobStarter.attach(this);
		}
	}

    @Override
    protected void onPause() {
        super.onPause();
        if (!_jobStarter.taskRunning())
        {
            this.finish();
        }
    }

	@Override
	public void onPreExecute() {
	}

	@Override
	public void onFail() {
		if (_uiFragment != null)
		{
			_uiFragment.setTitleText(this.getString(R.string.dict_sql_no_dict));
		}
		if (_jobStarter != null)
		{
			_jobStarter.onTaskFinished();
		}
	}

	@Override
	public void onSuccessString(String result) {
		Log.e(LOG_TAG, "RESULT from loading dict");
		if (_uiFragment != null)
		{
			_uiFragment.setTitleText(this.getString(R.string.dict_sql_success));
			_uiFragment.setDictInfoText(result);
		}
		if (_jobStarter != null)
		{
			_jobStarter.onTaskFinished();
		}
	}

	@Override
	public void onSuccessCode(Integer errorCode) {

	}

	@Override
	public void onProgressUpdate(Integer... values) {
		_uiFragment.setProgress(values[0]);
	}

	@Override
	public void onSuccessWords(List<String> result) {

	}

	@Override
	public void onSuccessTranslations(Pair<String, List<String>> result) {

	}

	@Override
	public void onSuccessMyWords(List<Map<String, String>> result) {
	}

	@Override
	public void noTaskSpecified() {
		Log.d(LOG_TAG, "no task, careful");
	}

	@Override
	public void onTaskFinished() {

	}

	@Override
	public boolean taskRunning() {
		return false;
	}
}

