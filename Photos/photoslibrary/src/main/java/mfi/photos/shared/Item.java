package mfi.photos.shared;

import lombok.Data;

@Data
public class Item {

    private String key;
    private String name;
    private String identifier;
    private String normDate;
    private String hash;
    private String[] users;
}
