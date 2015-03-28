package com.klinker.android.twitter.settings.configure_pages;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.setup.LoginActivity;
import com.klinker.android.twitter.utils.Utils;

import org.lucasr.smoothie.AsyncListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Twitter;

public class SearchChooser extends Activity {

    private Context context;
    private AppSettings settings;
    private ActionBar actionBar;

    private AsyncListView listView;
    private SearchChooserArrayAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        settings = AppSettings.getInstance(this);

        Utils.setUpTheme(context, settings);
        setContentView(R.layout.list_chooser);

        actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.lists));

        listView = (AsyncListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String search = adapter.getSearch(i);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("search_query", search);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        new GetLists().execute();
    }

    class GetLists extends AsyncTask<String, Void, ResponseList<SavedSearch>> {

        protected ResponseList<SavedSearch> doInBackground(String... urls) {
            try {
                Twitter twitter =  Utils.getTwitter(context, settings);


                ResponseList<SavedSearch> searches = twitter.getSavedSearches();

                Collections.sort(searches, new Comparator<SavedSearch>() {
                    public int compare(SavedSearch result1, SavedSearch result2) {
                        return result1.getQuery().compareTo(result2.getQuery());
                    }
                });

                return searches;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(ResponseList<SavedSearch> searches) {

            if (searches != null) {

                final ArrayList<String> searchNames = new ArrayList<String>();

                for (SavedSearch sear : searches) {
                    searchNames.add(sear.getQuery());
                }

                adapter = new SearchChooserArrayAdapter(context, searchNames);
                listView.setAdapter(adapter);
                listView.setVisibility(View.VISIBLE);
            }

            LinearLayout spinner = (LinearLayout) findViewById(R.id.list_progress);
            spinner.setVisibility(View.GONE);
        }
    }

}
