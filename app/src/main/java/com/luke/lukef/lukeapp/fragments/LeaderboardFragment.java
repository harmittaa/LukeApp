package com.luke.lukef.lukeapp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;

import java.util.ArrayList;
import java.util.Arrays;


public class LeaderboardFragment extends Fragment implements View.OnClickListener {
    private View fragmentView;
    Button leaderboardMapButton;
    Button userProfileButton;
    private String[] listview_names =  {"User Name"};
    static Context mcontext;
    private ListView lv;
    private static ArrayList<String> array_sort;
    private static ArrayList<Integer> image_sort;
    private static int[] listview_images = {};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        leaderboardMapButton = (Button) fragmentView.findViewById(R.id.leaderboard_map_button);
        userProfileButton = (Button) fragmentView.findViewById(R.id.user_profile_button);
        setupButtons();
        return fragmentView;

        lv = (ListView) fragmentView.findViewById(R.id.list);
        array_sort = new ArrayList<String>(Arrays.asList(listview_names));
        image_sort = new ArrayList<Integer>();
        for (int index = 0; index < listview_images.length; index++) {
            image_sort.add(listview_images[index]);
        }

        lv.setListAdapter(new bsAdapter(this));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0,
                                    View arg1, int position, long arg3) {
                Toast.makeText(getActivity().getApplicationContext(), array_sort.get(position),
                        Toast.LENGTH_SHORT).show();
            }
        });
        class bsAdapter extends BaseAdapter {
            Activity cntx;

            public bsAdapter(Activity context) {
                // TODO Auto-generated constructor stub
                this.cntx = context;
            }

            public int getCount() {
                // TODO Auto-generated method stub
                return array_sort.size();
            }

            public Object getItem(int position) {
                // TODO Auto-generated method stub
                return array_sort.get(position);
            }

            public long getItemId(int position) {
                // TODO Auto-generated method stub
                return array_sort.size();
            }

            public View getView(final int position, View convertView, ViewGroup parent) {
                View row = null;
                LayoutInflater inflater = cntx.getLayoutInflater();
                row = inflater.inflate(R.layout.list_item_leaderboard, null);
                TextView tv = (TextView) row.findViewById(R.id.title);
                ImageView im = (ImageView) row.findViewById(R.id.imageview);
                tv.setText(array_sort.get(position));

                im.setImageBitmap(getRoundedShape(decodeFile(cntx, listview_images[position]), 200));

                return row;
            }

            public Bitmap decodeFile(Context context, int resId) {
                try {
// decode image size
                    mcontext = context;
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    BitmapFactory.decodeResource(mcontext.getResources(), resId, o);
// Find the correct scale value. It should be the power of 2.
                    final int REQUIRED_SIZE = 200;
                    int width_tmp = o.outWidth, height_tmp = o.outHeight;
                    int scale = 1;
                    while (true) {
                        if (width_tmp / 2 < REQUIRED_SIZE
                                || height_tmp / 2 < REQUIRED_SIZE)
                            break;
                        width_tmp /= 2;
                        height_tmp /= 2;
                        scale++;
                    }
// decode with inSampleSize
                    BitmapFactory.Options o2 = new BitmapFactory.Options();
                    o2.inSampleSize = scale;
                    return BitmapFactory.decodeResource(mcontext.getResources(), resId, o2);
                } catch (Exception e) {
                }
                return null;
            }
        //}
    //}
    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage, int width) {
        // TODO Auto-generated method stub
        int targetWidth = width;
        int targetHeight = width;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);
        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth,
                        targetHeight), null);
        return targetBitmap;
    }





    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.leaderboard_map_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP);
                break;
            case R.id.user_profile_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_PROFILE);
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        leaderboardMapButton.setOnClickListener(this);
        userProfileButton.setOnClickListener(this);
    }

}
