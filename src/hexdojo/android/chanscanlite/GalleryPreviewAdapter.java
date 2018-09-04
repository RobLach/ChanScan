package hexdojo.android.chanscanlite;

import hexdojo.android.chanscanlite.GalleryPreview.imagePair;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class GalleryPreviewAdapter extends ArrayAdapter<imagePair>{
	
	ArrayList<imagePair> items;
	Context m_context;
	public ImageLoader imageLoader;
	
	public GalleryPreviewAdapter(Context context, int textViewResourceId, ArrayList<imagePair> data, ImageLoader iLoader) {
		super(context, textViewResourceId, data);
		this.items = data;
		this.m_context = context;
		imageLoader = iLoader;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		//Log.e("GalleryPreviewAdapter","GetViewCalled");
		View v = convertView;
		if (v == null){
			LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.gallerypreviewitem, null);
		}
	
		imagePair o = items.get(position);
		if (o != null){
			//Log.e("GalleryPreviewAdapter","SHOWINSHIT");
			ImageView iView = (ImageView) v.findViewById(R.id.gallerypreviewitemimage);
			if(iView!=null){
				iView.setTag(o.thumbURL);
				imageLoader.DisplayImage(o.thumbURL, iView);
			}
		} 
		
		return v;	
	}

}
