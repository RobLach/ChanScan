package hexdojo.android.chanscanlite;

import java.util.ArrayList;

import android.view.*;
import android.widget.*;
import android.content.*;

public class ChanAdapter extends ArrayAdapter<Chan>{
	
	private ArrayList<Chan> items;
	public ImageLoader imageLoader;
	Context m_context; 
	
	public ChanAdapter(Context context, int textViewResourceId, ArrayList<Chan> items){
		super(context, textViewResourceId, items);
		this.items = items;
		this.m_context = context;
		imageLoader= new ImageLoader(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		//Log.e("CHANADAPTER","getviewCalled");
		View v = convertView;
		if (v == null){
			LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row, null);
		}
	
		Chan o = items.get(position);
		if (o != null){
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			ImageView icon = (ImageView) v.findViewById(R.id.image);
            if (tt != null) {
                tt.setText(o.getChanName());                            }
			if (bt != null) {
					String snap = "";
					int size = o.getSubBoardList().size();
					for (int i = 0; i<size; i++) {
						String shortname = o.getSubBoardList().get(i).getShortName();
						snap+=" "+shortname;
					}
					bt.setText(snap);
            }
			if(icon != null){
				icon.setTag(o.getIconURL());
				imageLoader.DisplayImage(o.getIconURL().toString(), icon);
			}
		}
		return v;	
	}
}
