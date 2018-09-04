package hexdojo.android.chanscanfree;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BoardAdapter extends ArrayAdapter<Board>{
	
	private ArrayList<Board> items;
	Context m_context;

	public BoardAdapter(Context context, int textViewResourceId, ArrayList<Board> items){
		super(context, textViewResourceId, items);
		this.m_context = context;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_context);
    	if(prefs.getBoolean(m_context.getString(R.string.useLongNames), false)){
    		Comparator<Board> wow = new Comparator<Board>() {
    			@Override
    			public int compare(Board o1, Board o2) {
    				return o1.boardName.compareTo(o2.boardName);
    			}
    		};
    		Collections.sort(items,wow);
    	
    	}
    	else{
    		Comparator<Board> wow = new Comparator<Board>() {

    			@Override
    			public int compare(Board o1, Board o2) {
    				return o1.shortName.compareTo(o2.shortName);
    			}
    		};
    		Collections.sort(items,wow);
    	}
		this.items = items;
	}
	
	@Override 
	public View getView(int position, View convertView, ViewGroup parent){
		View v = convertView;
		if (v == null){
			LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.picker, null);
		}
	
		Board o = items.get(position);
		if (o != null){
			TextView text = (TextView) v.findViewById(R.id.boardselectname);
            if (text != null) {
            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_context);
            	if(!prefs.getBoolean(m_context.getString(R.string.useLongNames), false)){
            		text.setText(o.getShortName());
            	}
            	else{
            		text.setText(o.getBoardName());
            		Log.e("Shit",o.getBoardName());
            		text.setMaxLines(1);
            		text.setTextSize(30.f);
            	}
            	
            }
		}
		return v;	
	}
}
