package ui;

import models.Song;
import models.Playlist;
import io.DataManager;
import search.SearchEngine;
import factory.SmartPlaylistFactory;
import factory.SmartPlaylistFactory.PlaylistType;
import iterator.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PlayerController {

    @GetMapping("/")
    public String index(
            @RequestParam(value = "id", required = false) String songId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String filterType,
            @RequestParam(value = "value", required = false) String filterValue,
            @RequestParam(value = "mode", required = false, defaultValue = "sequential") String playMode,
            @RequestParam(value = "action", required = false) String action,
            HttpSession session,
            Model model) {

        List<Song> library = DataManager.loadSongs("data/songs.txt");

        if (filterType != null && filterValue != null && !filterValue.isEmpty()) {

            Playlist smartPlaylist;

            if (filterType.equalsIgnoreCase("genre")) {

                smartPlaylist =
                        SmartPlaylistFactory.createSmartPlaylist(
                                PlaylistType.GENRE,
                                filterValue,
                                library
                        );

            } else {

                smartPlaylist =
                        SmartPlaylistFactory.createSmartPlaylist(
                                PlaylistType.ARTIST,
                                filterValue,
                                library
                        );
            }

            library = smartPlaylist.getSongs();
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            library = SearchEngine.fuzzySearch(library, keyword);
        }

        if (library.isEmpty()) {

            library.add(
                    new Song(
                            "0",
                            "Không có bài hát phù hợp",
                            "Hệ thống",
                            "00:00",
                            0,
                            "None"
                    )
            );
        }

        Playlist currentPlaylist = new Playlist("Current View");

        library.forEach(currentPlaylist::addSong);

        PlaylistIterator iterator =
                (PlaylistIterator) session.getAttribute("CURRENT_ITERATOR");

        String lastMode =
                (String) session.getAttribute("LAST_PLAY_MODE");

        if (
                iterator == null ||
                !playMode.equalsIgnoreCase(lastMode) ||
                "refresh_playlist".equalsIgnoreCase(action)
        ) {

            switch (playMode.toLowerCase()) {

                case "shuffle":
                    iterator = currentPlaylist.getShuffleIterator();
                    break;

                case "repeat":
                    iterator = currentPlaylist.getRepeatIterator();
                    break;

                case "sequential":
                default:
                    iterator = currentPlaylist.getSequentialIterator();
                    break;
            }

            if (songId != null) {

                while (iterator.hasNext()) {

                    Song s = iterator.next();

                    if (s.getId().equals(songId)) {
                        break;
                    }
                }
            }

            session.setAttribute("CURRENT_ITERATOR", iterator);

            session.setAttribute("LAST_PLAY_MODE", playMode);
        }

        Song currentSong = null;

        if ("next".equalsIgnoreCase(action)) {

            if (iterator.hasNext()) {

                currentSong = iterator.next();

            } else {

                iterator = currentPlaylist.getSequentialIterator();

                session.setAttribute("CURRENT_ITERATOR", iterator);

                if (iterator.hasNext()) {
                    currentSong = iterator.next();
                }
            }

            String redirectUrl =
                    "redirect:/?id=" +
                            (currentSong != null ? currentSong.getId() : "0") +

                            (keyword != null
                                    ? "&keyword=" + keyword
                                    : "") +

                            (filterType != null
                                    ? "&type=" + filterType + "&value=" + filterValue
                                    : "") +

                            "&mode=" + playMode;

            return redirectUrl;
        }

        if (songId != null) {

            for (Song s : library) {

                if (s.getId().equals(songId)) {

                    currentSong = s;
                    break;
                }
            }
        }

        if (currentSong == null) {
            currentSong = library.get(0);
        }

        model.addAttribute("library", library);
        model.addAttribute("currentSong", currentSong);
        model.addAttribute("keyword", keyword);
        model.addAttribute("mode", playMode);
        model.addAttribute("filterType", filterType);
        model.addAttribute("filterValue", filterValue);

        return "index";
    }

    @GetMapping("/albums")
    public String getAlbums(
            @RequestParam(name = "groupBy", defaultValue = "artist") String groupBy,
            Model model) {

        List<Song> allSongs =
                DataManager.loadSongs("data/songs.txt");

        Map<String, Long> groupedData;

        String pageTitle = "Album Theo Ca Sĩ";

        if ("genre".equalsIgnoreCase(groupBy)) {

            groupedData =
                    allSongs.stream()
                            .filter(s ->
                                    s.getGenre() != null &&
                                    !s.getGenre().trim().isEmpty()
                            )
                            .collect(
                                    Collectors.groupingBy(
                                            Song::getGenre,
                                            Collectors.counting()
                                    )
                            );

            pageTitle = "Album Theo Thể Loại";

        } else if ("topic".equalsIgnoreCase(groupBy)) {

            groupedData =
                    allSongs.stream()
                            .filter(s -> s.getGenre() != null)
                            .collect(
                                    Collectors.groupingBy(
                                            Song::getGenre,
                                            Collectors.counting()
                                    )
                            );

            pageTitle = "Album Theo Chủ Đề";

        } else {

            groupedData =
                    allSongs.stream()
                            .filter(s ->
                                    s.getArtist() != null &&
                                    !s.getArtist().trim().isEmpty()
                            )
                            .collect(
                                    Collectors.groupingBy(
                                            Song::getArtist,
                                            Collectors.counting()
                                    )
                            );
        }

        model.addAttribute("albums", groupedData);
        model.addAttribute("type", groupBy);
        model.addAttribute("title", pageTitle);

        return "albums";
    }

    @PostMapping("/library/add")
    @ResponseBody
    public ResponseEntity<String> addToLibrary(
            @RequestParam("songId") String songId) {

        if (
                songId == null ||
                songId.trim().isEmpty() ||
                "0".equals(songId)
        ) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid Song ID");
        }

        String filePath = "data/favorites.txt";

        File file = new File(filePath);

        List<String> favoriteIds = new ArrayList<>();

        try {

            if (file.exists()) {

                favoriteIds =
                        Files.readAllLines(Paths.get(filePath))
                                .stream()
                                .map(String::trim)
                                .filter(line -> !line.isEmpty())
                                .collect(Collectors.toList());

            } else {

                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
            }

            if (favoriteIds.contains(songId.trim())) {

                favoriteIds.remove(songId.trim());

            } else {

                favoriteIds.add(songId.trim());
            }

            Files.write(Paths.get(filePath), favoriteIds);

            return ResponseEntity.ok("Success");

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body("Database Error");
        }
    }

    @GetMapping("/library/check")
    @ResponseBody
    public ResponseEntity<Boolean> checkFavorite(
            @RequestParam("songId") String songId) {

        String filePath = "data/favorites.txt";

        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.ok(false);
        }

        try {

            List<String> ids =
                    Files.readAllLines(Paths.get(filePath));

            boolean exists =
                    ids.stream()
                            .map(String::trim)
                            .anyMatch(id -> id.equals(songId));

            return ResponseEntity.ok(exists);

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/library")
    public String viewPersonalLibrary(
            @RequestParam(value = "id", required = false) String songId,
            @RequestParam(value = "mode", required = false, defaultValue = "sequential") String playMode,
            @RequestParam(value = "type", required = false) String filterType,
            @RequestParam(value = "value", required = false) String filterValue,
            Model model) {

        List<Song> allSongs =
                DataManager.loadSongs("data/songs.txt");

        List<Song> favoriteSongs =
                new ArrayList<>();

        List<String> savedIds =
                new ArrayList<>();

        String filePath = "data/favorites.txt";

        File file = new File(filePath);

        if (file.exists()) {

            try {

                savedIds =
                        Files.readAllLines(Paths.get(filePath))
                                .stream()
                                .map(String::trim)
                                .filter(line -> !line.isEmpty())
                                .collect(Collectors.toList());

            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        for (String id : savedIds) {

            Song matchingSong =
                    allSongs.stream()
                            .filter(s -> s.getId().equals(id))
                            .findFirst()
                            .orElse(null);

            if (matchingSong != null) {
                favoriteSongs.add(matchingSong);
            }
        }

        if (favoriteSongs.isEmpty()) {

            favoriteSongs.add(
                    new Song(
                            "0",
                            "Chưa có bài hát yêu thích nào",
                            "Thư viện trống",
                            "00:00",
                            0,
                            "None"
                    )
            );
        }

        Song currentSong = null;

        if (songId != null) {

            for (Song s : favoriteSongs) {

                if (s.getId().equals(songId)) {

                    currentSong = s;
                    break;
                }
            }
        }

        if (currentSong == null) {
            currentSong = favoriteSongs.get(0);
        }

        model.addAttribute("library", favoriteSongs);
        model.addAttribute("currentSong", currentSong);
        model.addAttribute("mode", playMode);
        model.addAttribute("keyword", "");
        model.addAttribute("filterType", "library");
        model.addAttribute("filterValue", "");

        return "index";
    }
}