package edu.media.mit.prg.launcher;


//import edu.mit.media.funf.storage.DatabaseService;
//import edu.mit.media.funf.storage.NameValueDatabaseService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.util.Log;


abstract public class LauncherActivity extends Activity {
	private static final String TAG ="AppLauncher";
	public IconAdapter iconAdapter;
	private static final boolean cameraEnabled = false;
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        iconAdapter = new IconAdapter(this);
        setupActivities();
        setContentView(R.layout.activity_fullscreen);
        setupGridView();
      
    }

	public void onPause() {
		Log.v(TAG, "onPause");

		super.onPause();
		//finish();
	}
	
	public void onResume() {
			Log.v(TAG, "OnResume");
		super.onResume();
	}
	
	public void setupGridView() {
		
		if (iconAdapter.titles.size() == 1) {
			launchActivity(0);
			finish();
		}
        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(iconAdapter);

        gridView.setOnItemClickListener(new GridView.OnItemClickListener() 
        {
            public void onItemClick(AdapterView parent, View v, int position, long id) 
            {                
            	launchActivity(position);
            }
        });  
	}
	
	public void launchActivity(int position) {
       	Intent intent = new Intent(Intent.ACTION_MAIN);
       	if (cameraEnabled) {
       		intent.setComponent(new ComponentName("com.android.camerarecorder","com.android.camerarecorder.CameraRecorder"));
       		startActivity(intent);
       		try {
       			Thread.sleep(1000);
       		} catch (Exception e) {

       		}
       	}
    	String type = iconAdapter.titles.get(position).type;
    	if (type.equals("video") || type.equals("film") || type.equals("story") || type.equals("music")) {
//        	sendLog("LauncherApp", iconAdapter.titles[position][1]);
        	String movieurl = "file://mnt/sdcard/Movies/"+iconAdapter.titles.get(position).file;
        	Uri myUri = Uri.parse(movieurl); // initialize Uri here
        	intent = new Intent(Intent.ACTION_VIEW);
        	intent.setDataAndType(myUri, "video/mp4");
        	startActivity(intent);
    	} else if(type.equals("app")) {
//        	sendLog("LauncherApp", iconAdapter.titles[position][1]);
        	Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(iconAdapter.titles.get(position).file);
        	startActivity( LaunchIntent );
    	}
	}

	
//    public void sendLog(String name, String value) {
//        Intent i = new Intent();
//    	i.setAction(DatabaseService.ACTION_RECORD);
//    	Bundle b = new Bundle();
//    	b.putString(NameValueDatabaseService.DATABASE_NAME_KEY, "mainPipeline");
//    	b.putLong(NameValueDatabaseService.TIMESTAMP_KEY, System.currentTimeMillis()/1000);
//    	b.putString(NameValueDatabaseService.NAME_KEY, name);
//    	b.putString(NameValueDatabaseService.VALUE_KEY, value);
//    	i.putExtras(b);
//    	sendBroadcast(i);
//    }
    
    
    abstract public void setupActivities();
    
    
}    
    
    
