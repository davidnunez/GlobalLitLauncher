package edu.media.mit.prg.launcher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import edu.media.mit.prg.launcher.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    private String TAG = "edu.media.mit.prg.launcher";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = false;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private float mx, my;
    private float curX, curY;

    public IconAdapter iconAdapter;

    private GestureDetectorCompat gDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);
        //setContentView(R.layout.main);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.scrolling_view);
        final TextView textView = ((TextView) findViewById(R.id.manifest));

        iconAdapter = new IconAdapter(this);
        loadManifest(textView);
        setupGridView();
        setupSecrets();

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });
        mSystemUiHider.show();

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    private void loadManifest(TextView textView) {
        Log.i(TAG, "apps.json should go here: " + Environment.getExternalStorageDirectory());
        textView.setText("Couldn't find manifest: " + Environment.getExternalStorageDirectory() + "/launcher/apps.json");

        File sdcard = Environment.getExternalStorageDirectory();

        //Get the text file
        File file = new File(sdcard + "/launcher", "apps.json");

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            textView.setText(text);


            Object obj = JSONValue.parse(text.toString());
            JSONObject JSONMain = (JSONObject) obj;
            JSONArray array = (JSONArray) (JSONMain.get("apps"));

            for (Iterator<JSONObject> iter = array.iterator(); iter.hasNext(); ) {
                JSONObject element = iter.next();
                AppModel title = new AppModel();
                title.title = (String) element.get("title");
                title.file = (String) element.get("file");
                title.type = (String) element.get("type");
                //title.bounce = (Float)element.get("bounce");
                //title.scale = (Float)element.get("scale");

                iconAdapter.titles.add(title);
            }
        } catch (IOException e) {
            //You'll need to add proper error handling here
            textView.setText("Couldn't find manifest: " + Environment.getExternalStorageDirectory() + "/launcher/apps.json");
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    public void onClickHideStatus(View view) {
        View settingsPane = findViewById(R.id.settings_pane);
        settingsPane.setVisibility(View.INVISIBLE);
    }

    public void onClickSettings(View view) {
        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.android.settings");
        startActivity(LaunchIntent);
    }


    public void onClickPasswordManager(View view) {
        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.carrotapp.protect");
        startActivity(LaunchIntent);
    }

    public void onClickFunfFileMover(View view) {
        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("edu.mit.media.prg.funffilemover");
        startActivity(LaunchIntent);
    }


    public void setupGridView() {

        if (iconAdapter.titles.size() == 1) {
            launchActivity(0);
            finish();
        }
        GridView gridView = (GridView) findViewById(R.id.gridView);


        gridView.setAdapter(iconAdapter);

        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                launchActivity(position);
            }
        });
    }

    public void launchActivity(int position) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);

            String type = iconAdapter.titles.get(position).type;
            if (type.equals("video") || type.equals("film") || type.equals("story") || type.equals("music")) {
////        	sendLog("LauncherApp", iconAdapter.titles[position][1]);
//            String movieurl = "file://mnt/sdcard/Movies/"+iconAdapter.titles[position][1];
//            Uri myUri = Uri.parse(movieurl); // initialize Uri here
//            intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(myUri, "video/mp4");
//            startActivity(intent);
            } else if (type.equals("app")) {
            	sendLog("LauncherApp", iconAdapter.titles.get(position).file);
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(iconAdapter.titles.get(position).file);
                startActivity(LaunchIntent);
            }
        } catch (Exception e) {

        }
    }

    private boolean secret1_value = false;
    private boolean secret2_value = false;
    private boolean secret3_value = false;

    private void setupSecrets() {
        final Button secret1 = (Button) findViewById(R.id.secret1);
        secret1.setVisibility(View.VISIBLE);
        secret1.setBackgroundColor(Color.TRANSPARENT);
        secret1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "secret1");
                secret1_value = true;
                secret2_value = false;
                secret3_value = false;
                playSound();
            }
        });


        Button secret2 = (Button) findViewById(R.id.secret2);
        secret2.setVisibility(View.VISIBLE);
        secret2.setBackgroundColor(Color.TRANSPARENT);
        secret2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "secret2");
                if (secret1_value && !secret2_value && !secret3_value) {
                    secret2_value = true;
                    playSound();
                } else {
                    secret1_value = false;
                    secret2_value = false;
                    secret3_value = false;
                }

            }
        });


        Button secret3 = (Button) findViewById(R.id.secret3);
        secret3.setVisibility(View.VISIBLE);
        secret3.setBackgroundColor(Color.TRANSPARENT);
        secret3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "secret3");
                if (secret1_value && secret2_value && !secret3_value) {
                    View settingsPane = findViewById(R.id.settings_pane);
                    settingsPane.setVisibility(View.VISIBLE);
                    playSound();
                }

                secret1_value = false;
                secret2_value = false;
                secret3_value = false;

            }
        });


    }

    void playSound() {

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static final String DATABASE_NAME_KEY = "DATABASE_NAME";
    public static final String NAME_KEY = "NAME";
    public static final String VALUE_KEY = "VALUE";
    public static final String TIMESTAMP_KEY = "TIMESTAMP";
    public static final String ACTION_RECORD = "edu.mit.media.funf.RECORD";
    public void sendLog(String name, String value) {
        Intent i = new Intent();
        i.setAction(ACTION_RECORD);
        Bundle b = new Bundle();
        b.putString(DATABASE_NAME_KEY, "mainPipeline");
        b.putLong(TIMESTAMP_KEY, System.currentTimeMillis()/1000);
        b.putString(NAME_KEY, name);
        b.putString(VALUE_KEY, value);
        i.putExtras(b);
        sendBroadcast(i);
        Log.i(TAG, "Funf Record: " + name + " = " + value);
    }

}
