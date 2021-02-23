package com.example.stocks;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsDataStructure> data;
    public  NewsAdapter(Context context, List<NewsDataStructure>  data){
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.news_recyclerview_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        final int pos = position;
        NewsDataStructure article = data.get(position);
        holder.news_card_title.setText(article.getTitle());
        holder.news_card_time.setText(article.getTime());
        holder.news_card_source.setText(article.getSource());
        String urlVal = article.getImageUrl();
        if (urlVal.equals("") || urlVal.equals("null")) {
            Glide.with(holder.news_card_image.getContext()).load(R.drawable.no_image).into(holder.news_card_image);
        }
        else {
            Glide.with(holder.news_card_image.getContext()).load(urlVal).into(holder.news_card_image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewsDataStructure article = data.get(pos);
                String articleLink = article.getNewsUrl();
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(articleLink));
                context.startActivity(intent1);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                NewsDataStructure article = data.get(pos);
                Log.d("CLICK:", "onClick: " + article.getTitle());
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.news_popup);

                ImageView dial_image, dial_twitter, dial_chrome;
                TextView dial_title;

                dial_title = dialog.findViewById(R.id.modal_news_title);
                dial_image = dialog.findViewById(R.id.modal_news_image);
                dial_twitter = dialog.findViewById(R.id.modal_news_twitter);
                dial_chrome = dialog.findViewById(R.id.modal_news_chrome);

                Glide.with(dial_image.getContext()).load(article.getImageUrl()).into(dial_image);
                dial_title.setText(article.getTitle());
                dial_chrome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NewsDataStructure article = data.get(pos);
                        String articleLink = article.getNewsUrl();
                        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(articleLink));
                        context.startActivity(intent1);
                    }
                });

                dial_twitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NewsDataStructure article = data.get(pos);
                        String hashTag = "#CSCI571StockApp";
                        String articleLink = article.getNewsUrl() ;
                        String twitterShareLink = "https://twitter.com/intent/tweet?&url=Check out this Link: " + articleLink +" "+ Uri.encode(hashTag);
                        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterShareLink));
                        context.startActivity(intent1);
                    }
                });
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder  {
        ImageView news_card_image;
        TextView news_card_title,  news_card_time,  news_card_source;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            news_card_image = itemView.findViewById(R.id.news_card_image);
            news_card_title = itemView.findViewById(R.id.news_card_title);
            news_card_time = itemView.findViewById(R.id.news_card_time);
            news_card_source = itemView.findViewById(R.id.news_card_source);
        }
    }

}

