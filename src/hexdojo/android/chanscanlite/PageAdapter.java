package hexdojo.android.chanscanlite;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PageAdapter extends ArrayAdapter<String>{

	private ArrayList<String> items;
	Context m_context;

	public PageAdapter(Context context, int textViewResourceId, ArrayList<String> items){
		super(context, textViewResourceId, items);
		this.m_context = context;
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		//Log.e("PageAdapter","getviewCalled");
		View v = convertView;
		if (v == null){
			LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.pager, null);
		}
	
		String o = items.get(position);
		if (o != null){
			TextView text = (TextView) v.findViewById(R.id.pagenum);
            if (text != null) { 
            	text.setText(o);
            }
		}
		return v;	
	}
}
