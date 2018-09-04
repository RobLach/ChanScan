package hexdojo.android.chanscanfree;

import java.util.ArrayList;
import java.util.Hashtable;

import com.revmob.RevMob;
import com.revmob.ads.banner.RevMobBanner;



import android.app.Activity;
import android.content.Context;
//import android.graphics.Color;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.RelativeLayout.LayoutParams;

public class ThreadAdapter extends ArrayAdapter<ThreadItem>{
	private ArrayList<ThreadItem> m_items = null;
	public ImageLoader imageLoader;
	Context m_context;

	public ThreadAdapter(Context context, int textViewResourceId, ArrayList<ThreadItem> items, ImageLoader iLoader){
		super(context, textViewResourceId, items);
		try{
		this.m_items = items;
		this.m_context = context;
		imageLoader = iLoader;
		}
		catch(Exception e){
			//Log.e("TheadADAPTER","OH BALLS");
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View v = convertView;
		try{
			ThreadItem item = m_items.get(position);
			//Log.e("THREADADAPTER","getviewCalled");

		if(item != null){
			if(item.isAd){
				int width = 320;
			    int height = 52;

			    DisplayMetrics displayMetrics = m_context.getResources().getDisplayMetrics();
			    float density = displayMetrics.density;
//
			    width = (int) (width * density);
			    height = (int) (height * density);
//			    

			    
			    
			    RevMob revmob = RevMob.start((Activity)m_context);
		        RevMobBanner banner = revmob.createBanner((Activity)m_context);
		        return banner;
		        

			}
			else{
				
				if (v == null || v instanceof RevMobBanner){
					LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					v = vi.inflate(R.layout.threaditemwad, null);
				}
				ImageView imageV = (ImageView) v.findViewById(R.id.thumbImage);
				TextView textV = (TextView) v.findViewById(R.id.threadText);
				TextView textVauth = (TextView) v.findViewById(R.id.threadAuthor);
				TextView textVdate = (TextView) v.findViewById(R.id.threadDate);
				TextView textVid = (TextView) v.findViewById(R.id.threadID);
				if(imageV != null){
					imageV.setTag(item.getThumbURL());
					imageLoader.DisplayImage(item.getThumbURL(), imageV);
				}
				if(textV != null){
					textV.setAutoLinkMask(Linkify.EMAIL_ADDRESSES|Linkify.WEB_URLS|Linkify.MAP_ADDRESSES);
					textV.setText(item.getPostText());
				}
				if(textVauth != null){
					textVauth.setText(item.getPostername());
				}
				if(textVdate != null){
					textVdate.setText(item.getDate());
					//Log.e("ThreadAdapter", "Date :"+item.getDate());
				}
				if(textVid != null){
					textVid.setText(item.getCommentID());
					//Log.e("ThreadAdapter", "ID: "+item.getCommentID());
				}
			}
		}
		}catch(Exception e){
			//Log.e("OHBALLS","SNAP");
		}
		return v;
	}
}
