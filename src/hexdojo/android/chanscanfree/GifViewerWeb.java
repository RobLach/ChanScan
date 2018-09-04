package hexdojo.android.chanscanfree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.revmob.RevMob;
import com.revmob.ads.banner.RevMobBanner;
//import java.util.Arrays;
//import java.util.HashSet;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
//import android.util.DisplayMetrics;
//import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class GifViewerWeb extends Activity 
{
	private String imgURL = null;
	private ProgressDialog m_ProgressDialog = null;
	  private File imagefile;
	  private File cacheDir;
	  private Runnable getGif;
	  private Thread   getGifThread;
	
	  private Runnable returnRes = new Runnable() {
		
		@Override
		public void run() {			
	        WebView gifView = (WebView)findViewById(R.id.gifWebView);
	        WebSettings settings = gifView.getSettings();
	        settings.setBuiltInZoomControls(true);
	        settings.setSupportZoom(true);
	        settings.setLoadsImagesAutomatically(true);
	        gifView.setBackgroundColor(Color.argb(0, 200, 200, 200));
	        String html = new String();
	        html = ("<html><head></head><body><center><img src=\""+"file:/"+imagefile.getAbsolutePath()+"\"/></body></html>");
	        //Log.e("html",html);
	        //gifView.loadUrl("file:/"+imagefile.getAbsolutePath());
	        gifView.loadDataWithBaseURL("file:/"+imagefile.getAbsolutePath(), html, "text/html", "utf-8", "");
	        //gifView.loadData(html, "text/html", "utf-8");
	        m_ProgressDialog.dismiss();
			}
	  };
	  
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  		super.onCreate(savedInstanceState);
  		setContentView(R.layout.gifpageweb);
  		
  		
  		RevMob revmob = RevMob.start(this);
        RevMobBanner banner = revmob.createBanner(this);
        ViewGroup view = (ViewGroup) findViewById(R.id.gifWebViewBanner);
        view.addView(banner);
  		
  		
  		
  		
  		
  		
  		m_ProgressDialog = ProgressDialog.show(GifViewerWeb.this,"Updating...", "Downloading Gif");
		m_ProgressDialog.setOnCancelListener( new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				m_ProgressDialog.dismiss();
				if(getGifThread != null) getGifThread.interrupt();
				finish();
			}
		});
		m_ProgressDialog.setCancelable(true);
  		
  		

		Bundle b = this.getIntent().getExtras();
        imgURL = b.getString("imageURL");
        
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"ChanScan Cache");
        else{
            cacheDir=new File(GifViewerWeb.this.getCacheDir(), "ChanScan Cache");
        }
        if(!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        String filename=String.valueOf(imgURL.hashCode()+".gif");
        imagefile=new File(cacheDir, filename);
        
        getGif = new Runnable() {
			
			@Override
			public void run() {
				try{
					URL hotzone = new URL(imgURL);	
		        	if(!imagefile.exists() && !Thread.interrupted()){
		        		InputStream is= hotzone.openStream();
		        		if(!Thread.interrupted()){
		        			OutputStream os = new FileOutputStream(imagefile);
		        			Utils.CopyStream(is, os);
		        			os.flush();
		        			os.close();
		        			is.close();
		        		}
		        	}
		        	m_ProgressDialog.dismiss();
		        	runOnUiThread(returnRes);
				}
				catch(Exception e){
				}
			}
		};
        
		getGifThread = new Thread(null, getGif, "MagentoBackground2");
		getGifThread.setPriority(Thread.MIN_PRIORITY);
		getGifThread.start();
		
	}



}
