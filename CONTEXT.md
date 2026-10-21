# CONTEXT.md — стан робіт swe-android-cli

Стисло: що зроблено, що відкрито. Факт, що змінився, замінювати, а не дописувати. Спільні для
обох Android-портів речі (джерело виправлення, скрипти перевірки) — у
`swisseph-2026/CONTEXT.md`.

Стан на 21.10.2026.

## Що це

Android-застосунок із JNI-портом `swetest` і `obama` (модуль `swe-2.10.03`, чотири ABI). Java
кличе `SwissephTest.swe_test_main(String args, StringBuilder sout)` /
`obama_test_main(...)` з `CliFragment.sweTestMain()` — у потоці UI (обробник кнопки). Обгортки —
`jni/swetest_jni.c`, `jni/obama_jni.c`; вивід збирається в глобальні `SWE_OUT` (1 МБ) /
`SWE_OBAMA_OUT` (1 МБ) і віддається одним `append`.

## Стан `jni/`

* `sweph.c` / `sweph.h` = `swisseph-2026` (upstream `c69c14a` + `segfix.patch`), решта файлів
  бібліотеки = `c69c14a` (коміти `e9b0f79`, `90a6904` — `.so`; запушено).
* `swetest.c` — тристороннє злиття upstream `85e65da` -> `c69c14a` (порт узято з `85e65da`).
  Дописано `-astpos` (`print_asteroids()` через `JNI_PRINTF`, `static int arr[30000]` замість
  VLA). `swe_test_init()` скидає й `spmoon` (`-xv`).
* `obama_jni.h` — прототипи `obama_test_main`/`obama_test_init`; `obama.c` в upstream не мінявся
  з `bebba26`.
* Хост-тест без пристрою: `python tools/android/host_test/run.py <swe-android-cli>/jni`
  у swisseph-2026 — 17 команд в одному процесі проти `swetest.exe`; останній прогін (21.10)
  `differ: 0`.

## Опції збірки (21.10, коміт `036c470`)

* `jni/Application.mk`: `APP_PLATFORM := android-24` (було 21) — та сама причина, що в
  swe-android-lib: на 32-бітних ABI `fseeko`/`ftello` за API 21 були неявними оголошеннями й
  позиціювання в JPL-файлі було хибним. `.so` нижче API 24 не вантажиться; застосунку це не
  заважає (`minSdk 26` в `app/build.gradle`).
* `jni/Android.mk`: додано `-O2` (armeabi-v7a, thumb, отримувала типове `-Oz`), прибрано
  `-Wno-error=implicit-function-declaration` і `-ffunction-sections -fdata-sections` з
  `LOCAL_LDFLAGS`.

## Огляд JNI-обгорток (21.10) — правки ще не внесено

1. **`obama_jni.c`: переповнення стека довгою командою.** `CPY_CSTRING_TO_CHARS` робить
   `strcpy` у `char[AS_MAXCH]` (256). У `swetest_jni.c` це вже виправлено (`SWE_ARGS_SIZE 4096`,
   `strncpy`) — перенести так само.
2. **`obama.c`: `JNI_PRINTF` без меж** (`obama.c:38`): `sprintf` у `SWE_OBAMA_TMP[1024]` і
   `strcat` у `SWE_OBAMA_OUT` без перевірки довжини (і `strcat` — квадратичний). Зробити як у
   swetest: `snprintf` + `swe_out_append` з обрізанням.
3. **Виклик у потоці UI**: довга команда (`-n` з великим числом, пошук затемнень) блокує UI ->
   ANR. Перенести в робочий потік — але **один** (див. п. 4).
4. **Глобальний стан не потокобезпечний**: `SWE_OUT`, `SWE_TMP`, `swe_out_len`, `SWE_OBAMA_*`,
   статичні змінні `swetest.c` і `strtok` спільні для всіх потоків, а стан swisseph — свій у
   кожного потоку (`TLS __thread`). Тож виклики — лише з одного потоку, послідовно.
5. **Розбір аргументів лише за пробілами** (`strtok(args, " ")`): лапок немає, тож `-edir` зі
   шляхом із пробілом ламається (зараз шлях застосунку без пробілів). `realloc` без перевірки
   на `NULL`.
6. **`RLZ_STRING_UTF_CHARS` звільняє лише за `IS_COPY`** (`swetest_jni.c`, `obama_jni.c`): за
   специфікацією JNI `ReleaseStringUTFChars` потрібен завжди. На ART `GetStringUTFChars` завжди
   копіює, тож на практиці витоку немає; у swe-android-lib макрос уже правильний.
7. **`NewStringUTF(SWE_OUT)`**: обрізання на межі 1 МБ може розрізати UTF-8-послідовність
   (`°`, назви) -> `abort` під CheckJNI. Обрізати по межі символу.
8. Дрібне: `addToBuilder1`/`addToBuilder2` однакові й не `static`; `obama_jni.c` щоразу
   `memset` 1 МБ.

## Відкрито

* Огляд вище — правки не внесено («потім зробимо», 21.10).
* Справжній вивід `-astpos` не перевірено: немає `seasnam.txt` і файлів астероїдів.
* Інструментальні тести на пристрої (`app/src/androidTest/java/swisseph/AndroidSwetestTest.java`)
  не запускались.
