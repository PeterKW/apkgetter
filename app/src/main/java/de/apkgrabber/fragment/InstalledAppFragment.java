package com.apkgetter.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;

import android.util.Log;
import com.apkgetter.R;
import com.apkgetter.adapter.InstalledAppAdapter;
import com.apkgetter.event.InstalledAppTitleChange;
import com.apkgetter.event.UpdateInstalledAppsEvent;
import com.apkgetter.model.InstalledApp;
import com.apkgetter.updater.UpdaterOptions;
import com.apkgetter.util.GenericCallback;
import com.apkgetter.util.InstalledAppUtil;
import com.apkgetter.util.MyBus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment(R.layout.fragment_installed_apps)
public class InstalledAppFragment
		extends Fragment
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.list_view)
	RecyclerView mRecyclerView;

	@ViewById(R.id.swipe_container)
	SwipeRefreshLayout swipeRefreshLayout;

	@Bean
	InstalledAppUtil mInstalledAppUtil;

	@Bean
	MyBus mBus;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		mBus.register(this);
		updateInstalledApps(new UpdateInstalledAppsEvent());

		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				updateInstalledApps(new UpdateInstalledAppsEvent());
				Log.d("InstalledAppFragmnet","refreshing...");
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDestroy() {
		mBus.unregister(this);
		super.onDestroy();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void updateInstalledApps(
			UpdateInstalledAppsEvent ev
	) {
		mInstalledAppUtil.getInstalledAppsAsync(getContext(), new GenericCallback<List<InstalledApp>>() {
			@Override
			public void onResult(List<InstalledApp> items) {
				setListAdapter(items);
				swipeRefreshLayout.setRefreshing(false);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	protected void setListAdapter(
			List<InstalledApp> items
	) {
		if (mRecyclerView == null || mBus == null) {
			return;
		}

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		if (new UpdaterOptions(getContext()).disableAnimations()) {
			mRecyclerView.setItemAnimator(null);
		} else {
			((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
		}
		mRecyclerView.setAdapter(new InstalledAppAdapter(getContext(), mRecyclerView, items));
		mBus.post(new InstalledAppTitleChange(getString(R.string.tab_installed) + " (" + items.size() + ")"));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
