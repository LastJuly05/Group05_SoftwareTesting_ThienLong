# Test Cases - Lương Hoài Sang (23130270)

**Thành viên:** Lương Hoài Sang  
**MSSV:** 23130270  
**Vai trò:** Nhóm trưởng  

## Nhóm chức năng: Giỏ hàng & Khuyến mãi (Cart & Promotion)

| STT | Chức năng | File Test Case |
|-----|-----------|---------------|
| 1 | Thêm sản phẩm vào giỏ (từ trang chủ, trang chi tiết, kiểm tra icon) | Group05_23130270_LuongHoaiSang.xlsx |
| 2 | Quản lý giỏ hàng (tăng/giảm số lượng, xóa sản phẩm, tính tổng tiền) | Group05_23130270_LuongHoaiSang.xlsx |
| 3 | Mã giảm giá / Voucher (áp dụng mã hợp lệ/hết hạn/sai, kiểm tra tiền giảm) | Group05_23130270_LuongHoaiSang.xlsx |

## ASM7 - Automation Script

- **Công cụ:** Katalon Studio (Groovy + Selenium WebDriver)
- **File script:** `23130270_LuongHoaiSang_ASM7.groovy`
- **Hướng dẫn:** `ASM7/README.md`
- **Tổng số test cases:** 13 (TC-CART-01 đến TC-CART-13)

### Danh sách Test Cases

| Test Case | Chức năng | Mô tả |
|-----------|-----------|-------|
| TC-CART-01 | Thêm vào giỏ | Thêm sản phẩm từ trang chủ |
| TC-CART-02 | Thêm vào giỏ | Thêm sản phẩm từ trang chi tiết |
| TC-CART-03 | Thêm vào giỏ | Kiểm tra số lượng trên icon giỏ hàng |
| TC-CART-04 | Quản lý giỏ | Tăng số lượng sản phẩm |
| TC-CART-05 | Quản lý giỏ | Giảm số lượng sản phẩm |
| TC-CART-06 | Quản lý giỏ | Xóa sản phẩm khỏi giỏ |
| TC-CART-07 | Quản lý giỏ | Kiểm tra tổng tiền tạm tính |
| TC-CART-08 | Quản lý giỏ | Kiểm tra giỏ hàng trống |
| TC-CART-09 | Voucher | Áp dụng mã giảm giá hợp lệ |
| TC-CART-10 | Voucher | Áp dụng mã giảm giá hết hạn |
| TC-CART-11 | Voucher | Áp dụng mã giảm giá không tồn tại |
| TC-CART-12 | Voucher | Kiểm tra số tiền được giảm |
| TC-CART-13 | Voucher | Nhập mã giảm giá rỗng |

## Ghi chú

- File Excel test case được đặt tên: `Group05_23130270_LuongHoaiSang.xlsx`
- Mỗi chức năng gồm 20–30 test case
- Format theo mẫu của giảng viên

ASM7:
# ASM7 - Automation Script: Giỏ hàng & Khuyến mãi

**Sinh viên:** Lương Hoài Sang  
**MSSV:** 23130270  
**Nhóm:** Nhóm 5  
**Website:** https://thienlong.vn  
**Công cụ:** Katalon Studio (Groovy + Selenium WebDriver)

---

## Mô tả

Script automation test cho nhóm chức năng **"Giỏ hàng & Khuyến mãi" (Cart & Promotion)** trên website Thiên Long Shop (thienlong.vn - Shopify).

## Chức năng được test

### Chức năng 1: Thêm sản phẩm vào giỏ (Add to Cart)
| Test Case | Mô tả |
|-----------|-------|
| TC-CART-01 | Thêm sản phẩm vào giỏ từ trang chủ |
| TC-CART-02 | Thêm sản phẩm vào giỏ từ trang chi tiết sản phẩm |
| TC-CART-03 | Kiểm tra số lượng trên icon giỏ hàng sau khi thêm |

### Chức năng 2: Quản lý giỏ hàng (Cart Management)
| Test Case | Mô tả |
|-----------|-------|
| TC-CART-04 | Tăng số lượng sản phẩm trong giỏ hàng |
| TC-CART-05 | Giảm số lượng sản phẩm trong giỏ hàng |
| TC-CART-06 | Xóa sản phẩm khỏi giỏ hàng |
| TC-CART-07 | Kiểm tra tính tổng tiền tạm tính |
| TC-CART-08 | Kiểm tra hiển thị giỏ hàng trống |

### Chức năng 3: Mã giảm giá / Voucher (Promotion)
| Test Case | Mô tả |
|-----------|-------|
| TC-CART-09 | Áp dụng mã giảm giá hợp lệ |
| TC-CART-10 | Áp dụng mã giảm giá hết hạn |
| TC-CART-11 | Áp dụng mã giảm giá không tồn tại |
| TC-CART-12 | Kiểm tra số tiền được giảm sau khi áp dụng voucher |
| TC-CART-13 | Nhập mã giảm giá rỗng (khoảng trắng) |

## Cấu trúc file

```
ASM7/
├── 23130270_LuongHoaiSang_ASM7.groovy   # Script Katalon Studio (Groovy)
├── 23130270_LuongHoaiSang_ASM7.txt      # File nộp bài
└── README.md                            # File hướng dẫn này
```

## Cách chạy

### Yêu cầu
- Katalon Studio (phiên bản 8.x hoặc mới hơn)
- Java JDK 11+
- Kết nối internet để truy cập thienlong.vn

### Các bước

#### Cách 1: Chạy trực tiếp trong Katalon Studio
1. Mở Katalon Studio
2. Tạo mới Project hoặc mở project hiện có
3. Tạo mới **Test Case** trong folder `Test Cases`
4. Đặt tên: `23130270_LuongHoaiSang_ASM7`
5. Chuyển sang **Script** view
6. Copy toàn bộ nội dung file `../23130270_LuongHoaiSang_ASM7.groovy` vào
7. Click **Run** (nút Play) để chạy test

#### Cách 2: Import Test Case
1. Mở Katalon Studio
2. File → Import → Test Case from File
3. Chọn file `../23130270_LuongHoaiSang_ASM7.groovy`
4. Chạy Test Case

## Cấu hình

### Mã giảm giá
Script sử dụng các mã giảm giá mẫu:
- **Mã hợp lệ:** `WELCOME10` (cần thay bằng mã thực tế đang hoạt động)
- **Mã hết hạn:** `EXPIRED2025`
- **Mã không tồn tại:** `INVALIDCODE12345`

> **Lưu ý:** Trước khi chạy, hãy kiểm tra và cập nhật mã giảm giá hợp lệ đang hoạt động trên thienlong.vn.

### CSS Selectors
Script sử dụng CSS Selectors linh hoạt (wildcard matching) để tương thích với cấu trúc Shopify:
- Nút thêm vào giỏ: `button[class*="add-to-cart"]`, `button.product-form__submit`
- Giỏ hàng: `.cart-item`, `.cart__item`
- Mã giảm giá: `input[class*="discount"]`, `.discount-code input`

Nếu website thay đổi cấu trúc HTML, cần cập nhật các selector tương ứng.

## Kết quả dự kiến

| Test Case | Kết quả mong đợi |
|-----------|-----------------|
| TC-CART-01 | PASS - Sản phẩm được thêm vào giỏ từ trang chủ |
| TC-CART-02 | PASS - Sản phẩm được thêm vào giỏ từ trang chi tiết |
| TC-CART-03 | PASS - Số lượng trên icon giỏ hàng tăng lên |
| TC-CART-04 | PASS - Số lượng sản phẩm tăng thành công |
| TC-CART-05 | PASS - Số lượng sản phẩm giảm thành công |
| TC-CART-06 | PASS - Sản phẩm bị xóa khỏi giỏ hàng |
| TC-CART-07 | PASS - Tổng tiền tính đúng |
| TC-CART-08 | PASS - Hiển thị thông báo giỏ hàng trống |
| TC-CART-09 | PASS - Mã giảm giá hợp lệ được áp dụng |
| TC-CART-10 | PASS - Hiển thị lỗi cho mã hết hạn |
| TC-CART-11 | PASS - Hiển thị lỗi cho mã không tồn tại |
| TC-CART-12 | PASS - Số tiền giảm được tính đúng |
| TC-CART-13 | PASS - Xử lý mã rỗng không gây lỗi |

## Lưu ý quan trọng

1. **Popup quảng cáo:** Script tự động đóng popup nếu có, nhưng nếu website thêm popup mới, cần cập nhật hàm `closePopup()`.
2. **Thời gian chờ:** Script sử dụng `WebUI.delay()` để chờ trang tải. Nếu mạng chậm, có thể tăng thời gian delay.
3. **Giỏ hàng:** Một số test case yêu cầu giỏ hàng có sản phẩm. Script tự động thêm sản phẩm nếu cần.
4. **Mã giảm giá:** Cần cập nhật mã giảm giá hợp lệ trước khi chạy các test case về voucher.

## Tác giả

- **Lương Hoài Sang** (23130270) - Nhóm trưởng, Nhóm 5
- Môn học: Kiểm thử phần mềm
- Website test: https://thienlong.vn
