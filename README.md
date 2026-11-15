# Weather Sense

تطبيق طقس لأندرويد بلغة **Kotlin** و**Jetpack Compose**، بساعة **Flip Clock** و**تحديد موقعك الحالي** تلقائيًا.

---

## العربية

### المزايا

- **ساعة Flip Clock**: كل رقم على بطاقة منفصلة بخط مفصل في المنتصف، وتدوير ثلاثي الأبعاد (`rotationX`) عند تغيّر الرقم.
- **تحديد الموقع الحالي**: يطلب إذن الموقع ويستخدم `FusedLocationProviderClient` بدقة عالية، مع رجوع إلى آخر موقع معروف عند تعذّر الحصول على موقع جديد، ثم تحويل الإحداثيات إلى اسم مدينة عبر `Geocoder`.
- **طقس حقيقي** من [Open-Meteo](https://open-meteo.com/) **بدون مفتاح API**: الحرارة، الإحساس، الرطوبة، الرياح، الضغط، الشروق، الغروب، ومؤشر UV.
- **توقعات 6 أيام** مع أيقونات مرسومة بالكود (`Canvas`) دون أي صور أو خطوط إيموجي.
- **حالات واضحة**: تحميل، طلب إذن، خطأ مع إعادة المحاولة.
- **زر تحديث الموقع** في أسفل الشاشة.
- **مظهر داكن/فاتح** يتبع إعدادات النظام عبر Material 3.

### البنية

```
app/src/main/java/com/weathersense/app/
  MainActivity.kt                 حالة واحدة (Loading/NeedsPermission/Ready/Error) + طلب إذن الموقع
  data/model/WeatherData.kt       نماذج الطقس والتوقعات
  location/WeatherLocationManager.kt   الموقع الحالي + تحويل الإحداثيات إلى مدينة
  service/WeatherApiService.kt    عميل Open-Meteo وتحليل JSON
  ui/components/FlipClock.kt      الساعة القلبتة
  ui/screen/WeatherScreen.kt      الواجهة الرئيسية والأيقونات
  ui/theme/Theme.kt               ألوان Material 3
```

### البناء

```bash
gradle assembleDebug
# الناتج: app/build/outputs/apk/debug/app-debug.apk
```

يتطلب JDK 21 و Android SDK 36. الإصدارات المستخدمة: AGP 9.5.1، Gradle 9.9.1، Kotlin 2.1.21، Compose BOM 2026.06.01.

> ملاحظة: AGP 9 يجلب دعم Kotlin داخليًا، لذلك لا تُضاف إضافة `org.jetbrains.kotlin.android` يدويًا، ويكفي `org.jetbrains.kotlin.plugin.compose`.

### الأذونات

`INTERNET`، `ACCESS_NETWORK_STATE`، `ACCESS_FINE_LOCATION`، `ACCESS_COARSE_LOCATION`.

### الخصوصية

لا يُخزَّن الموقع على الجهاز ولا يُرسَل إلى أي خدمة غير Open-Meteo (خط العرض/الطول فقط، لتحديد الطقس). لا يوجد تتبّع ولا تحليلات.

### مصدر البيانات

[Open-Meteo.com](https://open-meteo.com/) — مجاني للاستخدام غير التجاري ويتطلب الإشارة إلى المصدر.

---

## English

**Weather Sense** is an Android weather app in **Kotlin + Jetpack Compose** featuring a **flip clock** and **automatic current-location detection**.

- **Flip clock**: each digit on its own split-flap card with a hinge line and a 3D `rotationX` flip on change.
- **Current location**: `FusedLocationProviderClient` at high accuracy, falling back to the last known location, reverse-geocoded to a city name with `Geocoder`.
- **Real weather** from [Open-Meteo](https://open-meteo.com/) with **no API key**: temperature, feels-like, humidity, wind, pressure, sunrise, sunset and UV index.
- **6-day forecast** with icons drawn in `Canvas` — no image assets or emoji fonts.
- **Explicit states**: loading, permission request, and error with retry.
- **Material 3** light/dark theming that follows the system setting.

**Build:** JDK 21, Android SDK 36 — `gradle assembleDebug`. Versions: AGP 9.5.1, Gradle 9.9.1, Kotlin 2.1.21, Compose BOM 2026.06.01.

**Privacy:** the location is never stored or shared with anyone except Open-Meteo (lat/lon only, to fetch the forecast). No tracking, no analytics.
