package hexdojo.android.chanscanfree;

 
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.*;
import java.io.*;
import java.util.*;

import com.revmob.RevMob;
import com.revmob.ads.banner.RevMobBanner;

//import org.apache.http.impl.client.DefaultHttpClient;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.os.Bundle;
//import android.os.Debug;
import android.os.Handler;
//import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class GifDecoder - Decodes a GIF file into one or more frames.
 * <br><pre>
 * Example:
 *    GifDecoder d = new GifDecoder();
 *    d.read("sample.gif");
 *    int n = d.getFrameCount();
 *    for (int i = 0; i < n; i++) {
 *       BufferedImage frame = d.getFrame(i);  // frame i
 *       int t = d.getDelay(i);  // display duration of frame in milliseconds
 *       // do something with frame
 *    }
 * </pre>
 * No copyright asserted on the source code of this class.  May be used for
 * any purpose, however, refer to the Unisys LZW patent for any additional
 * restrictions.  Please forward any corrections to <!-- e --><a href="mailto:kweiner@fmsware.com">kweiner@fmsware.com</a><!-- e -->.
 *
 * @author Kevin Weiner, FM Software; LZW decoder adapted from John Cristy's ImageMagick.
 * @version 1.03 November 2003
 *
 */

public class GifViewer extends Activity /*implements AdWhirlInterface*/
{

  /**
   * File read status: No errors.
   */
  public static final int STATUS_OK = 0;

  /**
   * File read status: Error decoding file (may be partially decoded)
   */
  public static final int STATUS_FORMAT_ERROR = 1;

  /**
   * File read status: Unable to open source.
   */
  public static final int STATUS_OPEN_ERROR = 2;

  protected BufferedInputStream in;
  protected int status;

  protected int width; // full image width
  protected int height; // full image height
  protected boolean gctFlag; // global color table used
  protected int gctSize; // size of global color table
  protected int loopCount = 1; // iterations; 0 = repeat forever

  protected int[] gct; // global color table
  protected int[] lct; // local color table
  protected int[] act; // active color table

  protected int bgIndex; // background color index
  protected int bgColor; // background color
  protected int lastBgColor; // previous bg color
  protected int pixelAspect; // pixel aspect ratio

  protected boolean lctFlag; // local color table flag
  protected boolean interlace; // interlace flag
  protected int lctSize; // local color table size

  protected int ix, iy, iw, ih; // current image rectangle
  protected Rect lastRect; // last image rect
  protected Bitmap image; // current frame
  protected Bitmap lastImage; // previous frame

  protected byte[] block = new byte[256]; // current data block
  protected int blockSize = 0; // block size

  // last graphic control extension info
  protected int dispose = 2;
  // 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
  protected int lastDispose = 0;
  protected boolean transparency = false; // use transparent color
  protected int delay = 0; // delay in milliseconds
  protected int transIndex; // transparent color index

  protected static final int MaxStackSize = 4096;
  // max decoder pixel stack size

  // LZW decoder working arrays
  protected short[] prefix;
  protected byte[] suffix;
  protected byte[] pixelStack;
  protected byte[] pixels;

  protected ArrayList<GifFrame> frames; // frames read from current file
  protected int frameCount;

  static class GifFrame {
    public GifFrame(/*Bitmap im*/String pa, int del) {
      //image = im;
    path = pa;
      delay = del;
    }
    //public Bitmap image;
    public int delay;
    public String path;
  }

  private String imgURL;
  private ImageView gifImage;
  
  private Runnable getGif;
  private Thread   getGifThread;
  private Runnable playGif;
  private Thread   playGifThread;
  
  private Bitmap currentImage;
  private int curFrame = 0;
  private boolean finishedLoading = false;
  private boolean gifPlaying = false;
  private File cacheDir;
  private URL pizza;
  private Handler gifLoadHandler = new Handler();
  private File imagefile;
  private File gifCache;
  
  private ProgressDialog m_ProgressDialog = null;
  
  private void cleanBitmaps(){
	  //TODO
//	  for(int i = 0; i<frames.size(); i++){
//		  Bitmap im = frames.get(i).image;
//		  if(im!=null){
//			  im.recycle();
//		  }
//	  }
	  
	  if(image!=null)image.recycle();
	  if(lastImage!=null)lastImage.recycle();
  }
  
  final Runnable mUpdateResults = new Runnable() {
      public void run() {
    	  try{
    		  //Bitmap update = frames.get(frames.size()-1).image;
    		  if(currentImage!=null){
    			  gifImage.setImageBitmap(currentImage);
    		  }
    	  m_ProgressDialog.dismiss();
    	  //Toast.makeText(GifViewer.this, "Loading Frame "+frameCount+"...", Toast.LENGTH_SHORT).show();
    	  TextView text = (TextView)findViewById(R.id.gifText);
    	  text.setText("Loading Frame "+(frameCount+1)+"...");
    	  }
    	  catch(OutOfMemoryError e){
    		  //Log.e("Line",""+165);
    		  //Toast.makeText(GifViewer.this, "Not Enough Memory To Finish Loading Gif", Toast.LENGTH_LONG).show();
    		  //cleanBitmaps();
    		  //finish();
    	  }
      }
  };
  
  final Runnable fail = new Runnable() {
	
	@Override
	public void run() {
		//Toast.makeText(GifViewer.this, "Not Enough Memory To Finish Loading Gif", Toast.LENGTH_LONG).show();
		cleanBitmaps();
		//finish();		
	}
};
  
  final Runnable mPlayGif = new Runnable() {
	
	@Override
	public void run() {
		try{
			if(!Thread.interrupted()){
				if(frames.get(curFrame).delay<0){
					Toast.makeText(GifViewer.this, "Error in Downloading Gif", Toast.LENGTH_LONG).show();
					finish();
				}
				//Log.i("mPlayGif","HitIt");
				TextView text = (TextView)findViewById(R.id.gifText);
		    	  text.setText("Playing");
				//Bitmap update = frames.get(curFrame).image;
		    	  String update = frames.get(curFrame).path;
				if(update!=null){
					//gifImage.setImageBitmap(frames.get(curFrame).image);
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inPurgeable = true;
					opts.inInputShareable = true;
					gifImage.setImageBitmap(BitmapFactory.decodeFile(update,opts));
					//Log.i("update frame dealy",update+" "+curFrame+" "+frames.get(curFrame).delay);
					//Log.i("GifCurrentFrame",""+curFrame);
			}
			}
		}
		  catch(OutOfMemoryError e){
			  //Log.e("Line",""+195);
			  Toast.makeText(GifViewer.this, "Not Enough Memory To Finish Loading Gif", Toast.LENGTH_LONG).show();
			  cleanBitmaps();
			  finish();
		  }
	}
};

	Bundle b;
  
  public void onCreate(Bundle savedInstanceState) {
	  	setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  		super.onCreate(savedInstanceState);
  		setContentView(R.layout.gifpage);
  		
		RevMob revmob = RevMob.start(this);
        RevMobBanner banner = revmob.createBanner(this);
        ViewGroup view = (ViewGroup) findViewById(R.id.gifpagebanner);
        view.addView(banner);
  		

  		
  		b = null;
  		init();
  		
		m_ProgressDialog = ProgressDialog.show(GifViewer.this,"Updating...", "Downloading Gif");
		m_ProgressDialog.setOnCancelListener( new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				getGifThread.interrupt();
				if(playGifThread!=null)	playGifThread.interrupt();
				gifPlaying = false;
				cleanBitmaps();
				finish();
			}
		});
		m_ProgressDialog.setCancelable(true);
		
  		gifImage = (ImageView)findViewById(R.id.gifImage);
  		gifImage.setBackgroundColor(Color.argb(255, 20, 20, 20));
  		b = this.getIntent().getExtras();
        imgURL = b.getString("imageURL");
        URL forTitle;
        try{
        	forTitle = new URL(imgURL);
        	setTitle("Gif Viewer: "+forTitle.getFile());
        }
        catch(Exception e){};
        
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"ChanScan Cache");
        else{
            cacheDir=new File(GifViewer.this.getCacheDir(), "ChanScan Cache");
        }
        if(!cacheDir.exists()){
            cacheDir.mkdirs();
        }
        
        gifCache = new File(cacheDir, "GIF Cache");
        gifCache = new File(gifCache, ""+imgURL.hashCode()+" Cache");
        if(!gifCache.exists()){
        	gifCache.mkdirs();
        }
        
        String filename=String.valueOf(imgURL.hashCode());
        imagefile=new File(cacheDir, filename);
		
        getGif = new Runnable() {
			
			@Override
			public void run() {
				try{
					pizza = new URL(imgURL);	
		        	if(!imagefile.exists()){
		        		InputStream is= pizza.openStream();
		        		OutputStream os = new FileOutputStream(imagefile);
		        		Utils.CopyStream(is, os);
		        		os.flush();
		        		os.close();
		        		is.close();
		        		
		        	}
		        	else{
		        		m_ProgressDialog.dismiss();
		        	}
		        	File[] files =gifCache.listFiles();
		        	boolean found = false;
		        	for(File f : files){
						if(f.getAbsolutePath().contains(""+imgURL.hashCode()+"`frame")){
							found = true;
							break;
						}
					}
					if(!found){
						read(imagefile.toURL().toString());
					}		   
					
					files =gifCache.listFiles();
					found = false;
					for(File f : files){
						if(f.getAbsolutePath().contains(""+imgURL.hashCode()+"`frame")){
							found = true;
							break;
						}
					}
					int currentFrame = 0;
					System.gc();
					//BitmapFactory.Options opts = new BitmapFactory.Options();
					//opts.inPurgeable = true;
					do{
						found=false;
						String curPath = ""+imgURL.hashCode()+"`frame"+currentFrame;
						for(File f : files){
							if(f.getAbsolutePath().contains(curPath)){
								String[] parts = f.getAbsolutePath().split("`");
								parts[1] = parts[1].replace("frame","").trim();
								if(Integer.parseInt(parts[1])==currentFrame){
									parts[2] = parts[2].replace("delay", "").trim();

									//Bitmap doit = null;
									try{
										//doit = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
									}
									catch(OutOfMemoryError e){
										//Log.e("line","292");
										e.printStackTrace();
										found = false;
										break;
									}
									catch(Exception damn){
										//damn.printStackTrace();
										found = false;
										break;
									}
									
									//frames.add(new GifFrame(doit,Integer.parseInt(parts[2])<20?20:Integer.parseInt(parts[2])));
									frames.add(new GifFrame(f.getAbsolutePath(), Integer.parseInt(parts[2])<50?100:Integer.parseInt(parts[2])));
									//Log.i("Gif Framecount",""+frames.size());
									found=true;
									break;
								}
							}
						}
						currentFrame++;
					}while(found && !Thread.interrupted());
				}
				catch(Exception e){
					//Toast.makeText(GifViewer.this, "Not Enough Memory to Finish Loading Gif", Toast.LENGTH_LONG).show();
					//Log.e("Line",""+258);
					e.printStackTrace();
					gifLoadHandler.post(fail);
					return;
					//finish();
				}
				if(!Thread.interrupted())
				{
					finishedLoading = true;
					gifPlaying = true;
					playGifThread = new Thread(playGif);
					playGifThread.setPriority(Thread.MIN_PRIORITY);
					playGifThread.start();
				}
				else{
					cleanBitmaps();
					return;
					//finish();
				};
				return;
			}
		};
		playGif = new Runnable() {
			
			@Override
			public void run() {
				//Log.i("PlayGif","HitIt");
			     //int n = frames.size();
	             //int ntimes = getLoopCount();
				//Log.i("playgif length",""+frames.size());
	             do {
	                  for (int i = 0; i < frames.size(); i++) {
	                	  //Log.i("PlayGif","CurFrame"+i);
	                	  curFrame = i;
	                	  gifLoadHandler.post(mPlayGif);
	                      int t = frames.get(curFrame).delay; 
	                      try {
	                            Thread.sleep(t);
	                      } catch (InterruptedException ex) {
	                    	  cleanBitmaps();
	                    	  return;
	                    	  //finish();
	                            //System.out.println(ex.getMessage());
	                      }
	                      catch(Exception e){
	                    	  cleanBitmaps();
	                    	  return;
	                    	  //finish();
	                      }
	                  }
	             } while (gifPlaying && !Thread.interrupted());
	             cleanBitmaps();
	             return;
				//finish();
			}
		};
        
		getGifThread = new Thread(null,getGif,"MagentoBackground2");
		getGifThread.setPriority(Thread.MAX_PRIORITY);
		getGifThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				//Toast.makeText(GifViewer.this, "Not Enough Memory to Finish Loading Gif", Toast.LENGTH_LONG).show();
				ex.printStackTrace();
				//Log.e("Line","333");
				gifLoadHandler.post(fail);
				return;
				//finish();
				
			}
		});
		getGifThread.start();

		
        //gifImage.setImageBitmap(getImage());
        gifImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(gifPlaying)curFrame=0;	
			}
		});
  }

  
  
  	@Override
  		public void onBackPressed() {
  			super.onBackPressed();
  			gifPlaying = false;
  			gifImage.setVisibility(View.INVISIBLE);
  			if(getGifThread!=null)getGifThread.interrupt();
  			if(playGifThread!=null)	playGifThread.interrupt();
  			cleanBitmaps();
  			finish();
  		}
  
  
  	@Override
  		protected void onDestroy() {
  			// TODO Auto-generated method stub
  			cleanBitmaps();
  			if(playGifThread!=null)	playGifThread.interrupt();
  			if(getGifThread!=null)getGifThread.interrupt();
  			super.onDestroy();
  		}
  	
  	@Override
  		protected void onResume() {
  			// TODO Auto-generated method stub
  			super.onResume();
  		}
  		
  	@Override
  		protected void onStart() {
  			// TODO Auto-generated method stub
  			super.onStart();
  		}
  	
  	@Override
  		protected void onStop() {
  			// TODO Auto-generated method stub
  			super.onStop();
  			if(playGifThread!=null)	playGifThread.interrupt();
  			if(getGifThread!=null)getGifThread.interrupt();
  			
  	}
  
 
  private final int MENU_ID_FIX = 0;
  
  @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	  if(gifPlaying && playGifThread!=null) menu.add(Menu.NONE, MENU_ID_FIX, 0, "Re-Download / Fix").setIcon(R.drawable.icmenurefresh_1);
		return super.onCreateOptionsMenu(menu);
	}
  
  @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if(gifPlaying && playGifThread!=null && playGifThread.isAlive()) menu.add(Menu.NONE, MENU_ID_FIX, 0, "Re-download / Fix").setIcon(R.drawable.icmenurefresh_1);
		return super.onPrepareOptionsMenu(menu);
	}
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
          case MENU_ID_FIX:
        	  if(playGifThread != null){
	        	  playGifThread.interrupt();
	        	  getGifThread.interrupt();
	        	  File[] files =gifCache.listFiles();
	        	  for(File f : files) f.delete();
	        	  imagefile.delete();
	        	  while(playGifThread.isAlive() && getGifThread.isAlive()){ 
	        		  //Log.e("shit","still alive");
	        		  
	        	  }
	        	  onCreate(b);
	        	  
	        	 // getGifThread = new Thread(getGif);
	        	 // getGifThread.start();
        	  }
        	  else{
        		  Toast.makeText(GifViewer.this, "Already reloading.", Toast.LENGTH_LONG).show();
        	  }
              break;
      	}
      return super.onOptionsItemSelected(item);
      }
  
  
  
  /**
   * Gets display duration for specified frame.
   *
   * @param n int index of frame
   * @return delay in milliseconds
   */
  public int getDelay(int n) {
    //
    delay = -1;
    if ((n >= 0) && (n < frameCount)) {
      delay = ((GifFrame) frames.get(n)).delay;
    }
    //Log.i("CurDelay",""+delay);
    return delay;
  }

  /**
   * Gets the number of frames read from file.
   * @return frame count
   */
  public int getFrameCount() {
    return frameCount;
  }

  /**
   * Gets the first (or only) image read.
   *
   * @return BufferedImage containing first frame, or null if none.
   */
  public Bitmap getImage() {
    return getFrame(0);
  }

  /**
   * Gets the "Netscape" iteration count, if any.
   * A count of 0 means repeat indefinitiely.
   *
   * @return iteration count if one was specified, else 1.
   */
  public int getLoopCount() {
    return loopCount;
  }

  public int[] copyPixelToSetInteger(Bitmap B) {
    System.out.println("GIF >> "+B.getWidth()+" X "+B.getHeight());
    int[] tmp = new int[B.getWidth()*B.getHeight()];
    for (int i = 0;i<B.getHeight();i++) {
        for (int j = 0;j<B.getWidth();j++) {
            tmp[j + i * B.getWidth()] = B.getPixel(j, i);
        }
    }
    return tmp;
  }

  /**
   * Creates new frame image from current data (and previous
   * frames as specified by their disposition codes).
   */
  protected void setPixels() {
    // expose destination image's pixels as int array
    int[] dest = copyPixelToSetInteger(image);
      // ((DataBufferInt) image.getRaster().getDataBuffer()).getData(); // 144key

    // fill in starting image contents based on last image's dispose code
    if (lastDispose > 0) {
      if (lastDispose == 3) {
        // use image before last
        int n = frameCount - 2;
        if (n > 0) {
        	lastImage = getFrame(n - 1);
        	//lastImage = null;
        } else {
          lastImage = null;
        }
      }

      if (lastImage != null) {
        int[] prev = copyPixelToSetInteger(lastImage);
          // ((DataBufferInt) lastImage.getRaster().getDataBuffer()).getData(); // 144key
        System.arraycopy(prev, 0, dest, 0, width * height);
        // copy pixels

        if (lastDispose == 2) {
            // fill last image rect area with background color
            System.gc();
            Paint paint = new Paint();
            paint.setStyle(Style.FILL);
            if (transparency) {
                paint.setARGB(0, 0, 0, 0);
            } else {
                paint.setColor(lastBgColor);
            }
            Canvas canvas = new Canvas();
            canvas.drawRect(lastRect, paint);

            //Graphics2D g = image.createGraphics();
            //Color c = null;
            //if (transparency) {
                //c = Color.argb(0, 0, 0, 0); // new Color(0, 0, 0, 0);  // assume background is transparent
            //} else {
                //c = new Color(lastBgColor); // use given background color
            //}
            //g.setColor(c);
            //g.setComposite(AlphaComposite.Src); // replace area
            //g.fill(lastRect);
            //g.dispose();
        }
      }
    }

    // copy each source line to the appropriate place in the destination
    int pass = 1;
    int inc = 8;
    int iline = 0;
    for (int i = 0; i < ih; i++) {
      int line = i;
      if (interlace) {
        if (iline >= ih) {
          pass++;
          switch (pass) {
            case 2 :
              iline = 4;
              break;
            case 3 :
              iline = 2;
              inc = 4;
              break;
            case 4 :
              iline = 1;
              inc = 2;
          }
        }
        line = iline;
        iline += inc;
      }
      line += iy;
      if (line < height) {
        int k = line * width;
        int dx = k + ix; // start of line in dest
        int dlim = dx + iw; // end of dest line
        if ((k + width) < dlim) {
          dlim = k + width; // past dest edge
        }
        int sx = i * iw; // start of line in source
        while (dx < dlim) {
          // map color and insert in destination
          int index = ((int) pixels[sx++]) & 0xff;
          int c = act[index];
          if (c != 0) {
            dest[dx] = c;            
          }
          dx++;
        }
      }
    }
    // Final Output
    for (int i=0;i<dest.length;i++)
        image.setPixel((i % image.getWidth()), ((i-(i % image.getWidth())) / image.getWidth()), dest[i]);
  }

  /**
   * Gets the image contents of frame n.
   *
   * @return BufferedImage representation of frame, or null if n is invalid.
   */
  public Bitmap getFrame(int n) {
    System.out.println("GIF FrameCount >> "+frames.size()+" >> "+frameCount);
    Bitmap tmp = null;
    if ((n >= 0) && (n < frameCount)) {
      //System.out.println("GIF Frame "+n+" >> image : "+((GifFrame) frames.get(n)).image.toString());
      System.out.println("GIF Frame "+n+" >> delay : "+((GifFrame) frames.get(n)).delay);
      //tmp = ((GifFrame) frames.get(n)).image;
    }
    return tmp;
  }

  /**
   * Gets image size.
   *
   * @return GIF image dimensions
   */
  //public Dimension getFrameSize() {
  //  return new Dimension(width, height);
  //} // 144key
  public int getWidth() {
      return width;
  }
  
  public int getHeight() {
      return height;
  }

  /**
   * Reads GIF image from stream
   *
   * @param BufferedInputStream containing GIF file.
   * @return read status code (0 = no errors)
   */
  public int read(BufferedInputStream is) {
    //init();
    if (is != null) {
      in = is;
      readHeader();
      if (!err()) {
        readContents();
        if (frameCount < 0) {
          status = STATUS_FORMAT_ERROR;
        }
      }
    } else {
      status = STATUS_OPEN_ERROR;
    }
    try {
      is.close();
    } catch (IOException e) {
    }
    return status;
  }

  /**
   * Reads GIF image from stream
   *
   * @param InputStream containing GIF file.
   * @return read status code (0 = no errors)
   */
  public int read(InputStream is) {
    init();
    if (is != null) {
      if (!(is instanceof BufferedInputStream))
        is = new BufferedInputStream(is);
      in = (BufferedInputStream) is;
      readHeader();
      if (!err()) {
        readContents();
        if (frameCount < 0) {
          status = STATUS_FORMAT_ERROR;
        }
      }
    } else {
      status = STATUS_OPEN_ERROR;
    }
    try {
      is.close();
    } catch (IOException e) {
    }
    return status;
  }

  /**
   * Reads GIF file from specified file/URL source
   * (URL assumed if name contains ":/" or "file:")
   *
   * @param name String containing source
   * @return read status code (0 = no errors)
   */
  public int read(String name) {
    status = STATUS_OK;
    try {
      name = name.trim().toLowerCase();
      if ((name.indexOf("file:") >= 0) ||
        (name.indexOf(":/") > 0)) {
        URL url = new URL(name);
        in = new BufferedInputStream(url.openStream());
        Log.i("GifViewer","Loading " + name);
      } else {
        in = new BufferedInputStream(new FileInputStream(name));
        Log.i("GifViewer","Loading from Cache");
      }
      status = read(in);
    } catch (IOException e) {
      status = STATUS_OPEN_ERROR;
    }

    return status;
  }

  /**
   * Decodes LZW image data into pixel array.
   * Adapted from John Cristy's ImageMagick.
   */
  protected void decodeImageData() {
    int NullCode = -1;
    int npix = iw * ih;
    int available,
      clear,
      code_mask,
      code_size,
      end_of_information,
      in_code,
      old_code,
      bits,
      code,
      count,
      i,
      datum,
      data_size,
      first,
      top,
      bi,
      pi;

    if ((pixels == null) || (pixels.length < npix)) {
      pixels = new byte[npix]; // allocate new pixel array
    }
    if (prefix == null) prefix = new short[MaxStackSize];
    if (suffix == null) suffix = new byte[MaxStackSize];
    if (pixelStack == null) pixelStack = new byte[MaxStackSize + 1];

    //  Initialize GIF data stream decoder.

    data_size = read();
    clear = 1 << data_size;
    end_of_information = clear + 1;
    available = clear + 2;
    old_code = NullCode;
    code_size = data_size + 1;
    code_mask = (1 << code_size) - 1;
    for (code = 0; code < clear; code++) {
      prefix[code] = 0;
      suffix[code] = (byte) code;
    }

    //  Decode GIF pixel stream.

    datum = bits = count = first = top = pi = bi = 0;

    for (i = 0; i < npix;) {
      if (top == 0) {
        if (bits < code_size) {
          //  Load bytes until there are enough bits for a code.
          if (count == 0) {
            // Read a new data block.
            count = readBlock();
            if (count <= 0)
              break;
            bi = 0;
          }
          datum += (((int) block[bi]) & 0xff) << bits;
          bits += 8;
          bi++;
          count--;
          continue;
        }

        //  Get the next code.

        code = datum & code_mask;
        datum >>= code_size;
        bits -= code_size;

        //  Interpret the code

        if ((code > available) || (code == end_of_information))
          break;
        if (code == clear) {
          //  Reset decoder.
          code_size = data_size + 1;
          code_mask = (1 << code_size) - 1;
          available = clear + 2;
          old_code = NullCode;
          continue;
        }
        if (old_code == NullCode) {
          pixelStack[top++] = suffix[code];
          old_code = code;
          first = code;
          continue;
        }
        in_code = code;
        if (code == available) {
          pixelStack[top++] = (byte) first;
          code = old_code;
        }
        while (code > clear) {
          pixelStack[top++] = suffix[code];
          code = prefix[code];
        }
        first = ((int) suffix[code]) & 0xff;

        //  Add a new string to the string table,

        if (available >= MaxStackSize)
          break;
        pixelStack[top++] = (byte) first;
        prefix[available] = (short) old_code;
        suffix[available] = (byte) first;
        available++;
        if (((available & code_mask) == 0)
          && (available < MaxStackSize)) {
          code_size++;
          code_mask += available;
        }
        old_code = in_code;
      }

      //  Pop a pixel off the pixel stack.

      top--;
      pixels[pi++] = pixelStack[top];
      i++;
    }

    for (i = pi; i < npix; i++) {
      pixels[i] = 0; // clear missing pixels
    }

  }

  /**
   * Returns true if an error was encountered during reading/decoding
   */
  protected boolean err() {
    return status != STATUS_OK;
  }

  /**
   * Initializes or re-initializes reader
   */
  protected void init() {
    status = STATUS_OK;
    frameCount = 0;
    frames = new ArrayList<GifFrame>();
    gct = null;
    lct = null;
  }

  /**
   * Reads a single byte from the input stream.
   */
  protected int read() {
    int curByte = 0;
    try {
      curByte = in.read();
    } catch (IOException e) {
      status = STATUS_FORMAT_ERROR;
    }
    return curByte;
  }

  /**
   * Reads next variable length block from input.
   *
   * @return number of bytes stored in "buffer"
   */
  protected int readBlock() {
    blockSize = read();
    int n = 0;
    if (blockSize > 0) {
      try {
        int count = 0;
        while (n < blockSize) {
          count = in.read(block, n, blockSize - n);
          if (count == -1)
            break;
          n += count;
        }
      } catch (IOException e) {
      }

      if (n < blockSize) {
        status = STATUS_FORMAT_ERROR;
      }
    }
    return n;
  }

  /**
   * Reads color table as 256 RGB integer values
   *
   * @param ncolors int number of colors to read
   * @return int array containing 256 colors (packed ARGB with full alpha)
   */
  protected int[] readColorTable(int ncolors) {
    int nbytes = 3 * ncolors;
    int[] tab = null;
    byte[] c = new byte[nbytes];
    int n = 0;
    try {
      n = in.read(c);
    } catch (IOException e) {
    }
    if (n < nbytes) {
      status = STATUS_FORMAT_ERROR;
    } else {
      tab = new int[256]; // max size to avoid bounds checks
      int i = 0;
      int j = 0;
      while (i < ncolors) {
        int r = ((int) c[j++]) & 0xff;
        int g = ((int) c[j++]) & 0xff;
        int b = ((int) c[j++]) & 0xff;
        tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
      }
    }
    return tab;
  }

  /**
   * Main file parser.  Reads GIF content blocks.
   */
  private boolean done = false;
  protected void readContents() {
    // read GIF file content blocks
    //boolean done = false;
    while (!(done || err()||Thread.interrupted())) {
      int code = read();
      switch (code) {

        case 0x2C : // image separator
          readImage();
          break;

        case 0x21 : // extension
          code = read();
          switch (code) {
            case 0xf9 : // graphics control extension
              readGraphicControlExt();
              break;

            case 0xff : // application extension
              readBlock();
              String app = "";
              for (int i = 0; i < 11; i++) {
                app += (char) block[i];
              }
              if (app.equals("NETSCAPE2.0")) {
                readNetscapeExt();
              }
              else
                skip(); // don't care
              break;

            default : // uninteresting extension
              skip();
          }
          break;

        case 0x3b : // terminator
          done = true;
          break;

        case 0x00 : // bad byte, but keep going and see what happens
          break;

        default :
          status = STATUS_FORMAT_ERROR;
      }
    }
    //lastImage.recycle();
    gct = null;
    act = null;
    block = null;
    lct = null; 
  }

  /**
   * Reads Graphics Control Extension values
   */
  protected void readGraphicControlExt() {
    read(); // block size
    int packed = read(); // packed fields
    dispose = (packed & 0x1c) >> 2; // disposal method
    if (dispose == 0) {
      dispose = 1; // elect to keep old image if discretionary
    }
    transparency = (packed & 1) != 0;
    delay = readShort() * 10; // delay in milliseconds
    transIndex = read(); // transparent color index
    read(); // block terminator
  }

  /**
   * Reads GIF file header information.
   */
  protected void readHeader() {
    String id = "";
    for (int i = 0; i < 6; i++) {
      id += (char) read();
    }
    System.out.println("GIF >> "+id);
    if (!id.startsWith("GIF")) {
      status = STATUS_FORMAT_ERROR;
      return;
    }    
    readLSD();
    if (gctFlag && !err()) {
      gct = readColorTable(gctSize);
      bgColor = gct[bgIndex];
    }
  }

  /**
   * Reads next frame image
   */
  protected void readImage() {
	  try{
		    File gifOut = null;
	    	if(!gifCache.exists()) gifCache.mkdirs();
		    try {
		    	
		    	gifOut = new File(gifCache,""+imgURL.hashCode()+"`frame"+(frameCount)+"`delay"+delay);
		    	} 
		    catch (Exception e) {
		    		//Log.e("Line",""+976);
		    		//finish();
		    		
		        e.printStackTrace();
		        return;
		    }
		    	//TODO
			    ix = readShort(); // (sub)image position & size
			    iy = readShort();
			    iw = readShort();
			    ih = readShort();
			
			    int packed = read();
			    lctFlag = (packed & 0x80) != 0; // 1 - local color table flag
			    interlace = (packed & 0x40) != 0; // 2 - interlace flag
			    // 3 - sort flag
			    // 4-5 - reserved
			    lctSize = 2 << (packed & 7); // 6-8 - local color table size
			
			    if (lctFlag) {
			      lct = readColorTable(lctSize); // read table
			      act = lct; // make local table active
			    } else {
			      act = gct; // make global table active
			      if (bgIndex == transIndex)
			        bgColor = Color.BLACK;
			    }
			    int save = 0;
			    if (transparency) {
			      save = act[transIndex];
			      act[transIndex] = 0; // set transparent color if specified
			    }
			
			    if (act == null) {
			      status = STATUS_FORMAT_ERROR; // no color table defined
			    }
			
			    if (err()) return;
			
			    
	    		decodeImageData(); // decode pixel data
			    skip();
			
			    if (err()) return;
			
			    
			    //Log.e("Available MEM",""+Debug.getNativeHeapFreeSize());
			    // create new image to receive frame data
			    //System.gc();
			    try{
			    image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			    }
			    catch(OutOfMemoryError e){
			    	//Log.e("Line",""+1026);
			    	--frameCount;
			    	done = true;
			    	return;
			    }

			    setPixels(); // transfer pixel data to image
			    

		    
		    
		    BitmapFactory.Options opts = new BitmapFactory.Options();
		    opts.inPurgeable = true;
		    
		    Bitmap dam = null;
		    
		    if(width > 300 && height > 300){
		    	double ratio = 1.0;
		    	if(width>height){
		    		ratio = 300.0/(double)width;
		    	}else{
		    		ratio = 300.0/(double)height;		    		
		    	}
		    	dam = Bitmap.createScaledBitmap(image, (int)Math.round(width*ratio), (int)Math.round(height*ratio), false);
		    }
		    else if(width>300){
		    	double ratio = 300.0/(double)width;
		    	dam = Bitmap.createScaledBitmap(image, 300, (int)Math.round(height*ratio), false);
		    }
		    else if(height > 300){
		    	double ratio = 300.0/(double)height;
		    	dam = Bitmap.createScaledBitmap(image, (int)Math.round(width*ratio), 300, false);
		    }
		    else{
		    	dam = image;
		    }
		    
		    try{
		    	FileOutputStream out = new FileOutputStream(gifOut);
		    	BufferedOutputStream bos = new BufferedOutputStream(out);
		    	dam.compress(Bitmap.CompressFormat.PNG, 90, bos);
		    	bos.flush();
		    	bos.close();
		    }catch (Exception e) {
		    	//Log.e("Line",""+1072);
			}
		    dam = null;
		    //if(currentImage!=null) currentImage.recycle();
		    currentImage = BitmapFactory.decodeFile(gifOut.getAbsolutePath(), opts);
		    //frames.add(new GifFrame(BitmapFactory.decodeFile(gifOut.getAbsolutePath(), opts), delay));
		    frameCount++;
		    if (transparency) {
			      act[transIndex] = save;
			    }
			resetFrame();

		    
		    gifLoadHandler.post(mUpdateResults);
	  }
	  catch(OutOfMemoryError e){
		  //Toast.makeText(GifViewer.this, "Not Enough Memory To Finish Loading Gif", Toast.LENGTH_LONG).show();
		  //Log.e("Line",""+1083);
		  frameCount--;
	    	done = true;
	    	return;
	  }
  }

  /**
   * Reads Logical Screen Descriptor
   */
  protected void readLSD() {

    // logical screen size
    width = readShort();
    height = readShort();

    // packed fields
    int packed = read();
    gctFlag = (packed & 0x80) != 0; // 1   : global color table flag
    // 2-4 : color resolution
    // 5   : gct sort flag
    gctSize = 2 << (packed & 7); // 6-8 : gct size

    bgIndex = read(); // background color index
    pixelAspect = read(); // pixel aspect ratio
  }

  /**
   * Reads Netscape extenstion to obtain iteration count
   */
  protected void readNetscapeExt() {
    do {
      readBlock();
      if (block[0] == 1) {
        // loop count sub-block
        int b1 = ((int) block[1]) & 0xff;
        int b2 = ((int) block[2]) & 0xff;
        loopCount = (b2 << 8) | b1;
      }
    } while ((blockSize > 0) && !err());
  }

  /**
   * Reads next 16-bit value, LSB first
   */
  protected int readShort() {
    // read 16-bit value, LSB first
    return read() | (read() << 8);
  }

  /**
   * Resets frame state for reading next image.
   */
  protected void resetFrame() {
    lastDispose = dispose;
    lastRect = new Rect(ix, iy, (ix+iw), (iy+ih)); //(ix, iy, iw, ih);
    lastImage = image;
    lastBgColor = bgColor;
    dispose = 0;
    transparency = false;
    delay = 0;
    lct = null;
  }

  /**
   * Skips variable length blocks up to and including
   * next zero length block.
   */
  protected void skip() {
    do {
      readBlock();
    } while ((blockSize > 0) && !err());
  }



//@Override
//public void adWhirlGeneric() {
//	// TODO Auto-generated method stub
//	
//}
}

