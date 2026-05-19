package iterator;

import models.Song;
import java.util.List;

public class RepeatIterator implements PlaylistIterator {
    private List<Song> songs;
    private int position = 0;

    public RepeatIterator(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public boolean hasNext() {
        // Nếu có ít nhất 1 bài thì luôn có next
        return !songs.isEmpty();
    }

    @Override
    public Song next() {
        if (!hasNext()) {
            return null;
        }

        // Luôn trả về bài hiện tại (KHÔNG tăng position)
        return songs.get(position);
    }
}