package com.example.stocks;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailsPage extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferencesPortfolio;
    public static final String watchlistTickers = "watchlistTickers";
    public static final String watchlistTickersToBeSent = "watchlistTickersToBeSent";
    public static final String portfolioTickersToBeSent = "portfolioTickersToBeSent";

    private boolean isChecked = false;

    String TAG = "Volley Error";
    String ticker;
    TextView tickerID, compNameID, currPriceID, changePriceID, sharesOwnedID, marketValueID, currPrice2ID, lowID, bidPriceID, openPriceID, midPriceID, highPriceID, volumeID, aboutID;
    TextView footerID;
    Button tradeBtnID;
    RequestQueue queue;
    JSONObject companyDescription, companyData, newsData, chartData;
    RelativeLayout spinner_objectID;
    public WebView webView;
    String watchList;
    String portfolioList;
    Float liquidAmount;
    String currentStockPrice;
    RecyclerView newsList;
    TextView showMore;
    TextView showLess;
    private List<NewsDataStructure> allNewsList = new ArrayList<NewsDataStructure>();

    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static DecimalFormat df4 = new DecimalFormat("#.####");
    private ProgressBar spinner;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_page);

        sharedPreferences = getSharedPreferences("WatclistData", Context.MODE_APPEND);
        sharedPreferencesPortfolio = getSharedPreferences("DataPortfolio", Context.MODE_APPEND);

        if(sharedPreferences.contains(watchlistTickersToBeSent))
        {
            watchList = sharedPreferences.getString(watchlistTickersToBeSent,null);
        }

        if(sharedPreferencesPortfolio.contains("liquidAmount"))
        {
            liquidAmount = sharedPreferencesPortfolio.getFloat("liquidAmount", (float) 20000.0);
        }


        if(sharedPreferencesPortfolio.contains(portfolioTickersToBeSent))
        {
            portfolioList = sharedPreferencesPortfolio.getString(portfolioTickersToBeSent,null);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\"><b>" + getString(R.string.app_name) + "</b></font>"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spinner = (ProgressBar)findViewById(R.id.progressBarID);
        spinner_objectID = (RelativeLayout) findViewById(R.id.spinner_objectID);
        spinner.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
                spinner_objectID.setVisibility(View.GONE);

            }
        }, 2500 );

        Intent intent = getIntent();
        String str = intent.getStringExtra("message_key");
        Log.d("message_key value:", str);
        String[] temp = str.split(" ");
        ticker = temp[0];


        queue = Volley.newRequestQueue(this);
        GetCompanyDetails();
        GetCompanyData();
        GetNews();
        GetChart();

        footerID = (TextView) findViewById(R.id.footerID);
        footerID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiingo.com/"));
                startActivity(intent);
            }
        });

        aboutID = (TextView) findViewById(R.id.aboutID);
        showMore = (TextView) findViewById(R.id.showMore);
        showLess = (TextView) findViewById(R.id.showLess);




        showMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showMore.setVisibility(View.GONE);
                showLess.setVisibility(View.VISIBLE);
                aboutID.setMaxLines(Integer.MAX_VALUE);

            }
        });

        showLess.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showLess.setVisibility(View.GONE);
                showMore.setVisibility(View.VISIBLE);
                aboutID.setMaxLines(2);

            }
        });


    }


    public void GetCompanyDetails() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.0.2.2:3000/details1?ticker=" + ticker;


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    companyDescription = response;
                    tickerID = (TextView) findViewById(R.id.tickerID);
                    compNameID = (TextView) findViewById(R.id.compNameID);
                    aboutID = (TextView) findViewById(R.id.aboutID);

                    tickerID.setText(companyDescription.getString("ticker"));
                    compNameID.setText(companyDescription.getString("name"));
                    aboutID.setText(companyDescription.getString("description"));

                    if(aboutID.getLineCount()<2)
                    {
                        showMore.setVisibility(View.GONE);
                        showLess.setVisibility(View.GONE);
                        aboutID.setGravity(Gravity.CENTER);
                    }
                } catch (JSONException e) { e.printStackTrace(); }


            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.i(TAG, "ERROR::"+error);
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

    }

    public void GetCompanyData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.0.2.2:3000/details2?ticker=" + ticker;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    companyData = response;

                    currPriceID = (TextView) findViewById(R.id.currPriceID);
                    changePriceID = (TextView) findViewById(R.id.changePriceID);
                    sharesOwnedID = (TextView) findViewById(R.id.sharesOwnedID);
                    marketValueID = (TextView) findViewById(R.id.marketValueID);
                    currPrice2ID = (TextView) findViewById(R.id.currPrice2ID);
                    lowID = (TextView) findViewById(R.id.lowID);
                    bidPriceID = (TextView) findViewById(R.id.bidPriceID);
                    openPriceID = (TextView) findViewById(R.id.openPriceID);
                    midPriceID = (TextView) findViewById(R.id.midPriceID);
                    highPriceID = (TextView) findViewById(R.id.highPriceID);
                    volumeID = (TextView) findViewById(R.id.volumeID);

                    String last = companyData.getString("last");
                    if (last == "null")
                    {
                        last = "0.0";
                    }
                    String low = companyData.getString("low");
                    if (low == "null")
                    {
                        low = "0.0";
                    }
                    String bidPrice = companyData.getString("bidPrice");
                    if (bidPrice == "null")
                    {
                        bidPrice = "0.0";
                    }
                    String open = companyData.getString("open");
                    if (open == "null")
                    {
                        open = "0.0";
                    }
                    String mid = companyData.getString("mid");
                    if (mid == "null")
                    {
                        mid = "0.0";
                    }
                    String high = companyData.getString("high");
                    if (high == "null")
                    {
                        high = "0.0";
                    }
                    String volume = companyData.getString("volume");
                    if (volume == "null")
                    {
                        volume = "0.0";
                    }

                    Double change = Double.parseDouble(companyData.getString("last")) - Double.parseDouble(companyData.getString("prevClose"));
                    change = (float)Math.round(change * 100.0)/100.0;
                    if (change > 0)
                    {
                        changePriceID.setText("$" + change.toString());
                        changePriceID.setTextColor(Color.parseColor("#3f925b"));
                    }
                    else if (change < 0)
                    {
                        change = change * -1.0; changePriceID.setText("-$" + change.toString());
                        changePriceID.setTextColor(Color.parseColor("#9b4049"));
                    }
                    else
                        {
                            changePriceID.setText("$" + change.toString()); changePriceID.setTextColor(Color.parseColor("#000000"));
                        }

                    currPriceID.setText("$" + String.valueOf(df2.format(Float.valueOf(last).floatValue())));
                    currPrice2ID.setText("Current Price: " +String.valueOf(df2.format(Float.valueOf(last).floatValue())));
                    lowID.setText("Low: " + String.valueOf(df2.format(Float.valueOf(low).floatValue())));
                    bidPriceID.setText("Bid Price: " + String.valueOf(df2.format(Float.valueOf(bidPrice).floatValue())));
                    openPriceID.setText("OpenPrice: " + String.valueOf(df2.format(Float.valueOf(open).floatValue())));
                    midPriceID.setText("Mid: " + String.valueOf(df2.format(Float.valueOf(mid).floatValue())));
                    highPriceID.setText("High: " + String.valueOf(df2.format(Float.valueOf(high).floatValue())));
                    volumeID.setText("Volume: " + volume);
                    currentStockPrice = last;
                    if(sharedPreferencesPortfolio.contains(ticker))
                    {
                        int z = sharedPreferencesPortfolio.getInt(ticker,0);
                        DecimalFormat df_true = new DecimalFormat("#.0000");
                        sharesOwnedID.setText("Shares owned: " + String.valueOf(df_true.format(z)));
                        float x = z * Float.valueOf(last).floatValue();
                        marketValueID.setText("Market Value: $"+String.valueOf(x));
                    }
                    else {
                        sharesOwnedID.setText("You have 0 shares of " + ticker + ".");
                        marketValueID.setText("Start trading!");
                    }

                } catch (JSONException e) { e.printStackTrace(); }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.i(TAG, "ERROR::"+error);
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));   //Local host timeout issue
        queue.add(jsonObjReq);

    }

    public void GetNews() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.0.2.2:3000/news?ticker=" + ticker;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                newsData = response;
                try {
                    newsList = (RecyclerView) findViewById(R.id.newsList);
                    newsList.setNestedScrollingEnabled(false);
                    newsList.setLayoutManager(new LinearLayoutManager(DetailsPage.this));
                    newsList.addItemDecoration(new DividerItemDecoration(newsList.getContext(),DividerItemDecoration.VERTICAL));
                    JSONArray array = (JSONArray) newsData.get("articles");

                    if (array.length() > 0 ){

                        for (int i = 0; i < array.length(); i++) {

                            if (i == 0) {
                                JSONObject art = array.getJSONObject(i);
                                JSONObject art_child = art.getJSONObject("source");
                                final String title = art.getString("title"); final String source = art_child.getString("name"); final String image = art.getString("urlToImage");
                                final String newsUrl = art.getString("url"); final String time = calculateTime(art.getString("publishedAt"));

                                ImageView news_card_image1 = (ImageView) findViewById(R.id.news_card_image1);
                                TextView news_card_title1 = (TextView) findViewById(R.id.news_card_title1);
                                TextView news_card_time1 = (TextView) findViewById(R.id.news_card_time1);
                                TextView news_card_source1 = (TextView) findViewById(R.id.news_card_source1);

                                news_card_title1.setText(title);
                                news_card_source1.setText(source);

                                String urlVal = image;
                                if (urlVal.equals("") || urlVal.equals("null")) {
                                    Glide.with(news_card_image1.getContext()).load(R.drawable.no_image).into(news_card_image1);
                                }
                                else {
                                    Glide.with(news_card_image1.getContext()).load(urlVal).into(news_card_image1);
                                }

                                news_card_time1.setText(calculateTime(time));

                                final CardView news1Card = (CardView) findViewById(R.id.news1Card);
                                news1Card.setVisibility(View.VISIBLE);
                                news1Card.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsUrl));
                                        startActivity(intent);
                                    }
                                });

                                news1Card.setOnLongClickListener(new View.OnLongClickListener() {

                                    @Override
                                    public boolean onLongClick(View v) {
                                        Dialog dialog = new Dialog(news1Card.getContext());
                                        dialog.setContentView(R.layout.news_popup);

                                        ImageView dial_image, dial_twitter, dial_chrome;
                                        TextView dial_title;

                                        dial_title = dialog.findViewById(R.id.modal_news_title);
                                        dial_image = dialog.findViewById(R.id.modal_news_image);
                                        dial_twitter = dialog.findViewById(R.id.modal_news_twitter);
                                        dial_chrome = dialog.findViewById(R.id.modal_news_chrome);

                                        Glide.with(dial_image.getContext()).load(image).into(dial_image);
                                        dial_title.setText(title);
                                        dial_chrome.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsUrl));
                                                startActivity(intent);
                                            }
                                        });
                                        dial_twitter.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                String twitterShareLink = "https://twitter.com/intent/tweet?&url=Check out this Link: " + newsUrl;
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterShareLink));
                                                startActivity(intent);
                                            }
                                        });
                                        dialog.show();
                                        return true;
                                    }
                                });

                            }
                            else {
                                JSONObject art = array.getJSONObject(i);
                                JSONObject art_child = art.getJSONObject("source");
                                String title = art.getString("title"); String source = art_child.getString("name"); String image = art.getString("urlToImage"); String newsUrl = art.getString("url");
                                String time = calculateTime(art.getString("publishedAt"));

                                NewsDataStructure newz = new NewsDataStructure(title, source, image, newsUrl, time);
                                allNewsList.add(newz);
                            }
                        }


                        newsList.setAdapter(new NewsAdapter(DetailsPage.this, allNewsList));
                    }
                    else {
                        TextView n = (TextView) findViewById(R.id.newsTitle);
                        n.setVisibility(View.INVISIBLE);
                    }


                } catch (JSONException e) { e.printStackTrace(); }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.i(TAG, "ERROR::"+error);
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

    }

    public void GetChart() {
        webView =  (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new CustomWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.loadUrl("file:///android_asset/chart.html");
    }


    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:(function setText() {myFunction('"+ticker+"');})()");
            String newUrl = "javascript:JavaFunct()";
            view.loadUrl(newUrl);
        }
    }
    public String calculateTime(String date) {
        String periodDiffStr = "";

        try{
            LocalDateTime ldt = LocalDateTime.now();
            ZoneId zoneId = ZoneId.of("America/Los_Angeles");
            ZonedDateTime zonedCurrent = ldt.atZone(zoneId);

            Instant timestamp = Instant.parse(date);
            ZonedDateTime zonedArticle = timestamp.atZone(zoneId);

            Duration d = Duration.between(zonedArticle,zonedCurrent);
            Long diff = d.getSeconds();
            if(diff > 3600 * 24)
            {
                Long numdays = diff/(3600 * 24);
                periodDiffStr = numdays + "d ago";
            }
            else if(diff > 3600)
            {
                Long numHours = diff/3600;
                periodDiffStr = numHours + " h ago";

            }
            else if(diff > 60)
            {
                Long numMin = diff/60;
                periodDiffStr = numMin + " m ago";
            }
            else if(diff >= 0){
                periodDiffStr = diff + " s ago";
            }
            else{
                periodDiffStr = "1 h ago";
            }
        }
        catch (Exception e){
            periodDiffStr = "1 h ago";
        }
        return periodDiffStr;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.action_favorite);
        if(sharedPreferences.contains(ticker))
        {
            Drawable myDrawable = getResources().getDrawable(R.drawable.ic_baseline_star_24);
            checkable.setIcon(myDrawable);
            checkable.setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (id == R.id.action_favorite) {
            isChecked = item.isChecked();
            if(!isChecked)
            {

                Drawable myDrawable = getResources().getDrawable(R.drawable.ic_baseline_star_24);
                item.setIcon(myDrawable);
                Toast.makeText(DetailsPage.this, '"'+ticker+'"'+ " was added to favorites", Toast.LENGTH_SHORT).show();
                editor.putBoolean(ticker,true);
                watchList = watchList + "," + ticker;
            }
            else {

                Drawable myDrawable = getResources().getDrawable(R.drawable.ic_baseline_star_border_24);
                item.setIcon(myDrawable);
                Toast.makeText(DetailsPage.this, '"'+ticker+'"'+ " was removed from favorites", Toast.LENGTH_SHORT).show();
                editor.remove(ticker);
                ArrayList<String> ticks = new ArrayList<>(Arrays.asList(watchList.split(",")));
                ticks.remove(ticker);
                String result = "";
                int len = ticks.size();
                for (int i =0;i<len;i++)
                {
                    if((len-1) ==i)
                    {
                        result = result + ticks.get(i);
                    }
                    else {
                        result = result + ticks.get(i) + ",";
                    }

                }
                watchList = result;
            }
            isChecked = !item.isChecked();

            item.setChecked(isChecked);

            editor.putString(watchlistTickersToBeSent,watchList);

            editor.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorite_toolbar, menu);

        return true;
    }


    public void trade_funct(View view) {
        tradeBtnID = (Button) findViewById(R.id.tradeBtnID);
        final SharedPreferences.Editor editor = sharedPreferencesPortfolio.edit();


        final Dialog dialog = new Dialog(tradeBtnID.getContext());
        dialog.setContentView(R.layout.buysell_popup);
        dialog.show();
        final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE );

        final TextView tradeCompID, calcID, liquidAmtID;
        final EditText noOfSharesID;
        Button buyBtnID, sellBtnID;


        tradeCompID = dialog.findViewById(R.id.tradeCompID);
        calcID = dialog.findViewById(R.id.calcID);
        noOfSharesID = dialog.findViewById(R.id.noOfSharesID);
        liquidAmtID = dialog.findViewById(R.id.liquidAmtID);
        buyBtnID = dialog.findViewById(R.id.buyBtnID);
        sellBtnID = dialog.findViewById(R.id.sellBtnID);

        final Double currPrice =  Double.parseDouble( currentStockPrice);


        tradeCompID.setText("Trade " + compNameID.getText() + " shares");
        calcID.setText("0 x $" + String.valueOf(df2.format(currPrice)) + "/share = $" + "0.0");
        liquidAmtID.setText("$" + df2.format(liquidAmount)  + " available to buy " + tickerID.getText());


        noOfSharesID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = noOfSharesID.getText().toString();

                Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                Matcher matcher = pattern.matcher(input);
                if (!matcher.matches()) {

                    Pattern pattern1 = Pattern.compile("-*[0-9][0-9]*\\.");
                    Matcher matcher1 = pattern1.matcher(input);
                    if ( input.isEmpty() || matcher1.matches() || input.equals("0") || input.startsWith("-")) {

                        Double no_shares = 0.0;

                        Double total = 0.0;
                        calcID.setText(no_shares +" x $" + String.valueOf(df2.format(currPrice)) + "/share = $" + total);
                    }
                }
                else {
                    Double no_shares = Double.parseDouble(input);

                    Double total = 0.0;
                    total = no_shares * currPrice;
                    calcID.setText(no_shares.toString() +" x $" + String.valueOf(df2.format(currPrice)) + "/share = $" + String.valueOf(df2.format(total)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buyBtnID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = noOfSharesID.getText().toString();
                Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                Matcher matcher = pattern.matcher(input);
                if (!matcher.matches()) {

                    Pattern pattern1 = Pattern.compile("^-[0-9][0-9]*$");
                    Matcher matcher1 = pattern1.matcher(input);
                    if ( input.isEmpty() || matcher1.matches() || input.equals("0") || input.startsWith("-")) {
                        Toast.makeText(DetailsPage.this, "Cannot buy less than 0 shares", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(DetailsPage.this, "‘Please enter valid amount", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Double no_shares = Double.parseDouble(noOfSharesID.getText().toString());

                    Double total = 0.0;
                    total = no_shares * currPrice;
                    if (total > liquidAmount) {
                        Toast.makeText(DetailsPage.this, "Not enough money to buy", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(sharedPreferencesPortfolio.contains(ticker))
                        {
                            int a;
                            a = sharedPreferencesPortfolio.getInt(ticker,-1);
                            a = a + (int) Math.round(no_shares);
                            editor.putInt(ticker,a);
                        }
                        else
                        {
                            int b = (int) Math.round(no_shares);
                            editor.putInt(ticker,b);
                            portfolioList = portfolioList + "," + ticker;
                        }

                        liquidAmount = liquidAmount - total.floatValue();

                        editor.putString(portfolioTickersToBeSent,portfolioList);

                        editor.putFloat("liquidAmount",liquidAmount);


                        editor.commit();

                        if(sharedPreferencesPortfolio.contains(ticker))
                        {
                            int z = sharedPreferencesPortfolio.getInt(ticker,0);

                            sharesOwnedID.setText("You have " + String.valueOf(z) + " shares of " + ticker + ".");
                            float x = z * currPrice.floatValue();
                            marketValueID.setText("Market Value: $"+String.valueOf(df2.format(x)));
                        }
                        else {
                            sharesOwnedID.setText("You have 0 shares of " + ticker + ".");
                            marketValueID.setText("Start trading!");
                        }

                        dialog.hide();
                        dialog.dismiss();
                        final Dialog dialog_congrats = new Dialog(tradeBtnID.getContext());
                        dialog_congrats.setContentView(R.layout.success_popup);
                        TextView congrats_msgID;
                        Button close_congratsID;
                        congrats_msgID = dialog_congrats.findViewById(R.id.congrats_msgID);
                        close_congratsID = dialog_congrats.findViewById(R.id.close_congratsID);
                        congrats_msgID.setText("You have successfully bought " + no_shares + " shares of "+ tickerID.getText().toString());
                        close_congratsID.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog_congrats.hide();
                                dialog_congrats.dismiss();
                            }
                        });
                        dialog_congrats.show();
                    }
                }
            }
        });

        sellBtnID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = noOfSharesID.getText().toString();
                Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                Matcher matcher = pattern.matcher(input);
                if (!matcher.matches()) {

                    Pattern pattern1 = Pattern.compile("^-[0-9][0-9]*$");
                    Matcher matcher1 = pattern1.matcher(input);
                    if ( input.isEmpty() || matcher1.matches() || input.equals("0") || input.startsWith("-")) {
                        Toast.makeText(DetailsPage.this, "Cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(DetailsPage.this, "‘Please enter valid amount", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Double no_shares = Double.parseDouble(noOfSharesID.getText().toString());

                    Double total = 0.0;
                    total = no_shares * currPrice;

                    int current_stocks;
                    if(sharedPreferencesPortfolio.contains(ticker))
                    {
                        current_stocks = sharedPreferencesPortfolio.getInt(ticker,0);
                    }
                    else {
                        current_stocks = 0;
                    }

                    if (no_shares.floatValue() > current_stocks) {
                        Toast.makeText(DetailsPage.this, "Not enough shares to sell", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        int total_stocks;
                        int curStocks = (int)Math.round(no_shares);
                        total_stocks = current_stocks - curStocks;
                        if(total_stocks>0)
                        {
                            editor.putInt(ticker,total_stocks);
                        }
                        else {
                            editor.remove(ticker);

                            ArrayList<String> Pticks = new ArrayList<>(Arrays.asList(portfolioList.split(",")));
                            Pticks.remove(ticker);
                            String result = "";
                            int len = Pticks.size();
                            for (int i =0;i<len;i++)
                            {
                                if((len-1) ==i)
                                {
                                    result = result + Pticks.get(i);
                                }
                                else {
                                    result = result + Pticks.get(i) + ",";
                                }

                            }
                            portfolioList = result;
                        }


                        liquidAmount = liquidAmount + total.floatValue();
                        editor.putString(portfolioTickersToBeSent,portfolioList);

                        editor.putFloat("liquidAmount",liquidAmount);


                        editor.commit();
                        if(sharedPreferencesPortfolio.contains(ticker))
                        {
                            int z = sharedPreferencesPortfolio.getInt(ticker,0);

                            sharesOwnedID.setText("You have " + String.valueOf(z) + " shares of " + ticker + ".");
                            float x = z * currPrice.floatValue();
                            marketValueID.setText("Market Value: $"+String.valueOf(x));
                        }
                        else {
                            sharesOwnedID.setText("You have 0 shares of " + ticker + ".");
                            marketValueID.setText("Start trading!");
                        }

                        dialog.hide();
                        dialog.dismiss();
                        final Dialog dialog_congrats = new Dialog(tradeBtnID.getContext());
                        dialog_congrats.setContentView(R.layout.success_popup);
                        TextView congrats_msgID;
                        Button close_congratsID;
                        congrats_msgID = dialog_congrats.findViewById(R.id.congrats_msgID);
                        close_congratsID = dialog_congrats.findViewById(R.id.close_congratsID);
                        congrats_msgID.setText("You have successfully sold " + no_shares + " shares of "+ tickerID.getText().toString());
                        close_congratsID.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog_congrats.hide();
                                dialog_congrats.dismiss();
                            }
                        });
                        dialog_congrats.show();
                    }
                }
            }
        });
    }
}
