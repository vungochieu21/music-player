package iterator;

import models.Song;
import java.util.List;

public class SequentialIterator implements PlaylistIterator {
    private List<Song> songs;
    private int position = 0;

    public SequentialIterator(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public boolean hasNext() {
        return position < songs.size();
    }

    @Override
    public Song next() {
        if (!hasNext()) {
            return null;
        }
        return songs.get(position++);
    }
}