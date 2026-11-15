# NewhtcDRIDI

ساعة وطقس بأسلوب شاشة **HTC Diamond (TouchFLO 3D)** لأندرويد.
A weather clock for Android in the style of the HTC Diamond home screen.

---

## العربية

### ما الجديد في الإصدار 1.1.0
النسخة 1.0.0 كانت شاشة ثابتة (كل القيم مكتوبة يدويًا ولا يوجد إذن إنترنت). الآن:

- **ساعة حية** بتوقيت المدينة المختارة (24 أو 12 ساعة حسب إعدادات الجهاز) مع نقطتين تومضان كما في الأصل.
- **طقس حقيقي** من [Open-Meteo](https://open-meteo.com/) بدون مفتاح API: الحرارة، الإحساس، الرطوبة، الرياح، وحالة الجو.
- **توقعات 5 أيام** مع العظمى والصغرى.
- **اختيار المدينة**: قائمة مدن تونسية جاهزة (تعمل دون إنترنت) + بحث عن أي مدينة في العالم.
- **أيقونات طقس مرسومة بالكود** (شمس، قمر، غيوم، مطر، ثلج، رعد، ضباب) بحركة هادئة، دون أي اعتماد على خطوط الإيموجي.
- **يعمل دون اتصال**: آخر بيانات ناجحة تُحفظ وتُعرض مع وقت التحديث.
- **عربي/إنجليزي ودعم RTL** كامل.
- **إصلاحات**: إضافة إذن `INTERNET` المفقود، معالجة هوامش النظام في أندرويد 15 (كان المحتوى قد يختفي تحت شريط الحالة)، `allowBackup=false`، أيقونة تطبيق (adaptive)، وتفعيل R8 لنسخة release.

### تحسينات إضافية (تطوير ما بعد 1.1.0)
- **فصل المنطق عن إطار أندرويد**: تحويلات JSON صارت عبر طبقة `Json` داخلية (تغلف `org.json`)، والاختبارات تعمل فعليًا دون أندرويد (كانت ترمي "not mocked").
- **بحث مدينة يعمل دائمًا**: زر بحث صريح بجانب حقل الإدخال (يعمل حتى لو لوحة المفاتيح لا تملك إجراء بحث أو كانت عربية)، مع دعم IME Search، ورسالة عند كتابة أقل من حرفين.
- **كشف حالة الشبكة**: فشل فوري عند عدم الاتصال بدل انتظار انتهاء المهلة (10 ثوانٍ)، وتمييز واضح بين "آخر تحديث" و"غير متصل".
- **مفتاح مدينة مستقر**: `cityKey` يُشتق تلقائيًا من الإحداثيات (تقريب 4 خانات) فلا تتأثر البيانات المخزّنة أو اختيار المدينة بفروق إحداثية صغيرة.
- **نظافة الكود**: إزالة `Key` اليدوي من `City`، توحيد وحدات الحرارة عبر `Units`، معالجة أخطاء التخزين المؤقت بشكل صريح.

### الاستخدام
| الإجراء | النتيجة |
|---|---|
| الضغط على اسم المدينة | فتح قائمة اختيار/بحث المدن |
| الضغط على درجة الحرارة | التبديل بين °C و °F (يُحفظ) |
| الضغط على الأيقونة أو سطر الحالة | تحديث فوري |

يتحدّث الطقس تلقائيًا كل 30 دقيقة أثناء فتح التطبيق.

### البناء
المتطلبات: JDK 17 وAndroid SDK 35.

```bash
gradle assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
gradle testDebugUnitTest      # اختبارات الوحدة
```
أو ادفع التعديلات إلى GitHub ودع الـ workflow المرفق (`.github/workflows/android.yml`) يبني الـ APK ويشغّل الاختبارات تلقائيًا.

### هيكل المشروع
```
settings.gradle.kts     إعداد المستودع (repositories) وتضمين :app
build.gradle.kts        إعداد الإصدارات (AGP 8.5 + Kotlin 1.9)
gradle.properties       إعدادات البناء
app/build.gradle.kts     إعداد وحدة التطبيق (SDK 35، R8، اختبارات)
.github/workflows/android.yml   بناء + اختبار تلقائي على GitHub Actions
app/src/main/java/com/newhtcdridi/weather/
  MainActivity.kt      الشاشة: الساعة، العرض، حوار المدن، التحديث
  WeatherApi.kt        طلبات HTTPS إلى Open-Meteo (الطقس + البحث عن المدن)
  WeatherParser.kt     تحويل JSON إلى نماذج (قابل للاختبار)
  Json.kt              طبقة داخلية تغلف org.json (فصل المنطق عن أندرويد)
  NetworkState.kt      كشف حالة الاتصال بالإنترنت
  WeatherModels.kt     City / WeatherReport / Condition (رموز WMO → أيقونة)
  WeatherLabels.kt     نصوص حالات الجو المترجمة
  WeatherIconView.kt   رسم الأيقونات المتحركة بـ Canvas
  Presets.kt           مدن تونسية جاهزة
  AppSettings.kt       المدينة والوحدة وآخر بيانات (SharedPreferences)
app/src/test/...       اختبارات JUnit للمحلل والتحويلات وروابط الـ API
```
لا توجد أي مكتبات خارجية: التطبيق يستخدم إطار أندرويد فقط (`HttpURLConnection` + `org.json`).

### مصدر البيانات والخصوصية
- بيانات الطقس من **Open-Meteo.com** (مجانية للاستخدام غير التجاري وتتطلب الإشارة إلى المصدر؛ الرابط ظاهر أسفل الشاشة). راجع شروط الاستخدام على موقعهم قبل أي استخدام تجاري.
- لا يطلب التطبيق إذن الموقع ولا يجمع أي بيانات؛ يُرسَل فقط خط عرض/طول المدينة المختارة إلى Open-Meteo.

### أصل التصميم
اللوحة اللونية والأحجام مستخرجة من الـ APK الأصلي: خلفية `#0C0E12`، نص أبيض و`#CCCCCC` و`#888888`، لون التمييز `#E0B767`، والساعة بحجم 76sp.

### أفكار قادمة
Gradle wrapper في المستودع، ودجت للشاشة الرئيسية، الموقع الحالي (GPS)، توقعات بالساعة، ترقية AGP/Kotlin، توقيع نسخة release.

---

## English

**1.1.0** turns the original static mock-up into a working app: live clock in the selected city's time zone, real weather and a 5-day forecast from Open-Meteo (no API key), a city picker (built-in Tunisian cities + worldwide search), hand-drawn animated weather icons, offline cache, Arabic/English with RTL, and fixes (missing `INTERNET` permission, Android 15 edge-to-edge insets, adaptive icon, R8 for release).

- **Tap the city** to change it, **the temperature** to switch °C/°F, **the status line / icon** to refresh.
- **Build:** JDK 17, Android SDK 35 — `gradle assembleDebug`, tests with `gradle testDebugUnitTest`.
- **Dependencies:** none (Android framework only).
- **Data:** [Open-Meteo.com](https://open-meteo.com/) — free for non-commercial use, attribution required (shown in the app). The app requests no location permission.
