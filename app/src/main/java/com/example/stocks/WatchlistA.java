package com.example.stocks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;


public class WatchlistA extends RecyclerView.Adapter<WatchlistA.MyViewHolder> implements WItemMoveCB.ItemTouchHelperContract {

    private ArrayList<String> watchList;
    private Context context;
    JSONObject companyDescription, companyData;
    private static SharedPreferences sharedPreferences;
    private static DecimalFormat df2 = new DecimalFormat("#.##");


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tc_ID, cp_ID, s_ID, change_ID;
        ImageView changeImg_ID;
        CardView portfolioID;
        View cardRow;

        public MyViewHolder(View itemView) {
            super(itemView);
            cardRow = itemView;
            tc_ID = itemView.findViewById(R.id.tc_ID);
            cp_ID = itemView.findViewById(R.id.cp_ID);
            s_ID = itemView.findViewById(R.id.s_ID);
            change_ID = itemView.findViewById(R.id.change_ID);
            changeImg_ID = itemView.findViewById(R.id.changeImg_ID);
            portfolioID = itemView.findViewById(R.id.portfolioID);

        }
    }

    public WatchlistA(Context context, ArrayList<String> watchList) {
        this.context = context;
        this.watchList = watchList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerviewer_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final String ticker = watchList.get(position);
        sharedPreferences = context.getSharedPreferences("DataPortfolio", Context.MODE_PRIVATE);

        RequestQueue queue = Volley.newRequestQueue(context);

        if(sharedPreferences.contains(ticker))
        {
            float a = (float) sharedPreferences.getInt(ticker,0);
            holder.s_ID.setText(String.valueOf(a)+" shares");
        }
        else
        {
            String url1 ="http://10.0.2.2:3000/details1?ticker=" + ticker;
            JsonObjectRequest jsonObjReq1 = new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        companyDescription = response;

                        holder.s_ID.setText(companyDescription.getString("name"));
                    } catch (JSONException e) { e.printStackTrace(); }

                }}, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) { Log.i("Volley error", String.valueOf(error)); }
            });
            jsonObjReq1.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjReq1);
        }

        String url2 ="http://10.0.2.2:3000/details2?ticker=" + ticker;
        JsonObjectRequest jsonObjReq2 = new JsonObjectRequest(Request.Method.GET, url2, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    companyData = response;
                    holder.cp_ID.setText(String.valueOf(df2.format(Float.valueOf(companyData.getString("last")).floatValue())));

                    Double change = Double.parseDouble(companyData.getString("last")) - Double.parseDouble(companyData.getString("prevClose"));
                    change = (float) Math.round(change * 100.0)/100.0;
                    if (change > 0) {
                        holder.change_ID.setText(String.valueOf(df2.format(change)));
                        holder.change_ID.setTextColor(Color.parseColor("#3f925b"));
                        holder.changeImg_ID.setImageResource(R.drawable.ic_twotone_trending_up_24);
                    }
                    else if (change < 0) {
                        change = change * -1.0;
                        holder.change_ID.setText(String.valueOf(df2.format(change)));
                        holder.change_ID.setTextColor(Color.parseColor("#9b4049"));
                        holder.changeImg_ID.setImageResource(R.drawable.ic_baseline_trending_down_24);
                    }
                    else {
                        holder.change_ID.setText(String.valueOf(df2.format(change)));
                        holder.change_ID.setTextColor(Color.parseColor("#cfcfcf"));}
                } catch (JSONException e) { e.printStackTrace(); }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.i("Volley Error", String.valueOf(error)); }
        });
        jsonObjReq2.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq2);

        holder.tc_ID.setText(watchList.get(position));

        holder.portfolioID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailsPage.class);
                intent.putExtra("message_key", ticker + " - " + "TEMP");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return watchList.size();
    }

    public String removeItem(int position) {
        String ticker = watchList.get(position);
        watchList.remove(position);
        notifyItemRemoved(position);

        String tempWatchList = "null";
        ArrayList<String> ticks = new ArrayList<>();
        for (int i = 0; i <  watchList.size(); i++) {
            tempWatchList += "," + watchList.get(i);
        }

        return tempWatchList;
    }

    public ArrayList<String> getData() {
        return watchList;
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(watchList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(watchList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        String tempWatchList = "null";
        ArrayList<String> ticks = new ArrayList<>();
        for (int i = 0; i <  watchList.size(); i++) {
            tempWatchList += "," + watchList.get(i);
        }

    }

    @Override
    public void onRowSelected(MyViewHolder myViewHolder) {
        myViewHolder.cardRow.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(MyViewHolder myViewHolder) {
        myViewHolder.cardRow.setBackgroundColor(Color.WHITE);
    }


}

