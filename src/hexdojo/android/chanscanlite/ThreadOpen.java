package hexdojo.android.chanscanlite;
import hexdojo.android.chanscanlite.ChanScan.BoardType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
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
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.jsoup.Jsoup;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.ccil.cowan.tagsoup.*;

import com.revmob.RevMob;
import com.revmob.ads.banner.RevMobBanner;


import android.app.AlertDialog;
//import android.app.Dialog;
import android.app.ListActivity;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
//import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
//import android.os.Handler;
import android.preference.PreferenceManager;
//import android.provider.Settings;
//import android.util.Log;
import android.text.ClipboardManager;
//import android.util.DisplayMetrics;
import android.view.ContextMenu;
//import android.view.Gravity;
//import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
//import android.widget.RemoteViews;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ThreadOpen extends ListActivity 
{
	private static final int MENU_ID_REFRESH = 0;
	private static final int MENU_ID_DLIMAGES = 1;
	private static final int MENU_ID_POSTREPLY = 2;
	private static final int MENU_ID_FULLVIEW = 3;
	private static final int MENU_ID_GALLERY = 4;
	private static final int MENU_ID_BUYIT = 5;
	private static final int MENU_ID_COPY = 6;
	private static final int MENU_ID_QUOTE = 7;
	private static final int REQUEST_CODE = 3117;
	private ProgressDialog m_ProgressDialog = null;
	private String threadURL;
	private ThreadAdapter m_threadAdapter;
	private ThreadListHandler m_threadHandler;
	private Runnable loadThread;
	private ArrayList<ThreadItem> m_comments;
	private BoardType m_boardType;
	private ListView threadListView = null;
	private ImageLoader m_imageLoader = null;
	
	private volatile Thread loadit;
	
	private AlertDialog ask;
	private AlertDialog askSingle;
	private AlertDialog replyForm;
	private File uploadFile = null;
	
	private String chanName;
	private String boardName;
	
	Thread imageDLThread;
	
	private PostingInfo postingInfo;
	private List<NameValuePair> captchaPairs;
	
	

	@Override
	public void onBackPressed() {
		if(!loadit.isInterrupted()){
			loadit.interrupt();
			
		}
		//System.gc();
		
		super.onBackPressed();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		try{
		//Log.e("THREADOPEN","LAUNCHED ACTIVITY");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread);
	    buildImageDwonloadForm();
	    
	    buildReplyForm();
		
		
	    
	    
		

	    	    
		m_imageLoader = new ImageLoader(ThreadOpen.this);
		m_imageLoader.SetMaxImageSize(160);
		m_imageLoader.setStubId(R.drawable.blackdot);
		Bundle b = this.getIntent().getExtras();
		m_comments = new ArrayList<ThreadItem>();
		m_threadAdapter = new ThreadAdapter(ThreadOpen.this, R.layout.thread, m_comments, m_imageLoader);
		threadURL = b.getString("ThreadURL");
		chanName = b.getString("ChanName");
		boardName = b.getString("BoardName");
		
		setTitle("ChanScan Lite - "+chanName+" - "+boardName);
		
		
		m_boardType = BoardType.values()[b.getInt("BoardType")];
		//Log.e("THREADOPEN","Activity Received: "+threadURL);
		//Log.e("THREADOPEN","Activity Received Type: "+m_boardType.toString());
		
		setListAdapter(m_threadAdapter);
		threadListView = getListView();
		threadListView.setVisibility(View.GONE);
		
		threadListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				AdapterContextMenuInfo minfo = (AdapterContextMenuInfo) menuInfo;
				ThreadItem item = (ThreadItem) threadListView.getAdapter().getItem(minfo.position);
				menu.add(Menu.NONE, MENU_ID_COPY, 0, "Copy Text");
				menu.add(Menu.NONE, MENU_ID_QUOTE, 1, "Quote and Reply");
				if(item.getImageURL().contains("http")){
					menu.setHeaderTitle("Image Options");
					menu.add(Menu.NONE, MENU_ID_FULLVIEW, 2, "Open in Image Viewer");
					menu.add(Menu.NONE, MENU_ID_DLIMAGES, 3, "Download Image");
					menu.add(Menu.NONE, MENU_ID_GALLERY, 4, "Open Image Gallery");
				}
			}
		});
			
		loadThread = new Runnable() {
			@Override
			public void run() {
				downloadThread(threadURL, m_boardType);
			}
		};
		
		
		
		loaditRun();
	    buildImageDwonloadForm();  
	    buildReplyForm();
		
		}catch(Exception e){
			if(e.getMessage()!=null){
			//	Log.e("THREADOPEN",e.getMessage());
			}
		}
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem troll) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) troll.getMenuInfo();
		ThreadItem item = (ThreadItem) threadListView.getAdapter().getItem(menuInfo.position);
		switch(troll.getItemId()){
		case MENU_ID_COPY:
			ClipboardManager ClipMan = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipMan.setText(item.getPostText());
			break;
		case MENU_ID_QUOTE:
			String quote = ">>" + item.getCommentID() + "\n";
			//if(replyForm == null){
			buildReplyForm();
			//replyForm.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
			replyForm.show();
			
			//try{
				//throw new Exception();
			//	buildReplyForm();
				//throw new Exception();
			//	replyForm.show();
			//}catch(Exception e){
			//	buildReplyForm();
			//	replyForm.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
			//	replyForm.show();
			//}
			//}
			//replyForm.getWindow().requestFeature()
			//replyForm.show();
			EditText t = (EditText) replyForm.findViewById(R.id.formEditComment);
			t.setText(quote);
			break;
		case MENU_ID_FULLVIEW:
			
			if(item.getImageURL().contains("gif")){
				Intent i = null;
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String gifType = prefs.getString(getApplicationContext().getString(R.string.gifViewerType), "nogif");
				//Log.e("GifType",gifType);
				
				if(gifType.contentEquals("defaultGif"))
				{
					i = new Intent(ThreadOpen.this, GifViewer.class);
				}
				else if(gifType.contentEquals("webGif")){
					i = new Intent(ThreadOpen.this, GifViewerWeb.class);
				}
				else{
					i = new Intent(ThreadOpen.this, ImageGallery.class);
				}
				
				if(item != null && i != null && item.getImageURL() != null){
					Bundle b = new Bundle();
					b.putString("imageURL", item.getImageURL());
					i.putExtras(b);
					startActivity(i);		
				}
			}
			else if(item.getImageURL().contains("jpeg")||item.getImageURL().contains("jpg")||item.getImageURL().contains("png")||item.getImageURL().contains("bmp")){
				Intent i = new Intent(ThreadOpen.this, ImageGallery.class);
				Bundle b = new Bundle();
				b.putString("imageURL", item.getImageURL());
				i.putExtras(b);
				startActivity(i);
			}
			else{
				Toast.makeText(ThreadOpen.this, "Could Not Find Full Image", Toast.LENGTH_SHORT);
			}
			break;
		case MENU_ID_DLIMAGES:
			buildSingleImageDLForm(item);
        	askSingle.show();
			break;
		case MENU_ID_GALLERY:
			openGallery();
			break;
		}
		return true;
	}

	private void buildImageDwonloadForm() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	    View layout = inflater.inflate(R.layout.imagedlform,null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Download All Images?")
	    	   .setView(layout)
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   //ThreadOpen.this.finish();
	            	   
	            	   doImageDL();
	            	   //mNotificationManager.notify(BALLER_ID, notification);
	            	   //imageDLThread = new Thread(null, downloadImages, "MagentoBackground");
	            	   //imageDLThread.setPriority(Thread.MIN_PRIORITY);	            
	            	   //imageDLThread.start();
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	               }
	           });
	    ask = builder.create();
	} 
	
	private void buildSingleImageDLForm(final ThreadItem item) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	    View layout = inflater.inflate(R.layout.imagedlform,null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Download Image?")
	    	   .setView(layout)
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
          	   
	            	   //mNotificationManager.notify(BALLER_ID, notification);
	            	  // imageDLThread = new Thread(null, downloadImages, "MagentoBackground");
	            	   //imageDLThread.start();
	            	   //DownloadImageSingle(imageURL);
	            	   doImageDLSingle(item);
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	               }
	           });
	    askSingle = builder.create();
	} 

	private void buildReplyForm() {
		AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
	    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	    View layout = inflater.inflate(R.layout.replyform,null);
	    builder2.setTitle("Post Reply")
	    		.setCancelable(false)
	    		.setPositiveButton("Post", new DialogInterface.OnClickListener() {	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(m_boardType.equals(BoardType.FOURCHAN)||m_boardType.equals(BoardType.NEWFOURCHAN)||m_boardType.equals(BoardType.NEWFOURCHANv2)){
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
									//UrlEncodedFormEntity ent2 = new UrlEncodedFormEntity(sendingPairs);
									MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
									
									//String path = Environment.getExternalStorageDirectory().getPath();
									//path = path.concat("/Image Boards/4chan/b/1291867293129.jpg");
									//File testIMG = new File(path);
									if(uploadFile != null){
										FileBody testImgBody = new FileBody(uploadFile);
										mpEntity.addPart(postingInfo.fileRequestName, testImgBody);
									}
									for(NameValuePair pair : sendingPairs){
										mpEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
									}
									post2.setEntity(mpEntity);
									HttpResponse responsePOST2 = client2.execute(post2);
									HttpEntity resEntity2 = responsePOST2.getEntity();
									if(resEntity2 != null){
										String respo = EntityUtils.toString(resEntity2);
										//Log.i("PostResponse",respo);
										if(respo.contains("refresh")){
											Toast.makeText(ThreadOpen.this, "Refreshing Board...", Toast.LENGTH_SHORT);
											loadit= new Thread(null, loadThread, "MagentoBackground");
											loadit.start();
											
										}
										else{
											Toast.makeText(ThreadOpen.this, "Error in Making Post" , Toast.LENGTH_SHORT).show();
										}
									}									
								}
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						else if(m_boardType.equals(BoardType.FOURTWENTYCHAN)){
							//Log.e("Post Reply", "Got Board Type " + m_boardType.toString());
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
									HttpEntity resEntity2 = responsePOST2.getEntity();
									//ballerDialog.dismiss();
									if(resEntity2 != null){
										String respo = EntityUtils.toString(resEntity2);
										//Log.i("PostResponse",respo);
										if(respo.contains("refresh")){
											//m_comments.clear();
											//loaditRun();
											Toast.makeText(ThreadOpen.this, "Refreshing Board...", Toast.LENGTH_SHORT);
											loadit= new Thread(null, loadThread, "MagentoBackground");
											loadit.start();
										}
										else{
											Toast.makeText(ThreadOpen.this, "Error in Making Post" , Toast.LENGTH_SHORT).show();
										}
									}									
								
							}catch(Exception e){
								e.printStackTrace();
							}
						
						}else if(m_boardType.equals(BoardType.SEVENCHAN)){
							//Log.e("Post Reply", "Got Board Type " + m_boardType.toString());
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
									//UrlEncodedFormEntity ent2 = new UrlEncodedFormEntity(sendingPairs);
									MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
									
									//String path = Environment.getExternalStorageDirectory().getPath();
									//path = path.concat("/Image Boards/4chan/b/1291867293129.jpg");
									//File testIMG = new File(path);
									if(uploadFile != null){
										FileBody testImgBody = new FileBody(uploadFile);
										mpEntity.addPart(postingInfo.fileRequestName, testImgBody);
									}
									for(NameValuePair pair : sendingPairs){
										mpEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
									}
									post2.setEntity(mpEntity);
								
									client2.execute(post2);
									Toast.makeText(ThreadOpen.this, "Refreshing Board...", Toast.LENGTH_SHORT);
									loadit= new Thread(null, loadThread, "MagentoBackground");
									loadit.start();
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						else if(m_boardType.equals(BoardType.WAKABA)){
							///Log.e("Post Reply", "Got Board Type " + m_boardType.toString());
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
									HttpEntity resEntity2 = responsePOST2.getEntity();
									//ballerDialog.dismiss();
									if(resEntity2 != null){
										String respo = EntityUtils.toString(resEntity2);
										//Log.i("PostResponse",respo);
										if(!respo.contains("Error")){
											//m_comments.clear();
											//loaditRun();
											Toast.makeText(ThreadOpen.this, "Refreshing Board...", Toast.LENGTH_SHORT);
											loadit= new Thread(null, loadThread, "MagentoBackground");
											loadit.start();
											
											
										}
										else{
											Toast.makeText(ThreadOpen.this, "Error in Making Post" , Toast.LENGTH_SHORT).show();
										}
									}									
								
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						else if(m_boardType.equals(BoardType.KUSABAX)){
							//Log.e("Post Reply", "Got Board Type " + m_boardType.toString());
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
									//UrlEncodedFormEntity ent2 = new UrlEncodedFormEntity(sendingPairs);
									MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
									
									//String path = Environment.getExternalStorageDirectory().getPath();
									//path = path.concat("/Image Boards/4chan/b/1291867293129.jpg");
									//File testIMG = new File(path);
									if(uploadFile != null){
										FileBody testImgBody = new FileBody(uploadFile);
										mpEntity.addPart(postingInfo.fileRequestName, testImgBody);
									}
									for(NameValuePair pair : sendingPairs){
										mpEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
									}
									post2.setEntity(mpEntity);
				
									HttpResponse responsePOST2 = client2.execute(post2);
									HttpEntity resEntity2 = responsePOST2.getEntity();
									//ballerDialog.dismiss();
									if(resEntity2 != null){
										String respo = EntityUtils.toString(resEntity2);
										//Log.i("PostResponse",respo);
										if(respo.contains("kusaba")){
											//m_comments.clear();
											//loaditRun();
											Toast.makeText(ThreadOpen.this, "Refreshing Board...", Toast.LENGTH_SHORT);
											loadit= new Thread(null, loadThread, "MagentoBackground");
											loadit.start();
											
										}
										else{
											Toast.makeText(ThreadOpen.this, "Error in Making Post" , Toast.LENGTH_SHORT).show();
										}
									}									
								
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						
					}
				})
				.setNeutralButton("Select File", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(ThreadOpen.this, FileBrowser.class);
						startActivityForResult(intent, REQUEST_CODE);						
					}
				})	    		
	    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();						
					}
				})
				.setView(layout);
	    replyForm = builder2.create();
	    //replyForm = builder2.show();
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

	private void loaditRun() {
		//if(m_comments!=null){
		//	m_comments.clear();
		//}

		m_ProgressDialog = ProgressDialog.show(ThreadOpen.this, "Updating...", "Downloading Thread");
		//if(!showedAd && sdk.isAdReady()){
		//	showedAd = sdk.displayAd(this);
		//}
		m_ProgressDialog.setCancelable(true);
		m_ProgressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				loadit.interrupt();
				Toast.makeText(ThreadOpen.this, "Cancelled Thread Loading", Toast.LENGTH_LONG).show();
				if(m_comments== null || m_comments.size()<1){
					//System.gc();
					finish();
				}
			}
		});
		loadit= new Thread(null, loadThread, "MagentoBackground");
		loadit.start();
		
	}
	
	public void downloadThread(String threadurl, BoardType bType){
		//Log.e("THREADOPEN","Entered Download thread Activity");
		//Log.e("THREADOPEN","Loader Received: "+threadurl);
		try{
			if(replyForm.findViewById(R.id.formEditSubject)!=null){
				((EditText)replyForm.findViewById(R.id.formEditSubject)).setText("");
				((EditText)replyForm.findViewById(R.id.formEditComment)).setText("");
			}
		}catch(Exception e){}
		try {
			if(!loadit.isInterrupted()){
				m_imageLoader.clearMemCache();
				postingInfo = new PostingInfo();
				if(!loadit.isInterrupted()){
					ArrayList<ThreadItem> tempComments = new ArrayList<ThreadItem>();
					if(bType.equals(BoardType.NEWFOURCHANv2)){
						StringBuilder jsonBuilder = new StringBuilder(2048);
						HttpClient client = new DefaultHttpClient();
						HttpGet get = new HttpGet(threadurl);
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
							JSONArray posts = jObj.getJSONArray("posts");
							for(int i = 0; i < posts.length(); i++){
								JSONObject post = posts.getJSONObject(i);
								ThreadItem tempItem = new ThreadItem();
								try{
									tempItem.setCommentID(""+post.getInt("no"));
								}catch(Exception e){}
								try{
									tempItem.setDate(post.getString("now"));
								}catch(Exception e){}
								try{
									tempItem.setPostername(post.getString("name"));
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
											tempItem.setPostText(postText);
										}
										catch(Exception e){}
									}
								}catch(Exception e){} 
								try{
									String currentBoard = threadurl.split("/")[3];
									//tempItem.setImageURL("http://i.4cdn.org/"+currentBoard+"/src/"+post.getLong("tim")+post.getString("ext"));
									tempItem.setImageURL("http://i.4cdn.org/"+currentBoard+"/"+post.getLong("tim")+post.getString("ext"));
								}catch(Exception e){}
								try{
									String currentBoard = threadurl.split("/")[3];
									//tempItem.setThumbURL(("http://t.4cdn.org/"+currentBoard+"/thumb/"+post.getLong("tim")+"s.jpg"));
									tempItem.setThumbURL(("http://t.4cdn.org/"+currentBoard+"/"+post.getLong("tim")+"s.jpg"));
								}catch(Exception e){}
								
								tempComments.add(tempItem);
								m_comments.clear();
								for(ThreadItem item : tempComments){
									m_comments.add(item);
								}
								ThreadItem ad =new ThreadItem();
								ad.isAd=true;
								m_comments.add(ad);
								//m_comments = m_threadHandler.getM_comments();
				//				Log.e("ThreadOpenWUT", postingInfo.actionURL);
				//				Log.i("NumComments", "" + m_comments.size());
							}
						}
						else{
								throw new Exception("Couldn't DL ThreadList");
						}
					}
					else{
						
						//Log.e("Dling","hrmm");
						//m_threadHandler = new ThreadListHandler(m_comments, bType, postingInfo, threadurl);
						m_threadHandler = new ThreadListHandler(tempComments, bType, postingInfo, threadurl);
						if(!loadit.isInterrupted()){
							Parser sp = new Parser();
							sp.setContentHandler(m_threadHandler);
							sp.setFeature(Parser.namespacesFeature, true);
							sp.setFeature(Parser.CDATAElementsFeature, true);
							sp.setFeature(Parser.ignorableWhitespaceFeature, false);
							sp.setFeature(Parser.namespacePrefixesFeature, false);
							sp.setFeature(Parser.validationFeature, true);
							if(!loadit.isInterrupted()){
								if(m_boardType.equals(BoardType.TWOCHAN)){
									InputSource src = new InputSource(new InputStreamReader(new URL(threadurl).openStream(),"SJIS"));
									sp.parse(src);
								}
								else{
									//Log.e("ThreadOpen","parsing");
									sp.parse(new InputSource(new URL(threadurl).openStream()));
								}
								if(!loadit.isInterrupted()){
									tempComments = m_threadHandler.getM_comments();
									m_comments.clear();
									for(ThreadItem item : tempComments){
										m_comments.add(item);
									}
								ThreadItem ad =new ThreadItem();
								ad.isAd=true;
								m_comments.add(ad);
									//m_comments = m_threadHandler.getM_comments();
									postingInfo = m_threadHandler.getPostingInfo();
									//Log.e("ThreadOpenWUT", postingInfo.actionURL);
									//Log.i("NumComments", "" + m_comments.size());
								}
							}
							
						}
					}
				}
			}
		} catch (Exception e) {
			finish(); 
		}
	
		runOnUiThread(returnThreadDL);
		
	}
	
	private Runnable returnThreadDL = new Runnable() {
		@Override
		public void run() {
			if (m_comments != null && m_comments.size() > 0) {
				m_threadAdapter.notifyDataSetChanged();
			}
			else{
				Toast.makeText(ThreadOpen.this, "Error Updating Thread. (Possibly out of memory)" , Toast.LENGTH_LONG);
			}
			m_ProgressDialog.dismiss();
			//m_threadAdapter.notifyDataSetInvalidated();
			m_threadAdapter.notifyDataSetChanged();
			threadListView.setVisibility(View.VISIBLE);
			getListView().invalidateViews();
		}
	};
	
	private void doImageDL(){
		//Log.e("Threadopen","Hit doImageDL");
		if(m_comments != null){
			ArrayList<String> imageURLs = new ArrayList<String>();
			ArrayList<String> paths = new ArrayList<String>();
			for(ThreadItem item : m_comments){
				if(item!=null && item.getImageURL()!=null && item.getImageURL()!=""){
					imageURLs.add(item.getImageURL());
					
					EditText et =  (EditText)ask.findViewById(R.id.imageDlFormEditText);
					String tag = et.getEditableText().toString();
					if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
						{
						Toast.makeText(this, "No sdcard Mounted.", Toast.LENGTH_LONG);
						return;
						}
					String path = Environment.getExternalStorageDirectory().getPath();
					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					path = path.concat("/"+prefs.getString(getString(R.string.customrootforsave), getString(R.string.defaultrootforsave))+"/"+chanName+"/"+boardName+"/");
					if(tag.length()>0){
						path = path.concat(tag+"/");
					}
					
					paths.add(path);
					
					
				}
			}
			
			Intent dlthem = new Intent(this, ImageDownloader.class);
			dlthem.addFlags(Service.START_NOT_STICKY);
			Bundle extras = new Bundle();
			//extras.putInt("NUMIMAGES", imageURLs.size());
			//imageURLs.trimToSize();
			//paths.trimToSize();
			//String[] iUrls = new String[imageURLs.size()];
			//String[] iPaths = new String[imageURLs.size()];
			//for(int i = 0; i< imageURLs.size(); i++){
			//	iUrls[i]=imageURLs.get(i);
			//	iPaths[i]=paths.get(i);
			//}
			
			extras.putInt("NUMIMAGES", Math.min(5, imageURLs.size()));
			imageURLs.trimToSize();
			paths.trimToSize();
			String[] iUrls = new String[Math.min(5, imageURLs.size())];
			String[] iPaths = new String[Math.min(5, imageURLs.size())];
			for(int i = 0; i< Math.min(5, imageURLs.size()); i++){
				iUrls[i]=imageURLs.get(i);
				iPaths[i]=paths.get(i);
			}
			
			extras.putStringArray("IMAGEURLS", iUrls);
			extras.putStringArray("IMAGEPATHS", iPaths);
			dlthem.putExtras(extras);
			startService(dlthem);
			 new AlertDialog.Builder(ThreadOpen.this)
				.setMessage("The free version only allows mass downloading of 5 images. The paid version has no limit.")
				.setPositiveButton("Buy ChanScan", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
		                        "http://market.android.com/details?id=hexdojo.android.chanscan"));
		                startActivity(marketIntent);
		                finish();
					}
				})
				.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//finish();
					}
				})
				.setTitle("Free Version")
				.setIcon(R.drawable.chanscaniconsmall)
				.create().show();
		}
		
	}
	
	private void doImageDLSingle(ThreadItem item){
		//Log.e("Threadopen","Hit doImageDL");
		if(item != null){
			ArrayList<String> imageURLs = new ArrayList<String>();
			ArrayList<String> paths = new ArrayList<String>();
				if(item!=null && item.getImageURL()!=null && item.getImageURL()!=""){
					imageURLs.add(item.getImageURL());
					EditText et =  (EditText)askSingle.findViewById(R.id.imageDlFormEditText);
					String tag = et.getEditableText().toString();
					if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
						{
						Toast.makeText(this, "No sdcard Mounted.", Toast.LENGTH_LONG);
						return;
						}
					String path = Environment.getExternalStorageDirectory().getPath();
					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					path = path.concat("/"+prefs.getString(getString(R.string.customrootforsave), getString(R.string.defaultrootforsave))+"/"+chanName+"/"+boardName+"/");
					if(tag.length()>0){
						path = path.concat(tag+"/");
					}
					
					paths.add(path);
					
					
				}
			
			Intent dlthem = new Intent(this, ImageDownloader.class);
			dlthem.addFlags(Service.START_NOT_STICKY);
			Bundle extras = new Bundle();
			//extras.putInt("NUMIMAGES", imageURLs.size());
			//imageURLs.trimToSize();
			//paths.trimToSize();
			//String[] iUrls = new String[imageURLs.size()];
			//String[] iPaths = new String[imageURLs.size()];
			//for(int i = 0; i< imageURLs.size(); i++){
			//	iUrls[i]=imageURLs.get(i);
			//	iPaths[i]=paths.get(i);
			//}
			
			extras.putInt("NUMIMAGES", Math.min(5, imageURLs.size()));
			imageURLs.trimToSize();
			paths.trimToSize();
			String[] iUrls = new String[Math.min(5, imageURLs.size())];
			String[] iPaths = new String[Math.min(5, imageURLs.size())];
			for(int i = 0; i< Math.min(5, imageURLs.size()); i++){
				iUrls[i]=imageURLs.get(i);
				iPaths[i]=paths.get(i);
			}
			
			extras.putStringArray("IMAGEURLS", iUrls);
			extras.putStringArray("IMAGEPATHS", iPaths);
			dlthem.putExtras(extras);
			startService(dlthem);
		}
		
	}
	
	
	public void DownloadImageSingle(String url){
		try {
			//Log.e("Download ADDRESS",url);
			URL imageURL = new URL(url);
			EditText et =  (EditText)askSingle.findViewById(R.id.imageDlFormEditText);
			String tag = et.getEditableText().toString();
			String path = Environment.getExternalStorageDirectory().getPath();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			path = path.concat("/"+prefs.getString(getString(R.string.customrootforsave), getString(R.string.defaultrootforsave))+"/"+chanName+"/"+boardName+"/");
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
			MediaScannerConnection scanner = new MediaScannerConnection(this, null);
			scanner.connect();
			scanner.scanFile(download.getAbsolutePath(), null);
			scanner.disconnect();
			Toast.makeText(ThreadOpen.this, "Image Downloaded", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(ThreadOpen.this, "Could not download image.", Toast.LENGTH_SHORT).show();
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_REFRESH, 0, R.string.menu_refresh).setIcon(R.drawable.icmenurefresh_1);
        menu.add(Menu.NONE, MENU_ID_DLIMAGES, 1, R.string.menu_dlimages).setIcon(R.drawable.icmenuarchive_1);
        menu.add(Menu.NONE, MENU_ID_POSTREPLY, 2, R.string.menu_reply).setIcon(R.drawable.icmenucompose_1);
        menu.add(Menu.NONE, MENU_ID_GALLERY, 4, R.string.menu_gallery).setIcon(R.drawable.icmenuslideshow_1);
        menu.add(Menu.NONE, MENU_ID_BUYIT, 5, "Buy Full Version").setIcon(R.drawable.shopping_36);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_REFRESH:
            	//System.gc();
        		loadit = new Thread(null, loadThread, "MagentoBackground");
        		loadit.start();
        		Toast.makeText(ThreadOpen.this, "Updating Thread in Background", Toast.LENGTH_LONG).show();
                break;
            case MENU_ID_DLIMAGES:
            	//System.gc();
            	buildImageDwonloadForm();
            	ask.show();
            	break;
            case MENU_ID_POSTREPLY:
            	uploadFile = null;
            	replyForm.show();
            	break;
            case MENU_ID_GALLERY:
            	//System.gc();
            	openGallery();
            	break;
            case MENU_ID_BUYIT:
            	Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "http://market.android.com/details?id=hexdojo.android.chanscan"));
            	startActivity(marketIntent);
            	finish();
            	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
    	super.onActivityResult(requestCode, resultCode, intent);
    	if(intent != null){
    		Bundle extras = intent.getExtras();
    	
	    	if(extras != null){
		    	String fPath = extras.getString("filePath");
		    	uploadFile = new File(fPath);
		    	if(!uploadFile.exists() || !uploadFile.isFile()){
		    		uploadFile = null;
		    		Toast.makeText(ThreadOpen.this, "No File Attached", Toast.LENGTH_SHORT).show();
		    	}else if(uploadFile.getName() != null){
		    		Toast.makeText(ThreadOpen.this, "Attached: "+uploadFile.getName(), Toast.LENGTH_SHORT).show();
		    	}
		    	
		    	replyForm.show();
	    	}
	    	else{
	    		Toast.makeText(ThreadOpen.this, "No File Attached", Toast.LENGTH_SHORT).show();
	    	}
    	}
    }

	private void openGallery() {
		m_imageLoader.clearMemCache();
		//Intent i = new Intent(ThreadOpen.this, ImageGallery.class);
		//startActivity(i);
		Intent i = new Intent(ThreadOpen.this, GalleryPreview.class);
		Bundle b = new Bundle();
		ArrayList<String> temp = new ArrayList<String>();
		ThreadItem titem;
		for(int foo = 0; foo < m_comments.size(); foo++){
			titem = m_comments.get(foo);
			temp.add(titem.getThumbURL()+"<<>>"+titem.getImageURL());
			//if(!(titem.getImageURL().contains("jpg")||titem.getImageURL().contains("jpeg")||titem.getImageURL().contains("png")||titem.getImageURL().contains("bmp")||titem.getImageURL().contains("gif"))){
			//	Toast.makeText(ThreadOpen.this, "Error Building Image List", Toast.LENGTH_SHORT).show();
			//	return;
			//}
		}
		b.putStringArrayList("ThumbImagePairs", temp);
		i.putExtras(b);
		startActivity(i);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		m_imageLoader.stopThread();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//finish();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		RevMob revmob = RevMob.start(this);
        RevMobBanner banner = revmob.createBanner(this);
        ViewGroup view = (ViewGroup) findViewById(R.id.threadbanner);
        view.addView(banner);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
}
