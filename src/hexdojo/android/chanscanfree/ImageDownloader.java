package hexdojo.android.chanscanfree;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

class imageToDL {
    String downloadURL;
    String dlPath;
    imageToDL(String url, String path){ downloadURL=url; dlPath = path;}
}


public class ImageDownloader extends Service{
	Thread imageDLThread;
	public volatile Queue<imageToDL> images;
	
	private NotificationManager mNotificationManager;
	private Notification notification;
	private Intent notificationIntent;
	private PendingIntent contentIntent;
	private RemoteViews contentView;
	private static final int BALLER_ID = 13;
	private static int totalImages = 0;
	private Context m_context;
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		m_context = this;
		images = new LinkedList<imageToDL>();
		try{
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notification = new Notification(R.drawable.chanscanicondownload, "Downloading Images...", System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			
			contentView = new RemoteViews(getPackageName(), R.layout.statusnotification);
			contentView.setImageViewResource(R.id.statusImage, R.drawable.chanscanicondownload);
			contentView.setTextViewText(R.id.statusText, "Downlading Images...");
			
			notification.contentView = contentView;
			contentView.setProgressBar(R.id.statusProgress, 1, 0, false);
			
			notificationIntent = new Intent(this, ThreadOpen.class);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET|Intent.FLAG_ACTIVITY_NEW_TASK);
			contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent, 0);
			notification.contentIntent = contentIntent;
			
			contentView.setTextViewText(R.id.statusText, "Downlading Images 1 out of "+images.size());
			contentView.setProgressBar(R.id.statusProgress, images.size(), 1, false);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			mNotificationManager.notify(BALLER_ID, notification);

			}catch(Exception e){
			//	Log.e("NOTIFY","WE GOT PROBLEMS"); 
			}
		
		//Log.e("ImgDLService", "created");

	}
	
	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}
	
	@Override
	public int onStartCommand(Intent arg0, int flags, int startId) {
		if(arg0!=null){
			int numImages = arg0.getIntExtra("NUMIMAGES", 0);
			totalImages += numImages;
			String imageurls[] = arg0.getStringArrayExtra("IMAGEURLS");
			String imagepaths[] = arg0.getStringArrayExtra("IMAGEPATHS");
			for(int i =0; i<numImages; i++){
				images.add(new imageToDL(imageurls[i], imagepaths[i]));
				//Log.e("ImgDLService", "added "+imageurls[i]);
			}
			//Log.e("ImgDLService", "added total: "+images.size());
			if(imageDLThread==null){
				imageDLThread = new Thread(null, downloadImages, "MagentoBackground");
	 	    	imageDLThread.setPriority(Thread.MIN_PRIORITY);	            
	 	    	imageDLThread.start();
	 	    }
		}
		return super.onStartCommand(arg0, flags, startId);
	}
	
	private Runnable downloadImages = new Runnable(){
		//@Override
		public void run() {
			int counter = 0;
			MediaScannerConnection scanner = new MediaScannerConnection(m_context, null);
			scanner.connect();
			

			while(!images.isEmpty()){
				//Thread.currentThread().yield();
				Thread.yield();
				try{
					counter++;
					contentView.setTextViewText(R.id.statusText, "Downlading Images "+String.valueOf(counter)+" out of "+totalImages);
					contentView.setProgressBar(R.id.statusProgress, totalImages,counter, false);
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					mNotificationManager.notify(BALLER_ID, notification);
					//Thread.currentThread().yield();
					Thread.yield();
					imageToDL dl = images.peek();
					URL imageURL = new URL(dl.downloadURL);
					
					File directory = new File(dl.dlPath);
					String filename[] = imageURL.getFile().split("/");	
					File download = new File(directory, filename[filename.length-1]);
					Log.i("ChanScan ImageDler", download.getAbsolutePath());
					try{
						if(!directory.exists()) directory.mkdirs();
						if(download.exists()){ 
							images.remove();
							continue;
						}
										
						InputStream is= imageURL.openStream();
						OutputStream os = new FileOutputStream(download);
				        Utils.CopyStream(is, os);
				        os.close();
				        is.close();
				        scanner.scanFile(download.getAbsolutePath(), null);
					}catch (Exception e){}
					images.remove();
					Thread.yield();
					//Thread.currentThread().yield();
				}
				catch(Exception ex){}			
			}
			scanner.disconnect();
			stopSelf();
		}
	};
	
    @Override
    public void onDestroy() {
		contentView.setTextViewText(R.id.statusText, "Image Download Complete");
		contentView.setProgressBar(R.id.statusProgress, 1,1, false);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(BALLER_ID, notification);
          super.onDestroy();
    }
	
}
