let currentMenu = 'explore'; 

// Hàm chuyển đổi Menu (Khám phá / Thư viện)
function switchMenu(menuName, element) {
    currentMenu = menuName;
    
    document.querySelectorAll('.sidebar-menu li').forEach(li => {
        li.classList.remove('active-menu');
    });
    element.classList.add('active-menu');
    
    // Tạo đường dẫn URL để gửi yêu cầu xuống Spring Boot Backend xử lý
    let url = '/?mode=' + getPlayMode(); // Hàm giả định lấy chế độ phát hiện tại (sequential/shuffle/repeat)
    
    if (menuName === 'library') {
        // Gửi thêm tham số báo cho SmartPlaylistFactory biết cần tạo playlist theo Thư viện/Yêu thích
        // Ví dụ: Lọc theo thể loại "Favorite" hoặc cấu hình riêng theo ý nhóm bạn
        url += '&type=genre&value=Favorite'; 
    }
    
    // Điều hướng trình duyệt sang URL mới, Backend Spring Boot sẽ load lại trang và đổ đúng nhạc ra
    window.location.href = url;
}

// Hàm thực hiện tìm kiếm bài hát
function triggerSearch() {
    const keyword = document.getElementById('searchInput').value;
    const currentUrlParams = new URLSearchParams(window.location.search);
    
    // Giữ lại các tham số cũ (như bài hát đang phát, chế độ phát) và chỉ cập nhật keyword mới
    currentUrlParams.set('keyword', keyword);
    
    // Điều hướng để SearchEngine.java ở Backend làm việc
    window.location.href = '/?' + currentUrlParams.toString();
}

// Hàm xử lý khi bấm nút Yêu thích bài hát hiện tại
function handleFavorite(event) {
    event.preventDefault();
    
    // Lấy ID của bài hát hiện tại đang phát hiển thị trên giao diện HTML
    // (Bạn nên đặt một thuộc tính data-id ở thanh Player Bar phía dưới)
    const currentSongElement = document.getElementById('current-track-id'); 
    if (!currentSongElement) return;
    
    const songId = currentSongElement.value;
    
    // Thay vì tự xử lý ở JS, hãy gửi một request (hoặc redirect) để Backend xử lý lưu trạng thái yêu thích
    // Ví dụ gửi qua một API hoặc cập nhật qua URL:
    window.location.href = `/?id=${songId}&action=toggleFavorite&mode=${getPlayMode()}`;
}

// Hàm bổ trợ phụ để lấy chế độ chơi nhạc hiện tại từ giao diện (Thường là một thẻ <select> hoặc nút bấm)
function getPlayMode() {
    const modeElement = document.getElementById('playModeSelect');
    return modeElement ? modeElement.value : 'sequential';
}