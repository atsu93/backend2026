package app.domain;

import java.util.List;
import java.util.ArrayList;

public class UserProfile {
    private final String id;
    private String name;
    private ProfileType type;
    private double balance;
    private final List<String> watchedMediaIds = new ArrayList<>();

    public UserProfile(String id, String name, ProfileType type){
        this.id = id;
        this.name = name;
        this.type = type;
        this.balance = 500.0;
    }

    public void addWatchedMedia(String mediaId){
        if (!watchedMediaIds.contains(mediaId)){
            watchedMediaIds.add(mediaId);
        }
    }

    public void spendMoney(double amount) {
        this.balance -= amount;
    }

    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public ProfileType getType(){
        return type;
    }
    public void setType(ProfileType type){
        this.type = type;
    }
    public List<String> getWatchedMediaIds(){
        return watchedMediaIds;
    }
    public double getBalance() {
        return balance;
    }
}
