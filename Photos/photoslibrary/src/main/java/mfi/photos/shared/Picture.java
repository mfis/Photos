package mfi.photos.shared;

import lombok.Data;

@Data
public class Picture {

    private String name;
    private int tnh;
    private int tnw;
    private int h;
    private int w;
    private String hash;
    private long fileSize;
}
