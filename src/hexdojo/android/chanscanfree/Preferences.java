package hexdojo.android.chanscanfree;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Preferences extends PreferenceActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,0,0,"Restore to Default Values");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == 0){
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			pref.edit().clear().commit();
			PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
			getPreferenceScreen().removeAll();
			addPreferencesFromResource(R.xml.preferences);
			
			Toast.makeText(this, "Preferences Reset to Defaults", Toast.LENGTH_SHORT).show();
			
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
