package hexdojo.android.chanscanlite;

//import hexdojo.android.chanscanfree.LicenseCheckActivity.MyLicenseCheckerCallback;
import java.io.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
//import java.net.URLConnection;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.*;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.ccil.cowan.tagsoup.Parser;


import com.revmob.RevMob;
import com.revmob.RevMobUserGender;
import com.revmob.ads.fullscreen.RevMobFullscreen;
import com.revmob.ads.banner.RevMobBanner;



import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.jsoup.Jsoup;

import android.app.AlertDialog;
//import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
//import android.location.Location;
//import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
//import android.provider.Settings;
//import android.util.Log;
//import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
//import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
//import android.view.ViewGroup;
//import android.view.MotionEvent;
import android.view.View;
//import android.view.View.OnTouchListener;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
//import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
//import com.android.vending.licensing.*;
//import android.widget.TableLayout.LayoutParams;

public class ChanScan extends ListActivity 
{
	private ProgressDialog m_ProgressDialog = null;
	private ChanAdapter m_chanadapter;
	private BoardAdapter m_boardadapter;
	private BoardPreviewAdapter m_boardPreviewAdapter;
	private Runnable viewChans;
	private Runnable viewThreads;
	private Context m_context;
	private ArrayList<Chan> m_chans = null;
	private ArrayList<BoardThread> m_threads = null;
	private ArrayList<String> m_pages = null;
	private ImageLoader m_imageLoader;
	private PageAdapter m_pageAdapter;
	private static final int REQUEST_CODE = 3117;
	
	
	private RevMob revmob;
	private RevMobFullscreen fullscreenAd;
	
	private volatile Thread viewChansThread;
	private volatile Thread viewThreadsThread;
	private volatile Thread cleanUp;
	private Runnable cleanUpRunnable;
	
	private static final int MENU_ID_REFRESH = 0;
	private static final int MENU_ID_REPLY = 1;
	private static final int MENU_ID_SETTINGS = 3;
	private static final int MENU_ID_EMAIL = 4;
	private static final int MEMU_ID_BUYIT = 5;
	private static final int MENU_ID_HIDE = 6;
	private static final int MENU_ID_CLEARHIDDEN = 7;
	private static final int MENU_ID_ADDFAV = 8;
	private static final int MENU_ID_REMOVEFAV = 9;
	private static final int MENU_ID_SHOWFAVS = 10;
	private static final int MENU_ID_HELP = 11;
	
	
	private static ArrayList<String> hiddenBoards = null;
	private static String hiddenBoardsFile = "hiddenboards";
	
	private Board m_board;
	private Chan m_chan;
	ListView frontpage = null;
	ViewFlipper flipper = null;
	Gallery chooser = null;
	Gallery pageNumero = null;
	Gallery selector = null;

	private AlertDialog replyForm;
	private AlertDialog disclaimer;
	private AlertDialog devMessage;
	
	private String devMsg = "";
	private Handler mHandler2;
//	private AlertDialog.Builder buildit;
	
	private File uploadFile = null;
	private PostingInfo postingInfo;
	private List<NameValuePair> captchaPairs;
	
	private HttpClient m_httpclient;
//	private GSSDK sdk;
	
	private boolean showingFavs = false;

	public enum BoardType {
		FOURCHAN, SEVENCHAN, FOURTWENTYCHAN, TWOCHAN, KUSABAX, WAKABA, FOURARCHIVE, NEWFOURCHAN, NEWFOURCHANv2;
	}
	
	
	private Thread getDevMsg = new Thread() {
        public void run() {
            try {
                URL updateURL = new URL("http://www.hexdojo.com/apps/chanscan/devmsg");
                HttpURLConnection conn = (HttpURLConnection)updateURL.openConnection();
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                conn.setIfModifiedSince(0);
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
 
                int current = 0;
                while((current = bis.read()) != -1){
                    baf.append((byte)current);
                }
                /* Convert the Bytes read to a String. */
                devMsg = new String(baf.toByteArray());
                mHandler2.post(showDevMsg);
                conn.disconnect();
                bis.close();
                is.close();
            } catch (Exception e) {
            }
        }
    };
    
    private Runnable showDevMsg = new Runnable(){
        public void run(){
        	try{
	        	if(devMsg.length()>4){
		        	final String[] devMsgs = devMsg.split("\\n");
		        	final int devmsgnum = Integer.parseInt(devMsgs[0].trim());
		        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChanScan.this);
		        	final int currentnum = prefs.getInt("devMsgNum", 0);
		        	Log.e("DevMsgNum",""+devmsgnum);
		        	Log.e("CurrentNum",""+currentnum);
		        	String newmsg = new String("");
		        	for(int i = 1; i< devMsgs.length; i++){
		        		newmsg = newmsg.concat(devMsgs[i]);
		        		newmsg = newmsg.concat("\n");
		        	}
		        	
		        	if(devmsgnum>currentnum){
			        	AlertDialog.Builder buildit = new AlertDialog.Builder(ChanScan.this);
			    		devMessage = buildit
						.setMessage(newmsg)
						.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChanScan.this);
								prefs.edit().putInt("devMsgNum", devmsgnum).commit();
								Log.e("devmsgnumput",""+devmsgnum);
								//prefs.getInt("devMsgNum", 0)
							}
						})
						.setTitle("Update Notes")
						.setIcon(R.drawable.chanscaniconsmall)
						.create();
			    		devMessage.show();
		        	}
		        	
	        	}
	        }
        	catch(Exception e){}
        }
    };
	
  
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//System.gc();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		
	    /*Hashtable<String, String> map = new Hashtable<String, String>();
		map.put("age" , "21");
		map.put("gender", "male");
		map.put("income", "85000");
		map.put("keywords", "lonely,depression,hacking,drugs,computers,games,dating");
		map.put("ethnicity", "white");
		map.put("orientation", "straight"); 
		map.put("marital", "single");
		map.put("children", "false");
		map.put("education", "college");
		map.put("politics", "libertarian");
		*/
		
		revmob = RevMob.start(this);
		
		revmob.setUserGender(RevMobUserGender.MALE);
		revmob.setUserAgeRangeMax(30);
		revmob.setUserAgeRangeMin(21);
		ArrayList<String> interests = new ArrayList<String>();
		interests.add("games");
		interests.add("anime");
		interests.add("manga");
		interests.add("video");
		interests.add("japan");
		interests.add("hentai");
		interests.add("politics");
		interests.add("libertarian");
		interests.add("single");
		interests.add("dating");
		interests.add("lonely");
		revmob.setUserInterests(interests);
		
		fullscreenAd = revmob.createFullscreen(this, null);
		
		
		//MMAdView interAdView = new MMAdView(this, "36358", MMAdView.FULLSCREEN_AD_LAUNCH, true, map);
		//interAdView.setId(1337);
		//interAdView.callForAd();
	
		hiddenBoards = new ArrayList<String>();

		//AdServerInterstitialView interstitialView = new AdServerInterstitialView(ChanScan.this, 117766, 25054);
		//interstitialView.setShowCloseButtonTime(3);
		//interstitialView.setAutoCloseInterstitialTime(20);
		//interstitialView.show();
		
		//DevMsg
		mHandler2 = new Handler();
		getDevMsg.start();
		
		m_imageLoader = new ImageLoader(ChanScan.this);

		m_httpclient =  new DefaultHttpClient();
		this.m_chans = new ArrayList<Chan>();
		this.m_threads = new ArrayList<BoardThread>();
		m_context = this;
		this.m_chanadapter = new ChanAdapter(this, R.layout.row, m_chans);
		setListAdapter(this.m_chanadapter);
		
			
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		frontpage = getListView();
		frontpage.setVisibility(View.GONE);
		
		
		
		frontpage.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
		    	//System.gc();
				flipper.setInAnimation(inFromRightAnimation());
				flipper.setOutAnimation(outToLeftAnimation());
				flipper.showNext();
				Chan temp = (Chan) arg0.getItemAtPosition(arg2);
				setTitle("ChanScan Lite - "+temp.getChanName());
				loadBoards(temp,false);
				Toast blam = Toast.makeText(ChanScan.this, "Tap on a board letter to load.", Toast.LENGTH_SHORT);
				blam.setGravity(Gravity.TOP, 0, 140);
				blam.show();
			}
		});
		frontpage.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				flipper.setInAnimation(inFromRightAnimation());
				flipper.setOutAnimation(outToLeftAnimation());
				flipper.showNext();
				Chan temp = (Chan) arg0.getItemAtPosition(arg2);
				setTitle("ChanScan Lite - "+temp.getChanName());
				loadBoards(temp,true);
				Toast blam = Toast.makeText(ChanScan.this, "Tap on a board letter to load.", Toast.LENGTH_SHORT);
				blam.setGravity(Gravity.TOP, 0, 140);
				blam.show();
				return true;
			}
		});
		
		viewThreads = new Runnable() {
			
			@Override
			public void run() {
				try {
			    	//System.gc();
			    	URL url;
			    	if(m_board.getCurrentPage() != 0){
			    		if(m_board.getBoardType()!=BoardType.FOURARCHIVE){
			    			url = new URL(m_board.getURL()+m_board.getCurrentPage()+m_board.getPageSuffix());
			    		}
			    		else{
			    			String yurl = m_board.getURL().substring(0, m_board.getURL().indexOf("?"));
			    			url = new URL(yurl+m_board.getPageSuffix()+(m_board.getCurrentPage()+1)+"&mode=compressed");
			    		}
			    	}else{
			    		if(m_board.getBoardType()!=BoardType.FOURARCHIVE){
			    			url = new URL(m_board.getURL());
			    		}
			    		else{
			    			url = new URL(m_board.getURL()+"&mode=compressed");
			    		}
			    		
			    	}
			    	//Log.e("BoardListURL",url.toExternalForm());
					getThread(url, m_board.getBoardType());
				} catch (MalformedURLException e) {
				}
			}
		};
		
		cleanUpRunnable = new Runnable() {
			
			@Override
			public void run() {
				//System.gc();
				clearCache();
				
			}
		};
		
		viewChans = new Runnable() {
			@Override
			public void run() {
					//System.gc();
					getChans();
				}
		};
		
		
		//ADS
		
//		 sdk = GSSDK.initialize(this, "ed04d332-c19f-41b1-8298-4e07d02bab26");
		 
		 int width = 320;
		    int height = 52;

		    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		    float density = displayMetrics.density;

		    width = (int) (width * density);
		    height = (int) (height * density);

//		    AdWhirlTargeting.setAge(23);
//		    AdWhirlTargeting.setGender(AdWhirlTargeting.Gender.MALE);
//		    String keywords[] = { "online", "games", "gaming", "singles", "dating", "depression", "music", "loneliness", "android", "4chan", "anime", "asian", "japan", "manga" };
//		    AdWhirlTargeting
//		        .setKeywordSet(new HashSet<String>(Arrays.asList(keywords)));
//		    //AdWhirlTargeting.setPostalCode("94123");
//		    //AdWhirlTargeting.setPostalCode(postalCode)
//		    AdWhirlTargeting.setTestMode(false);
//
//		    AdWhirlAdapter.setGoogleAdSenseAppName("ChanScan Free");
//		    AdWhirlAdapter.setGoogleAdSenseCompanyName("HexDojo");
//
//		    // Optional, will fetch new config if necessary after five minutes.
//		    AdWhirlManager.setConfigExpireTimeout(1000 * 60 * 5);
//
//		    // References AdWhirlLayout defined in the layout XML.
//		    AdWhirlLayout adWhirlLayout = (AdWhirlLayout) findViewById(R.id.adwhirl_FrontPage);
//		    adWhirlLayout.setAdWhirlInterface(this);
//		    adWhirlLayout.setMaxWidth(width);
//		    adWhirlLayout.setMaxHeight(height);
//		    
//		    AdWhirlLayout adWhirlLayout2 = (AdWhirlLayout) findViewById(R.id.adwhirl_BoardSelector);
//		    adWhirlLayout2.setAdWhirlInterface(this);
//		    adWhirlLayout2.setMaxWidth(width);
//		    adWhirlLayout2.setMaxHeight(height-2);
//		    adWhirlLayout2.setMinimumHeight(height-2);
		// \ADS
		
		
		    
		runViewChansThread();
		try {
			FileInputStream fis = openFileInput(hiddenBoardsFile);
			InputStreamReader inputR = new InputStreamReader(fis);
			BufferedReader buffR = new BufferedReader(inputR);
			String line;
			while(true){
				try {
					line = buffR.readLine();
					if(line!=null && line.length()>1){
						hiddenBoards.add(line);
					}
					else{
						break;
					}
				} catch (IOException e) {
					}
				
			}
			fis.close();
		} catch (Exception e) {
		}
		
//	AdServerView view = (AdServerView) findViewById(R.id.adViewFront);
//	view.setUpdateTime(20);
//	
//	AdServerView view2 = (AdServerView) findViewById(R.id.adViewBack);
//	view2.setUpdateTime(20);
		
		fullscreenAd.show();
//		
	}
	
	private void runViewChansThread() {
		if(viewChansThread !=null){
			viewChansThread.interrupt();
		}
		viewChansThread = new Thread(null, viewChans, "MagentoBackground");
		viewChansThread.start();
		
		m_ProgressDialog = ProgressDialog.show(ChanScan.this, "Updating...", "Downloading Board List...");
		if(disclaimer != null && disclaimer.isShowing()){
			m_ProgressDialog.dismiss();
		}
		m_ProgressDialog.setOnCancelListener( new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				viewChansThread.interrupt();
				m_httpclient.getConnectionManager().shutdown();
				m_httpclient = new DefaultHttpClient();
			}
		});
		m_ProgressDialog.setCancelable(true);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		disclaimer = builder
					.setMessage(R.string.disclaimer)
					.setPositiveButton("I Understand", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					})
					.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.setTitle("Disclaimer")
					.setIcon(R.drawable.chanscaniconsmall)
					.create();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean(getString(R.string.showDisclaimer), true)){
			disclaimer.show();
		}
		
//		AdServerView adViewFront = (AdServerView)findViewById(R.id.adViewFront);
//		adViewFront.update();
	}

	private void loadBoards(Chan chan, boolean favs) {
		//BoardThread temp = new BoardThread();
		//temp.setThumbImageURL("http://dl.dropbox.com/u/8367315/chooseboard.png");
		//m_threads.add(temp);
		m_chan = chan;
		showingFavs = favs;
		m_imageLoader.clearMemCache();
		m_boardPreviewAdapter = new BoardPreviewAdapter(ChanScan.this,	R.layout.threadpreview, this.m_threads, m_imageLoader);
		chooser = (Gallery) findViewById(R.id.threadselection);
		chooser.setScrollbarFadingEnabled(false);
		chooser.setAdapter(this.m_boardPreviewAdapter);
		
		ArrayList<Board> subboardListtemp = chan.getSubBoardList();
		ArrayList<Board> subboardList = new ArrayList<Board>();
		//subboardList = subboardListtemp;
		if(favs){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String[] favboards = prefs.getString(chan.getChanName()+"favs", "").split("`");
			//Log.e("Favboards length",""+favboards.length);
			if(favboards.length == 1 && favboards[0].length()<1){
				Toast.makeText(ChanScan.this, "No Favorites Exist\nLoading Normal List", Toast.LENGTH_LONG).show();
				subboardList = subboardListtemp;
				showingFavs = false;
			}
			else{
				for(Board b : subboardListtemp){
					for(int i =0 ; i<favboards.length; i++){
						if(b.getShortName().contentEquals(favboards[i])){
							subboardList.add(b);
							break;
						}
					}
				}
			}
		}
		else{
			subboardList = new ArrayList<Board>(subboardListtemp);
		}
		this.m_boardadapter = new BoardAdapter(m_context, R.layout.picker, subboardList);
		selector = (Gallery) findViewById(R.id.boardselectsgallery);
		selector.setScrollbarFadingEnabled(true);
		selector.setAdapter(this.m_boardadapter);
		

		
		selector.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Board board = (Board) arg0.getItemAtPosition(arg2);
				TextView tview = (TextView) findViewById(R.id.boardselecttext);
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(m_context);
				if(prefs.getBoolean(getString(R.string.useLongNames), false)){
					tview.setText(board.getShortName());
				}
				else{
					tview.setText(board.getBoardName());
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		selector.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("Board Options");
				menu.add(Menu.NONE, MENU_ID_ADDFAV, 0, "Add to Favorites");
				menu.add(Menu.NONE, MENU_ID_REMOVEFAV, 0, "Remove from Favorites");
			}
		});
		
		selector.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//				viewThreads = new Runnable() {
//					@Override
//					public void run() {
//						try {
//							getThread(new URL(m_board.getURL()), m_board.getBoardType());
//						} catch (MalformedURLException e) {
//							Log.e("GetThreadURL", e.getMessage());
//						}
//					}
//				};
				
				m_board = (Board) arg0.getItemAtPosition(arg2);
				m_pages = new ArrayList<String>();
				for(int i =0; i<m_board.getPages(); i++){
					m_pages.add(String.valueOf(i));
				}
				m_pageAdapter = new PageAdapter(m_context, R.layout.pager, m_pages);
				pageNumero = (Gallery) findViewById(R.id.pageselection);
				pageNumero.setScrollbarFadingEnabled(false);
				pageNumero.setAdapter(m_pageAdapter); 
				m_pageAdapter.notifyDataSetChanged();
				pageNumero.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {
						int page = Integer.parseInt((String)arg0.getItemAtPosition(arg2));
						m_board.setCurrentPage(page);
						runViewThreadsThread();
					}
				});
				
				runViewThreadsThread();

			}
		});

		chooser.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("Thread Options");
				menu.add(Menu.NONE, MENU_ID_HIDE, 0, "Hide Thread");
				
			}
		});
		chooser.setOnItemClickListener(new OnItemClickListener() { 

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				try{
				BoardThread temp = (BoardThread) arg0.getItemAtPosition(arg2);
		         Intent i = new Intent(ChanScan.this, ThreadOpen.class);
		         Bundle b = new Bundle();
		         b.putString("ThreadURL", temp.getOpenThreadURL());
		         b.putInt("BoardType", temp.getBtype().ordinal());
		         b.putString("BoardName", m_board.getShortName());
		         b.putString("ChanName", m_board.getParent().getChanName());
		         i.putExtras(b);
		         startActivity(i);
				}catch(Exception e){
				}
			}
		});
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem troll) {
		AdapterContextMenuInfo minfo = (AdapterContextMenuInfo) troll.getMenuInfo();
		switch(troll.getItemId()){
		case MENU_ID_HIDE:
			BoardThread item = (BoardThread) chooser.getAdapter().getItem(minfo.position);
			String openurl = item.getOpenThreadURL();
			hiddenBoards.add(openurl);
				try {
					OutputStreamWriter out = new OutputStreamWriter(openFileOutput(hiddenBoardsFile, MODE_APPEND));
					out.write(openurl+"\n");
					out.close();
				} catch (Exception e) {

				}
			m_boardPreviewAdapter.remove(item);
			Toast.makeText(this, "Thread is now Hidden", Toast.LENGTH_LONG);
			break;
		case MENU_ID_ADDFAV:
			Board board = (Board) selector.getAdapter().getItem(minfo.position);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String favboards = prefs.getString(board.getParent().getChanName()+"favs", "");
			String[] favs = favboards.split("`");
			boolean found = false;
			for(int i = 0; i <favs.length; i++){
				if(favs[i].equals(board.shortName)){
					found=true;
					Toast.makeText(ChanScan.this, "Already in Favorites", Toast.LENGTH_LONG).show();
					break;
				}
			}
			if(!found){
				favboards = favboards.concat(board.shortName+"`");
				prefs.edit().putString(board.getParent().getChanName()+"favs", favboards).commit();
			}
			Toast.makeText(ChanScan.this, "Added " + board.boardName + " to favorites", Toast.LENGTH_LONG).show();
			if(showingFavs) loadBoards(board.getParent(), true);
			break;
		case MENU_ID_REMOVEFAV:
			Board board2 = (Board) selector.getAdapter().getItem(minfo.position);
			SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(this);
			String favboards2 = prefs2.getString(board2.getParent().getChanName()+"favs", "");
			String[] favs2 = favboards2.split("`");
			String newfavs = "";
			boolean added = false;
			for(int i = 0; i <favs2.length; i++){
				if(favs2[i].equals(board2.shortName)){
					for(int j = 0; j<favs2.length; j++){
						if(j!=i){
							newfavs = newfavs.concat(favs2[j]+"`");
						}
					}
					prefs2.edit().putString(board2.getParent().getChanName()+"favs", newfavs).commit();
					added = true;
					Toast.makeText(ChanScan.this, "Removed " + board2.boardName + " from favorites", Toast.LENGTH_LONG).show();
					break;
				}
			}
			if(!added) Toast.makeText(ChanScan.this, "Board not in favorites.", Toast.LENGTH_LONG).show();
			if(showingFavs) loadBoards(board2.getParent(), true);
			break;
			
			
		}
		return true;
	}

	private long datime = 0;;
	@Override
	public void onBackPressed() {
		if(flipper.getCurrentView().equals(findViewById(R.id.second))){
			flipper.setInAnimation(inFromLeftAnimation());
			flipper.setOutAnimation(outToRightAnimation());
			flipper.showNext();
			m_imageLoader.clearMemCache();
			flipper.forceLayout();
			if(m_threads != null) m_threads.clear();
			if(m_pages != null) m_pages.clear();
			if(m_boardPreviewAdapter != null) m_boardPreviewAdapter.clear();
			if(pageNumero != null) pageNumero.setVisibility(View.INVISIBLE);
			setTitle("ChanScan Lite");
		}else{
			if((datime + 1500)<System.currentTimeMillis()){
				Toast.makeText(this, "Press Back Again to Exit", Toast.LENGTH_SHORT).show();
				datime = System.currentTimeMillis();
			}else{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				if(prefs.getBoolean(getString(R.string.clearCache), false)){
					cleanUp = new Thread(null, cleanUpRunnable, "MagentoBackground");
					cleanUp.start();
					m_ProgressDialog = ProgressDialog.show(ChanScan.this, "Cache", "Clearing Cache");
				}
				else{
					finish();
				}
			}
		}
	}
	
	private void clearCache() {
		m_imageLoader.clearCache();
		runOnUiThread(returnClean);
	}


	private void getChans() {
		if(!viewChansThread.isInterrupted()){
		try {
			m_chans.clear();
	    	//System.gc();
			URL url = new URL(getString(R.string.defaultBoardUrl));
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			ChanListHandler chanListHandler = new ChanListHandler(m_chans);
			xr.setContentHandler(chanListHandler);
			InputSource source = new InputSource(url.openStream());
			xr.parse(source);
		} catch (Exception e) {
		}
		runOnUiThread(returnRes);
		}
	}

	private void getThread(URL url, BoardType btype) {
		
		try {
	    	//System.gc();
			if(!viewThreadsThread.isInterrupted()){
				postingInfo = new PostingInfo();
				if(!viewThreadsThread.isInterrupted()){
					if(btype.equals(BoardType.NEWFOURCHANv2)){
						StringBuilder jsonBuilder = new StringBuilder(2048);
						HttpClient client = new DefaultHttpClient();
						//HttpGet get = new HttpGet("http://a.4cdn.org/"+m_board.getShortName()+"/"+m_board.getCurrentPage()+".json");
						HttpGet get = new HttpGet("http://a.4cdn.org/"+m_board.getShortName()+"/"+(m_board.getCurrentPage()+1)+".json");
						HttpResponse response = client.execute(get);
						if(response.getStatusLine().getStatusCode() == 200){
							InputStreamReader is = new InputStreamReader(response.getEntity().getContent());
							BufferedReader reader = new BufferedReader(is);
							String line;
							while((line = reader.readLine())!= null){
								jsonBuilder.append(line);
							}
							is.close();
							//Begin parsing json
							JSONObject jObj = new JSONObject(jsonBuilder.toString());
							JSONArray threads = jObj.getJSONArray("threads");
							m_threads = new ArrayList<BoardThread>();
							for(int i = 0; i < threads.length(); i++){
								JSONObject postObj = threads.getJSONObject(i);
								JSONArray posts = postObj.getJSONArray("posts");
								JSONObject post = posts.getJSONObject(0);
								BoardThread tempThread = new BoardThread(m_board.getBoardType());
								try{
									tempThread.setNumImages(post.getInt("images"));
								}catch(Exception e){}
								try{
									tempThread.setNumPosts(post.getInt("replies"));
								}catch(Exception e){}
								try{
									String postText = post.getString("com");
									if(postText != null && postText.length() > 0){
										try{
											postText = postText.replaceAll("(?i)<br[^>]*>", "br2nl");
											postText = postText.replaceAll("\n", "br2nl");
											Source src = new Source(postText);
											TextExtractor textEx = new TextExtractor(src);
											postText = textEx.toString();
											postText = postText.replaceAll("br2nl ", "\n").replaceAll("br2nl", "\n").trim();
											tempThread.setPostText(postText);
										}
										catch(Exception e){}
									}
								}catch(Exception e){} 
								try{
									tempThread.setPostTitle(post.getString("sub"));
								}catch(Exception e){}
								try{
									tempThread.setThreadAuthor(post.getString("name"));
								}catch(Exception e){}
								try{
									//tempThread.setOpenThreadURL("http://a.4cdn.org/"+m_board.getShortName()+"/res/"+post.getInt("no")+".json");
									tempThread.setOpenThreadURL("http://a.4cdn.org/"+m_board.getShortName()+"/thread/"+post.getInt("no")+".json");
								}catch(Exception e){}
								try{
									//tempThread.setThumbImageURL("http://t.4cdn.org/"+m_board.getShortName()+"/thumb/"+post.getLong("tim")+"s.jpg");
									tempThread.setThumbImageURL("http://t.4cdn.org/"+m_board.getShortName()+"/"+post.getLong("tim")+"s.jpg");
								}catch(Exception e){}
								m_threads.add(tempThread);
							}
						}
						else{
								throw new Exception("Couldn't DL ThreadList");
						}
					}
					else{
						BoardListHandler boardListHander = new BoardListHandler(m_threads,	btype, m_board.getURL(), postingInfo); //url.toString(), postingInfo);
						if(!viewThreadsThread.isInterrupted()){
							Parser sp = new Parser();
							//m_httpclient.execute(new HttpGet(url.toURI())).getEntity().getContent();
							sp.setContentHandler(boardListHander);
							sp.setFeature(Parser.namespacesFeature, true);
							sp.setFeature(Parser.CDATAElementsFeature, true);
							sp.setFeature(Parser.ignorableWhitespaceFeature, false);
							sp.setFeature(Parser.namespacePrefixesFeature, false);
							if(!viewThreadsThread.isInterrupted()){
								if(btype.equals(BoardType.TWOCHAN)){
									InputSource src = new InputSource(new InputStreamReader(url.openStream(),"SJIS"));
									sp.parse(src);
								}
								else{ 
									//sp.parse(new InputSource(url.openStream()));
									
									sp.parse(new InputSource(m_httpclient.execute(new HttpGet(url.toURI())).getEntity().getContent()));
								}
				
								if(!viewThreadsThread.isInterrupted()){
										m_threads = boardListHander.getThreadList();
									postingInfo = boardListHander.getPostingInfo();
								}
							}
							
						}
					}
				}
			}
		} catch (Exception e) {
		}
		
		runOnUiThread(returnThread);
		
	}
	
	private Runnable returnClean = new Runnable() {
		@Override
		public void run() {
			m_ProgressDialog.dismiss();
			finish();
		}
	};

	private Runnable returnThread = new Runnable() {
		@Override
		public void run() {
			if(viewThreadsThread != null){
				if(!viewThreadsThread.isInterrupted()){
				if (m_threads != null && m_threads.size() > 0) {
					m_boardPreviewAdapter.clean();
					
					
					
					for(int banana=0; banana<m_threads.size(); banana++){
						m_boardPreviewAdapter.add(m_threads.get(banana));
						for(int i =0; i<hiddenBoards.size(); i++){
								if(m_threads.get(banana).openThreadURL.contains(hiddenBoards.get(i))){
									m_boardPreviewAdapter.remove(m_threads.get(banana));
								}
							}
						}
					
					}
					if(!viewThreadsThread.isInterrupted()){
						
						pageNumero.setVisibility(View.VISIBLE);
					}
				}
				else{
					Toast.makeText(ChanScan.this, "Could Not Load Board.", Toast.LENGTH_LONG).show();
				}
			
				m_boardPreviewAdapter.notifyDataSetChanged();
	
				m_pageAdapter.notifyDataSetChanged();
				
				viewThreadsThread.interrupt();
				if(m_ProgressDialog!=null){
					m_ProgressDialog.dismiss();
				}
				if(chooser!=null){
					chooser.setSelection(0, true);
				}
			}
		}
	};

	private Runnable returnRes = new Runnable() {
		@Override
		public void run() {
			if(viewChansThread != null){
				if(!viewChansThread.isInterrupted()){
				if (m_chans != null && m_chans.size() > 0) {
					m_chanadapter.notifyDataSetChanged();
				}
				else{
					Toast.makeText(m_context, "Error Loading List of Imageboards", Toast.LENGTH_LONG).show();
				}
				m_chanadapter.notifyDataSetChanged();
				}
				viewChansThread.interrupt();
				m_ProgressDialog.dismiss();
				frontpage.setVisibility(View.VISIBLE);
			}
		}
	};
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, MENU_ID_REFRESH, 0, R.string.menu_refresh).setIcon(R.drawable.icmenurefresh);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if(flipper.getCurrentView().equals(findViewById(R.id.second))){
    		menu.clear();
    		menu.add(Menu.NONE, MENU_ID_REFRESH, 0, R.string.menu_refresh).setIcon(R.drawable.icmenurefresh_1);
    		menu.add(Menu.NONE, MENU_ID_REPLY,3, "Create Thread").setIcon(R.drawable.icmenucompose_1);
    		menu.add(Menu.NONE, MEMU_ID_BUYIT, 4, "Buy Full Version").setIcon(R.drawable.shopping_36);
    		menu.add(Menu.NONE, MENU_ID_CLEARHIDDEN, 2, "Clear Hidden Threads").setIcon(R.drawable.icmenuinfodetails_1); 
    		if(!showingFavs){
    			menu.add(Menu.NONE, MENU_ID_SHOWFAVS, 1, "Favorites").setIcon(R.drawable.ic_menu_star_1);
    		}
    		else{
    			menu.add(Menu.NONE, MENU_ID_SHOWFAVS, 1, "Regular List").setIcon(R.drawable.ic_menu_star_1);
    		}
    	}
    	else{
    		menu.clear();
    		menu.add(Menu.NONE, MENU_ID_REFRESH, 0, R.string.menu_refresh).setIcon(R.drawable.icmenurefresh_1);
    		menu.add(Menu.NONE, MENU_ID_SETTINGS, 0 , "Configuration").setIcon(R.drawable.icmenupreferences_1);
    		menu.add(Menu.NONE, MENU_ID_EMAIL, 0, "Email Developers").setIcon(R.drawable.ic_menu_send_1);
			menu.add(Menu.NONE, MENU_ID_HELP, 2, "Help").setIcon(R.drawable.ic_menu_help_1);
    		menu.add(Menu.NONE, MEMU_ID_BUYIT, 0, "Buy Full Version").setIcon(R.drawable.shopping_36);
    	}
    	return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_REFRESH:
            	//System.gc();
            	if(flipper.getCurrentView().equals(findViewById(R.id.second))){
            		if(m_board==null){
            			Toast.makeText(ChanScan.this, "No Board To Refresh", Toast.LENGTH_LONG).show();
            		}else{
            			runViewThreadsThread();
            		}
            	}else{
            		runViewChansThread();
            	}
                break;
            case MENU_ID_REPLY:
            	//System.gc();
            	if(flipper.getCurrentView().equals(findViewById(R.id.second))){
            		if(m_board==null){
            			Toast.makeText(ChanScan.this, "No Board To Post To", Toast.LENGTH_LONG).show();
            		}else{
            			buildReplyForm();
            			//System.gc();
            			replyForm.show();
            			
            		}
            	}
            	break;
            case MENU_ID_SETTINGS:
            	 startActivity(new Intent(ChanScan.this, Preferences.class));
            	break;
            case MENU_ID_EMAIL:
            	Intent i = new Intent(Intent.ACTION_SEND);
            	i.setType("text/plain");
            	i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"devs@hexdojo.com"});
            	i.putExtra(Intent.EXTRA_SUBJECT, "[ChanScan]");
            	try {
            	    startActivity(Intent.createChooser(i, "Send mail..."));
            	} catch (android.content.ActivityNotFoundException ex) {
            	    Toast.makeText(ChanScan.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            	}
            	break;
            case MEMU_ID_BUYIT:
            	Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "http://market.android.com/details?id=hexdojo.android.chanscan"));
            	startActivity(marketIntent);
            	finish();
            	break;
            case MENU_ID_CLEARHIDDEN:
            	deleteFile(hiddenBoardsFile);
            	hiddenBoards.clear();
            	break;
            case MENU_ID_SHOWFAVS:
            	if(m_chan != null){
            		if(!showingFavs){
            			loadBoards(m_chan, true);
            		}
            		else{
            			loadBoards(m_chan, false);
            		}
            	}
            	break;
            case MENU_ID_HELP:
            	startActivity(new Intent(ChanScan.this, Help.class));
            	break;
        }

        return super.onOptionsItemSelected(item);
    }
	
	private Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(600);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	private Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(600);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	private Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(600);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}

	private Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(600);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}

	private void runViewThreadsThread() {
		
		if(viewThreadsThread!=null){
			viewThreadsThread.interrupt();
			try{
				if(replyForm.findViewById(R.id.formEditSubject)!=null){
					((EditText)replyForm.findViewById(R.id.formEditSubject)).setText("");
					((EditText)replyForm.findViewById(R.id.formEditComment)).setText("");
				}
			}catch(Exception e){}
		}
		viewThreadsThread = new Thread(null, viewThreads, "MagentoBackground2");
		viewThreadsThread.start();
//		if(((Math.random()<0.33) && sdk.isAdReady())){
//			sdk.displayAd(this);
//		}
		m_ProgressDialog = ProgressDialog.show(ChanScan.this,"Updating...", "Downloading Threads...");
		m_ProgressDialog.setOnCancelListener( new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				viewThreadsThread.interrupt();
				m_httpclient.getConnectionManager().shutdown();
				m_httpclient = new DefaultHttpClient();
			}
		});
		m_ProgressDialog.setCancelable(true);
	}
	
   @Override
protected void onPause() {
	super.onPause();
}
   
   @Override
protected void onResume() {
	super.onResume();
//    AdWhirlLayout adWhirlLayout = (AdWhirlLayout) findViewById(R.id.adwhirl_FrontPage);
//    adWhirlLayout.setAdWhirlInterface(this);
//   
//    AdWhirlLayout adWhirlLayout2 = (AdWhirlLayout) findViewById(R.id.adwhirl_BoardSelector);
//    adWhirlLayout2.setAdWhirlInterface(this);
	
	revmob = RevMob.start(this);
	
	RevMobBanner banner = revmob.createBanner(this);
	ViewGroup view = (ViewGroup) findViewById(R.id.frontpagebanner);
	view.addView(banner);
	
	RevMobBanner banner2 = revmob.createBanner(this);
	ViewGroup view2 = (ViewGroup) findViewById(R.id.boardselectorbanner);
	view2.addView(banner2);
	
}
   
@Override
protected void onStop() {
	super.onStop();
}
	
	private void buildReplyForm() {
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
	    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	    View layout = inflater.inflate(R.layout.replyform,null);
	    builder2.setView(layout)
	    		.setTitle("Create Thread")
	    		.setCancelable(false)
	    		.setPositiveButton("Post", new DialogInterface.OnClickListener() {	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ProgressDialog ballerDialog = ProgressDialog.show(ChanScan.this, "Posting...", "Uploading Post Data...");
						if(m_board.getBoardType().equals(BoardType.FOURCHAN) || m_board.getBoardType().equals(BoardType.NEWFOURCHAN) || m_board.getBoardType().equals(BoardType.NEWFOURCHANv2)){
							try{
								HttpClient client = new DefaultHttpClient();
								HttpPost post = new HttpPost(postingInfo.captchaTestURL);
								NameValuePair dashit = null;
								BasicNameValuePair toAdd = null;
								for(NameValuePair pair : captchaPairs){
									if(pair.getName().equals("recaptcha_response_field")){
										String name = pair.getName();
										dashit = pair;
										EditText text = (EditText)replyForm.findViewById(R.id.formEditCaptcha);
										toAdd = new BasicNameValuePair(name, text.getText().toString());
									}
								}
								captchaPairs.remove(dashit);
								captchaPairs.add(toAdd);
								UrlEncodedFormEntity ent = new UrlEncodedFormEntity(captchaPairs);
								post.setEntity(ent);
								HttpResponse responsePOST = client.execute(post);
								HttpEntity resEntity = responsePOST.getEntity();
								String captchaXML = null;
								if(resEntity != null){
									captchaXML = EntityUtils.toString(resEntity);
								}

								HtmlCleaner cleaner = new HtmlCleaner();
								CleanerProperties properties = cleaner.getProperties();
								properties.setAdvancedXmlEscape(false);//true);
								properties.setUseCdataForScriptAndStyle(true);
								properties.setAllowHtmlInsideAttributes(true);
								properties.setAllowMultiWordAttributes(true);
								properties.setRecognizeUnicodeChars(true);
								properties.setOmitComments(true);
								properties.setOmitDoctypeDeclaration(true);
								properties.setNamespacesAware(false);
								TagNode cleanerHead = cleaner.clean(captchaXML);
								
								SAXParserFactory spf = SAXParserFactory.newInstance();
								SAXParser sp = spf.newSAXParser();
								XMLReader xr = sp.getXMLReader();
								CaptchaHandler handler = new CaptchaHandler();
								xr.setContentHandler(handler);

								PrettyXmlSerializer serializer = new PrettyXmlSerializer(properties);
								InputSource source = new InputSource(new StringReader(serializer.getXmlAsString(cleanerHead)));
								
								//InputSource source = new InputSource(new StringReader(captchaXML));

								
								xr.parse(source);
								String captchaResponse = handler.getCaptchResponse();
								EditText enteredText;
								
								if(captchaResponse != null){
									List<NameValuePair> sendingPairs = new ArrayList<NameValuePair>();
									sendingPairs.add(new BasicNameValuePair("recaptcha_challenge_field", captchaResponse));
									for(NameValuePair pair: postingInfo.inputPairs){
										if(pair.getName()!= null && pair.getValue()!= null && pair.getValue()!=""){
											sendingPairs.add(new BasicNameValuePair(pair.getName(), pair.getValue()));
										}
										else if(pair.getName()!= null){
											if(pair.getName().equals("name")){
												enteredText = (EditText)replyForm.findViewById(R.id.formEditName);
												sendingPairs.add(new BasicNameValuePair(pair.getName(), enteredText.getText().toString()));
											}
											else if(pair.getName().equals("email")){
												enteredText = (EditText)replyForm.findViewById(R.id.formEditEmail);
												sendingPairs.add(new BasicNameValuePair(pair.getName(), enteredText.getText().toString()));
											}
											else if(pair.getName().equals("sub")){
												enteredText = (EditText)replyForm.findViewById(R.id.formEditSubject);
												sendingPairs.add(new BasicNameValuePair(pair.getName(), enteredText.getText().toString()));
											}
											else if(pair.getName().equals("com")){
												enteredText = (EditText)replyForm.findViewById(R.id.formEditComment);
												sendingPairs.add(new BasicNameValuePair(pair.getName(), enteredText.getText().toString()));
											}
											else if(pair.getName().equals("pwd")){
												enteredText = (EditText)replyForm.findViewById(R.id.formEditPassword);
												sendingPairs.add(new BasicNameValuePair(pair.getName(), enteredText.getText().toString()));
											}
										}
									}
									HttpClient client2 = new DefaultHttpClient();
									HttpPost post2 = new HttpPost(postingInfo.actionURL);
									MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
									

									if(uploadFile != null){
										FileBody testImgBody = new FileBody(uploadFile);
										mpEntity.addPart(postingInfo.fileRequestName, testImgBody);
									}
									for(NameValuePair pair : sendingPairs){
										mpEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
									}
									post2.setEntity(mpEntity);
									
									HttpResponse responsePOST2 = client2.execute(post2);
									responsePOST2.getEntity();
									ballerDialog.dismiss();
									
									runViewThreadsThread();								
								}
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						else if(m_board.getBoardType().equals(BoardType.FOURTWENTYCHAN)){
							try{
								EditText enteredText;
								List<NameValuePair> sendingPairs = new ArrayList<NameValuePair>();
								for(NameValuePair dair: postingInfo.inputPairs){
									if(dair.getName()!= null && dair.getValue()!= null && dair.getValue()!=""){
										if(dair.getName().equals("nofile")){
											if(uploadFile==null){
												sendingPairs.add(new BasicNameValuePair(dair.getName(), dair.getValue()));
											}
										}else if(dair.getName().equals("sage")){
												//sendingPairs.add(new BasicNameValuePair(dair.getName(), "off"));
										}else{
												sendingPairs.add(new BasicNameValuePair(dair.getName(), dair.getValue()));
										}
									}	
									else if(dair.getName()!= null){
										if(dair.getName().equals("name")){
											sendingPairs.add(new BasicNameValuePair(dair.getName(), ""));
										}
										else if(dair.getName().equals("link")){
											sendingPairs.add(new BasicNameValuePair(dair.getName(), ""));
										}
										else if(dair.getName().equals("field3")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditSubject);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("field4")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditComment);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("password")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditPassword);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
									}
									}
									
									HttpClient client2 = new DefaultHttpClient();
									HttpPost post2 = new HttpPost(postingInfo.actionURL);
									post2.getParams().setBooleanParameter("http.protocol.expect-continue", false);
									//UrlEncodedFormEntity ent2 = new UrlEncodedFormEntity(sendingPairs);
									MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

									if(uploadFile != null){
										FileBody testImgBody = new FileBody(uploadFile);
										mpEntity.addPart(postingInfo.fileRequestName, testImgBody);
									}
									for(NameValuePair pair : sendingPairs){
										mpEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
									}
									post2.setEntity(mpEntity);
									HttpResponse responsePOST2 = client2.execute(post2);
									responsePOST2.getEntity();
									ballerDialog.dismiss();
									runViewThreadsThread();
									
								
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						else if(m_board.getBoardType().equals(BoardType.SEVENCHAN)){
							try{
								EditText enteredText;
								List<NameValuePair> sendingPairs = new ArrayList<NameValuePair>();
								for(NameValuePair dair: postingInfo.inputPairs){
									if(dair.getName()!= null && dair.getValue()!= null && dair.getValue()!=""){
										sendingPairs.add(new BasicNameValuePair(dair.getName(), dair.getValue()));
									}	
									else if(dair.getName()!= null){
										if(dair.getName().equals("name")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditName);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("em")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditEmail);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("subject")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditSubject);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("message")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditComment);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("password")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditPassword);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("embed")){
											sendingPairs.add(new BasicNameValuePair(dair.getName(), ""));
										}
										else if(dair.getName().equals("embedtype")){
											sendingPairs.add(new BasicNameValuePair(dair.getName(), "google"));
										}
									}
									}
									
									HttpClient client2 = new DefaultHttpClient();
									HttpPost post2 = new HttpPost(postingInfo.actionURL);
									post2.getParams().setBooleanParameter("http.protocol.expect-continue", false);
									MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
									if(uploadFile != null){
										FileBody testImgBody = new FileBody(uploadFile);
										mpEntity.addPart(postingInfo.fileRequestName, testImgBody);
									}
									for(NameValuePair pair : sendingPairs){
										mpEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
									}
									post2.setEntity(mpEntity);
									HttpResponse responsePOST2 = client2.execute(post2);
									responsePOST2.getEntity();
									ballerDialog.dismiss();
									runViewThreadsThread();											
									
								
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						else if(m_board.getBoardType().equals(BoardType.TWOCHAN)){
							try{
								EditText enteredText;
								List<NameValuePair> sendingPairs = new ArrayList<NameValuePair>();
								for(NameValuePair dair: postingInfo.inputPairs){
									if(dair.getName()!= null && dair.getValue()!= null && dair.getValue()!=""){
										if(dair.getName().equals("textonly")){
											if(uploadFile == null){
												sendingPairs.add(new BasicNameValuePair(dair.getName(), "on"));
											}else{
												sendingPairs.add(new BasicNameValuePair(dair.getName(), "off"));
											}
										}
										sendingPairs.add(new BasicNameValuePair(dair.getName(), dair.getValue()));
									}	
									else if(dair.getName()!= null){
										if(dair.getName().equals("name")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditName);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("email")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditEmail);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("sub")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditSubject);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("com")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditComment);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("pwd")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditPassword);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("scsz") || dair.getName().equals("flvr") || dair.getName().equals("pthd")){
											sendingPairs.add(new BasicNameValuePair(dair.getName(), ""));
										}

									}
									}
									
									HttpClient client2 = new DefaultHttpClient();
									HttpPost post2 = new HttpPost(postingInfo.actionURL);
									post2.getParams().setBooleanParameter("http.protocol.expect-continue", false);
									MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
									if(uploadFile != null){
										FileBody testImgBody = new FileBody(uploadFile);
										mpEntity.addPart(postingInfo.fileRequestName, testImgBody);
									}
									for(NameValuePair pair : sendingPairs){
										mpEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
									}
									post2.setEntity(mpEntity);
									
									HttpResponse responsePOST2 = client2.execute(post2);
									responsePOST2.getEntity();
									ballerDialog.dismiss();
									runViewThreadsThread();
								
								
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						else if(m_board.getBoardType().equals(BoardType.WAKABA)){
							try{
								EditText enteredText;
								List<NameValuePair> sendingPairs = new ArrayList<NameValuePair>();
								for(NameValuePair dair: postingInfo.inputPairs){
									if(dair.getName()!= null && dair.getValue()!= null && dair.getValue()!=""){
										if(dair.getName().equals("nofile")){
											if(uploadFile == null){
												sendingPairs.add(new BasicNameValuePair(dair.getName(), "on"));
											}else{
												sendingPairs.add(new BasicNameValuePair(dair.getName(), "off"));
											}
										}
										sendingPairs.add(new BasicNameValuePair(dair.getName(), dair.getValue()));
									}	
									else if(dair.getName()!= null){
										if(dair.getName().equals("name") || dair.getName().equals("link") || dair.getName().equals("field2")){
											sendingPairs.add(new BasicNameValuePair(dair.getName(), ""));
										}
										if(dair.getName().equals("field1")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditName);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("field2")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditEmail);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("field3")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditSubject);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("field4")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditComment);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("password")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditPassword);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}

									}
									}
									
									HttpClient client2 = new DefaultHttpClient();
									HttpPost post2 = new HttpPost(postingInfo.actionURL);
									post2.getParams().setBooleanParameter("http.protocol.expect-continue", false);
									MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

									if(uploadFile != null){
										FileBody testImgBody = new FileBody(uploadFile);
										mpEntity.addPart(postingInfo.fileRequestName, testImgBody);
									}
									for(NameValuePair pair : sendingPairs){
										mpEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
									}
									post2.setEntity(mpEntity);
				
									HttpResponse responsePOST2 = client2.execute(post2);
									responsePOST2.getEntity();
									ballerDialog.dismiss();
									runViewThreadsThread();
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						else if(m_board.getBoardType().equals(BoardType.KUSABAX)){
							try{
								EditText enteredText;
								List<NameValuePair> sendingPairs = new ArrayList<NameValuePair>();
								for(NameValuePair dair: postingInfo.inputPairs){
									if(dair.getName()!= null && dair.getValue()!= null && dair.getValue()!=""){
										sendingPairs.add(new BasicNameValuePair(dair.getName(), dair.getValue()));
									}	
									else if(dair.getName()!= null){
										if(dair.getName().equals("name")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditName);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("em")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditEmail);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("subject")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditSubject);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("message")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditComment);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("postpassword")){
											enteredText = (EditText)replyForm.findViewById(R.id.formEditPassword);
											sendingPairs.add(new BasicNameValuePair(dair.getName(), enteredText.getText().toString()));
										}
										else if(dair.getName().equals("email")){
											sendingPairs.add(new BasicNameValuePair(dair.getName(), ""));
										}
										else if(dair.getName().equals("embedtype")){
											sendingPairs.add(new BasicNameValuePair(dair.getName(), "google"));
										}
									}
									}
									
									HttpClient client2 = new DefaultHttpClient();
									HttpPost post2 = new HttpPost(postingInfo.actionURL);
									post2.getParams().setBooleanParameter("http.protocol.expect-continue", false);
									MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
									
									if(uploadFile != null){
										FileBody testImgBody = new FileBody(uploadFile);
										mpEntity.addPart(postingInfo.fileRequestName, testImgBody);
									}
									for(NameValuePair pair : sendingPairs){
										mpEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
									}
									post2.setEntity(mpEntity);
				
									HttpResponse responsePOST2 = client2.execute(post2);
									responsePOST2.getEntity();
									ballerDialog.dismiss();
									runViewThreadsThread();											
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						ballerDialog.dismiss();
					}
				})
				.setNeutralButton("Select File", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(ChanScan.this, FileBrowser.class);
						startActivityForResult(intent, REQUEST_CODE);						
					}
				})	    		
	    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();						
					}
				});
	    replyForm = builder2.create();
	    replyForm.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				//System.gc();
				try{
					HtmlCleaner cleaner = new HtmlCleaner();
					CleanerProperties properties = cleaner.getProperties();
					properties.setAdvancedXmlEscape(false);//true);
					properties.setUseCdataForScriptAndStyle(true);
					properties.setAllowHtmlInsideAttributes(true);
					properties.setAllowMultiWordAttributes(true);
					properties.setRecognizeUnicodeChars(true);
					properties.setOmitComments(true);
					properties.setOmitDoctypeDeclaration(true);
					properties.setNamespacesAware(false);
					TagNode cleanerHead = cleaner.clean(new URL(postingInfo.captchaTestURL));
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser sp = spf.newSAXParser();
					XMLReader xr = sp.getXMLReader();
					CaptchaHandler handler = new CaptchaHandler();
					xr.setContentHandler(handler);
					PrettyXmlSerializer serializer = new PrettyXmlSerializer(properties);
					InputSource source = new InputSource(new StringReader(serializer.getXmlAsString(cleanerHead)));
					xr.parse(source);
					ImageView view = (ImageView)replyForm.findViewById(R.id.formCaptchaImage);
					view.setImageBitmap(m_imageLoader.GetBitmap(handler.getCaptchaImageURL(), 1000));
					captchaPairs = handler.getInputPairs();
				}catch(Exception e){
					
				}
				
			}
		});
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
    	if(intent!=null){
    	Bundle extras = intent.getExtras();
    	if(extras != null){
    	String fPath = extras.getString("filePath");
    	uploadFile = new File(fPath);
    	if(!uploadFile.exists() || !uploadFile.isFile()){
    		uploadFile = null;
    		Toast.makeText(ChanScan.this, "No File Attached", Toast.LENGTH_SHORT).show();
    	}else if(uploadFile.getName() != null){
    		Toast.makeText(ChanScan.this, "Attached: "+uploadFile.getName(), Toast.LENGTH_SHORT).show();
    	}
    	replyForm.show();
    	}
    	else{
    		Toast.makeText(ChanScan.this, "No File Attached", Toast.LENGTH_SHORT).show();
    	}
    	}
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean(getString(R.string.clearCache), false)){
			clearCache();
		}
		m_chanadapter.imageLoader.stopThread();
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