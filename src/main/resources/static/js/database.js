const MusicDB = {
    init: function() {
        if (!localStorage.getItem('fav_songs')) {
            localStorage.setItem('fav_songs', JSON.stringify([]));
        }
    },

    // Kiểm tra xem bài hát ID này có được thích không (Để Front-end tô đỏ trái tim)
    isFavorite: function(id) {
        this.init();
        const favs = JSON.parse(localStorage.getItem('fav_songs'));
        // Ép kiểu về String hoặc Number cho đồng bộ tùy thuộc ID từ Backend đổ ra
        return favs.includes(String(id)) || favs.includes(Number(id));
    },

    // Bấm tim hoặc bỏ tim
    toggleFavorite: function(id) {
        this.init();
        let favs = JSON.parse(localStorage.getItem('fav_songs'));
        id = String(id); // Đồng bộ chuỗi ID
        
        if (favs.includes(id)) {
            favs = favs.filter(favId => favId !== id);
        } else {
            favs.push(id);
        }
        localStorage.setItem('fav_songs', JSON.stringify(favs));
        return favs; // Trả về danh sách ID đang yêu thích
    },

    // Lấy ra chuỗi danh sách ID yêu thích dạng "1,3,5" để gửi lên Java Backend lọc bằng Factory
    getFavoriteIdsString: function() {
        this.init();
        const favs = JSON.parse(localStorage.getItem('fav_songs'));
        return favs.join(',');
    }
};