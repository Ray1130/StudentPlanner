# Planner - Student Manager App

Ứng dụng quản lý học tập dành cho sinh viên, tích hợp hệ thống Android và Backend Spring Boot để đồng bộ hóa công việc, thời khóa biểu và nhắc nhở.

## 🏗 Cấu trúc dự án (Fullstack Architecture)

Dự án bao gồm hai thành phần chính chạy song song:

### 1. Android App (`/app`)
- **UI & Navigation:** Tổ chức theo mô hình MVVM (Model-View-ViewModel).
- **Networking:** Sử dụng **Retrofit 2** để kết nối với Backend API.
- **Local Storage:** **Room Database** để lưu trữ offline và cache dữ liệu.
- **Features:** Quản lý Task theo môn học, thời khóa biểu, nhắc nhở qua WorkManager.

### 2. Backend Server (`/backend-server`)
- **Framework:** **Spring Boot 3.2** (Java 17).
- **Database:** H2 Database (In-memory) tích hợp sẵn cho quá trình phát triển.
- **API Endpoints:** Cung cấp RESTful API `/api/tasks` để quản lý danh sách công việc.
- **Seed Data:** Tự động nạp dữ liệu mẫu (Môn Mobile, CSDL Phân tán) khi khởi chạy server.

## 🚀 Tính năng hiện có
- [x] **Kết nối Fullstack:** App Android đã có thể lấy dữ liệu thật từ Spring Boot qua Retrofit.
- [x] **Splash Screen mượt mà:** Tích hợp `androidx.core:core-splashscreen` giúp loại bỏ màn hình trắng trên Android 12+, đồng bộ với hiệu ứng fade-in và màu nền tím đặc trưng.
- [x] **Điều hướng thông minh:** Hệ thống Bottom Navigation và Navigation Drawer giúp chuyển đổi nhanh giữa Trang chủ, Công việc, Lịch, Pomodoro, Thông báo và Cá nhân.
- [x] **Quản lý Task & Môn học:** Tích hợp backend để quản lý danh sách công việc và môn học (Subject), hiển thị theo deadline và độ ưu tiên.
- [x] **Thông báo & Nhắc nhở:** Màn hình thông báo chuyên biệt giúp theo dõi các hạn chót sắp tới.
- [x] **Dữ liệu cục bộ:** Sử dụng Room Database để lưu trữ dữ liệu, đảm bảo ứng dụng hoạt động ổn định.
- [x] **Màn hình Profile:** Quản lý thông tin cá nhân và cài đặt ứng dụng.

## 🛠 Hướng dẫn chạy dự án

### Chạy Backend (Spring Boot)
1. Mở Terminal tại thư mục gốc của project.
2. Gõ lệnh:
   ```powershell
   cd backend-server
   .\gradlew bootRun
   ```
3. Kiểm tra API tại: `http://localhost:8080/api/tasks`

### Chạy Android App
1. Mở toàn bộ thư mục `Planner` bằng Android Studio.
2. Nhấn **Sync Project with Gradle Files**.
3. Chạy ứng dụng trên Emulator (App sẽ tự động kết nối tới server qua địa chỉ `10.0.2.2:8080`).

## 🛠 Công nghệ sử dụng
- **Android:** Java, Retrofit, Room, Material Design 3.
- **Backend:** Spring Boot, Spring Data JPA, H2 Database.
- **Build Tool:** Gradle (Multi-module structure).

---
*Dự án hiện đã hoàn tất thiết lập cơ sở hạ tầng Fullstack.*
