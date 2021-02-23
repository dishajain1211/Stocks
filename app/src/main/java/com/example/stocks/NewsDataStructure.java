package com.example.stocks;

public class NewsDataStructure {

    private String title;
    private String source;
    private String imageUrl;
    private String newsUrl;
    private String time;

    public NewsDataStructure(String title, String source, String image, String newsUrl, String time) {
        this.title = title;
        this.source = source;
        this.imageUrl = image;
        this.newsUrl = newsUrl;
        this.time = time;
    }


    public void setTitle(String title) {
        this.title = title;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public void setNewsUrl(String newsUrl) {
        this.newsUrl = newsUrl;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }
    public String getSource(){
        return source;
    }
    public String getImageUrl(){
        return imageUrl;
    }
    public String getNewsUrl(){
        return newsUrl;
    }
    public String getTime(){
        return time;
    }

}
