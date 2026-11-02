package app.api.dto;

import app.domain.ProfileType;
import java.util.List;

public class ProfileDto {
    public String id;
    public String name;
    public ProfileType type;
    public double balance;
    public List<String> watchedMediaIds;
}
