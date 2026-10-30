import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.testobject.ConditionType as ConditionType
import com.kms.katalon.core.util.KeywordUtil as KeywordUtil
import com.kms.katalon.core.configuration.RunConfiguration as RunConfiguration
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import groovy.transform.Field
import org.openqa.selenium.By as By
import org.openqa.selenium.WebElement as WebElement

/**
 * ASM7 - Nhom chuc nang "Gio hang & Khuyen mai" (Cart & Promotion)
 * Gom 3 chuc nang trong 1 file:
 *   Chuc nang 7: Them san pham vao gio
 *   Chuc nang 8: Quan ly gio hang
 *   Chuc nang 9: Ma giam gia / Voucher
 * Website: https://thienlong.vn
 * Sinh vien: 23130270 - Luong Hoai Sang (Nhom 5)
 *
 * TC-CART-01: Them san pham vao gio tu trang chu
 * TC-CART-02: Them san pham vao gio tu trang chi tiet
 * TC-CART-03: Kiem tra so luong tren icon gio hang
 * TC-CART-04: Tang so luong san pham
 * TC-CART-05: Giam so luong san pham
 * TC-CART-06: Xoa san pham khoi gio
 * TC-CART-07: Kiem tra tong tien tam tinh
 * TC-CART-08: Kiem tra hien thi gio hang trong
 * TC-CART-09: Ap dung ma hop le
 * TC-CART-10: Ap dung ma het han
 * TC-CART-11: Ap dung ma khong ton tai
 * TC-CART-12: Kiem tra so tien duoc giam
 * TC-CART-13: Nhap ma rong (khoang trang)
 *
 * Cach chay: tao Test Case ten "CartAndPromotion" trong Katalon Studio, mo tab Script,
 * xoa het noi dung mac dinh, dan toan bo file nay vao, chon Chrome roi bam Run.
 * Luu y: project phai nam trong duong dan KHONG co chu co dau.
 *
 * CSS selector la selector "doan" theo cau truc Shopify. Neu FAIL vi khong tim thay
 * phan tu, dung Spy Web de lay selector that roi sua trong phan CAU HINH SELECTOR.
 */

// ============================================================================
// CAU HINH
// ============================================================================

@Field String BASE_URL = "https://thienlong.vn"
@Field String CART_URL = BASE_URL + "/cart"

// Ma giam gia - THAY ma hop le bang ma that dang hoat dong tren thienlong.vn
@Field String VALID_CODE   = "WELCOME10"
@Field String EXPIRED_CODE = "EXPIRED2025"
@Field String INVALID_CODE = "INVALIDCODE12345"

// Bo dem ket qua
@Field int totalTests  = 0
@Field int passedTests = 0
@Field int failedTests = 0

// ============================================================================
// CAU HINH SELECTOR (CSS)
// ============================================================================

@Field String SEL_POPUP_CLOSE = 'button.close, [data-dismiss="modal"], .modal-close, .popup-close, .close-popup, #close-icon, .btn-close, button[aria-label*="lose"], button[aria-label*="\u0110\u00F3ng"]'
@Field String SEL_ADD_TO_CART = 'button[class*="add-to-cart"], button[class*="add_to_cart"], button.product-form__submit, a[class*="quick-add"], button[type="submit"][name*="add"], input[type="submit"][value*="Th\u00EAm"]'
@Field String SEL_PRODUCT_LINK = 'a[href*="/products/"]'
@Field String SEL_CART_DRAWER = '[class*="cart-drawer"], [class*="cart-popup"], [class*="mini-cart"], [id*="cart-drawer"], [id*="quick-cart"]'
@Field String SEL_TOAST = '[class*="notification"], [class*="toast"], [class*="success-message"]'
@Field String SEL_CART_BADGE = '[class*="cart-count"], [class*="cart_count"], [class*="cart-badge"], [id*="cart-count"], .cart-count-bubble'
@Field String SEL_CART_TOTAL = '[class*="cart__subtotal"], [class*="cart-subtotal"], [class*="cart__total"], [class*="total-price"], .totals__subtotal-value'
@Field String SEL_CART_ITEM = '.cart-item, .cart__item, [class*="cart-item"]'
@Field String SEL_QTY_INPUT = 'input.qty, input[name="updates[]"], input[class*="quantity"], input[name*="quantity"], input[type="number"][name*="updates"]'
@Field String SEL_QTY_PLUS = 'button.qtyplus, .qtyplus, button[class*="increase"], button[class*="plus"], button[class*="qty-plus"], button[class*="increment"], button[aria-label*="\u0103ng"], button[aria-label*="ncrease"]'
@Field String SEL_QTY_MINUS = 'button.qtyminus, .qtyminus, button[class*="decrease"], button[class*="minus"], button[class*="qty-minus"], button[class*="decrement"], button[aria-label*="i\u1EA3m"], button[aria-label*="ecrease"]'
@Field String SEL_REMOVE = 'button[class*="remove"], button[class*="delete"], a[class*="remove"], a[class*="delete"], button[aria-label*="\u00F3a"], button[aria-label*="emove"], .icon-trash, .icon-delete'
@Field String SEL_EMPTY_CART = '[class*="empty-cart"], [class*="cart-empty"], [class*="cart__empty"], p[class*="empty"], .empty-message'
@Field String SEL_DISCOUNT_INPUT = 'input[class*="discount"], input[name*="discount"], input[placeholder*="m\u00E3"], input[placeholder*="M\u00E3"], input[placeholder*="voucher"], .discount-code input'
@Field String SEL_DISCOUNT_APPLY = 'button[class*="apply"], button[class*="discount"], [class*="discount-code"] button'
@Field String SEL_DISCOUNT_ERROR = '[class*="error"], [class*="invalid"], [class*="notice--danger"], [class*="alert-danger"], .form__message'
@Field String SEL_DISCOUNT_LINE = '[class*="discount-applied"], [class*="savings"], [class*="voucher-applied"], td[class*="discount"]'
@Field String SEL_MAIN = 'main, [class*="cart"]'

// ============================================================================
// HAM TIEN ICH
// ============================================================================

/** Tao TestObject tu CSS selector */
TestObject css(String name, String selector) {
    TestObject to = new TestObject(name)
    to.addProperty('css', ConditionType.EQUALS, selector)
    return to
}

/** Phan tu co ton tai trong DOM khong */
boolean present(TestObject to, int timeout = 5) {
    return WebUI.waitForElementPresent(to, timeout, FailureHandling.OPTIONAL)
}

/** Phan tu co dang hien thi khong */
boolean visible(TestObject to, int timeout = 5) {
    return WebUI.waitForElementVisible(to, timeout, FailureHandling.OPTIONAL)
}

/** So phan tu khop selector (0 neu khong co) */
int countElements(TestObject to, int timeout = 5) {
    try {
        return WebUI.findWebElements(to, timeout).size()
    } catch (Exception e) {
        return 0
    }
}

/** Click bang JavaScript (khong bi popup/modal chan: ElementClickIntercepted) */
void clickObj(TestObject to, int timeout = 10) {
    if (!present(to, timeout)) {
        throw new Exception("Kh\u00F4ng t\u00ECm th\u1EA5y ph\u1EA7n t\u1EED: " + to.getObjectId())
    }
    WebElement el = WebUI.findWebElement(to, 3)
    WebUI.executeJavaScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", Arrays.asList(el))
    WebUI.delay(1)
}

/** Nhap text vao o input */
void typeInto(TestObject to, String text) {
    if (!present(to, 10)) {
        throw new Exception("Kh\u00F4ng t\u00ECm th\u1EA5y \u00F4 nh\u1EADp: " + to.getObjectId())
    }
    WebUI.clearText(to)
    WebUI.setText(to, text)
    WebUI.delay(1)
}

/** Dong popup/modal quang cao neu co (click bang JavaScript) */
void closePopup() {
    TestObject popup = css('Popup Close', SEL_POPUP_CLOSE)
    if (visible(popup, 2)) {
        try {
            WebElement el = WebUI.findWebElement(popup, 2)
            WebUI.executeJavaScript("arguments[0].click();", Arrays.asList(el))
            WebUI.delay(1)
        } catch (Exception e) {
            // Khong dong duoc thi bo qua
        }
    }
}

/** Cho trang tai xong roi dong popup */
void waitForPageLoad() {
    try {
        WebUI.waitForPageLoad(30)
    } catch (Exception e) {
        WebUI.delay(3)
    }
    closePopup()
}

/** Ghi nhan ket qua test */
void addResult(String testCaseId, String description, boolean isPassed, String note = "") {
    totalTests++
    if (isPassed) {
        passedTests++
        KeywordUtil.logInfo("[PASS] ${testCaseId} - ${description}")
    } else {
        failedTests++
        KeywordUtil.markWarning("[FAIL] ${testCaseId} - ${description} - ${note}")
        try {
            WebUI.takeScreenshot(RunConfiguration.getReportFolder() + "/${testCaseId}_fail.png")
        } catch (Exception e) {
            // Bo qua loi chup anh
        }
    }
}

/** So luong tren icon gio hang (0 neu khong co badge) */
int getCartIconCount() {
    try {
        TestObject badge = css('Cart Badge', SEL_CART_BADGE)
        if (!present(badge, 3)) {
            return 0
        }
        String text = WebUI.getText(badge).trim()
        return text.matches("\\d+") ? Integer.parseInt(text) : 0
    } catch (Exception e) {
        return 0
    }
}

/** Tong tien hien thi trong gio hang (dang chuoi) */
String getCartTotal() {
    try {
        TestObject total = css('Cart Total', SEL_CART_TOTAL)
        if (!present(total, 5)) {
            return ""
        }
        return WebUI.getText(total).trim()
    } catch (Exception e) {
        return ""
    }
}

/** Chuyen chuoi gia (vd "1.250.000") thanh so */
long parsePrice(String priceText) {
    if (priceText == null) {
        return 0
    }
    String digits = priceText.replaceAll("[^0-9]", "")
    return digits.isEmpty() ? 0 : Long.parseLong(digits)
}

/** So luong cua dong san pham dau tien trong gio */
long readQty() {
    String v = WebUI.getAttribute(css('Qty Input', SEL_QTY_INPUT), 'value')
    return (v == null || v.trim().isEmpty()) ? 1 : Long.parseLong(v.trim())
}

/** Them san pham dau tien o trang chu vao gio (di qua trang chi tiet cho on dinh) */
void addFirstProductToCart() {
    WebUI.navigateToUrl(BASE_URL)
    waitForPageLoad()

    TestObject link = css('Product Link', SEL_PRODUCT_LINK)
    if (!present(link, 10)) {
        throw new Exception("Kh\u00F4ng t\u00ECm th\u1EA5y s\u1EA3n ph\u1EA9m n\u00E0o tr\u00EAn trang ch\u1EE7")
    }
    String href = WebUI.getAttribute(link, 'href')
    WebUI.navigateToUrl(href)
    waitForPageLoad()

    clickObj(css('Add To Cart', SEL_ADD_TO_CART))
    WebUI.delay(2)
}

/** Dam bao gio hang co it nhat 1 san pham, ket thuc o trang gio hang */
void ensureCartHasItem() {
    WebUI.navigateToUrl(CART_URL)
    waitForPageLoad()
    if (countElements(css('Cart Items', SEL_CART_ITEM), 5) == 0) {
        addFirstProductToCart()
        WebUI.navigateToUrl(CART_URL)
        waitForPageLoad()
    }
}

/** Nhap ma giam gia va bam ap dung */
void applyVoucher(String code) {
    typeInto(css('Discount Input', SEL_DISCOUNT_INPUT), code)
    clickObj(css('Discount Apply', SEL_DISCOUNT_APPLY))
    WebUI.delay(3)
}

/** Trang co hien loi ma giam gia khong (theo phan tu loi hien thi HOAC theo noi dung text) */
boolean hasVoucherError(List<String> keywords) {
    if (visible(css('Discount Error', SEL_DISCOUNT_ERROR), 5)) {
        return true
    }
    try {
        String text = WebUI.getText(css('Main Content', SEL_MAIN)).toLowerCase()
        for (String k : keywords) {
            if (text.contains(k)) {
                return true
            }
        }
    } catch (Exception e) {
        // Khong doc duoc noi dung
    }
    return false
}

// ============================================================================
// CHUC NANG 7: THEM SAN PHAM VAO GIO
// ============================================================================

/** TC-CART-01: Them san pham vao gio tu trang chu */
void test_AddToCart_FromHomepage() {
    String id = "TC-CART-01"
    String desc = "Th\u00EAm s\u1EA3n ph\u1EA9m v\u00E0o gi\u1ECF t\u1EEB trang ch\u1EE7"
    try {
        WebUI.navigateToUrl(BASE_URL)
        waitForPageLoad()
        int before = getCartIconCount()

        clickObj(css('Add To Cart Home', SEL_ADD_TO_CART))
        WebUI.delay(2)

        boolean success = visible(css('Cart Drawer', SEL_CART_DRAWER), 3) ||
                visible(css('Toast', SEL_TOAST), 2) ||
                getCartIconCount() > before

        if (!success) {
            // Cach chac chan nhat: vao trang gio hang xem co san pham khong
            WebUI.navigateToUrl(CART_URL)
            waitForPageLoad()
            success = countElements(css('Cart Items', SEL_CART_ITEM), 5) > 0
        }
        addResult(id, desc, success, success ? "" : "Kh\u00F4ng th\u1EA5y s\u1EA3n ph\u1EA9m \u0111\u01B0\u1EE3c th\u00EAm v\u00E0o gi\u1ECF")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-02: Them san pham vao gio tu trang chi tiet */
void test_AddToCart_FromProductDetail() {
    String id = "TC-CART-02"
    String desc = "Th\u00EAm s\u1EA3n ph\u1EA9m v\u00E0o gi\u1ECF t\u1EEB trang chi ti\u1EBFt"
    try {
        WebUI.navigateToUrl(BASE_URL)
        waitForPageLoad()

        TestObject link = css('Product Link', SEL_PRODUCT_LINK)
        if (!present(link, 10)) {
            addResult(id, desc, false, "Kh\u00F4ng t\u00ECm th\u1EA5y s\u1EA3n ph\u1EA9m tr\u00EAn trang ch\u1EE7")
            return
        }
        WebUI.navigateToUrl(WebUI.getAttribute(link, 'href'))
        waitForPageLoad()

        if (!WebUI.getUrl().contains("/products/")) {
            addResult(id, desc, false, "Kh\u00F4ng v\u00E0o \u0111\u01B0\u1EE3c trang chi ti\u1EBFt s\u1EA3n ph\u1EA9m")
            return
        }

        int before = getCartIconCount()
        clickObj(css('Add To Cart Detail', SEL_ADD_TO_CART))
        WebUI.delay(2)

        boolean success = WebUI.getUrl().contains("/cart") ||
                visible(css('Cart Drawer', SEL_CART_DRAWER), 3) ||
                getCartIconCount() > before

        if (!success) {
            WebUI.navigateToUrl(CART_URL)
            waitForPageLoad()
            success = countElements(css('Cart Items', SEL_CART_ITEM), 5) > 0
        }
        addResult(id, desc, success, success ? "" : "Kh\u00F4ng th\u1EA5y s\u1EA3n ph\u1EA9m \u0111\u01B0\u1EE3c th\u00EAm v\u00E0o gi\u1ECF")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-03: So luong tren icon gio hang tang sau khi them */
void test_CheckCartIconCount() {
    String id = "TC-CART-03"
    String desc = "Ki\u1EC3m tra s\u1ED1 l\u01B0\u1EE3ng tr\u00EAn icon gi\u1ECF h\u00E0ng"
    try {
        WebUI.navigateToUrl(BASE_URL)
        waitForPageLoad()
        int before = getCartIconCount()

        clickObj(css('Add To Cart Home', SEL_ADD_TO_CART))
        WebUI.delay(2)

        int after = getCartIconCount()
        boolean success = after > before
        addResult(id, desc, success, success ? "" : "S\u1ED1 l\u01B0\u1EE3ng tr\u01B0\u1EDBc: ${before}, sau: ${after}")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

// ============================================================================
// CHUC NANG 8: QUAN LY GIO HANG
// ============================================================================

/** TC-CART-04: Tang so luong */
void test_IncreaseQuantity() {
    String id = "TC-CART-04"
    String desc = "T\u0103ng s\u1ED1 l\u01B0\u1EE3ng s\u1EA3n ph\u1EA9m trong gi\u1ECF h\u00E0ng"
    try {
        ensureCartHasItem()
        long before = readQty()

        clickObj(css('Qty Plus', SEL_QTY_PLUS), 5)
        WebUI.delay(2)

        long after = readQty()
        boolean success = after > before
        addResult(id, desc, success, success ? "" : "S\u1ED1 l\u01B0\u1EE3ng tr\u01B0\u1EDBc: ${before}, sau: ${after}")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-05: Giam so luong */
void test_DecreaseQuantity() {
    String id = "TC-CART-05"
    String desc = "Gi\u1EA3m s\u1ED1 l\u01B0\u1EE3ng s\u1EA3n ph\u1EA9m trong gi\u1ECF h\u00E0ng"
    try {
        ensureCartHasItem()
        long before = readQty()

        // Can so luong > 1 de giam duoc
        if (before <= 1) {
            clickObj(css('Qty Plus', SEL_QTY_PLUS), 5)
            WebUI.delay(2)
            before = readQty()
        }

        clickObj(css('Qty Minus', SEL_QTY_MINUS), 5)
        WebUI.delay(2)

        long after = readQty()
        boolean success = after < before
        addResult(id, desc, success, success ? "" : "S\u1ED1 l\u01B0\u1EE3ng tr\u01B0\u1EDBc: ${before}, sau: ${after}")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-06: Xoa san pham khoi gio */
void test_RemoveProductFromCart() {
    String id = "TC-CART-06"
    String desc = "X\u00F3a s\u1EA3n ph\u1EA9m kh\u1ECFi gi\u1ECF h\u00E0ng"
    try {
        ensureCartHasItem()
        int before = countElements(css('Cart Items', SEL_CART_ITEM), 10)

        clickObj(css('Remove Button', SEL_REMOVE), 10)
        WebUI.delay(3)

        int after = countElements(css('Cart Items', SEL_CART_ITEM), 3)
        boolean success = after < before
        addResult(id, desc, success, success ? "" : "S\u1ED1 s\u1EA3n ph\u1EA9m tr\u01B0\u1EDBc: ${before}, sau: ${after}")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-07: Tong tien tam tinh = tong (don gia x so luong) */
void test_CheckCartSubtotal() {
    String id = "TC-CART-07"
    String desc = "Ki\u1EC3m tra t\u00EDnh t\u1ED5ng ti\u1EC1n t\u1EA1m t\u00EDnh"
    try {
        ensureCartHasItem()

        List<WebElement> items = WebUI.findWebElements(css('Cart Items', SEL_CART_ITEM), 10)
        long calculated = 0
        for (WebElement item : items) {
            try {
                String priceText = item.findElement(By.cssSelector('[class*="price"], [class*="money"]')).getText()
                String qtyText = item.findElement(By.cssSelector('input[type="number"], input[name*="quantity"]')).getAttribute('value')
                long qty = (qtyText == null || qtyText.trim().isEmpty()) ? 1 : Long.parseLong(qtyText.trim())
                calculated += parsePrice(priceText) * qty
            } catch (Exception e) {
                // Bo qua dong khong doc duoc
            }
        }

        long displayed = parsePrice(getCartTotal())
        boolean success = calculated > 0 && displayed > 0 && Math.abs(calculated - displayed) < 1000
        addResult(id, desc, success, success ? "" : "T\u00EDnh to\u00E1n: ${calculated}, Hi\u1EC3n th\u1ECB: ${displayed}")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-08: Gio hang trong hien thi thong bao */
void test_EmptyCart() {
    String id = "TC-CART-08"
    String desc = "Ki\u1EC3m tra hi\u1EC3n th\u1ECB gi\u1ECF h\u00E0ng tr\u1ED1ng"
    try {
        // Shopify: /cart/clear xoa toan bo gio hang
        WebUI.navigateToUrl(BASE_URL + "/cart/clear")
        waitForPageLoad()
        WebUI.navigateToUrl(CART_URL)
        waitForPageLoad()

        // Neu /cart/clear khong co tac dung thi xoa tung san pham
        for (int i = 0; i < 10 && countElements(css('Cart Items', SEL_CART_ITEM), 2) > 0; i++) {
            clickObj(css('Remove Button', SEL_REMOVE), 5)
            WebUI.delay(2)
        }

        boolean empty = visible(css('Empty Cart', SEL_EMPTY_CART), 5)
        if (!empty) {
            String text = WebUI.getText(css('Main Content', SEL_MAIN)).toLowerCase()
            empty = text.contains("ch\u01B0a c\u00F3 s\u1EA3n ph\u1EA9m") || text.contains("gi\u1ECF h\u00E0ng tr\u1ED1ng") ||
                    text.contains("kh\u00F4ng c\u00F3 s\u1EA3n ph\u1EA9m") || text.contains("empty")
        }
        boolean noItems = countElements(css('Cart Items', SEL_CART_ITEM), 3) == 0

        boolean success = empty && noItems
        addResult(id, desc, success, success ? "" : "Kh\u00F4ng th\u1EA5y th\u00F4ng b\u00E1o gi\u1ECF h\u00E0ng tr\u1ED1ng")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

// ============================================================================
// CHUC NANG 9: MA GIAM GIA / VOUCHER
// ============================================================================

/** TC-CART-09: Ma hop le duoc ap dung */
void test_ApplyValidVoucher() {
    String id = "TC-CART-09"
    String desc = "\u00C1p d\u1EE5ng m\u00E3 gi\u1EA3m gi\u00E1 h\u1EE3p l\u1EC7"
    try {
        ensureCartHasItem()
        long totalBefore = parsePrice(getCartTotal())

        applyVoucher(VALID_CODE)

        long totalAfter = parsePrice(getCartTotal())
        boolean success = (totalAfter > 0 && totalAfter < totalBefore) ||
                visible(css('Discount Line', SEL_DISCOUNT_LINE), 3)
        addResult(id, desc, success, success ? "" : "M\u00E3 kh\u00F4ng \u0111\u01B0\u1EE3c \u00E1p d\u1EE5ng (tr\u01B0\u1EDBc: ${totalBefore}, sau: ${totalAfter})")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-10: Ma het han bao loi */
void test_ApplyExpiredVoucher() {
    String id = "TC-CART-10"
    String desc = "\u00C1p d\u1EE5ng m\u00E3 gi\u1EA3m gi\u00E1 h\u1EBFt h\u1EA1n"
    try {
        ensureCartHasItem()
        applyVoucher(EXPIRED_CODE)

        boolean success = hasVoucherError(["kh\u00F4ng h\u1EE3p l\u1EC7", "invalid", "h\u1EBFt h\u1EA1n", "expired", "kh\u00F4ng t\u1ED3n t\u1EA1i"])
        addResult(id, desc, success, success ? "" : "Kh\u00F4ng th\u1EA5y th\u00F4ng b\u00E1o l\u1ED7i cho m\u00E3 h\u1EBFt h\u1EA1n")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-11: Ma khong ton tai bao loi */
void test_ApplyInvalidVoucher() {
    String id = "TC-CART-11"
    String desc = "\u00C1p d\u1EE5ng m\u00E3 gi\u1EA3m gi\u00E1 kh\u00F4ng t\u1ED3n t\u1EA1i"
    try {
        ensureCartHasItem()
        applyVoucher(INVALID_CODE)

        boolean success = hasVoucherError(["kh\u00F4ng h\u1EE3p l\u1EC7", "invalid", "kh\u00F4ng t\u1ED3n t\u1EA1i", "not found"])
        addResult(id, desc, success, success ? "" : "Kh\u00F4ng th\u1EA5y th\u00F4ng b\u00E1o l\u1ED7i cho m\u00E3 kh\u00F4ng t\u1ED3n t\u1EA1i")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-12: So tien giam > 0 sau khi ap dung ma hop le */
void test_CheckDiscountAmount() {
    String id = "TC-CART-12"
    String desc = "Ki\u1EC3m tra s\u1ED1 ti\u1EC1n \u0111\u01B0\u1EE3c gi\u1EA3m sau khi \u00E1p d\u1EE5ng voucher"
    try {
        ensureCartHasItem()
        long totalBefore = parsePrice(getCartTotal())
        if (totalBefore == 0) {
            addResult(id, desc, false, "Kh\u00F4ng th\u1EC3 l\u1EA5y t\u1ED5ng ti\u1EC1n")
            return
        }

        applyVoucher(VALID_CODE)

        long totalAfter = parsePrice(getCartTotal())
        long discount = totalBefore - totalAfter
        boolean success = discount > 0
        addResult(id, desc, success, success ? "Gi\u1EA3m: ${discount}" : "T\u1ED5ng tr\u01B0\u1EDBc: ${totalBefore}, T\u1ED5ng sau: ${totalAfter}")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

/** TC-CART-13: Ma rong (khoang trang) khong lam thay doi gio hang */
void test_EmptyVoucherCode() {
    String id = "TC-CART-13"
    String desc = "Nh\u1EADp m\u00E3 gi\u1EA3m gi\u00E1 r\u1ED7ng (kho\u1EA3ng tr\u1EAFng)"
    try {
        ensureCartHasItem()
        long totalBefore = parsePrice(getCartTotal())

        applyVoucher("   ")

        long totalAfter = parsePrice(getCartTotal())
        boolean success = WebUI.getUrl().contains("/cart") && totalBefore > 0 && totalAfter == totalBefore
        addResult(id, desc, success, success ? "" : "T\u1ED5ng ti\u1EC1n thay \u0111\u1ED5i ho\u1EB7c trang b\u1ECB l\u1ED7i (tr\u01B0\u1EDBc: ${totalBefore}, sau: ${totalAfter})")
    } catch (Exception e) {
        addResult(id, desc, false, e.getMessage())
    }
}

// ============================================================================
// CHAY TAT CA TEST CASE
// ============================================================================

/** Ghi log tong ket cua tung chuc nang */
void logGroupSummary(String groupName, int totalBefore, int passedBefore, int failedBefore) {
    KeywordUtil.logInfo("[${groupName}] T\u1ED5ng ${totalTests - totalBefore} | PASS ${passedTests - passedBefore} | FAIL ${failedTests - failedBefore}")
}

WebUI.openBrowser('')
WebUI.maximizeWindow()

try {
    KeywordUtil.logInfo("ASM7 - Cart & Promotion - 23130270 - L\u01B0\u01A1ng Ho\u00E0i Sang - https://thienlong.vn")

    // --- Chuc nang 7: Them san pham vao gio ---
    int t0 = totalTests, p0 = passedTests, f0 = failedTests
    test_AddToCart_FromHomepage()
    test_AddToCart_FromProductDetail()
    test_CheckCartIconCount()
    logGroupSummary("Th\u00EAm s\u1EA3n ph\u1EA9m v\u00E0o gi\u1ECF", t0, p0, f0)

    // --- Chuc nang 8: Quan ly gio hang ---
    t0 = totalTests; p0 = passedTests; f0 = failedTests
    test_IncreaseQuantity()
    test_DecreaseQuantity()
    test_RemoveProductFromCart()
    test_CheckCartSubtotal()
    test_EmptyCart()
    logGroupSummary("Qu\u1EA3n l\u00FD gi\u1ECF h\u00E0ng", t0, p0, f0)

    // --- Chuc nang 9: Ma giam gia / Voucher ---
    t0 = totalTests; p0 = passedTests; f0 = failedTests
    test_ApplyValidVoucher()
    test_ApplyExpiredVoucher()
    test_ApplyInvalidVoucher()
    test_CheckDiscountAmount()
    test_EmptyVoucherCode()
    logGroupSummary("M\u00E3 gi\u1EA3m gi\u00E1 / Voucher", t0, p0, f0)

    KeywordUtil.logInfo("K\u1EBET QU\u1EA2: T\u1ED5ng ${totalTests} | PASS ${passedTests} | FAIL ${failedTests}")
} finally {
    WebUI.closeBrowser()
}

if (failedTests > 0) {
    KeywordUtil.markFailed("C\u00F3 ${failedTests}/${totalTests} test case FAIL - xem Log Viewer \u0111\u1EC3 bi\u1EBFt chi ti\u1EBFt")
}
// Ket thuc chuong trinh