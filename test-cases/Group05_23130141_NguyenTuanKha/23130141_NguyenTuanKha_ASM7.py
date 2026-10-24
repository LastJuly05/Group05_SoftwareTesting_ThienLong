"""
ASM7 - Automation script (Playwright + Python)
Website : https://thienlong.vn
Chuc nang: Dang nhap / Dang xuat
SV      : 23130141 - Nguyen Tuan Kha  (Nhom 5)

Cach chay:
    pip install playwright
    python -m playwright install chromium

    # Tai khoan test (KHONG ghi mat khau vao file, KHONG day len GitHub)
    Windows CMD:        set TL_EMAIL=email_test@gmail.com
                        set TL_PASSWORD=mat_khau_test
    Windows PowerShell: $env:TL_EMAIL="email_test@gmail.com"; $env:TL_PASSWORD="mat_khau_test"
    macOS/Linux:        export TL_EMAIL=... TL_PASSWORD=...

    python 23130141_NguyenTuanKha_ASM7.py

Neu khong dat TL_EMAIL / TL_PASSWORD thi cac test can tai khoan se duoc SKIP.
Bien tuy chon: SLOW_MO (ms, mac dinh 400), HEADLESS=1 (an trinh duyet), TRACE=1 (in chi tiet loi).
"""
import os
import re
import sys
import time
import traceback

from playwright.sync_api import sync_playwright

# Tranh loi ma hoa khi in tieng Viet ra console Windows / khi chuyen huong ra file
try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:  # noqa: BLE001
    pass

# ---------------------------------------------------------------- cau hinh
BASE = os.getenv("TL_BASE", "https://thienlong.vn")
LOGIN_URL = f"{BASE}/account/login"
REGISTER_PATH = "/account/register"
ACCOUNT_URL = f"{BASE}/account"
EMAIL = os.getenv("TL_EMAIL")
PASSWORD = os.getenv("TL_PASSWORD")
HEADLESS = os.getenv("HEADLESS", "0") == "1"
SLOW_MO = int(os.getenv("SLOW_MO", "400"))  # ms giua cac thao tac, de quay video
TIMEOUT = 15000  # ms
SHOT_DIR = "screenshots"

ERR_TEXT = "Thông tin đăng nhập không hợp lệ"

results = []  # (id, mo ta, ket qua, ghi chu)


# ---------------------------------------------------------------- tien ich
def run_case(case_id, desc, func, page, needs_login=False):
    """Chay mot test case, ghi PASS/FAIL/SKIP va chup anh khi loi."""
    if needs_login and not (EMAIL and PASSWORD):
        results.append((case_id, desc, "SKIP", "Chua dat TL_EMAIL/TL_PASSWORD"))
        print(f"[SKIP] {case_id} - {desc}")
        return
    try:
        func(page)
        results.append((case_id, desc, "PASS", ""))
        print(f"[PASS] {case_id} - {desc}")
    except Exception as exc:  # noqa: BLE001
        os.makedirs(SHOT_DIR, exist_ok=True)
        shot = os.path.join(SHOT_DIR, f"{case_id}.png")
        try:
            page.screenshot(path=shot)
        except Exception:  # noqa: BLE001
            pass
        note = str(exc).strip().splitlines()[0] if str(exc).strip() else repr(exc)
        results.append((case_id, desc, "FAIL", note))
        print(f"[FAIL] {case_id} - {desc}\n       {note}")
        if os.getenv("TRACE"):
            traceback.print_exc()


def check(condition, message):
    if not condition:
        raise AssertionError(message)


def settle(page):
    """Cho trang on dinh; bo qua neu trang co script chay lien tuc (chat, quang cao)."""
    try:
        page.wait_for_load_state("networkidle", timeout=5000)
    except Exception:  # noqa: BLE001
        pass


def goto(page, url):
    page.goto(url, wait_until="domcontentloaded", timeout=45000)
    settle(page)


def email_box(page):
    return page.locator("input[placeholder='Nhập Email']:visible").first


def password_box(page):
    return page.locator("input[placeholder='Nhập Mật khẩu']:visible").first


def click_login_button(page):
    page.get_by_role("button", name=re.compile(r"^Đăng nhập$", re.I)).locator(
        "visible=true"
    ).first.click()


def validity(locator, flag):
    """Doc co kiem tra hop le HTML5 cua trinh duyet (valueMissing, typeMismatch...)."""
    return locator.evaluate(f"el => el.validity.{flag}")


def open_login(page):
    """Dam bao chua dang nhap roi mo trang Dang nhap."""
    page.context.clear_cookies()
    goto(page, LOGIN_URL)


def submit_login(page, email, password, expect_success=False):
    """Mo trang Dang nhap, nhap thong tin, bam nut Dang nhap.
    expect_success=True: cho trang chuyen khoi /account/login (dang nhap co the chay ngam)."""
    open_login(page)
    email_box(page).fill(email)
    password_box(page).fill(password)
    click_login_button(page)
    if expect_success:
        wait_leave_login(page)
    settle(page)


def wait_leave_login(page, timeout=15000):
    try:
        page.wait_for_url(lambda url: "/account/login" not in url, timeout=timeout)
    except Exception:  # noqa: BLE001
        pass


def page_hints(page):
    """Lay cac dong thong bao loi tren trang de ghi vao ly do FAIL."""
    try:
        text = page.inner_text("body")
    except Exception:  # noqa: BLE001
        return ""
    keys = re.compile(r"không hợp lệ|lỗi|captcha|robot|yêu cầu|thử lại|khóa|sai|thất bại", re.I)
    lines = [ln.strip() for ln in text.splitlines() if ln.strip() and keys.search(ln)]
    return " | ".join(lines[:3])


def is_logged_in(page):
    """Da dang nhap neu vao /account khong bi day ve trang dang nhap."""
    goto(page, ACCOUNT_URL)
    return "login" not in page.url


def logout_link(page):
    """Nut 'Dang xuat' dang hien thi (bo qua ban an trong menu dau trang)."""
    link = page.get_by_text("Đăng xuất").locator("visible=true").first
    try:
        link.wait_for(timeout=6000)
    except Exception:  # noqa: BLE001
        # Nam trong menu 'Tai khoan cua toi' tren dau trang: phai CLICK de mo menu
        page.get_by_text("Tài khoản của tôi").locator("visible=true").first.click()
        link.wait_for(timeout=TIMEOUT)
    return link


def check_login_failed(page, why):
    """Dang nhap that bai: o lai trang Dang nhap va chua co phien dang nhap."""
    check("/account/login" in page.url, f"{why}: khong o lai trang Dang nhap")
    check(not is_logged_in(page), f"{why}: nhung van dang nhap duoc")


# ---------------------------------------------------------------- TEST CASES (ID theo file Excel)
def log_1(page):
    """Hien thi form Dang nhap."""
    open_login(page)
    check(email_box(page).is_visible(), "Thieu o Email")
    check(password_box(page).is_visible(), "Thieu o Mat khau")
    check(page.get_by_role("button", name=re.compile(r"^Đăng nhập$", re.I)).first.is_visible(),
          "Thieu nut Dang nhap")
    check(page.get_by_text("Quên mật khẩu").first.is_visible(), "Thieu link Quen mat khau")
    check(page.get_by_text("Đăng ký tại đây").first.is_visible(), "Thieu link Dang ky")


def log_2(page):
    """Dang nhap thanh cong."""
    submit_login(page, EMAIL, PASSWORD, expect_success=True)
    check("/account/login" not in page.url,
          "Van o trang Dang nhap, dang nhap that bai. Trang bao: " + (page_hints(page) or "(khong co thong bao)")
          + f" [script doc: email {len(EMAIL)} ky tu, mat khau {len(PASSWORD)} ky tu]")
    check(is_logged_in(page), "Vao trang Tai khoan bi day ve trang Dang nhap")
    check(logout_link(page).is_visible(), "Khong thay nut Dang xuat sau khi dang nhap")


def log_3(page):
    """Dung email, sai mat khau."""
    submit_login(page, EMAIL, PASSWORD + "_sai")
    page.get_by_text(ERR_TEXT).first.wait_for(timeout=TIMEOUT)
    check_login_failed(page, "Sai mat khau")


def log_4(page):
    """Email chua dang ky."""
    fake = f"khong.ton.tai.{int(time.time())}@example.com"
    submit_login(page, fake, "Sai@12345")
    page.get_by_text(ERR_TEXT).first.wait_for(timeout=TIMEOUT)
    check_login_failed(page, "Email chua dang ky")


def log_6(page):
    """De trong ca Email va Mat khau."""
    open_login(page)
    click_login_button(page)
    check("/account/login" in page.url, "Form da bi gui di khi de trong")
    check(validity(email_box(page), "valueMissing"), "O Email khong bao bat buoc")


def log_7(page):
    """Bo trong Email (chi nhap Mat khau)."""
    open_login(page)
    password_box(page).fill("Test@12345")
    click_login_button(page)
    check("/account/login" in page.url, "Form da bi gui di khi thieu Email")
    check(validity(email_box(page), "valueMissing"), "O Email khong bao bat buoc")


def log_8(page):
    """Bo trong Mat khau (chi nhap Email)."""
    open_login(page)
    email_box(page).fill("abc@example.com")
    click_login_button(page)
    check("/account/login" in page.url, "Form da bi gui di khi thieu Mat khau")
    check(validity(password_box(page), "valueMissing"), "O Mat khau khong bao bat buoc")


def log_9(page):
    """Email sai dinh dang (thieu @)."""
    open_login(page)
    email_box(page).fill("abc")
    password_box(page).fill("Test@12345")
    click_login_button(page)
    settle(page)
    # Doc canh bao TRUOC khi roi trang: trinh duyet bao sai dinh dang HOAC trang hien thong bao loi
    on_login = "/account/login" in page.url
    flagged = validity(email_box(page), "typeMismatch") if on_login else False
    shown = page.get_by_text(ERR_TEXT).count() > 0
    check(flagged or shown, "Khong co canh bao nao cho email sai dinh dang")
    check_login_failed(page, "Email sai dinh dang")


def log_10(page):
    """Mat khau phan biet chu hoa/chu thuong."""
    swapped = PASSWORD.swapcase()
    check(swapped != PASSWORD, "Mat khau test khong co chu cai, bo qua test nay")
    submit_login(page, EMAIL, swapped)
    page.get_by_text(ERR_TEXT).first.wait_for(timeout=TIMEOUT)
    check_login_failed(page, "Mat khau doi hoa/thuong")


def log_11(page):
    """Chuoi ky tu dac biet (SQL injection, script) khong lam dang nhap duoc / khong chay script."""
    alerts = []

    def on_dialog(dialog):
        alerts.append(dialog.message)
        dialog.dismiss()

    page.on("dialog", on_dialog)
    try:
        submit_login(page, "admin'--@example.com", "' OR '1'='1")
        check_login_failed(page, "SQL injection")
        submit_login(page, "a@example.com", "<script>alert(1)</script>")
        check_login_failed(page, "Script trong mat khau")
        check(not alerts, f"Script bi thuc thi (alert: {alerts})")
    finally:
        page.remove_listener("dialog", on_dialog)


def log_12(page):
    """Mat khau duoc che khi nhap."""
    open_login(page)
    check(password_box(page).get_attribute("type") == "password", "O Mat khau khong che ky tu")


def log_13(page):
    """Nhan Enter o o Mat khau de dang nhap."""
    open_login(page)
    email_box(page).fill(EMAIL)
    password_box(page).fill(PASSWORD)
    password_box(page).press("Enter")
    wait_leave_login(page)
    settle(page)
    check(is_logged_in(page), "Nhan Enter khong dang nhap duoc")


def log_19(page):
    """Link 'Dang ky tai day' chuyen den trang Dang ky."""
    open_login(page)
    page.get_by_text("Đăng ký tại đây").first.click()
    page.wait_for_url(re.compile(REGISTER_PATH), timeout=TIMEOUT)


def log_20(page):
    """Link 'Quen mat khau? Nhan vao day' mo trang Dat lai mat khau."""
    open_login(page)
    page.get_by_text("đây", exact=True).first.click()
    page.get_by_text("ĐẶT LẠI MẬT KHẨU").first.wait_for(timeout=TIMEOUT)
    check(page.get_by_text("Lấy lại mật khẩu").first.is_visible(), "Thieu nut Lay lai mat khau")


def log_24(page):
    """Link 'Quay lai' tren trang Dat lai mat khau."""
    open_login(page)
    page.get_by_text("đây", exact=True).first.click()
    page.get_by_text("ĐẶT LẠI MẬT KHẨU").first.wait_for(timeout=TIMEOUT)
    page.get_by_text("Quay lại").first.click()
    email_box(page).wait_for(timeout=TIMEOUT)
    check("/account/login" in page.url, "Quay lai khong ve trang Dang nhap")


def log_27(page):
    """Dang xuat tu menu Tai khoan."""
    submit_login(page, EMAIL, PASSWORD, expect_success=True)
    check(is_logged_in(page), "Khong dang nhap duoc de test dang xuat")
    logout_link(page).click()
    settle(page)
    check(not is_logged_in(page), "Sau khi dang xuat van vao duoc trang Tai khoan")


def log_28(page):
    """Sau khi dang xuat, bam nut Back cua trinh duyet."""
    submit_login(page, EMAIL, PASSWORD, expect_success=True)
    check(is_logged_in(page), "Khong dang nhap duoc de test")
    logout_link(page).click()
    settle(page)
    page.go_back()
    page.reload()
    settle(page)
    check("login" in page.url or page.get_by_text("Đăng xuất").count() == 0,
          "Bam Back sau dang xuat van thay noi dung tai khoan")


def log_29(page):
    """Truy cap truc tiep trang Tai khoan khi chua dang nhap."""
    page.context.clear_cookies()
    goto(page, ACCOUNT_URL)
    check("login" in page.url, "Chua dang nhap nhung khong bi chuyen ve trang Dang nhap")


# ---------------------------------------------------------------- chay tat ca
def main():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=HEADLESS, slow_mo=SLOW_MO)
        context = browser.new_context(viewport={"width": 1366, "height": 800}, locale="vi-VN")
        context.set_default_timeout(TIMEOUT)
        page = context.new_page()

        print("=== DANG NHAP / DANG XUAT (thienlong.vn) ===")
        run_case("LOG-1", "Hien thi form Dang nhap", log_1, page)
        run_case("LOG-2", "Dang nhap thanh cong", log_2, page, needs_login=True)
        run_case("LOG-3", "Dung email, sai mat khau", log_3, page, needs_login=True)
        run_case("LOG-4", "Email chua dang ky", log_4, page)
        run_case("LOG-6", "De trong Email va Mat khau", log_6, page)
        run_case("LOG-7", "Bo trong Email", log_7, page)
        run_case("LOG-8", "Bo trong Mat khau", log_8, page)
        run_case("LOG-9", "Email sai dinh dang", log_9, page)
        run_case("LOG-10", "Mat khau phan biet hoa/thuong", log_10, page, needs_login=True)
        run_case("LOG-11", "Ky tu dac biet (SQLi, script)", log_11, page)
        run_case("LOG-12", "Mat khau duoc che khi nhap", log_12, page)
        run_case("LOG-13", "Nhan Enter de dang nhap", log_13, page, needs_login=True)
        run_case("LOG-19", "Link 'Dang ky tai day'", log_19, page)
        run_case("LOG-20", "Link 'Quen mat khau'", log_20, page)
        run_case("LOG-24", "Link 'Quay lai' o trang Dat lai mat khau", log_24, page)
        run_case("LOG-27", "Dang xuat", log_27, page, needs_login=True)
        run_case("LOG-28", "Bam Back sau khi dang xuat", log_28, page, needs_login=True)
        run_case("LOG-29", "Vao trang Tai khoan khi chua dang nhap", log_29, page)

        context.close()
        browser.close()

    passed = sum(1 for r in results if r[2] == "PASS")
    failed = sum(1 for r in results if r[2] == "FAIL")
    skipped = sum(1 for r in results if r[2] == "SKIP")
    print("\n=== TONG KET ===")
    print(f"PASS: {passed}   FAIL: {failed}   SKIP: {skipped}   TONG: {len(results)}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
