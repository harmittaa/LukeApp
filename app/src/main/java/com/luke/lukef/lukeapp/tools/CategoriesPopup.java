package com.luke.lukef.lukeapp.tools;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.SessionSingleton;

import java.util.ArrayList;
import java.util.List;

public class CategoriesPopup {
    private static final String TAG = "CategoriesPopup";
    private final Dialog dialog;
    private final MainActivity mainActivity;
    private ListView listView;
    private Button acceptButton;
    private AdapterView.OnItemClickListener onClickListener;
    private ArrayList<Category> confirmedCategories;
    private View.OnClickListener buttonListener;
    private DialogInterface.OnCancelListener onCancelListener;

    private View.OnClickListener clickListener;

    public CategoriesPopup(MainActivity mainActivity, AdapterView.OnItemClickListener onItemClickListener, View.OnClickListener buOnClickListener,
                           ArrayList<Category> confirmedCategories, DialogInterface.OnCancelListener onCancelListener) {
        this.mainActivity = mainActivity;
        this.dialog = new Dialog(mainActivity);
        this.onClickListener = onItemClickListener;
        this.buttonListener = buOnClickListener;
        this.confirmedCategories = confirmedCategories;
        // copy of the original
        this.onCancelListener = onCancelListener;
    }


    public void setupCategoriesPopup() {
        // Include dialog.xml file
        this.dialog.setContentView(R.layout.popup_categories);
        // setup RecyclerView
        this.listView = (ListView) this.dialog.findViewById(R.id.categoriesListView);

        this.listView.setAdapter(new ListViewAdapter(this.mainActivity, R.layout.popup_categories_item, SessionSingleton.getInstance().getCategoryList()));


        // find views
        this.acceptButton = (Button) this.dialog.findViewById(R.id.categories_accept_button);
        // set click listeners
        this.acceptButton.setOnClickListener(this.buttonListener);
        //this.cancelButton.setOnClickListener(this.buttonListener);
        this.listView.setOnItemClickListener(onClickListener);

        this.dialog.setOnCancelListener(this.onCancelListener);
        Log.e(TAG, "makeCategoryListPopup: confirmed size in popup" + this.confirmedCategories.size());

        this.dialog.show();
    }

    public void dismissCategoriesPopup() {
        this.dialog.dismiss();
    }


    public class ListViewAdapter extends ArrayAdapter<Category> {
        LayoutInflater make = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ListViewAdapter(Context context, int resource, List<Category> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = make.inflate(R.layout.popup_categories_item, parent, false);
            }
            ((TextView) v.findViewById(R.id.popup_category_title)).setText(SessionSingleton.getInstance().getCategoryList().get(position).getTitle());
            ((TextView) v.findViewById(R.id.popup_category_description)).setText(SessionSingleton.getInstance().getCategoryList().get(position).getDescription());
            ((ImageView) v.findViewById(R.id.popup_category_image)).setImageBitmap(SessionSingleton.getInstance().getCategoryList().get(position).getImage());
            //((CheckBox) v.findViewById(R.id.popup_categories_checkbox)).setOnCheckedChangeListener(checkedChangedListener);
            if (confirmedCategories.contains(SessionSingleton.getInstance().getCategoryList().get(position))) {
                ((CheckBox) v.findViewById(R.id.popup_categories_checkbox)).setChecked(true);
            } else  {
                ((CheckBox) v.findViewById(R.id.popup_categories_checkbox)).setChecked(false);
            }
            return v;
        }
    }
}
