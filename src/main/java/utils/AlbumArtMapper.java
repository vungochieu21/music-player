package utils;

import java.util.HashMap;
import java.util.Map;

public class AlbumArtMapper {

    private static final Map<String, String> ARTIST_ARTS = new HashMap<>();
    private static final Map<String, String> GENRE_ARTS = new HashMap<>();

    // Ảnh mặc định nếu không tìm thấy ảnh riêng cho ca sĩ/thể loại đó
    private static final String DEFAULT_ARTIST_ART = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80";
    private static final String DEFAULT_GENRE_ART = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80";

    static {
        // --- 1. ĐỊNH NGHĨA ẢNH CHO TỪNG CA SĨ ---
        // Bạn có thể thay link ảnh bên dưới bằng ảnh trong thư mục /images/ của bạn hoặc link mạng tùy ý
        ARTIST_ARTS.put("Sơn Tùng M-TP", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80");
        ARTIST_ARTS.put("Dan Music", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&q=80");
        ARTIST_ARTS.put("KillnTea", "https://images.unsplash.com/photo-1511735111819-9a3f7709049c?w=500&q=80");
        ARTIST_ARTS.put("Djo Music", "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500&q=80");
        ARTIST_ARTS.put("arthou", "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&q=80");

        // --- 2. ĐỊNH NGHĨA ẢNH CHO TỪNG THỂ LOẠI ---
        ARTIST_ARTS.put("Pop", "https://images.unsplash.com/photo-1487180142328-054b783fc471?w=500&q=80");
        ARTIST_ARTS.put("Lo-fi", "https://images.unsplash.com/photo-1557672172-298e090bd0f1?w=500&q=80");
        ARTIST_ARTS.put("Ballad", "https://images.unsplash.com/photo-1446057032654-9d8885b7518a?w=500&q=80");
        ARTIST_ARTS.put("Rock", "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500&q=80");
    }

    /**
     * Lấy link ảnh đại diện dựa theo loại hiển thị (artist/genre) và tên cụ thể
     */
    public static String getAlbumArt(String type, String key) {
        if (key == null) return "artist".equalsIgnoreCase(type) ? DEFAULT_ARTIST_ART : DEFAULT_GENRE_ART;
        
        String cleanKey = key.trim();
        if ("artist".equalsIgnoreCase(type)) {
            return ARTIST_ARTS.getOrDefault(cleanKey, DEFAULT_ARTIST_ART);
        } else if ("genre".equalsIgnoreCase(type)) {
            return GENRE_ARTS.getOrDefault(cleanKey, DEFAULT_GENRE_ART);
        }
        return DEFAULT_ARTIST_ART;
    }
}