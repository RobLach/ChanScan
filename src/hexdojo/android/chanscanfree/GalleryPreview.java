package hexdojo.android.chanscanfree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.*;

import com.revmob.RevMob;
import com.revmob.ads.banner.RevMobBanner;

public class GalleryPreview extends Activity
{
	
	public class imagePair{
		int id;
		String imageURL;
		String thumbURL;
	}
	
	private ImageLoader mImageLoader;
	private ImageLoader mLargeImageLoader;
	private ArrayList<imagePair> m_images;
	private GalleryPreviewAdapter m_galAdapter;
	private final int MENU_DL_IMAGE = 0;
	private final int MENU_FULLVIEW = 1;
	imagePair iPair;
	
	private AlertDialog ask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		Bundle b = this.getIntent().getExtras();
		mImageLoader = new ImageLoader(GalleryPreview.this);
		mLargeImageLoader = new ImageLoader(GalleryPreview.this);
		mLargeImageLoader.setHoldOnto(3);
		ArrayList<String> temp = b.getStringArrayList("ThumbImagePairs");
		buildImageDLForm();
		m_images = new ArrayList<imagePair>();
		int id = 0;
		for(int i = 0; i < temp.size(); i++){
			imagePair pair = new imagePair();
			String balls[] = temp.get(i).split("<<>>");
			if(balls.length > 1){
				if(balls[0]!=null || balls[1]!=null){
					pair.id = id;
					id++;
					pair.thumbURL = balls[0];
					pair.imageURL = balls[1];
					m_images.add(pair);
				}
				//Log.e("GALLERYPREVIEW",pair.thumbURL+"(--)"+pair.imageURL);
			}
		}
		m_galAdapter = new GalleryPreviewAdapter(GalleryPreview.this, R.layout.gallerypreviewitem, m_images, mImageLoader);
		
		setContentView(R.layout.gallerypreview);
		

		
		RevMob revmob = RevMob.start(this);
        RevMobBanner banner = revmob.createBanner(this);
        ViewGroup view = (ViewGroup) findViewById(R.id.gallerypreviewbanner);
        view.addView(banner);
	    
	    
	    int width = 320;
	    int height = 52;

	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    float density = displayMetrics.density;

	    width = (int) (width * density);
	    height = (int) (height * density);


	    
//		AdServerView view = (AdServerView) findViewById(R.id.adViewgallery);
//		view.setUpdateTime(20);
	    
		Gallery gal = (Gallery) findViewById(R.id.threadpreviewgallery);
		gal.setAdapter(m_galAdapter);

		gal.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {
				//Log.e("GALLERYPREVIEW","CLICKED");
				iPair = (imagePair) arg0.getItemAtPosition(arg2);
				ImageView iView = (ImageView) findViewById(R.id.threadpreviewimage);
				if(iView != null){
					//Log.e("GALLERYPREVIEW","ATTEMPTING LOAD");
					iView.setTag(iPair.imageURL);
					//Log.e("GALLERYPREVIEW", "Got:" + iPair.imageURL);
					//mLargeImageLoader.clearMemCache();
					mLargeImageLoader.DisplayImage(iPair.imageURL, iView);
				}
				iView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
				    	loadFullView();	
					}
				});
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});		
	}
	
	private void buildImageDLForm() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	    View layout = inflater.inflate(R.layout.imagedlform,null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Download Image?")
	    	   .setView(layout)
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   //ThreadOpen.this.finish();
	            	   
	            	   if(iPair!=null){
	            		   
	            		   ProgressDialog pgd = ProgressDialog.show(GalleryPreview.this, "Image Downloader", "Downloading Image" );
	            		   DownloadImage(iPair.imageURL);
	            		   pgd.dismiss();
	            	   }
	           		
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	               }
	           });
	    ask = builder.create();
	} 
	
	public void DownloadImage(String url){
		try {
			//Log.e("Download ADDRESS",url);
			URL imageURL = new URL(url);
			EditText et =  (EditText)ask.findViewById(R.id.imageDlFormEditText);
			String tag = et.getEditableText().toString();
			String path = Environment.getExternalStorageDirectory().getPath();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			path = path.concat("/"+prefs.getString(getString(R.string.customrootforsave), getString(R.string.defaultrootforsave))+"/"+"Saved Images"+"/");
			if(tag.length()>0){
				path = path.concat(tag+"/");
			}
			File directory = new File(path);
			String filename[] = imageURL.getFile().split("/");
			
			File download = new File(directory, filename[filename.length-1]);
			//Log.e("Downloading DIR", download.toString());
			//Log.e("Downlading file", imageURL.getFile());
			try{
				if(!directory.exists()) directory.mkdirs();
				if(download.exists()) return;
								
				InputStream is= imageURL.openStream();
				OutputStream os = new FileOutputStream(download);
		        Utils.CopyStream(is, os);
		        os.close();
		        is.close();
			}catch (Exception e){}
			Toast.makeText(GalleryPreview.this, "Image Downloaded", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(GalleryPreview.this, "Could not download image.", Toast.LENGTH_SHORT).show();
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_DL_IMAGE, 1, R.string.menu_dlimage).setIcon(R.drawable.icmenusave_1);
        menu.add(Menu.NONE, MENU_FULLVIEW, 2, R.string.menu_zoom).setIcon(R.drawable.icmenuzoom_1);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DL_IMAGE:
            	ask.show();
                break;
            case MENU_FULLVIEW:
            	loadFullView();	
            	break;
        }
        return super.onOptionsItemSelected(item);
    }
		
	 
	@Override
	protected void onDestroy() {
		 super.onDestroy();
		 mImageLoader.clearMemCache();
		 mLargeImageLoader.clearMemCache();
		 mImageLoader.stopThread();
		 mLargeImageLoader.stopThread();
	}

	private void loadFullView() {
		System.gc();
		if(mLargeImageLoader != null && mImageLoader != null && iPair !=null && iPair.imageURL != null){
			mLargeImageLoader.clearMemCache();
			mImageLoader.clearMemCache();
			if(iPair.imageURL.contains("gif")){
				Intent i = null;
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String gifType = prefs.getString(getApplicationContext().getString(R.string.gifViewerType), "defaultGif");
				Log.e("GifType",gifType);
				if(gifType.contentEquals("noGif")){
					i = new Intent(GalleryPreview.this, ImageGallery.class);
				}
				else if(gifType.contentEquals("defaultGif"))
				{
					i = new Intent(GalleryPreview.this, GifViewer.class);
				}
				else if(gifType.contentEquals("webGif")){
					i = new Intent(GalleryPreview.this, GifViewerWeb.class);
				}
				else{
					i = new Intent(GalleryPreview.this, GifViewer.class);
				}
				
				
				Bundle b = new Bundle();
				b.putString("imageURL", iPair.imageURL);
				i.putExtras(b);
				startActivity(i);	
				
			}else{
				Intent bong = new Intent(GalleryPreview.this, ImageGallery.class);
				Bundle shit = new Bundle();
				shit.putString("imageURL", iPair.imageURL);
				bong.putExtras(shit);
				startActivity(bong);
			}
		}
		else{
			Toast.makeText(GalleryPreview.this, "Error Loading Image", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
//	@Override
//	public void adWhirlGeneric() {
//		// TODO Auto-generated method stub
//		
//	}
}
