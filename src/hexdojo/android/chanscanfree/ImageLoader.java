package hexdojo.android.chanscanfree;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ImageLoader {
    
    private int IMAGE_MAX_SIZE = 320;

	//the simplest in-memory cache implementation. This should be replaced with something like SoftReference or BitmapOptions.inPurgeable(since 1.6)
    private HashMap<String, Bitmap> cache=new HashMap<String, Bitmap>();
    private Queue<String> latestImages = new LinkedList<String>();
    private File cacheDir;
    int stub_id;
    int holdOnto = 25;
    Context m_context;
    
    public int getHoldOnto() {
		return holdOnto;
	}

	public void setHoldOnto(int holdOnto) {
		this.holdOnto = holdOnto;
	}

	public ImageLoader(Context context){
		try{
			m_context=context;
	        //Make the background thead low priority. This way it will not affect the UI performance
	        photoLoaderThread.setPriority(Thread.MIN_PRIORITY);
	        stub_id = R.drawable.stub;
	        //Find the dir to save cached images
	        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
	            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"ChanScan Cache");
	        else{
	            cacheDir=new File(context.getCacheDir(), "ChanScan Cache");
	        }
	        if(!cacheDir.exists()){
	            cacheDir.mkdirs();
	        }
		}
		catch(Exception e){
			Toast.makeText(m_context, "Error Creating Cache", Toast.LENGTH_LONG);
		}
        
    }
    
    public void SetMaxImageSize(int max){
    	IMAGE_MAX_SIZE = max;
    }
    
    public void setStubId(int assetId){
    	stub_id = assetId;
    }
    
    public void DisplayImage(String url,  ImageView imageView, ProgressBar bar)
    {
        if(cache.containsKey(url)){
            imageView.setImageBitmap(cache.get(url));
        	imageView.setVisibility(View.VISIBLE);
        	bar.setVisibility(View.INVISIBLE);
        }
        else
        {
        	if(url != null && imageView != null){
	            queuePhoto(url, imageView, bar);
	            if(url.contains("http")){
	            	imageView.setImageResource(stub_id);
	            }
	            else{
	            	imageView.setImageResource(R.drawable.blackdot_old);
	            }
        	}
        }    
    }
    

	public void DisplayImage(String url,  ImageView imageView)
    {
        if(cache.containsKey(url)){
            imageView.setImageBitmap(cache.get(url));
        	imageView.setVisibility(View.VISIBLE);
        }
        else
        {
        	if(url != null && imageView != null){
	            queuePhoto(url, imageView);
	            if(url.contains("http")){
	            	imageView.setImageResource(stub_id);
	            }
	            else{
	            	imageView.setImageResource(R.drawable.blackdot_old);
	            }
        	}
        }    
    }
	
	private void CheckHashMap(String url){
		latestImages.add(url);
		while(latestImages.size()>holdOnto){
			cache.remove(latestImages.remove());
			//Log.e("IMAGELOADER", "PRUNED HASHAMP");
		}
	}

	
	public Bitmap GetBitmap(String url){
		if(cache.containsKey(url))
            return getBitmap(url);
        else
        {
            Bitmap bmp=getBitmap(url);
            CheckHashMap(url);
            cache.put(url, bmp);
            return bmp;
        }    
	}
	
	public Bitmap GetBitmap(String url, int maxSize){
        Bitmap bmp=getBitmap(url, maxSize);
        CheckHashMap(url);
        cache.put(url, bmp);
        return bmp;
	}
	
	public Bitmap GetBitmapRotated(String url, int maxSize){
        Bitmap bmp=getBitmapRotated(url, maxSize);
        CheckHashMap(url+"rotated");
        cache.put(url, bmp);
        return bmp;
	}
	
	private void queuePhoto(String url, ImageView imageView, ProgressBar bar)
	{
        //This ImageView may be used for other images before. So there may be some old tasks in the queue. We need to discard them. 
        photosQueue.Clean(imageView);
        PhotoToLoad p=new PhotoToLoad(url, imageView, bar);
        synchronized(photosQueue.photosToLoad){
            photosQueue.photosToLoad.push(p);
            photosQueue.photosToLoad.notifyAll();
        }
        
        //start thread if it's not started yet
        if(photoLoaderThread.getState()==Thread.State.NEW)
            photoLoaderThread.start();
	}
        
    private void queuePhoto(String url, ImageView imageView)
    {
        //This ImageView may be used for other images before. So there may be some old tasks in the queue. We need to discard them. 
        photosQueue.Clean(imageView);
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        synchronized(photosQueue.photosToLoad){
            photosQueue.photosToLoad.push(p);
            photosQueue.photosToLoad.notifyAll();
        }
        
        //start thread if it's not started yet
        if(photoLoaderThread.getState()==Thread.State.NEW)
            photoLoaderThread.start();
    }
    
    private Bitmap getBitmap(String url) 
    {
    	if(url != null && url.contains("http")){
    	
	        //I identify images by hashcode. Not a perfect solution, good for the demo.
	        String filename=String.valueOf(url.hashCode());
	        File f=new File(cacheDir, filename);
	        
	        //from SD cache
	        Bitmap b = decodeFile(f);
	        if(b!=null)
	            return b;
	        
	        //from web
	        try {
	            Bitmap bitmap=null;
	            InputStream is=new URL(url).openStream();
	            OutputStream os = new FileOutputStream(f);
	            Utils.CopyStream(is, os);
	            os.close();
	            bitmap = decodeFile(f);
	            return bitmap;
	        } catch (Exception ex){
	           ex.printStackTrace();
	           return null;
	        }
    	}
    	return null;
    }
    
    private Bitmap getBitmap(String url, int maxSize) 
    {
    	if(url != null && url.contains("http")){
	        //I identify images by hashcode. Not a perfect solution, good for the demo.
	        String filename=String.valueOf(url.hashCode())+String.valueOf(maxSize);
	        File f=new File(cacheDir, filename);
	        
	        //from SD cache
	        Bitmap b = decodeFile(f, maxSize);
	        if(b!=null)
	            return b;
	        
	        //from web
	        try {
	            Bitmap bitmap=null;
	            InputStream is=new URL(url).openStream();
	            OutputStream os = new FileOutputStream(f);
	            Utils.CopyStream(is, os);
	            os.close();
	            bitmap = decodeFile(f,maxSize);
	            return bitmap;
	        } catch (Exception ex){
	           ex.printStackTrace();
	           return null;
	        }
    	}
    	return null;
    }
    
    private Bitmap getBitmapRotated(String url, int maxSize) 
    {
    	if(url != null && url.contains("http")){
	        //I identify images by hashcode. Not a perfect solution, good for the demo.
	        String filename=String.valueOf(url.hashCode())+String.valueOf(maxSize);
	        File f=new File(cacheDir, filename);
	        
	        //from SD cache
	        Bitmap b = decodeFile(f, maxSize);
	        if(b!=null)
	            return b;
	        
	        //from web
	        try {
	            Bitmap bitmap=null;
	            InputStream is=new URL(url).openStream();
	            OutputStream os = new FileOutputStream(f);
	            Utils.CopyStream(is, os);
	            os.close();
	            bitmap = decodeFileRotated(f,maxSize);
	            return bitmap;
	        } catch (Exception ex){
	           ex.printStackTrace();
	           return null;
	        }
    	}
    	return null;
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inPurgeable = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            try{
            	b = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            }catch(OutOfMemoryError ex){
            	b = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.blackdot_old);
            }
        } catch (FileNotFoundException e) {
        }
        return b;
    }  
   
    public Bitmap decodeFile(File f, int maxSize){
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inPurgeable = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            int scale = 1;
            if (o.outHeight > maxSize || o.outWidth > maxSize) {
                scale = (int)Math.pow(2, (int) Math.round(Math.log(maxSize / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            try{
            	b = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            }catch(OutOfMemoryError ex){
            	b = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.blackdot_old);
            }
        } catch (FileNotFoundException e) {
        }
        return b;
    } 
    
    public Bitmap decodeFileRotated(File f, int maxSize){
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inPurgeable = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            int scale = 1;
            if (o.outHeight > maxSize || o.outWidth > maxSize) {
                scale = (int)Math.pow(2, (int) Math.round(Math.log(maxSize / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }
            

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPurgeable = true;
            try{
            	b = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            }catch(OutOfMemoryError ex){
            	b = BitmapFactory.decodeResource(m_context.getResources(), R.drawable.blackdot_old);
            }
            if(b.getWidth()>b.getHeight()){
            	Matrix m = new Matrix();
            	m.postRotate(90f);
            	b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
            }
        } catch (FileNotFoundException e) {
        }
        return b;
    } 
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public ProgressBar progressBar;
        public PhotoToLoad(String u, ImageView i){
            url=u; 
            imageView=i;
            progressBar=null;
        }
        public PhotoToLoad(String u, ImageView i, ProgressBar bar){
            url=u; 
            imageView=i;
            progressBar=bar;
        }
    }
    
    PhotosQueue photosQueue=new PhotosQueue();
    
    public void stopThread()
    {
        photoLoaderThread.interrupt();
    }
    
    //stores list of photos to download
    class PhotosQueue
    {
        private Stack<PhotoToLoad> photosToLoad=new Stack<PhotoToLoad>();
        
        //removes all instances of this ImageView
        public void Clean(ImageView image)
        {
        	try{
	            for(int j=0 ;j<photosToLoad.size();){
	                if(photosToLoad.get(j).imageView==image)
	                    photosToLoad.remove(j);
	                else{
	                    ++j;
	                }
	            }
        	}
        	catch(Exception e){}
        }
    }
    
    class PhotosLoader extends Thread {
        public void run() {
            try {
                while(true)
                {
                    //thread waits until there are any images to load in the queue
                    if(photosQueue.photosToLoad.size()==0)
                        synchronized(photosQueue.photosToLoad){
                            photosQueue.photosToLoad.wait();
                        }
                    if(photosQueue.photosToLoad.size()!=0)
                    {
                        PhotoToLoad photoToLoad;
                        synchronized(photosQueue.photosToLoad){
                            photoToLoad=photosQueue.photosToLoad.pop();
                        }
                        Bitmap bmp=getBitmap(photoToLoad.url);
                        cache.put(photoToLoad.url, bmp);
                        CheckHashMap(photoToLoad.url);
                        if(photoToLoad != null && photoToLoad.imageView!= null && photoToLoad.imageView.getTag()!=null){
	                        if(((String)photoToLoad.imageView.getTag()).equals(photoToLoad.url)){
	                        	if(bmp!=null){
	                        		BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad.imageView);
	                        		if(bd!=null){
	                        			Activity a=(Activity)photoToLoad.imageView.getContext();
	                        			a.runOnUiThread(bd);
	                        		}
	                        	}
	                        }
                        }
                    }
                    if(Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {
                //allow thread to exit
            }
        }
    }
    
    PhotosLoader photoLoaderThread=new PhotosLoader();
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        ImageView imageView;
        ProgressBar progressBar;
        public BitmapDisplayer(Bitmap b, ImageView i, ProgressBar bar){bitmap=b;imageView=i; progressBar = bar;}
        public BitmapDisplayer(Bitmap b, ImageView i){bitmap=b;imageView=i; progressBar = null;}
        public void run()
        {
            if(bitmap!=null && (!bitmap.isRecycled())){
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
                if(progressBar != null){
                	progressBar.setVisibility(View.INVISIBLE);
                }
            }
            else
                imageView.setImageResource(R.drawable.blackdot_old);
        }
    }

    public Boolean clearCache() {
    	//Log.i("ImageLoader", "Full Cache Cleared");
        //clear memory cache
        cache.clear();
        
        
        //Log.e("ImageLoader","CLEARING FILES");
        //clear SD cache
        File[] files=cacheDir.listFiles();
        for(File f:files){
        	if(f.isDirectory())
        	{
        		File[] filesd = f.listFiles();
        		for(File f2:filesd){
        			f2.delete();
        		}
        		f.delete();
        	}
            if(f.getAbsolutePath().contains("ChanScan")){
            	f.delete();
            }
        }
        //Log.e("ImageLoader","FILES DONE");
        return true;
    }
    
    public Boolean clearMemCache(){
    	//Log.i("ImageLoader", "Mem Cache Cleared");
        cache.clear();
        return true;
    }

}
