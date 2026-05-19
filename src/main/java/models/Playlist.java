package models;

import iterator.PlaylistIterator;
import iterator.RepeatIterator;
import iterator.SequentialIterator;
import iterator.ShuffleIterator;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;
    private List<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public void addSong(Song song) { 
        this.songs.add(song); 
    }
    
    public String getName() { 
        return name; 
    }
    
    public List<Song> getSongs() { 
        return songs; 
    }

    // =========================================================================
    // 🔥 BỔ SUNG CÁC HÀM FACTORY ĐỂ TRẢ VỀ ITERATOR PHỤC VỤ TÍNH NĂNG CHUYỂN BÀI
    // =========================================================================

    /**
     * Lấy bộ duyệt phát tuần tự từ đầu đến cuối danh sách
     */
    public PlaylistIterator getSequentialIterator() {
        return new SequentialIterator(this.songs);
    }

    /**
     * Lấy bộ duyệt trộn bài ngẫu nhiên (không ảnh hưởng thứ tự playlist gốc)
     */
    public PlaylistIterator getShuffleIterator() {
        return new ShuffleIterator(this.songs);
    }

    /**
     * Lấy bộ duyệt lặp bài vòng tròn vô hạn
     */
    public PlaylistIterator getRepeatIterator() {
        return new RepeatIterator(this.songs);
    }

    // Bổ sung toString() để dễ dàng in kiểm tra thông tin playlist khi debug
    @Override
    public String toString() {
        return "Playlist{" +
                "name='" + name + '\'' +
                ", totalSongs=" + songs.size() +
                '}';
    }
}