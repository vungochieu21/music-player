package factory;

import models.Playlist;
import models.Song;
import java.util.List;
import java.util.stream.Collectors;

public class SmartPlaylistFactory {

    // 1. Định nghĩa các "loại" Playlist thông minh mà hệ thống hỗ trợ
    public enum PlaylistType {
        GENRE,  // Lọc theo thể loại
        ARTIST  // Lọc theo ca sĩ
    }

    /**
     * Hàm Factory duy nhất để tạo Playlist thông minh dựa trên tiêu chí
     * @param type Loại playlist muốn tạo (GENRE hoặc ARTIST)
     * @param criteria Từ khóa tìm kiếm (Ví dụ: "Pop", "Sơn Tùng M-TP")
     * @param allSongs Toàn bộ kho nhạc trong hệ thống để lọc
     * @return Một đối tượng Playlist đã được thêm các bài hát phù hợp
     */
    public static Playlist createSmartPlaylist(PlaylistType type, String criteria, List<Song> allSongs) {
        // Kiểm tra dữ liệu đầu vào để tránh lỗi NullPointerException
        if (allSongs == null || criteria == null || type == null) {
            return new Playlist("Empty Playlist");
        }

        // Khởi tạo đối tượng Playlist với tên hiển thị động
        Playlist playlist = new Playlist("Smart Playlist (" + type + ": " + criteria + ")");

        // 2. Sử dụng Java Stream để lọc bài hát (vừa gọn vừa xử lý nhanh)
        List<Song> filteredSongs = allSongs.stream()
                .filter(song -> {
                    switch (type) {
                        case GENRE:
                            return song.getGenre() != null && song.getGenre().equalsIgnoreCase(criteria);
                        case ARTIST:
                            return song.getArtist() != null && song.getArtist().equalsIgnoreCase(criteria);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());

        // 3. Đổ danh sách bài hát đã lọc vào Playlist
        // (Nếu class Playlist của bạn chưa có hàm addAll, ta dùng vòng lặp hoặc forEach)
        filteredSongs.forEach(playlist::addSong);

        return playlist;
    }
}