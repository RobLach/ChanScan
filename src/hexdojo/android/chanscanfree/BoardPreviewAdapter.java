package hexdojo.android.chanscanfree;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BoardPreviewAdapter extends ArrayAdapter<BoardThread>{
	
	private ArrayList<BoardThread> items;
	Context m_context;
	public ImageLoader imageLoader;
	
	public BoardPreviewAdapter(Context context, int textViewResourceId, ArrayList<BoardThread> items, ImageLoader iLoader){
		super(context, textViewResourceId, items);
		this.m_context = context;
		imageLoader = iLoader;
		imageLoader.setStubId(R.drawable.loading);
		this.items = items;
	}
	
	public ArrayList<BoardThread> getItems() {
		return items;
	}

	public void setItems(ArrayList<BoardThread> items) {
		this.items = items;
	}
	
	public void addItem(BoardThread bThread){
		this.items.add(bThread);
	}
	
	public void clean(){
		this.items.clear();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		//Log.e("BOARDPREVIEWADAPTER","GetViewCalled");
		View v = convertView;
		if (v == null){
			LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.threadpreview, null);
		}
	
		BoardThread o = items.get(position);
		if (o != null){
			//Log.e("PREVIEWADAPT","SHOWINSHIT");
			TextView threadText = (TextView) v.findViewById(R.id.threadText);
			TextView threadAuthor = (TextView) v.findViewById(R.id.threadAuthor);
			TextView threadTitle = (TextView) v.findViewById(R.id.threadTitle);
			TextView threadPosts = (TextView) v.findViewById(R.id.postcount);
			ImageView threadThumb = (ImageView) v.findViewById(R.id.threadImage);
            if(threadAuthor != null){
            	threadAuthor.setText(o.getThreadAuthor());
            }
            if(threadTitle != null){
            	threadTitle.setText(o.getPostTitle());
            	if(o.getPostTitle().length() < 1){
            		threadTitle.setHeight(0);
            	}
            }
            if (threadText != null) {
            	//threadText.setText(o.getPostText()+"\n\n\n\n\n\n\n\n\n");
            	threadText.setText(o.getPostText());
            }
            if(threadThumb!=null){
            	threadThumb.setTag(o.getThumbImageUrl()); 
            	if(o.getThumbImageUrl()!=null){
            		if(imageLoader!=null){
            			imageLoader.DisplayImage(o.getThumbImageUrl(), threadThumb);
            		}
            	}
            } 
            if(threadPosts != null){
            	String holla = new String("");
            	if(o.getNumPosts()>5){
            		holla = holla.concat(String.valueOf(o.getNumPosts())+" Posts");
            	}
            	if(o.getNumImages()>5){
            		if(holla.length()>3){
            			holla = holla.concat(", "+String.valueOf(o.getNumImages())+" Images");
            		}
            		else
            		{
            			holla = holla.concat(String.valueOf(o.getNumImages())+" Images");
            		}
            	}
            	if(holla.length()>3){
            		threadPosts.setText(holla);
            		threadPosts.setVisibility(0); //Set visible
            	}
            }
		}
		return v;	
	}

}
