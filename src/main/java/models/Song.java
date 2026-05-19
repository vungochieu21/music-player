package models;

public class Song {
    // Thêm final để đảm bảo dữ liệu bài hát không bị thay đổi vô ý sau khi tạo
    private final String id;
    private final String title;
    private final String artist;
    private final String duration;
    private final int trackNumber;
    private final String genre; 

    public Song(String id, String title, String artist, String duration, int trackNumber, String genre) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.trackNumber = trackNumber;
        this.genre = genre;
    }

    // Các hàm Getter giữ nguyên
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getDuration() { return duration; }
    public int getTrackNumber() { return trackNumber; }
    public String getGenre() { return genre; } 

    // Bổ sung toString() để phục vụ việc debug/in kiểm tra danh sách nhạc
    @Override
    public String toString() {
        return "Song{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration='" + duration + '\'' +
                ", trackNumber=" + trackNumber +
                ", genre='" + genre + '\'' +
                '}';
    }
}