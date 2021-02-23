package com.example.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import java.util.Timer;
import java.util.TimerTask;
import androidx.appcompat.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.appcompat.widget.SearchView;
import org.json.JSONObject;
import org.json.JSONArray;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter arrayAdapter;
    TextView footerID;
    TextView netWorthID;
    SharedPreferences sharedPreferencesPorfolio;
    SharedPreferences.Editor editor;
    String portfolioList;
    float networth;
    final Handler handler = new Handler();
    float stockAmount;
    List<String> stringList;
    WatchlistA wlAdapter;
    SharedPreferences sharedPreferences;
    public static final String watchlistTickersToBeSent = "watchlistTickersToBeSent";
    SharedPreferences.Editor editorW;
    String watchList;
    String results;
    RecyclerView portfolioRV;
    RelativeLayout spinner_objectID;
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    TextView todayID;
    TimerTask timerTask;
    PortfolioA pfAdapter;
    float liquidAmount;
    RecyclerView watchlistRV;
    private ProgressBar spinner;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        spinner = (ProgressBar)findViewById(R.id.progressBarID);
        spinner_objectID = (RelativeLayout) findViewById(R.id.spinner_objectID);
        spinner.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
                spinner_objectID.setVisibility(View.GONE);
            }
        }, 2000 );

        Instant currUTC = Instant.now();
        ZoneId dateToday = ZoneId.of("PST");
        ZonedDateTime currDate = ZonedDateTime.ofInstant(currUTC, dateToday);
        int day = currDate.getDayOfMonth();
        int year = currDate.getYear();
        String month = String.valueOf(currDate.getMonth());
        String m = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
        todayID = (TextView) findViewById(R.id.todayID);
        todayID.setText(m+" "+String.valueOf(day)+", "+String.valueOf(year));

        sharedPreferencesPorfolio = getSharedPreferences("DataPortfolio",Context.MODE_APPEND);
        editor = sharedPreferencesPorfolio.edit();
        if(sharedPreferencesPorfolio.contains("liquidAmount"))
        {
            liquidAmount = sharedPreferencesPorfolio.getFloat("liquidAmount", liquidAmount);
        }
        else {
            liquidAmount = 20000;
            editor.putFloat("liquidAmount",liquidAmount);
            editor.commit();
        }
        sharedPreferences = getSharedPreferences("WatclistData", Context.MODE_APPEND);
        editorW = sharedPreferences.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\"><b>" + getString(R.string.app_name) + "</b></font>"));

        sharedPreferences = getSharedPreferences("WatclistData", Context.MODE_APPEND);
        if(sharedPreferences.contains(watchlistTickersToBeSent))
        {
            watchList = sharedPreferences.getString(watchlistTickersToBeSent,null);
        }
        footerID = (TextView) findViewById(R.id.footerID);
        footerID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiingo.com/"));
                startActivity(intent);
            }
        });

        watchlistRV = findViewById(R.id.watchlistRV);
        //watchlistRecyclerViewer();
        portfolioRV = findViewById(R.id.portfolioRV);
        //portfolioRecyclerViewer();

        startTimer();
        stockAmount = sharedPreferencesPorfolio.getFloat("StockAmount", 0);
        netWorthID = (TextView) findViewById(R.id.netWorthID);
        networth = stockAmount + liquidAmount;
        netWorthID.setText(String.valueOf(df2.format(stockAmount + liquidAmount)));
    }

    public void startTimer() {
        Timer timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, 15000);
    }

    public void initializeTimerTask()
    {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        stockAmount = sharedPreferencesPorfolio.getFloat("StockAmount", 0);
                        liquidAmount = sharedPreferencesPorfolio.getFloat("liquidAmount", 0);
                        netWorthID = (TextView) findViewById(R.id.netWorthID);
                        netWorthID.setText(String.valueOf(df2.format(stockAmount + liquidAmount)));
                        Log.i("Fetching Data for Portfolio and Watchlist every 15 secs","true");
                        portfolioRecyclerViewer();

                        watchlistRecyclerViewer();
                    }

                });
            }
        };
    }

    @Override
    public void onRestart() {
        super.onRestart();
        watchlistRV = findViewById(R.id.watchlistRV);
        watchlistRecyclerViewer();
        portfolioRV = findViewById(R.id.portfolioRV);
        portfolioRecyclerViewer();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search_icon);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_icon).getActionView();

        final SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getApplicationContext(), DetailsPage.class);
                intent.putExtra("message_key", results);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()>=3)
                {
                    if (!TextUtils.isEmpty(searchAutoComplete.getText())) {
                        stringList = new ArrayList<>();
                        stringList.clear();
                        String TAG = "Check API Value";
                        ApiCall.make(MainActivity.this, newText, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject responseObject = new JSONObject(response);
                                    JSONArray array = responseObject.getJSONArray("data1");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject row = array.getJSONObject(i);
                                        stringList.add(row.getString("ticker") + " - " + row.getString("name"));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, stringList);
                                searchAutoComplete.setAdapter(arrayAdapter);
                                arrayAdapter.notifyDataSetChanged();


                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("Volley Error","Error");
                            }
                        });
                    }
                }
                else
                {
                    return false;
                }
                return true;
            }
        });

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String myItem=(String)adapterView.getItemAtPosition(i);
                searchAutoComplete.setText("" + myItem);
                results = "" + myItem;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    private void portfolioRecyclerViewer() {

        portfolioList = sharedPreferencesPorfolio.getString("portfolioTickersToBeSent", null);
        if (portfolioList != null) {
            ArrayList<String> ticks = new ArrayList<>(Arrays.asList(portfolioList.split(",")));
            ticks.remove("null");
            pfAdapter = new PortfolioA(MainActivity.this, ticks);
            portfolioRV.setAdapter(pfAdapter);
            ItemTouchHelper.Callback callback2 = new PItemMoveCB((PItemMoveCB.ItemTouchHelperContract) pfAdapter, MainActivity.this) {
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    pfAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                    ArrayList<String> ticks = pfAdapter.getData();
                    String tempPortfolio = "null";
                    for (int i = 0; i < ticks.size(); i++) {
                        tempPortfolio += "," + ticks.get(i);
                    }
                    editor.putString("portfolioTickersToBeSent", tempPortfolio);
                    editor.commit();
                    return true;
                }

                @Override
                public boolean isItemViewSwipeEnabled() {
                    return false;
                }

                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlag = 0;
                    return makeMovementFlags(dragFlags, swipeFlag);
                }
            };
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback2);
            touchHelper.attachToRecyclerView(portfolioRV);
        }
    }


    private void watchlistRecyclerViewer() {

        watchList = sharedPreferences.getString(watchlistTickersToBeSent, null);
        if (watchList != null) {

            ArrayList<String> ticks = new ArrayList<>(Arrays.asList(watchList.split(",")));
            ticks.remove("null");

            wlAdapter = new WatchlistA(MainActivity.this, ticks);
            watchlistRV.setAdapter(wlAdapter);

            ItemTouchHelper.Callback callback = new WItemMoveCB((WItemMoveCB.ItemTouchHelperContract) wlAdapter, MainActivity.this) {
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                    final int position = viewHolder.getAdapterPosition();
                    final String tk = wlAdapter.getData().get(position);
                    String tempWatchlist = wlAdapter.removeItem(position);

                    editorW.putString(watchlistTickersToBeSent, tempWatchlist);
                    editorW.remove(tk);
                    editorW.commit();
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,RecyclerView.ViewHolder target) {
                    wlAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    ArrayList<String> ticks = wlAdapter.getData();
                    String tempWatchlist = "null";
                    for (int i = 0; i < ticks.size(); i++) {
                        tempWatchlist += "," + ticks.get(i);
                    }
                    editorW.putString(watchlistTickersToBeSent, tempWatchlist);
                    editorW.commit();
                    return true;
                }
                @Override
                public boolean isItemViewSwipeEnabled() {
                    return true;
                }

                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlag = ItemTouchHelper.LEFT;
                    return makeMovementFlags(dragFlags, swipeFlag);
                }
            };
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(watchlistRV);
        }
    }

}
