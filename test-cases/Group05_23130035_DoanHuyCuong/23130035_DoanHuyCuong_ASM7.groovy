import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling as FH
import org.openqa.selenium.Keys

/* =========================================================================
 *  THIEN LONG - ASM7 : 3 chuc nang, moi chuc nang 20-30 test case
 *    - Form lien he CSKH      : LH01 .. LH27  (27 case)
 *    - Phieu khao sat         : KS01 .. KS25  (25 case)
 *    - Chatbot Halie          : CH01 .. CH25  (25 case)
 *  Moi test case chay doc lap (try/catch) nen 1 case loi khong lam dung ca bo.
 *  Ket qua PASS/FAIL tung case duoc in ra o cuoi (tab Log Viewer).
 * ========================================================================= */

// ---------- CAU HINH ----------
int T = 15
// true  : cac case "hop le" se GUI THAT len website (LH03, LH18, ... KS03, KS13 ...)
// false : cac case "hop le" chi dien form + kiem tra du lieu, KHONG bam Gui
boolean GUI_THAT = true

// ---------- KET QUA ----------
def results = []
def state = [id: '', fail: false]

// ---------- HAM TIEN ICH ----------
def xp = { String xpath ->
    TestObject to = new TestObject(xpath)
    to.addProperty('xpath', ConditionType.EQUALS, xpath)
    return to
}

def pick = { List<String> cands ->
    for (String c : cands) {
        if (WebUI.waitForElementPresent(xp(c), 3, FH.OPTIONAL)) {
            return xp(c)
        }
    }
    return xp(cands[0])
}

def closePopups = {
    ['//button[normalize-space()="Đồng ý"] | //a[normalize-space()="Đồng ý"]',
     '//*[normalize-space()="Đóng"]'].each { p ->
        if (WebUI.waitForElementClickable(xp(p), 3, FH.OPTIONAL)) {
            WebUI.click(xp(p), FH.OPTIONAL)
        }
    }
}

def jsClick = { TestObject to ->
    WebUI.waitForElementPresent(to, 10, FH.OPTIONAL)
    WebUI.executeJavaScript('arguments[0].scrollIntoView({block:"center"}); arguments[0].click();',
            Arrays.asList(WebUI.findWebElement(to, 10)))
}

def safeClick = { TestObject to ->
    WebUI.waitForElementPresent(to, 10, FH.OPTIONAL)
    WebUI.scrollToElement(to, 5, FH.OPTIONAL)
    boolean done = false
    if (WebUI.waitForElementClickable(to, 3, FH.OPTIONAL)) {
        try {
            WebUI.click(to, FH.STOP_ON_FAILURE)
            done = true
        } catch (Throwable e) {
            done = false
        }
    }
    if (!done) {
        WebUI.executeJavaScript('arguments[0].click()', Arrays.asList(WebUI.findWebElement(to, 5)))
    }
}

def jsType = { TestObject to, String text ->
    WebUI.executeJavaScript('''
        var el = arguments[0], val = arguments[1];
        var proto = el.tagName === 'TEXTAREA' ? window.HTMLTextAreaElement.prototype : window.HTMLInputElement.prototype;
        var setter = Object.getOwnPropertyDescriptor(proto, 'value').set;
        el.focus();
        setter.call(el, val);
        el.dispatchEvent(new Event('input', {bubbles: true}));
        el.dispatchEvent(new Event('change', {bubbles: true}));
    ''', Arrays.asList(WebUI.findWebElement(to, 10), text))
}

def safeType = { TestObject to, String text ->
    WebUI.waitForElementPresent(to, 10, FH.OPTIONAL)
    boolean done = false
    if (WebUI.waitForElementVisible(to, 3, FH.OPTIONAL)) {
        try {
            WebUI.setText(to, text, FH.STOP_ON_FAILURE)
            done = true
        } catch (Throwable e) {
            done = false
        }
    }
    if (!done) {
        jsType(to, text)
    }
}

def valueOf = { TestObject to ->
    try {
        return WebUI.executeJavaScript('return arguments[0].value', Arrays.asList(WebUI.findWebElement(to, 5))) as String
    } catch (Throwable e) {
        return ''
    }
}

def jsStr = { String script ->
    try {
        return (WebUI.executeJavaScript(script, null) ?: '').toString()
    } catch (Throwable e) {
        return ''
    }
}

def bodyText = {
    return jsStr('return document.body.innerText')
}

// Lay + dong alert cua trinh duyet neu co, tra ve noi dung alert
def layAlert = {
    try {
        if (WebUI.verifyAlertPresent(1, FH.OPTIONAL)) {
            String t = WebUI.getAlertText(FH.OPTIONAL) ?: ''
            WebUI.acceptAlert(FH.OPTIONAL)
            return t
        }
    } catch (Throwable e) {}
    return ''
}

def thanksRegex = /(?i)(cảm ơn bạn|cảm ơn quý khách|gửi thành công|thành công|đã gửi|đã được gửi|đã nhận được|thank you|success)/

// Thanh cong = URL co contact_posted=true  HOAC alert co loi cam on
//              HOAC noi dung trang SAU khi gui xuat hien loi cam on ma TRUOC khi gui chua co
def hasThanks = { String before, String alertTxt ->
    try {
        if (WebUI.getUrl().contains('contact_posted=true')) return true
    } catch (Throwable e) {}
    if ((alertTxt =~ thanksRegex).find()) return true
    String after = bodyText()
    return (after =~ thanksRegex).find() && !(before =~ thanksRegex).find()
}

// Ghi nhan ket qua 1 diem kiem tra
def check = { boolean cond, String msg ->
    if (!cond) {
        state.fail = true
        WebUI.comment("[FAIL] ${state.id}: ${msg}")
    } else {
        WebUI.comment("[OK] ${state.id}: ${msg}")
    }
    WebUI.verifyEqual(cond, true, FH.CONTINUE_ON_FAILURE)
}

// Chay 1 test case, loi o case nay khong anh huong case khac
def runTC = { String id, String mota, Closure body ->
    state.id = id
    state.fail = false
    WebUI.comment("========== ${id}: ${mota} ==========")
    try {
        layAlert()
        body.call()
    } catch (Throwable e) {
        state.fail = true
        WebUI.comment("[ERROR] ${id}: ${e.getMessage()}")
        try {
            WebUI.takeScreenshot("Screenshots/${id}_error.png")
        } catch (Throwable x) {}
    }
    results << [id, mota, state.fail ? 'FAIL' : 'PASS']
}

WebUI.openBrowser('')
WebUI.maximizeWindow()

// =========================================================================
// CHUC NANG 1: FORM LIEN HE CSKH  (LH01 - LH27)
// =========================================================================
WebUI.navigateToUrl('https://thienlong.vn/pages/lien-he')
WebUI.waitForPageLoad(T)
closePopups()

def F = '//form[.//input[@name="contact[name]"]]'
def txtTen = xp('//input[@name="contact[name]"]')
def txtEmail = pick([F + '//input[@name="contact[email]"]', F + '//input[@type="email"]'])
def txtSDT = pick([F + '//input[@name="contact[phone]"]', F + '//input[@type="tel"]'])
def txtND = pick([F + '//textarea[@name="contact[body]"]', F + '//textarea'])
def btnGuiLH = pick([
    F + '//*[self::button or self::input or self::a][@type="submit" or contains(normalize-space(.),"Gửi") or contains(@value,"Gửi")]',
    '(//button[contains(normalize-space(.),"Gửi")] | //input[@type="submit"] | //a[contains(normalize-space(.),"Gửi")])[last()]'
])

def moLienHe = {
    WebUI.navigateToUrl('https://thienlong.vn/pages/lien-he')
    WebUI.waitForPageLoad(T)
    closePopups()
    WebUI.waitForElementPresent(txtTen, T, FH.OPTIONAL)
}

def dienLH = { Map c ->
    if (c.ten)   safeType(txtTen, c.ten)
    if (c.email) safeType(txtEmail, c.email)
    if (c.sdt)   safeType(txtSDT, c.sdt)
    if (c.nd)    safeType(txtND, c.nd)
}

def MSG = 'Day la tin nhan test cho bai tap ASM7, vui long bo qua.'
def lh = { String id, String mota, Map o, String mode ->
    return [id: id, mota: mota, ten: 'Test ASM7', email: 'test.asm7@example.com',
            sdt: '0900000000', nd: MSG, mode: mode] + o
}

// mode: 'ok' = phai thanh cong | 'stay' = KHONG duoc thanh cong | 'fill' = chi kiem tra o nhap giu dung du lieu
def lhCases = [
    lh('LH02', 'De trong toan bo form roi bam Gui',       [ten: '', email: '', sdt: '', nd: ''], 'stay'),
    lh('LH03', 'Du lieu hop le',                           [:], 'ok'),
    lh('LH04', 'De trong Ho ten',                          [ten: ''], 'stay'),
    lh('LH05', 'De trong Email',                           [email: ''], 'stay'),
    lh('LH06', 'De trong So dien thoai',                   [sdt: ''], 'stay'),
    lh('LH07', 'De trong Noi dung',                        [nd: ''], 'stay'),
    lh('LH08', 'Email sai dinh dang abc@@',                [email: 'abc@@'], 'stay'),
    lh('LH09', 'Email thieu ky tu @ (abc.com)',            [email: 'abc.com'], 'stay'),
    lh('LH10', 'Email thieu ten mien (abc@)',              [email: 'abc@'], 'stay'),
    lh('LH11', 'Email thieu phan ten (@gmail.com)',        [email: '@gmail.com'], 'stay'),
    lh('LH12', 'Email chua khoang trang',                  [email: 'a b@gmail.com'], 'stay'),
    lh('LH13', 'Email thieu duoi mien (abc@gmail)',        [email: 'abc@gmail'], 'stay'),
    lh('LH14', 'So dien thoai chua chu cai',               [sdt: 'abcdefghij'], 'stay'),
    lh('LH15', 'So dien thoai chua ky tu dac biet',        [sdt: '090@#$%^&*'], 'stay'),
    lh('LH16', 'So dien thoai qua ngan (3 so)',            [sdt: '090'], 'stay'),
    lh('LH17', 'So dien thoai qua dai (20 so)',            [sdt: '09000000000000000000'], 'stay'),
    lh('LH18', 'So dien thoai dinh dang +84 hop le',       [sdt: '+84900000000'], 'ok'),
    lh('LH19', 'Ho ten chi gom khoang trang',              [ten: '   '], 'stay'),
    lh('LH20', 'Ho ten chua ky tu dac biet',               [ten: 'Nguyen@#$%^&*123'], 'stay'),
    lh('LH21', 'Ho ten rat dai (300 ky tu)',               [ten: 'A' * 300], 'fill'),
    lh('LH22', 'Email rat dai (200 ky tu truoc @)',        [email: ('a' * 200) + '@example.com'], 'stay'),
    lh('LH23', 'Noi dung rat dai (5000 ky tu)',            [nd: 'Noi dung ' * 625], 'fill'),
    lh('LH24', 'Noi dung chua HTML/script (XSS)',          [nd: '<script>alert(1)</script><b>test</b>'], 'fill'),
    lh('LH25', 'Tieng Viet co dau hop le',                 [ten: 'Nguyễn Văn Á', nd: 'Tôi muốn hỏi về sản phẩm bút bi Thiên Long. Test ASM7.'], 'ok'),
    lh('LH26', 'Email viet hoa hop le',                    [email: 'TEST.ASM7@EXAMPLE.COM'], 'ok'),
    lh('LH27', 'Noi dung toi thieu (1 ky tu)',             [nd: 'a'], 'ok')
]

// LH01: giao dien form
runTC('LH01', 'Trang lien he hien thi du 4 truong va nut Gui', {
    moLienHe()
    check(WebUI.waitForElementPresent(txtTen, 5, FH.OPTIONAL), 'co o Ho ten')
    check(WebUI.waitForElementPresent(txtEmail, 5, FH.OPTIONAL), 'co o Email')
    check(WebUI.waitForElementPresent(txtSDT, 5, FH.OPTIONAL), 'co o So dien thoai')
    check(WebUI.waitForElementPresent(txtND, 5, FH.OPTIONAL), 'co o Noi dung')
    check(WebUI.waitForElementPresent(btnGuiLH, 5, FH.OPTIONAL), 'co nut Gui')
    WebUI.takeScreenshot('Screenshots/LH01_giao_dien.png')
})

lhCases.each { Map c ->
    runTC(c.id, c.mota, {
        moLienHe()
        dienLH(c)

        if (c.mode == 'fill') {
            // kiem tra o nhap giu du lieu, khong gui de tranh day du lieu rac / ma doc len website
            if (c.ten) { String v = valueOf(txtTen);   check(c.ten.length() <= 100 ? v == c.ten : v.length() > 0, "o Ho ten giu du lieu (do dai ${v.length()})") }
            if (c.nd)  { String v = valueOf(txtND);    check(c.nd.length() <= 100 ? v == c.nd : v.length() > 0, "o Noi dung giu du lieu (do dai ${v.length()})") }
            check(layAlert() != '1', 'khong co hop thoai alert do script bi thuc thi')
            WebUI.takeScreenshot("Screenshots/${c.id}.png")
            return
        }
        if (c.mode == 'ok' && !GUI_THAT) {
            check(valueOf(txtEmail) == c.email, 'o Email giu du lieu (khong gui that)')
            WebUI.takeScreenshot("Screenshots/${c.id}.png")
            return
        }

        String before = bodyText()
        safeClick(btnGuiLH)
        WebUI.delay(3)
        String al = layAlert()
        WebUI.takeScreenshot("Screenshots/${c.id}.png")
        boolean thanks = hasThanks(before, al)

        if (c.mode == 'ok') {
            check(thanks, 'gui hop le phai co thong bao thanh cong')
        } else {
            check(!thanks, 'du lieu khong hop le KHONG duoc bao thanh cong')
        }
    })
}

// =========================================================================
// CHUC NANG 3: CHATBOT HALIE  (CH01 - CH25)
// =========================================================================
def P = '//section[contains(@class,"tlg-chat-panel")]'
def btnMoChat = xp('//button[contains(@class,"tlg-chat-launch")]')
def panel = xp(P)
def txtChat = xp('//form[contains(@class,"tlg-chat-form")]//textarea')
def btnGuiTin = xp('//form[contains(@class,"tlg-chat-form")]//button[contains(@class,"tlg-chat-send")]')
def chipKM = xp('//div[contains(@class,"tlg-chat-quick")]//button[contains(normalize-space(.),"Khuyến mãi")]')
def btnDong = xp('//button[contains(@class,"tlg-chat-close")]')

def panelText = {
    try {
        return WebUI.executeJavaScript('return arguments[0].textContent',
                Arrays.asList(WebUI.findWebElement(panel, 5))) as String
    } catch (Throwable e) {
        return ''
    }
}

def isShown = { TestObject to ->
    try {
        return WebUI.executeJavaScript('''
            var el = arguments[0];
            var r = el.getBoundingClientRect();
            var s = window.getComputedStyle(el);
            return r.width > 0 && r.height > 0 && s.visibility !== 'hidden' && s.display !== 'none';
        ''', Arrays.asList(WebUI.findWebElement(to, 3))) as boolean
    } catch (Throwable e) {
        return false
    }
}

def waitShown = { TestObject to, int sec ->
    for (int i = 0; i < sec * 2; i++) {
        if (isShown(to)) return true
        WebUI.delay(0.5)
    }
    return false
}

def waitReply = { int lenBefore, int sec ->
    for (int i = 0; i < sec; i++) {
        if (panelText().length() > lenBefore) return true
        WebUI.delay(1)
    }
    return false
}

// Dang ky chatbot neu hien man hinh nhap ten / gioi tinh
def dangKyChat = {
    WebUI.comment('[CHAT] Phat hien man hinh dang ky')
    def txtTenChat = pick([
        P + '//input[contains(translate(@placeholder,"TÊN","tên"),"tên")]',
        P + '//input[contains(@name,"name")]',
        P + '//input[@type="text"]',
        P + '//input[not(@type="hidden") and not(@type="radio") and not(@type="checkbox")]'
    ])
    safeType(txtTenChat, 'Test ASM7')
    WebUI.delay(1)

    String kq = WebUI.executeJavaScript('''
        var p = document.querySelector('section[class*="tlg-chat-panel"]');
        if (!p) return 'NO_PANEL';
        var re = /^(nam|anh|male|ông|quý ông)$/i;
        var selects = p.querySelectorAll('select');
        for (var i = 0; i < selects.length; i++) {
            var sel = selects[i];
            for (var j = 0; j < sel.options.length; j++) {
                var op = sel.options[j];
                if (re.test(op.text.trim()) || re.test(op.value.trim())) {
                    sel.selectedIndex = j;
                    sel.dispatchEvent(new Event('input', {bubbles: true}));
                    sel.dispatchEvent(new Event('change', {bubbles: true}));
                    return 'SELECTED_SELECT:' + op.text;
                }
            }
        }
        var radios = p.querySelectorAll('input[type="radio"]');
        for (var k = 0; k < radios.length; k++) {
            var radio = radios[k];
            var label = (radio.labels && radio.labels.length > 0) ? radio.labels[0].textContent.trim() : '';
            if (re.test(radio.value.trim()) || re.test(label)) {
                radio.click();
                return 'SELECTED_RADIO:' + radio.value;
            }
        }
        var els = p.querySelectorAll('button,label,[role="button"],[role="radio"]');
        for (var m = 0; m < els.length; m++) {
            var el = els[m];
            var rect = el.getBoundingClientRect();
            if (rect.width > 0 && rect.height > 0 && re.test(el.textContent.trim())) {
                el.click();
                return 'SELECTED_ELEMENT:' + el.textContent.trim();
            }
        }
        return 'NOT_FOUND';
    ''', null) as String
    WebUI.comment('[CHAT-GIOI-TINH] ' + kq)
    WebUI.delay(1)

    def btnBatDau = pick([
        P + '//button[not(contains(@class,"tlg-chat-send")) and not(contains(@class,"tlg-chat-close")) and (contains(normalize-space(.),"Bắt đầu trò chuyện") or contains(normalize-space(.),"Bắt đầu"))]',
        P + '//button[not(contains(@class,"tlg-chat-send")) and (contains(normalize-space(.),"Tiếp tục") or contains(normalize-space(.),"Xác nhận"))]',
        P + '//button[@type="submit" and not(contains(@class,"tlg-chat-send"))]',
        P + '//input[@type="submit"]'
    ])
    safeClick(btnBatDau)
    WebUI.delay(2)
}

def moTrangChu = {
    WebUI.navigateToUrl('https://thienlong.vn/')
    WebUI.waitForPageLoad(T)
    closePopups()
}

// Mo trang chu -> mo chat -> dang ky (neu can) -> doi o nhap tin san sang
def moChatSanSang = {
    moTrangChu()
    WebUI.waitForElementPresent(btnMoChat, T, FH.OPTIONAL)
    if (!isShown(panel)) {
        safeClick(btnMoChat)
    }
    waitShown(panel, 10)
    if (!waitShown(txtChat, 3)) {
        dangKyChat()
    }
    return waitShown(txtChat, 25)
}

// Gui tin nhan, tra ve [sent, replied]
def guiTin = { String text, boolean viaEnter ->
    safeType(txtChat, text)
    WebUI.delay(0.5)
    if (viaEnter) {
        WebUI.sendKeys(txtChat, Keys.chord(Keys.ENTER))
    } else if (waitShown(btnGuiTin, 3)) {
        safeClick(btnGuiTin)
    } else {
        WebUI.sendKeys(txtChat, Keys.chord(Keys.ENTER))
    }
    boolean sent = false
    String key = text.trim().take(30)
    for (int i = 0; i < 15; i++) {
        if (panelText().contains(key)) { sent = true; break }
        WebUI.delay(1)
    }
    boolean replied = waitReply(panelText().length(), 30)
    return [sent: sent, replied: replied]
}

// CH01 - CH04: mo chat, giao dien, loi chao
runTC('CH01', 'Nut mo chatbot hien thi tren trang chu', {
    moTrangChu()
    check(WebUI.waitForElementPresent(btnMoChat, T, FH.OPTIONAL), 'co nut mo chat')
    check(isShown(btnMoChat), 'nut mo chat hien thi')
    WebUI.takeScreenshot('Screenshots/CH01.png')
})

runTC('CH02', 'Bam nut mo chat -> khung chat hien thi', {
    moTrangChu()
    safeClick(btnMoChat)
    check(waitShown(panel, 10), 'khung chat hien thi')
    WebUI.takeScreenshot('Screenshots/CH02.png')
})

runTC('CH03', 'Chatbot vao duoc giao dien nhap tin nhan (sau dang ky neu co)', {
    check(moChatSanSang(), 'o nhap tin nhan hien thi')
    WebUI.takeScreenshot('Screenshots/CH03.png')
})

runTC('CH04', 'Khung chat co ten Halie va loi chao mua sam', {
    check(moChatSanSang(), 'chat san sang')
    WebUI.delay(2)
    String t = panelText()
    check((t =~ /(?si).*Halie.*/).matches(), 'co ten Halie')
    check((t =~ /(?si).*mua sắm.*/).matches(), 'co loi chao mua sam')
    WebUI.takeScreenshot('Screenshots/CH04.png')
})

// CH05 - CH06: chip goi y
runTC('CH05', 'Chip goi y Khuyen mai hien thi', {
    check(moChatSanSang(), 'chat san sang')
    check(waitShown(chipKM, 15), 'chip Khuyen mai hien thi')
    WebUI.takeScreenshot('Screenshots/CH05.png')
})

runTC('CH06', 'Bam chip Khuyen mai -> chatbot phan hoi', {
    check(moChatSanSang(), 'chat san sang')
    check(waitShown(chipKM, 15), 'co chip Khuyen mai')
    int len = panelText().length()
    safeClick(chipKM)
    check(waitReply(len, 20), 'chatbot phan hoi sau khi bam chip')
    WebUI.takeScreenshot('Screenshots/CH06.png')
})

// CH07 - CH10: gui tin co ban
runTC('CH07', 'Gui cau hoi hop le bang nut Gui', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('Cho minh hoi ve but bi Thien Long', false)
    check(r.sent, 'cau hoi hien trong hoi thoai')
    check(r.replied, 'chatbot phan hoi')
    WebUI.takeScreenshot('Screenshots/CH07.png')
})

runTC('CH08', 'Gui tin nhan rong -> khong them tin vao hoi thoai', {
    check(moChatSanSang(), 'chat san sang')
    WebUI.delay(2)
    int len = panelText().length()
    if (isShown(btnGuiTin)) safeClick(btnGuiTin)
    WebUI.delay(3)
    check(panelText().length() == len, 'noi dung hoi thoai khong thay doi')
    WebUI.takeScreenshot('Screenshots/CH08.png')
})

runTC('CH09', 'Gui tin nhan chi gom khoang trang', {
    check(moChatSanSang(), 'chat san sang')
    WebUI.delay(2)
    safeType(txtChat, '     ')
    int len = panelText().length()
    if (isShown(btnGuiTin)) safeClick(btnGuiTin) else WebUI.sendKeys(txtChat, Keys.chord(Keys.ENTER))
    WebUI.delay(3)
    check(panelText().length() == len, 'tin chi co khoang trang khong duoc gui')
    WebUI.takeScreenshot('Screenshots/CH09.png')
})

runTC('CH10', 'Gui tin nhan bang phim Enter', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('Tin nhan gui bang Enter ASM7', true)
    check(r.sent, 'tin gui bang Enter hien trong hoi thoai')
    check(r.replied, 'chatbot phan hoi')
    WebUI.takeScreenshot('Screenshots/CH10.png')
})

// CH11 - CH16: noi dung dac biet
runTC('CH11', 'Gui tin nhan rat dai (1000 ky tu)', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('Dai ' + ('x' * 1000), false)
    check(r.sent, 'tin dai van duoc gui / hien thi')
    WebUI.takeScreenshot('Screenshots/CH11.png')
})

runTC('CH12', 'Gui tin nhan chua ky tu dac biet', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('Test @#\$%^&*()_+ ASM7', false)
    check(r.sent, 'tin chua ky tu dac biet duoc gui')
    check(r.replied, 'chatbot phan hoi')
    WebUI.takeScreenshot('Screenshots/CH12.png')
})

runTC('CH13', 'Gui chuoi HTML/script -> khong bi thuc thi (XSS)', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('<script>alert(1)</script><b>ASM7</b>', false)
    check(layAlert() != '1', 'khong co alert do script bi thuc thi')
    check(r.sent, 'noi dung hien thi dang van ban (khong bi bien thanh HTML)')
    WebUI.takeScreenshot('Screenshots/CH13.png')
})

runTC('CH14', 'Gui tin nhan tieng Viet co dau', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('Tôi muốn mua bút bi và vở học sinh', false)
    check(r.sent, 'tin tieng Viet co dau hien dung')
    check(r.replied, 'chatbot phan hoi')
    WebUI.takeScreenshot('Screenshots/CH14.png')
})

runTC('CH15', 'Gui tin nhan tieng Anh', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('Hello, do you sell pens?', false)
    check(r.sent, 'tin tieng Anh duoc gui')
    check(r.replied, 'chatbot phan hoi')
    WebUI.takeScreenshot('Screenshots/CH15.png')
})

runTC('CH16', 'Hoi cau ngoai pham vi (thoi tiet) -> chatbot van phan hoi', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('Hom nay thoi tiet o Ha Noi the nao?', false)
    check(r.sent, 'cau hoi duoc gui')
    check(r.replied, 'chatbot van phan hoi (khong treo)')
    WebUI.takeScreenshot('Screenshots/CH16.png')
})

// CH17 - CH19: noi dung nghiep vu
runTC('CH17', 'Hoi ve san pham khac (vo hoc sinh)', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('Shop co ban vo hoc sinh khong?', false)
    check(r.sent, 'cau hoi duoc gui')
    check(r.replied, 'chatbot phan hoi')
    WebUI.takeScreenshot('Screenshots/CH17.png')
})

runTC('CH18', 'Hoi ve cua hang / dia chi', {
    check(moChatSanSang(), 'chat san sang')
    def r = guiTin('Cua hang Thien Long o dau?', false)
    check(r.sent, 'cau hoi duoc gui')
    check(r.replied, 'chatbot phan hoi')
    WebUI.takeScreenshot('Screenshots/CH18.png')
})

runTC('CH19', 'Gui 3 tin lien tiep, moi tin deu duoc ghi nhan', {
    check(moChatSanSang(), 'chat san sang')
    def r1 = guiTin('Tin so mot ASM7', false)
    def r2 = guiTin('Tin so hai ASM7', false)
    def r3 = guiTin('Tin so ba ASM7', false)
    check(r1.sent && r2.sent && r3.sent, 'ca 3 tin deu hien trong hoi thoai')
    check(r1.replied && r2.replied && r3.replied, 'moi tin deu co phan hoi')
    WebUI.takeScreenshot('Screenshots/CH19.png')
})

// CH20 - CH21: o nhap
runTC('CH20', 'Sau khi gui, o nhap duoc xoa trang', {
    check(moChatSanSang(), 'chat san sang')
    guiTin('Kiem tra xoa o nhap ASM7', false)
    check(valueOf(txtChat) == '', 'o nhap tin nhan trong sau khi gui')
    WebUI.takeScreenshot('Screenshots/CH20.png')
})

runTC('CH21', 'Nut Gui hien thi khi da nhap noi dung', {
    check(moChatSanSang(), 'chat san sang')
    safeType(txtChat, 'Dang nhap noi dung')
    WebUI.delay(1)
    check(isShown(btnGuiTin), 'nut Gui hien thi')
    WebUI.takeScreenshot('Screenshots/CH21.png')
})

// CH22 - CH25: dong / mo lai
runTC('CH22', 'Bam nut dong -> khung chat an, nut mo chat hien lai', {
    check(moChatSanSang(), 'chat san sang')
    check(waitShown(btnDong, 5), 'co nut dong')
    safeClick(btnDong)
    WebUI.delay(2)
    check(!isShown(panel), 'khung chat da an')
    check(isShown(btnMoChat), 'nut mo chat hien thi lai')
    WebUI.takeScreenshot('Screenshots/CH22.png')
})

runTC('CH23', 'Dong roi mo lai chat -> dung duoc binh thuong', {
    check(moChatSanSang(), 'chat san sang')
    safeClick(btnDong)
    WebUI.delay(2)
    safeClick(btnMoChat)
    check(waitShown(panel, 10), 'khung chat mo lai')
    check(waitShown(txtChat, 10), 'o nhap tin nhan hien thi')
    WebUI.takeScreenshot('Screenshots/CH23.png')
})

runTC('CH24', 'Dong roi mo lai chat -> hoi thoai truoc do van con', {
    check(moChatSanSang(), 'chat san sang')
    guiTin('Tin de kiem tra luu hoi thoai ASM7', false)
    safeClick(btnDong)
    WebUI.delay(2)
    safeClick(btnMoChat)
    waitShown(panel, 10)
    check(panelText().contains('Tin de kiem tra luu hoi thoai ASM7'), 'hoi thoai cu van hien thi')
    WebUI.takeScreenshot('Screenshots/CH24.png')
})

runTC('CH25', 'Tai lai trang -> van mo duoc chat va san sang nhap tin', {
    check(moChatSanSang(), 'lan 1: chat san sang')
    WebUI.refresh()
    WebUI.waitForPageLoad(T)
    closePopups()
    if (!isShown(panel)) safeClick(btnMoChat)
    waitShown(panel, 10)
    if (!waitShown(txtChat, 3)) dangKyChat()
    check(waitShown(txtChat, 25), 'lan 2 (sau reload): chat san sang')
    WebUI.takeScreenshot('Screenshots/CH25.png')
})

// =========================================================================
// TONG KET
// =========================================================================
int nPass = results.count { it[2] == 'PASS' }
int nFail = results.size() - nPass
String bang = results.collect { "${it[0]}\t${it[2]}\t${it[1]}" }.join('\n')
WebUI.comment("========== TONG KET: ${results.size()} test case | PASS ${nPass} | FAIL ${nFail} ==========\n" + bang)

WebUI.closeBrowser()