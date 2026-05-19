package io;

import models.Song;
import org.springframework.core.io.ClassPathResource; // Thêm import này
import java.io.BufferedReader;
import java.io.InputStreamReader; // Thêm import này
import java.nio.charset.StandardCharsets; // Thêm import này
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    
    public static List<Song> loadSongs(String path) {
        List<Song> songs = new ArrayList<>();
        
        // Thay FileReader bằng ClassPathResource + InputStreamReader chuẩn UTF-8
        try {
            ClassPathResource resource = new ClassPathResource("data/songs.txt");
            
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                int lineNumber = 0; // Giữ nguyên biến đếm dòng của bạn để dễ debug
                
                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    
                    // Bỏ qua dòng trống
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    
                    String[] parts = line.split("\\|");
                    if (parts.length == 6) {
                        try {
                            // Dùng .trim() loại bỏ khoảng trắng thừa
                            String id = parts[0].trim();
                            String title = parts[1].trim();
                            String artist = parts[2].trim();
                            String duration = parts[3].trim();
                            int trackNumber = Integer.parseInt(parts[4].trim()); 
                            String genre = parts[5].trim();
                            
                            songs.add(new Song(id, title, artist, duration, trackNumber, genre));
                            
                        } catch (NumberFormatException e) {
                            // Giữ nguyên log cảnh báo của bạn
                            System.out.println("Cảnh báo: Dòng " + lineNumber + " sai định dạng số trackNumber. Đã bỏ qua.");
                        }
                    } else {
                        System.out.println("Cảnh báo: Dòng " + lineNumber + " không đủ 6 thuộc tính (Có " + parts.length + " phần). Đã bỏ qua.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi nghiêm trọng khi đọc file: " + e.getMessage());
        }
        return songs;
    }
}