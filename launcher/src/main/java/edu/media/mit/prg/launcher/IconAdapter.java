package edu.media.mit.prg.launcher;

import android.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.LayoutInflater;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.util.Log;

import java.util.ArrayList;

public class IconAdapter extends BaseAdapter {
    String TAG = "edu.media.mit.prg.launcher";
    private Context context;

    public IconAdapter(Context c) {
        context = c;
    }

    public int getCount() {
        return titles.size();
    }

    //---returns the ID of an item--- 
    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        View v = new View(context);// convertView;
        if (titles.get(position).type.equals("app")) {
            if (convertView == null) {
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.icon, null);
            } else {
                v = convertView;
            }

            try {

                PackageManager pk = context.getPackageManager();

                //TextView tv = (TextView)v.findViewById(R.id.icon_text);

                PackageInfo info = pk.getPackageInfo(titles.get(position).file, 0);
                Log.v("appsLauncher", pk.getPackageInfo(titles.get(position).file, 0).toString());
                String label = pk.getApplicationLabel(info.applicationInfo).toString();

                //tv.setText(label);
                ImageView iv = (ImageView) v.findViewById(R.id.icon_image);
                android.graphics.drawable.Drawable drawable = pk.getApplicationIcon(titles.get(position).file);
                iv.setImageDrawable(drawable);
                Log.v("appsLauncher", titles.get(position).file + " size: " + pk.getApplicationIcon(titles.get(position).file).getIntrinsicWidth());

            } catch (Exception e) {
                Log.v("appsLauncher", e.toString());
                //TextView tv = (TextView)v.findViewById(R.id.icon_text);
                //tv.setText(titles[position][1]);
                ImageView iv = (ImageView) v.findViewById(R.id.icon_image);
                iv.setImageResource(R.drawable.missing_app_icon);
            } finally {
                ImageView iv = (ImageView) v.findViewById(R.id.icon_image);
                Float scale = titles.get(position).scale;
                int h = (int) (iv.getLayoutParams().height * scale);
                int w = (int) (iv.getLayoutParams().width * scale);
                iv.setLayoutParams(new LinearLayout.LayoutParams(w, h));
            }


        }

        return v;

    }

    public ArrayList<AppModel> titles = new ArrayList<AppModel>();


}
