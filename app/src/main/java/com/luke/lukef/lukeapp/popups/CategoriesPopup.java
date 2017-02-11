/*
        BalticApp, for studying and tracking the condition of the Baltic sea
        and Gulf of Finland throug user submissions.
        Copyright (C) 2016  Daniel Zakharin, LuKe

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/> or
        the beginning of MainActivity.java file.

*/

package com.luke.lukef.lukeapp.popups;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.SessionSingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the popup that is show when user clicks on categories button from NewSubmissionFragment
 */
public class CategoriesPopup {
    private static final String TAG = "CategoriesPopup";
    private final Dialog dialog;
    private final MainActivity mainActivity;
    private ListView listView;
    private ImageButton acceptButton;
    private AdapterView.OnItemClickListener onClickListener;
    private ArrayList<Category> confirmedCategories;
    private View.OnClickListener buttonListener;
    private DialogInterface.OnCancelListener onCancelListener;

    /**
     * Constructor for creating a pop up
     *
     * @param mainActivity        To pass the context
     * @param onItemClickListener To listen to clicks on the list of submissions
     * @param buOnClickListener   To listen to clicks on the accept button
     * @param confirmedCategories List of currently confirmed categories to add to the submission
     * @param onCancelListener    Listener to listen for cancel events, like clicking outside of the popup or the back button
     */
    public CategoriesPopup(MainActivity mainActivity, AdapterView.OnItemClickListener onItemClickListener, View.OnClickListener buOnClickListener,
                           ArrayList<Category> confirmedCategories, DialogInterface.OnCancelListener onCancelListener) {
        this.mainActivity = mainActivity;
        this.dialog = new Dialog(mainActivity);
        this.onClickListener = onItemClickListener;
        this.buttonListener = buOnClickListener;
        this.confirmedCategories = confirmedCategories;
        this.onCancelListener = onCancelListener;
    }

    /**
     * Setup the elements for the pop up, like click and cancel listeners
     */
    public void setupCategoriesPopup() {
        // Include dialog.xml file
        this.dialog.setContentView(R.layout.popup_categories);
        // setup ListView
        this.listView = (ListView) this.dialog.findViewById(R.id.categoriesListView);
        this.listView.setAdapter(new ListViewAdapter(this.mainActivity, R.layout.popup_categories_item, SessionSingleton.getInstance().getCategoryList()));
        this.acceptButton = (ImageButton) this.dialog.findViewById(R.id.categories_accept_button);
        // set click listeners
        this.acceptButton.setOnClickListener(this.buttonListener);
        this.listView.setOnItemClickListener(onClickListener);
        // set on cancel listener
        this.dialog.setOnCancelListener(this.onCancelListener);
        this.dialog.show();
    }

    /**
     * Dismisses the popup
     */
    public void dismissCategoriesPopup() {
        this.dialog.dismiss();
    }

    /**
     * Custom adapter for the ListView of the categories, handles populating the list view and marking correct
     * categories as selected based on the <code>confirmedCategories</code> ArrayList.
     */
    public class ListViewAdapter extends ArrayAdapter<Category> {
        LayoutInflater layoutInflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ListViewAdapter(Context context, int resource, List<Category> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = layoutInflater.inflate(R.layout.popup_categories_item, parent, false);
            }
            ((TextView) v.findViewById(R.id.popup_category_title)).setText(SessionSingleton.getInstance().getCategoryList().get(position).getTitle());
            ((TextView) v.findViewById(R.id.popup_category_description)).setText(SessionSingleton.getInstance().getCategoryList().get(position).getDescription());
            ((ImageView) v.findViewById(R.id.popup_category_image)).setImageBitmap(SessionSingleton.getInstance().getCategoryList().get(position).getImage());
            if (confirmedCategories.contains(SessionSingleton.getInstance().getCategoryList().get(position))) {
                ((CheckBox) v.findViewById(R.id.popup_categories_checkbox)).setChecked(true);
            } else {
                ((CheckBox) v.findViewById(R.id.popup_categories_checkbox)).setChecked(false);
            }
            ArrayList<Category> categories = SessionSingleton.getInstance().getCategoryList();
            if(SessionSingleton.getInstance().getCategoryList().get(position).getPositive() == Boolean.TRUE){
                v.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.category_popup_background_positivie));
            }else if(SessionSingleton.getInstance().getCategoryList().get(position).getPositive() == Boolean.FALSE) {
                v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.category_popup_background_negative));
            }else {

            }
            return v;
        }
    }
}
