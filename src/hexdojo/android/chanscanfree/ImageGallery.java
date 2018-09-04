package hexdojo.android.chanscanfree;


import java.net.MalformedURLException;
import java.net.URL;

import com.revmob.RevMob;
import com.revmob.ads.banner.RevMobBanner;
//import java.util.Arrays;
//import java.util.HashSet;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

public class ImageGallery extends Activity 
{

	private static final int MENU_ID_RESET = 0;
	private static final int MENU_ID_HELP = 1;

    private ImageZoomView mZoomView;
    private DynamicZoomControl mZoomControl;
    private Bitmap mBitmap;
    private ImageLoader iLoader;
    
    private Runnable loadImage = null;
    private ProgressDialog m_ProgressDialog = null;
    private String imgURL;
    private Boolean rotateIt = false;

    private LongPressZoomListener mZoomListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.zoomgallery);
    	

    	
    	RevMob revmob = RevMob.start(this);
        RevMobBanner banner = revmob.createBanner(this);
        ViewGroup view = (ViewGroup) findViewById(R.id.zoomgallerybanner);
        view.addView(banner);
	    
	    
    	iLoader = new ImageLoader(ImageGallery.this);
    	mZoomControl = new DynamicZoomControl();
    	
    	mZoomListener = new LongPressZoomListener(ImageGallery.this);
    	mZoomListener.setZoomControl(mZoomControl);
    	
        Bundle b = this.getIntent().getExtras();
        imgURL = b.getString("imageURL");
        URL pizza;
													try {
		pizza = new URL(imgURL);
		setTitle("Image Viewer: "+pizza.getFile());	} 	catch (MalformedURLException e1) {finish();}
        
        
        
        mZoomView = (ImageZoomView)findViewById(R.id.imageZoomer);
        mZoomView.setImage(BitmapFactory.decodeResource(getResources(),R.drawable.blackdot_old));
        mZoomView.setZoomState(mZoomControl.getZoomState());
        mZoomView.setOnTouchListener(mZoomListener);
        mZoomControl.setAspectQuotient(mZoomView.getAspectQuotient());
        resetZoomState();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        rotateIt = prefs.getBoolean(getString(R.string.autoRotate), false);
        loadImage = new Runnable() {
			
			@Override
			public void run() {
				try{
					//System.gc();
					if(rotateIt){
						mBitmap = iLoader.GetBitmapRotated(imgURL, 1400);
					}
					else{
						mBitmap = iLoader.GetBitmap(imgURL, 1400);
					}
	        	
		        	//Log.e("IMAGEGALLERY",imgURL);
				}catch(Exception e){
					//Log.e("IMAGEGALLERY", e.getMessage());
				}
		        runOnUiThread(returnThread);				
			}
		};
		
		Thread thread = new Thread(null, loadImage, "MagentoBackground");
		thread.start();
		m_ProgressDialog = ProgressDialog.show(ImageGallery.this, "Image Viewer", "Downloading Image...");
		
    }
    
	private Runnable returnThread = new Runnable() {
		@Override
		public void run() {
			try{
			iLoader.stopThread();
			if(mBitmap!=null){
				mZoomView.setImage(mBitmap);
				mZoomControl.setAspectQuotient(mZoomView.getAspectQuotient());
				m_ProgressDialog.dismiss();
				mZoomView.invalidate();
			}
			else{
				finish();
			}
			}
			catch(Exception e){
				finish();
			}
		}
	};
    
    @Override
    protected void onDestroy() {
    	try{
    	super.onDestroy();
        iLoader.clearMemCache();
        mBitmap.recycle();
        mZoomView.setOnTouchListener(null);
        mZoomControl.getZoomState().deleteObservers();
        iLoader.stopThread();
    	}
    	catch (Exception e) {
		}
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_RESET, 2, R.string.menu_reset).setIcon(R.drawable.icmenuview_1);
        menu.add(Menu.NONE, MENU_ID_HELP, 1, "Help").setIcon(R.drawable.ic_menu_help_1);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_RESET:
                resetZoomState();
                break;
            case MENU_ID_HELP:
            	 new AlertDialog.Builder(ImageGallery.this)
					.setMessage("Use touch screen to move image around.\nLong press to zoom.")
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					})
					.setTitle("Image Viewer Help")
					.setIcon(R.drawable.chanscaniconsmall)
					.create().show();
            	break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	super.onConfigurationChanged(newConfig);
    	
    }
    
    /**
     * Reset zoom state and notify observers
     */
    private void resetZoomState() {
        mZoomControl.getZoomState().setPanX(0.5f);
        mZoomControl.getZoomState().setPanY(0.5f);
        mZoomControl.getZoomState().setZoom(0.9f);
        mZoomControl.getZoomState().notifyObservers();
    }

//	@Override
//	public void adWhirlGeneric() {
//		// TODO Auto-generated method stub
//		
//	}
}
