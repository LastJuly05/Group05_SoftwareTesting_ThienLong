import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling as FH
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.configuration.RunConfiguration
import groovy.transform.Field

// ========================================================================
// CAU HINH
// ========================================================================
@Field final String URL_KS = 'https://thienlong.vn/pages/phieu-khao-sat-danh-gia'
@Field boolean GUI_THAT = false   // true = bam Gui that voi cac case 'ok'
@Field List RESULTS = []

// ========================================================================
// HAM PHU TRO
// ========================================================================
TestObject xp(String xpath) {
    TestObject to = new TestObject(xpath)
    to.addProperty('xpath', ConditionType.EQUALS, xpath)
    return to
}

TestObject pick(List<String> xpaths) {
    for (String x : xpaths) {
        TestObject to = xp(x)
        if (WebUI.verifyElementPresent(to, 2, FH.OPTIONAL)) {
            return to
        }
    }
    return xp(xpaths[0])
}

String jsStr(String script) {
    def r = WebUI.executeJavaScript(script, null)
    return (r == null) ? '' : r.toString()
}

void jsClick(TestObject to) {
    def el = WebUI.findWebElement(to, 10)
    WebUI.executeJavaScript('arguments[0].scrollIntoView({block:"center"}); arguments[0].click();', Arrays.asList(el))
}

void safeClick(TestObject to) {
    try {
        WebUI.scrollToElement(to, 5, FH.OPTIONAL)
        WebUI.click(to)
    } catch (Throwable e) {
        jsClick(to)
    }
}

void safeType(TestObject to, String text) {
    WebUI.waitForElementPresent(to, 10)
    WebUI.scrollToElement(to, 5, FH.OPTIONAL)
    def el = WebUI.findWebElement(to, 10)
    WebUI.executeJavaScript(
        'var e=arguments[0]; e.focus(); e.value=arguments[1];' +
        'e.dispatchEvent(new Event("input",{bubbles:true}));' +
        'e.dispatchEvent(new Event("change",{bubbles:true}));',
        Arrays.asList(el, text))
}

String valueOf(TestObject to) {
    def el = WebUI.findWebElement(to, 10)
    def v = WebUI.executeJavaScript('return arguments[0].value;', Arrays.asList(el))
    return v == null ? '' : v.toString()
}

String layAlert() {
    try {
        if (WebUI.waitForAlert(2)) {
            String t = WebUI.getAlertText()
            WebUI.acceptAlert()
            return t ?: ''
        }
    } catch (Throwable ignore) {
    }
    return ''
}

void closePopup() {
    try {
        WebUI.executeJavaScript(
            'var sel=["[aria-label=\'Close\']","[aria-label=\'Đóng\']",".popup-close",".modal-close",".close-popup","button.close"];' +
            'sel.forEach(function(s){document.querySelectorAll(s).forEach(function(e){try{e.click();}catch(x){}});});',
            null)
    } catch (Throwable ignore) {
    }
}

void moKS() {
    WebUI.navigateToUrl(URL_KS)
    WebUI.waitForPageLoad(15)
    closePopup()
}

TestObject btnGuiKS() {
    return pick([
        '//button[@type="submit" and normalize-space()="Gửi"]',
        '//button[normalize-space()="Gửi"]',
        '//input[@type="submit"]'
    ])
}

TestObject chkWebsite()  { xp('//input[@name="surveySource" and @value="Website"]') }
TestObject tKSTen()      { xp('(//input[@name="surveySource"])[1]/preceding::input[@type="text"][2]') }
TestObject tKSEmail()    { xp('(//input[@name="surveySource"])[1]/preceding::input[@type="text"][1]') }
TestObject taGopY(int i) { xp("(//textarea[not(@name)])[${i}]") }

boolean hasThanks(String before, String alertText) {
    String after = jsStr('return document.body.innerText').toLowerCase()
    String b = (before ?: '').toLowerCase()
    String a = (alertText ?: '').toLowerCase()
    List<String> kw = ['cảm ơn', 'cam on', 'thành công', 'thanh cong', 'thank']
    return kw.any { k -> a.contains(k) || (after.contains(k) && !b.contains(k)) }
}

void check(boolean cond, String msg) {
    if (cond) {
        KeywordUtil.logInfo("  [OK] ${msg}")
    } else {
        throw new AssertionError("  [FAIL] ${msg}".toString())
    }
}

void chup(String name) {
    try {
        String dir = RunConfiguration.getProjectDir() + '/Screenshots/'
        new File(dir).mkdirs()
        WebUI.takeScreenshot(dir + name + '.png')
    } catch (Throwable ignore) {
    }
}

void runTC(String id, String mota, Closure body) {
    KeywordUtil.logInfo("===== ${id}: ${mota} =====")
    try {
        body.call()
        RESULTS << [id, mota, 'PASS', '']
        KeywordUtil.logInfo("[PASS] ${id}")
    } catch (Throwable e) {
        RESULTS << [id, mota, 'FAIL', e.getMessage()]
        KeywordUtil.markWarning("[FAIL] ${id}: ${e.getMessage()}")
        chup(id + '_FAIL')
    }
}

// ========================================================================
// DU LIEU
// ========================================================================
@Field final List FULL = [4, 5, 4, 3, 5, 4, 5, 4, 4, 5, 5, 4, 4, 5, 5]
@Field final List ZERO = (1..15).collect { 0 }

@Field final Map KS_BASE = [
    ten  : 'Test ASM7',
    email: 'test.asm7@example.com',
    kenh : true,
    diem : FULL,
    total: 5,
    gy   : ['San pham chinh hang, giao hang nhanh', 'Nhieu uu dai', 'San pham mau sac', 'Test tu dong ASM7'],
    mode : 'ok'
]

Map ks(String id, String mota, Map o, String mode) {
    Map m = new LinkedHashMap(KS_BASE)
    m.putAll([id: id, mota: mota, mode: mode])
    m.putAll(o)
    return m
}

List minus(List base, int idx) {
    List l = new ArrayList(base)
    l[idx] = 0
    return l
}

void dienKS(Map s) {
    if (s.ten)   { safeType(tKSTen(), s.ten as String) }
    if (s.email) { safeType(tKSEmail(), s.email as String) }
    if (s.kenh)  { jsClick(chkWebsite()) }

    for (int i = 1; i <= 15; i++) {
        int d = s.diem[i - 1] as int
        if (d > 0) {
            jsClick(xp("//input[@name='radioq${i}' and @value='${d}']"))
        }
    }

    if ((s.total as int) > 0) {
        jsClick(xp("//input[@name='radiototalrate' and @value='${s.total}']"))
    }

    for (int i = 1; i <= 4; i++) {
        String g = s.gy[i - 1]
        if (g) { safeType(taGopY(i), g) }
    }
}

// mode: ok = hop le (phai thanh cong), stay = thieu/sai (khong duoc thanh cong), fill = chi kiem tra nhap lieu
List ksCases = [
    ks('KS02', 'Bam Gui khi chua dien/chon gi',
        [ten: '', email: '', kenh: false, diem: ZERO, total: 0, gy: ['', '', '', '']], 'stay'),
    ks('KS03', 'Dien day du du lieu hop le', [:], 'ok'),
    ks('KS04', 'De trong Ho ten', [ten: ''], 'stay'),
    ks('KS05', 'De trong Email', [email: ''], 'stay'),
    ks('KS06', 'Email sai dinh dang abc@@', [email: 'abc@@'], 'stay'),
    ks('KS07', 'Email thieu ky tu @ (abc.com)', [email: 'abc.com'], 'stay'),
    ks('KS08', 'Khong chon kenh mua hang', [kenh: false], 'stay'),
    ks('KS09', 'Khong danh gia tieu chi nao', [diem: ZERO, total: 0], 'stay'),
    ks('KS10', 'Bo sot tieu chi dau tien (q1)', [diem: minus(FULL, 0)], 'stay'),
    ks('KS11', 'Bo sot tieu chi cuoi cung (q15)', [diem: minus(FULL, 14)], 'stay'),
    ks('KS12', 'Chi danh gia 10/15 tieu chi', [diem: FULL.take(10) + [0, 0, 0, 0, 0]], 'stay'),
    ks('KS13', 'Khong chon danh gia chung', [total: 0], 'stay'),
    ks('KS14', 'Khong dien o gop y nao', [gy: ['', '', '', '']], 'ok'),
    ks('KS15', 'Tat ca tieu chi chon diem thap nhat (1)', [diem: (1..15).collect { 1 }, total: 1], 'ok'),
    ks('KS16', 'Tat ca tieu chi chon diem cao nhat (5)', [diem: (1..15).collect { 5 }, total: 5], 'ok'),
    ks('KS20', 'O gop y rat dai (3000 ky tu)', [gy: ['Gop y dai ' * 300, '', '', '']], 'fill'),
    ks('KS21', 'O gop y chua HTML/script (XSS)', [gy: ['<script>alert(1)</script><b>x</b>', '', '', '']], 'fill'),
    ks('KS22', 'Ho ten rat dai (300 ky tu)', [ten: 'A' * 300], 'fill')
]

// ========================================================================
// CHAY
// ========================================================================
WebUI.openBrowser('')
WebUI.maximizeWindow()

// KS01: giao dien
runTC('KS01', 'Trang hien thi nut Gui, 15 tieu chi va danh gia chung', {
    moKS()
    check(WebUI.waitForElementPresent(btnGuiKS(), 5, FH.OPTIONAL), 'Co nut Gui')

    String n = jsStr('return document.querySelectorAll("input[name^=\'radioq\']").length')
    check(n == '75', "Co 75 radio (15 tieu chi x 5 muc), thuc te ${n}")

    String t = jsStr('return document.querySelectorAll("input[name=\'radiototalrate\']").length')
    check(t == '5', "Co 5 lua chon Danh gia chung, thuc te ${t}")
    chup('KS01')
})

// KS02 - KS16, KS20 - KS22
ksCases.each { Map s ->
    runTC(s.id as String, s.mota as String, {
        moKS()
        dienKS(s)

        if (s.mode == 'fill') {
            if (s.ten) {
                check(valueOf(tKSTen()).length() > 0, 'O Ho ten giu du lieu')
            }
            if (s.gy[0]) {
                int len = valueOf(taGopY(1)).length()
                check(len > 0, "O gop y giu du lieu (${len} ky tu)")
            }
            check(layAlert() != '1', 'Khong co canh bao do script bi thuc thi')
            chup(s.id as String)
            return
        }

        if (s.mode == 'ok' && !GUI_THAT) {
            check(jsStr('return document.querySelectorAll("input[name^=\'radioq\']:checked").length') != '0',
                'Da chon diem (khong gui that)')
            chup(s.id as String)
            return
        }

        String before = jsStr('return document.body.innerText')
        safeClick(btnGuiKS())
        WebUI.delay(3)
        String al = layAlert()
        chup(s.id as String)
        boolean thanks = hasThanks(before, al)

        if (s.mode == 'ok') {
            check(thanks, 'Gui hop le phai co thong bao thanh cong')
        } else {
            check(!thanks, 'Thieu/sai du lieu KHONG duoc bao thanh cong')
        }
    })
}

// KS17: radio tieu chi chi chon 1 gia tri
runTC('KS17', 'Moi tieu chi chi chon duoc 1 muc (chon 2 roi 3)', {
    moKS()
    jsClick(xp("//input[@name='radioq1' and @value='2']"))
    jsClick(xp("//input[@name='radioq1' and @value='3']"))

    check(jsStr('return document.querySelectorAll("input[name=\'radioq1\']:checked").length') == '1',
        'Chi 1 radio q1 duoc chon')
    check(jsStr('var e=document.querySelector("input[name=\'radioq1\']:checked"); return e?e.value:""') == '3',
        'Gia tri giu lai la 3')
    chup('KS17')
})

// KS18: doi danh gia chung
runTC('KS18', 'Doi lua chon Danh gia chung (1 -> 5)', {
    moKS()
    jsClick(xp("//input[@name='radiototalrate' and @value='1']"))
    jsClick(xp("//input[@name='radiototalrate' and @value='5']"))

    check(jsStr('var e=document.querySelector("input[name=\'radiototalrate\']:checked"); return e?e.value:""') == '5',
        'Danh gia chung = 5')
    chup('KS18')
})

// KS19: checkbox nhieu kenh
runTC('KS19', 'Chon duoc nhieu kenh mua hang (checkbox)', {
    moKS()
    String tong = jsStr('return document.querySelectorAll("input[name=\'surveySource\']").length')

    WebUI.executeJavaScript(
        'document.querySelectorAll("input[name=\'surveySource\']").forEach(function(e){ if(!e.checked) e.click(); })', null)

    String daChon = jsStr('return document.querySelectorAll("input[name=\'surveySource\']:checked").length')
    check(tong != '0' && tong == daChon, "Chon duoc ${daChon}/${tong} kenh mua hang")
    chup('KS19')
})

// ========================================================================
// TONG KET
// ========================================================================
WebUI.closeBrowser()

int soFail = RESULTS.count { it[2] == 'FAIL' } as int
KeywordUtil.logInfo('================ TONG KET ================')
RESULTS.each { KeywordUtil.logInfo("${it[0]} | ${it[2]} | ${it[1]} ${it[3] ? '| ' + it[3] : ''}") }
KeywordUtil.logInfo("PASS: ${RESULTS.size() - soFail} / ${RESULTS.size()}  -  FAIL: ${soFail}")

if (soFail > 0) {
    KeywordUtil.markFailed("Co ${soFail} test case FAIL, xem log o tren")
}