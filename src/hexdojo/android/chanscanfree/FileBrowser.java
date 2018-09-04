package hexdojo.android.chanscanfree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
//import android.net.ContentURI;
import android.os.Bundle;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileBrowser extends ListActivity {

	private enum DISPLAYMODE {
		ABSOLUTE, RELATIVE;
	}

	private final DISPLAYMODE displayMode = DISPLAYMODE.RELATIVE;
	private List<IconifiedText> directoryEntries = new ArrayList<IconifiedText>();
	private File currentDirectory = new File( "/" );
	AlertDialog delicious;
	
	public void onBackPressed() {
	if(currentDirectory.getAbsolutePath().equals("/")){
    	Intent myIntent = new Intent();
		myIntent.putExtra("filePath", "");
		setResult(RESULT_CANCELED, myIntent);
		finish();
	}
	else
	{
		browseTo(currentDirectory.getParentFile());
	}
}

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle icicle ) {
		super.onCreate( icicle );
		if( android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			browseToRoot();
		}
		else{
	    	Intent myIntent = new Intent();
			myIntent.putExtra("filePath", "");
			setResult(RESULT_CANCELED, myIntent);
			finish();
		}
	}

	/**
	 * This function browses to the root-directory of the file-system.
	 */
	private void browseToRoot() {
		if(android.os.Environment.getExternalStorageDirectory().canRead()){
			browseTo( android.os.Environment.getExternalStorageDirectory() );
		}
		else{
			Toast.makeText(getApplicationContext(), "No SD Card Found.", Toast.LENGTH_LONG );
			Intent myIntent = new Intent();
			myIntent.putExtra("filePath", "");
			setResult(RESULT_CANCELED, myIntent);
			finish();
		}
		
	}

	/**
	 * This function browses up one level according to the field:
	 * currentDirectory
	 */
	private void upOneLevel() {
		if( this.currentDirectory.getParent() != null )
			this.browseTo( this.currentDirectory.getParentFile() );
	}

	private void browseTo( final File aDirectory ) {
		
		if(!(aDirectory.canRead() && aDirectory.canWrite())){
			Intent myIntent = new Intent();
			setResult(RESULT_CANCELED);
			myIntent.putExtra("filePath", "");
			finish();
		}
		// On relative we display the full path in the title.
		if( this.displayMode == DISPLAYMODE.RELATIVE )
			this.setTitle( aDirectory.getAbsolutePath() + " :: "
					+ getString( R.string.app_name ) );
		if( aDirectory.isDirectory() ) {
			this.currentDirectory = aDirectory;
			fill( aDirectory.listFiles() );
		} else {
			OnClickListener okButtonListener = new OnClickListener() {
				// @Override
				public void onClick( DialogInterface arg0, int arg1 ) {
					// Lets start an intent to View the file, that was
					// clicked...
					FileBrowser.this.openFile( aDirectory );
				}
			};
			OnClickListener cancelButtonListener = new OnClickListener() {
				// @Override
				public void onClick( DialogInterface arg0, int arg1 ) {
					// Do nothing ^^
				}
			};
			
			createFileOpenDialog(
				"Question", R.drawable.folder,
				"Open " + aDirectory.getName() +"?",
				"OK", okButtonListener, "Cancel", cancelButtonListener, aDirectory
			).show();
		}
	}

	private AlertDialog createFileOpenDialog( 
			String title, int icon_res, final String message,
			String positive_text, OnClickListener positive_listener,
			String negative_text, OnClickListener negative_listener, final File f
		)
	{
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	    View layout = inflater.inflate(R.layout.fileopenpanel,null);
		AlertDialog.Builder baller = new AlertDialog.Builder(this);
		baller	.setTitle(message)
			//.setMessage("Open n"+aDirectory.getName()+"?")
				.setView(layout)
				.setPositiveButton("OK", positive_listener)
				.setNegativeButton("Cancel", negative_listener);
		delicious = baller.create();
		delicious.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				TextView tv = (TextView)delicious.findViewById(R.id.fileOpenText);
				ImageView iv = (ImageView)delicious.findViewById(R.id.fileOpenImage);
				if(tv!=null){
					tv.setText(message);
				}
				if(iv!=null){
					if(f.isFile()){
						ImageLoader iLoader = new ImageLoader(FileBrowser.this);					
						iv.setImageBitmap(iLoader.decodeFile(f, 250));
					}
				}
				
			}
		});
		
		return delicious;
	}

	private void openFile( File aFile ) {
		//Log.e("filebrowser","OK button clicked");
		Intent myIntent = new Intent();
		myIntent.putExtra("filePath", aFile.getAbsolutePath());
		//Log.e("filebrowser", aFile.getAbsolutePath());
		//Intent.ACTION_PICK, Uri.parse("file://" + aDirectory.getAbsolutePath()));
		setResult(RESULT_OK, myIntent);
		//Log.e("filebrowser","SetIntent");
		finish();
	}

	private void fill( File[] files ) {
		this.directoryEntries.clear();

		// Add the "." == "current directory"
		this.directoryEntries.add( new IconifiedText(
				getString( R.string.current_dir ), getResources().getDrawable(
						R.drawable.folder ) ) );
		// and the ".." == 'Up one level'
		if( this.currentDirectory.getParent() != null )
			this.directoryEntries.add( new IconifiedText(
					getString( R.string.up_one_level ), getResources()
							.getDrawable( R.drawable.uponelevel ) ) );

		Drawable currentIcon = null;
		for( File currentFile : files ) {
			if( currentFile.isDirectory() ) {
				if(currentFile.canRead() && currentFile.canWrite() && !currentFile.isHidden()){
					currentIcon = getResources().getDrawable( R.drawable.folder );
				}
				else{
					continue;
				}
			} else {
				String fileName = currentFile.getName();
				/*
				 * Determine the Icon to be used, depending on the FileEndings
				 * defined in: res/values/fileendings.xml.
				 */
				if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingImage))) {
					currentIcon = getResources().getDrawable(R.drawable.image);
				} else if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingWebText))) {
					continue;
					//currentIcon = getResources().getDrawable(R.drawable.webtext);
				} else if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingPackage))) {
					continue;
					//currentIcon = getResources().getDrawable(R.drawable.packed);
				} else if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingAudio))) {
					continue;
					//currentIcon = getResources().getDrawable(R.drawable.audio);
				} else {
					continue;
					//currentIcon = getResources().getDrawable(R.drawable.text);
				}
			}
			switch( this.displayMode ) {
				case ABSOLUTE:
					/* On absolute Mode, we show the full path */
					this.directoryEntries.add( new IconifiedText( currentFile
							.getPath(), currentIcon ) );
					break;
				case RELATIVE:
					/*
					 * On relative Mode, we have to cut the current-path at the
					 * beginning
					 */
					int currentPathStringLength = this.currentDirectory
							.getAbsolutePath().length();
					this.directoryEntries.add( new IconifiedText( currentFile
							.getAbsolutePath().substring(
									currentPathStringLength ), currentIcon ) );

					break;
			}
		}
		Collections.sort( this.directoryEntries );

		IconifiedTextListAdapter itla = new IconifiedTextListAdapter( this );
		itla.setListItems( this.directoryEntries );
		this.setListAdapter( itla );
	}

	@Override
	protected void onListItemClick( ListView l, View v, int position, long id ) {
		super.onListItemClick( l, v, position, id );

		String selectedFileString = this.directoryEntries.get( position )
				.getText();
		if( selectedFileString.equals( getString( R.string.current_dir ) ) ) {
			// Refresh
			this.browseTo( this.currentDirectory );
		} else if( selectedFileString
				.equals( getString( R.string.up_one_level ) ) ) {
			this.upOneLevel();
		} else {
			File clickedFile = null;
			switch( this.displayMode ) {
				case RELATIVE:
					clickedFile = new File( this.currentDirectory
							.getAbsolutePath()
							+ this.directoryEntries.get( position ).getText() );
					break;
				case ABSOLUTE:
					clickedFile = new File( this.directoryEntries
							.get( position ).getText() );
					break;
			}
			if( clickedFile != null )
				this.browseTo( clickedFile );
		}
	}

	/**
	 * Checks whether checkItsEnd ends with one of the Strings from fileEndings
	 */
	private boolean checkEndsWithInStringArray( String checkItsEnd,
			String[] fileEndings ) {
		for( String aEnd : fileEndings ) {
			if( checkItsEnd.endsWith( aEnd ) )
				return true;
		}
		return false;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
}

//import java.io.File;
//import java.io.FileFilter;
//import java.util.ArrayList;
//
//import android.app.AlertDialog;
//import android.app.ListActivity;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.DialogInterface.OnClickListener;
//import android.content.DialogInterface.OnShowListener;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//public class FileBrowser extends ListActivity {
//	
//	private ArrayList<String> directoryEntries = new ArrayList<String>();
//	private File currentDirectory;
//	private File root;
//	AlertDialog delicious;
//	ImageLoader iLoader;
//	
//	@Override
//	public void onBackPressed() {
//		if(currentDirectory.getAbsolutePath().equals(root.getAbsolutePath())){
//	    	Intent myIntent = new Intent();
//			myIntent.putExtra("filePath", "");
//			setResult(RESULT_CANCELED, myIntent);
//			finish();
//		}
//		else
//		{
//			browseTo(currentDirectory.getParentFile());
//		}
//	}
//	
//	public void onCreate(Bundle icicle){
//		super.onCreate(icicle);
//		iLoader = new ImageLoader(FileBrowser.this);	
//    	Intent myIntent = new Intent();
//		myIntent.putExtra("filePath", "");
//		//Intent.ACTION_PICK, Uri.parse("file://" + aDirectory.getAbsolutePath()));
//		setResult(RESULT_CANCELED, myIntent);
//		
//		//Log.e("FileBrowser","Reached ACtivbity");
//		String mediaState = Environment.getExternalStorageState();
//		if(mediaState.contains(Environment.MEDIA_MOUNTED) 
//				&& !mediaState.contains(Environment.MEDIA_REMOVED)
//				&& !mediaState.contains(Environment.MEDIA_NOFS)
//				&& !mediaState.contains(Environment.MEDIA_UNMOUNTED)
//				&& !mediaState.contains(Environment.MEDIA_SHARED)){
//			//Log.e("FileBrowser","MEDIA STATE OK");
//			currentDirectory = Environment.getExternalStorageDirectory();
//			root = Environment.getExternalStorageDirectory();
//			browseToRoot();
//			
//		}
//		else{
//			//Log.e("FileBrowser","MEDIA STATE FAIL");
//			setResult(RESULT_CANCELED);
//			finish();
//		}
//	}
//	
//	private void browseToRoot() {
//		browseTo(Environment.getExternalStorageDirectory());
//    }
//	
//	private void upOneLevel(){
//		if(this.currentDirectory.getParent() != null)
//			this.browseTo(this.currentDirectory.getParentFile());
//	}
//
//	private void browseTo(final File aDirectory){
//		try{
//			if (aDirectory.isDirectory()){
//				this.currentDirectory = aDirectory;
//				FileFilter fileFilter = new FileFilter() {
//					
//					@Override
//					public boolean accept(File file) {
//						if(file.isDirectory() || file.getName().matches("\\S*jpg|\\S*jpeg|\\S*png|\\S*gif")){
//							return true;
//						}
//						return false;
//					}
//				};
//				fill(aDirectory.listFiles(fileFilter));
//			}else{
//				OnClickListener okButtonListener = new OnClickListener(){
//					//@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						//Log.e("filebrowser","OK button clicked");
//						Intent myIntent = new Intent();
//						myIntent.putExtra("filePath", aDirectory.getAbsolutePath());
//						//Intent.ACTION_PICK, Uri.parse("file://" + aDirectory.getAbsolutePath()));
//						setResult(RESULT_OK, myIntent);
//						//Log.e("filebrowser","SetIntent");
//						finish();
//					}
//				};
//				OnClickListener cancelButtonListener = new OnClickListener(){
//					//@Override
//					public void onClick(DialogInterface dialog, int which) {
//						
//					}
//				};
//				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//			    View layout = inflater.inflate(R.layout.fileopenpanel,null);
//				AlertDialog.Builder baller = new AlertDialog.Builder(this);
//				baller	.setTitle("File Browser")
//					//.setMessage("Open n"+aDirectory.getName()+"?")
//						.setView(layout)
//						.setPositiveButton("OK", okButtonListener)
//						.setNegativeButton("Cancel", cancelButtonListener);
//				delicious = baller.create();
//				delicious.setOnShowListener(new OnShowListener() {
//					
//					@Override
//					public void onShow(DialogInterface dialog) {
//						TextView tv = (TextView)delicious.findViewById(R.id.fileOpenText);
//						ImageView iv = (ImageView)delicious.findViewById(R.id.fileOpenImage);
//						if(tv!=null){
//							tv.setText("Open n"+aDirectory.getName()+"?");
//						}
//						if(iv!=null){
//							if(aDirectory.isFile()){
//								ImageLoader iLoader = new ImageLoader(FileBrowser.this);					
//								iv.setImageBitmap(iLoader.decodeFile(aDirectory, 250));
//							}
//						}
//						
//					}
//				});
//				delicious.show();
//			}
//		}catch(Exception e){
//			Toast.makeText(FileBrowser.this, "Error in opening directory", Toast.LENGTH_SHORT).show();
//		}
//	}
//	
//	 private void fill(File[] files) {
//         this.directoryEntries.clear();
//          
//         // Add the "." and the ".." == 'Up one level'
//         try {
//              Thread.sleep(10);
//         } catch (InterruptedException e1) {
//              e1.printStackTrace();
//         }
//         this.directoryEntries.add(".");
//          
//         if(this.currentDirectory.getParent() != null || this.currentDirectory.getParent() != root.getParent())
//              this.directoryEntries.add("..");
//          
//         for (File file : files){
//         	 this.directoryEntries.add(file.getPath());
//         }
//
//
//         ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this, R.layout.filerow, this.directoryEntries){
//        	 @Override
//             public View getView(int position, View convertView, ViewGroup parent) {
//        		 View row = null;
//        		 
//        		 if(convertView == null){
//        			 LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        			 row = inflater.inflate(R.layout.filerow, null);
//        		 }else{
//        			 row = convertView;
//        		 }
//        		 String path = getItem(position);
//        		 TextView text = (TextView) row.findViewById(R.id.fileText);
//        		 text.setText(path.subSequence(((path.length()-40)<0)?0:(path.length()-40), path.length()));
//        		 ImageView image = (ImageView) row.findViewById(R.id.fileImage);
//        		 BitmapFactory.Options o = new BitmapFactory.Options();
//                 o.inJustDecodeBounds = true;
//                 o.inPurgeable = true;
//                 
//                 File img = new File(path);
//                 //Log.e("FileBrowser",path);
//                 if(img.isFile()){
//                	 image.setImageBitmap(iLoader.decodeFile(img, 45));
//                 }
//                 else{
//                	 image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.blackdot, o));
//                 }
//        		 return row;
//        	 }
//         };
//         this.setListAdapter(directoryList);
//	 }
//	 
//         @Override
//         protected void onListItemClick(ListView l, View v, int position, long id) {
//              //int selectionRowID = (int) this.getSelectedItemId();
//              //String selectedFileString = this.directoryEntries.get(selectionRowID);
//        	 int selectionRowID = position;
//             String selectedFileString = this.directoryEntries.get(position);
//              if (selectedFileString.equals(".")) {
//                   // Refresh
//                   this.browseTo(this.currentDirectory);
//              } else if(selectedFileString.equals("..")){
//                   this.upOneLevel();
//              } else {
//                   File clickedFile = new File(this.directoryEntries.get(selectionRowID));
//                   if(clickedFile != null) this.browseTo(clickedFile);
//              } 
//         }
//    } 

