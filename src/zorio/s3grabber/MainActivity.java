package zorio.s3grabber;

import zorio.s3grabber.PreferenceListFragment.OnPreferenceAttachedListener;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.widget.ShareActionProvider;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, OnPreferenceAttachedListener, OnPreferenceChangeListener {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	ShareActionProvider shareProvider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		shareProvider = (ShareActionProvider)menu.findItem(R.id.menu_share).getActionProvider();
		return true;
	}
	

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		
		private Fragment settingsFragment;
		private Fragment uploadFragment;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			uploadFragment = new UploadFragment();
			settingsFragment = new PreferenceListFragment(R.xml.preferences);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position) {
			case 0:
				return uploadFragment;
			case 1:
				return settingsFragment;
			default:
				return new Fragment();
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase();
			case 1:
				return getString(R.string.title_section3).toUpperCase();
			}
			return null;
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		setPreferenceSummary(preference, newValue);
		return true;
	}

	@Override
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		if(root == null)
			return;
		if(xmlId == R.xml.preferences) {
			populatePreferences(root);
		}
	}
	
	private void populatePreferences(PreferenceGroup root) {
		for(int i = 0; i < root.getPreferenceCount(); i++) {
			Preference p = root.getPreference(i);
			if(p instanceof PreferenceGroup) {
				populatePreferences((PreferenceGroup)p);
			} else {
				if(p.getOnPreferenceChangeListener() != this) {
					p.setOnPreferenceChangeListener(this);
				}
				setPreferenceSummary(p, null);
			}
		}
	}
	
	private void setPreferenceSummary(Preference p, Object newVal) {
		if(p instanceof EditTextPreference) {
			if(newVal != null) {
				((EditTextPreference) p).setText(newVal.toString());
			}
			p.setSummary(((EditTextPreference)p).getText());
		} else if(p instanceof ListPreference) {
			if(newVal != null) {
				((ListPreference) p).setValue(newVal.toString());
			}			
			p.setSummary(((ListPreference)p).getValue());
		}		
	}
	
	public void updateShareIntent(String uri) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT, uri);
		shareProvider.setShareIntent(i);
	}


}
