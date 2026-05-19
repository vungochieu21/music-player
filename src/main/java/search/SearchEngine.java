package search;

import models.Song;
import java.util.ArrayList;
import java.util.List;

public class SearchEngine {

    public static List<Song> fuzzySearch(List<Song> songs, String keyword) {
        List<Song> result = new ArrayList<>();
        
        if (songs == null || keyword == null || keyword.trim().isEmpty()) {
            return result;
        }

        String cleanKeyword = keyword.toLowerCase().trim();

        for (Song song : songs) {
            String cleanTitle = song.getTitle().toLowerCase().trim();

            // 1. Ưu tiên số 1: Nếu chứa trọn vẹn từ khóa (Ví dụ: Tìm "Chạy" trong "Chạy Ngay Đi")
            if (cleanTitle.contains(cleanKeyword)) {
                result.add(song);
                continue; // Khớp luôn, bỏ qua bước tính Levenshtein phía dưới để tiết kiệm CPU
            }

            // 2. Ưu tiên số 2: Tính độ tương đồng phần trăm bằng Levenshtein để bắt lỗi gõ sai
            int distance = levenshtein(cleanTitle, cleanKeyword);
            int maxLength = Math.max(cleanTitle.length(), cleanKeyword.length());
            
            // Tính tỷ lệ giống nhau từ 0.0 đến 1.0
            double similarity = 1.0 - ((double) distance / maxLength);

            // Nếu độ giống nhau từ 70% trở lên (0.7), chấp nhận là gõ nhầm/gõ thiếu dấu
            if (similarity >= 0.7) {
                result.add(song);
            }
        }

        return result;
    }

    public static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }
}