# Planner - Student Manager App

Ứng dụng quản lý học tập dành cho sinh viên, giúp quản lý thời khóa biểu, deadline và nhắc nhở công việc.

## 🏗 Cấu trúc dự án (Project Structure)

Dự án được tổ chức theo mô hình phân lớp cơ bản để dễ dàng quản lý và mở rộng:

```
app/src/main/java/com/example/planner/
├── data/                 # Tầng xử lý dữ liệu
│   ├── local/            # Database (Room), SharedPreferences
│   ├── model/            # Các Data Class (Task, Subject, User...)
│   └── repository/       # Lớp trung gian xử lý logic lấy/cất dữ liệu
│
├── ui/                   # Tầng giao diện
│   ├── main/             # Màn hình chính (Dashboard/Home)
│   ├── schedule/         # Màn hình thời khóa biểu
│   ├── task/             # Màn hình quản lý deadline/công việc
│   └── splash/           # Màn hình khởi động
│
├── worker/               # Xử lý chạy ngầm (WorkManager cho nhắc nhở)
│
└── utils/                # Các hàm tiện ích dùng chung (Format, Constants)
```

## 🚀 Tính năng chính (Planned Features)
- [ ] Quản lý thời khóa biểu.
- [ ] Theo dõi deadline và danh sách công việc (To-do list).
- [ ] Thông báo nhắc nhở tự động.
- [ ] Lưu trữ dữ liệu offline với Room Database.

## 🛠 Công nghệ sử dụng
- **Language:** Java/Kotlin
- **UI:** XML Layouts, Material Design 3
- **Database:** Room Persistence Library
- **Background Tasks:** WorkManager
- **Architecture:** MVVM (Planned)

---
*Dự án đang trong quá trình phát triển.*
