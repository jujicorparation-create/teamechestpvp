# Auto Echest PvP (Fabric 1.21.4)

HP 6 dan (3 yurak) tushganda:
1. Kiyib turgan armor (helmet/chestplate/leggings/boots) avtomatik invga yechiladi
2. `/team echest` komandasi yuboriladi
3. Echest GUI ochilishi kutiladi
4. Hamma narsa (armor + hotbar + inv) echestga quick-move qilinadi
5. GUI yopiladi, 5 sekund cooldown boshlanadi

## Sozlash
`EchestTrigger.java` faylida:
- `HP_THRESHOLD` — trigger bo'ladigan HP (default 6.0f = 3 yurak)
- `COOLDOWN_MS` — ikki trigger orasidagi minimal vaqt (default 5000ms)
- `GUI_WAIT_TIMEOUT_TICKS` — echest ochilmasa qancha kutib bekor qilish (default 100 tick = 5 sek)

## Build (GitHub Actions orqali)
1. Bu repo'ni GitHub'ga push qil
2. Actions tabga o'tib "Build" workflow avtomatik ishlaydi
3. Tugagach, Artifacts bo'limidan `auto-echest-pvp.jar` yuklab ol
4. Jar'ni `.minecraft/mods/` papkasiga qo'y (Fabric Loader + Fabric API 1.21.4 kerak)

## MUHIM ESLATMA — versiyalarni tekshir
`gradle.properties` ichidagi `yarn_mappings` va `fabric_version` qiymatlari
men tomondan taxminiy qo'yilgan (internetga ulanmasdan yozilgan).
Build xato bersa, quyidagi manbalardan aniq versiyalarni ol va yangila:
- https://fabricmc.net/develop/ (yarn mappings + loader)
- https://modrinth.com/mod/fabric-api/versions?g=1.21.4 (fabric api)

## Testlash bo'yicha eslatma
- Avval survival'da xavfsiz joyda (mob yo'q) HP'ni ataylab tushirib test qil
- `TICKS_AFTER_COMMAND` qiymatini serverning armor-sync tezligiga qarab sozlashing kerak bo'lishi mumkin
- Agar echest boshqa item bilan to'lib ketsa, QUICK_MOVE ba'zi itemlarni tashlab ketishi mumkin — shu holatni alohida handle qilish kerak bo'lsa ayt
