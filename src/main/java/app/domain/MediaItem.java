package app.domain;

public class MediaItem {
    private final String id;
    private String title;
    private String ageRating;
    private boolean isPremium;
    private int viewsCount;

    public MediaItem(String id, String title, String ageRating, boolean isPremium){
        this.id = id;
        this.title = title;
        this.ageRating = ageRating;
        this.isPremium = isPremium;
        this.viewsCount = 0;
    }

    public void incrementViews(){
        this.viewsCount++;
    }

    public String getId(){return id;}

    public String getTitle(){return title;}
    public void setTitle(String title){this.title=title;}

    public String getAgeRating(){return ageRating;}
    public void setAgeRating(String ageRating){this.ageRating = ageRating;}

    public boolean isPremium(){return isPremium;}
    public void setPremium(boolean premium){isPremium = premium;}

    public int getViewsCount(){return viewsCount;}
}
