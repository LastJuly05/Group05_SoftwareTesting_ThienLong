# Kế Hoạch Automation Test - Group05 Thiên Long

## Tổng quan

Kế hoạch automation test cho website **Thiên Long Shop** (https://thienlong.vn).

## Công cụ sử dụng

| Công cụ | Mục đích |
|---------|----------|
| Selenium WebDriver | Tự động hóa trình duyệt |
| TestNG / JUnit | Framework kiểm thử |
| Maven | Quản lý dependency |
| Git | Quản lý source code |

## Checklist Automation Test

### Giai đoạn 1: Chuẩn bị

- [ ] Thiết lập môi trường test (Java, Selenium, TestNG)
- [ ] Cấu hình WebDriver cho các trình duyệt
- [ ] Tạo cấu trúc project automation
- [ ] Viết Base Test class

### Giai đoạn 2: Triển khai

- [ ] Automation test cho chức năng 1-3 (Lương Hoài Sang)
- [ ] Automation test cho chức năng 4-6 (Nguyễn Khánh Dinh)
- [ ] Automation test cho chức năng 7-9 (Nguyễn Tuấn Kha)
- [ ] Automation test cho chức năng 10-12 (Nguyễn Tấn Dũng)
- [ ] Automation test cho chức năng 13-15 (Đoàn Huy Cương)

### Giai đoạn 3: Tích hợp & Báo cáo

- [ ] Tích hợp tất cả test suite
- [ ] Tạo báo cáo Allure/Extent Reports
- [ ] Review và tối ưu hóa test

## Sơ đồ quy trình

```
Manual Test Cases (Excel)
        ↓
  Phân tích & Review
        ↓
  Viết Automation Script (Selenium)
        ↓
  Chạy thử & Debug
        ↓
  Tích hợp vào Test Suite
        ↓
  Sinh báo cáo kết quả
```

## Phân công

| Thành viên | MSSV | Chức năng | Nhánh Git |
|-----------|------|-----------|-----------|
| Lương Hoài Sang | 23130270 | 1, 2, 3 | feature/23130270-LuongHoaiSang |
| Nguyễn Khánh Dinh | 23130062 | 4, 5, 6 | feature/23130062-NguyenKhanhDinh |
| Nguyễn Tuấn Kha | 23130141 | 7, 8, 9 | feature/23130141-NguyenTuanKha |
| Nguyễn Tấn Dũng | 23130072 | 10, 11, 12 | feature/23130072-NguyenTanDung |
| Đoàn Huy Cương | 23130035 | 13, 14, 15 | feature/23130035-DoanHuyCuong |
