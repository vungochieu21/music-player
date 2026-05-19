package iterator;

import models.Song;

public interface PlaylistIterator {
    boolean hasNext();
    Song next();
}