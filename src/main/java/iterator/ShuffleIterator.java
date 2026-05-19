package iterator;

import models.Song;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShuffleIterator implements PlaylistIterator {
    private List<Song> shuffledSongs;
    private int position = 0;

    public ShuffleIterator(List<Song> songs) {
        // Copy ra danh sách mới để tránh làm xáo trộn danh sách gốc của Playlist
        this.shuffledSongs = new ArrayList<>(songs);
        // Xáo trộn ngẫu nhiên
        Collections.shuffle(this.shuffledSongs);
    }

    @Override
    public boolean hasNext() {
        return position < shuffledSongs.size();
    }

    @Override
    public Song next() {
        if (!hasNext()) {
            return null;
        }
        return shuffledSongs.get(position++);
    }
}