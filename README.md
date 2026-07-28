# 🟢 IMO Clone — Android Messenger

Барномаи мессенҷери Android монанди **IMO** бо чат, зангҳои овозӣ ва видеоӣ.

---

## 📱 Функсияҳо (Features)

| Функсия | Статус |
|----------|--------|
| 🔐 Вуруд бо рақами телефон (Firebase Auth) | ✅ |
| 💬 Чати матнии воқеӣ (Real-time) | ✅ |
| 👤 Профили корбар | ✅ |
| 🔍 Ҷустуҷӯи корбар бо рақами телефон | ✅ |
| 📞 Занги овозӣ (Voice Call) | 🟡 Сохтор сохта шудааст |
| 📹 Занги видеоӣ (Video Call) | 🟡 Сохтор сохта шудааст |
| 📩 Push-Notification | ✅ |
| 👥 Чати гурӯҳӣ | 🔜 Дар оянда |

---

## 🛠 Технологияҳо

- **Забон:** Kotlin
- **UI:** Material Design + ViewBinding
- **Базаи додаҳо:** Firebase Firestore
- **Аутентификатсия:** Firebase Auth (Phone)
- **Зангҳо:** Agora SDK (⚠️ Танзимро лозим аст)
- **Ҳабсозия:** Google Cloud Messaging (FCM)
- **Тасвирҳо:** Glide
- **Архитектура:** MVVM

---

## ⚙️ Чӣ тавр кор кардан

### 1. Талабҳо (Requirements)

- Android Studio Hedgehog (2023.1.1) ё навтар
- JDK 17
- Android SDK 24+
- Account дар [Firebase Console](https://console.firebase.google.com)
- Account дар [Agora Console](https://console.agora.io) (барои зангҳо)

### 2. Қадам ба қадам

#### Қадами 1: Firebase сохтан

1. Ба [Firebase Console](https://console.firebase.google.com) дароед
2. Project нав созед
3. Android App илова кунед бо package name: `tj.safaraligroup.sim`
4. **google-services.json**-ро download кунед
5. Онро дар ҷойи `app/google-services.json` гузоред

#### Қадами 2: Firebase Authentication

Дар Firebase Console:
- Authentication → Sign-in method → **Phone**-ро фаъол кунед

#### Қадами 3: Firebase Firestore

Дар Firebase Console:
- Cloud Firestore → Create database
- Test mode (ё production rules)

#### Қадами 4: Agora (барои зангҳо)

1. Ба [Agora Console](https://console.agora.io) дароед
2. Project нав созед
3. App ID-ро нусхабардорӣ кунед
4. Дар `Constants.kt` сатри зеринро иваз кунед:
   ```kotlin
   const val AGORA_APP_ID = "ШУМО_APP_ID_ИН_ҶО"
   ```

#### Қадами 5: Android Studio

1. Android Studio-ро кушоед
2. Лоиҳаро Import кунед: **File → Open → IMO-Clone**
3. Gradle sync-ро интизор шавед
4. Дар дастгоҳ ё эмулятор Run кунед ▶️

---

## 📁 Структураи лоиҳа

```
IMO-Clone/
├── app/
│   ├── src/main/
│   │   ├── java/com/imoclone/app/
│   │   │   ├── IMOApplication.kt          # Application class
│   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.kt           # Модели корбар
│   │   │   │   │   ├── Message.kt        # Модели паём
│   │   │   │   │   └── Chat.kt           # Модели чат
│   │   │   │   └── repository/
│   │   │   │       ├── AuthRepository.kt # Аутентификатсия
│   │   │   │       └── ChatRepository.kt # Чат ва паёмҳо
│   │   │   ├── ui/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── LoginActivity.kt       # Экран воридшавӣ
│   │   │   │   │   ├── PhoneAuthActivity.kt   # Вуруд бо телефон
│   │   │   │   │   └── ProfileSetupActivity.kt # Профил
│   │   │   │   ├── chat/
│   │   │   │   │   ├── ChatListActivity.kt    # Рӯйхати чатҳо
│   │   │   │   │   ├── ChatActivity.kt        # Экран чат
│   │   │   │   │   └── NewChatActivity.kt     # Чати нав
│   │   │   │   ├── call/
│   │   │   │   │   ├── VoiceCallActivity.kt   # Занги овозӣ
│   │   │   │   │   └── VideoCallActivity.kt   # Занги видеоӣ
│   │   │   │   └── components/
│   │   │   │       ├── ChatAdapter.kt        # Адаптери чат
│   │   │   │       └── MessageAdapter.kt     # Адаптери паём
│   │   │   ├── service/
│   │   │   │   ├── FirebaseService.kt        # FCM нотификатсия
│   │   │   │   └── CallService.kt            # Сервиси занг
│   │   │   └── util/
│   │   │       └── Constants.kt              # Константаҳо
│   │   ├── res/
│   │   │   ├── layout/             # XML Layout-ҳо
│   │   │   ├── drawable/           # Иконкаҳо ва drawable-ҳо
│   │   │   ├── values/             # Рангҳо, матнҳо, тема
│   │   │   └── menu/               # Менюи Toolbar
│   │   └── AndroidManifest.xml     # Манифест
│   └── build.gradle.kts            # App Gradle
├── build.gradle.kts                # Project Gradle
├── settings.gradle.kts             # Settings
├── gradle.properties               # Properties
└── README.md                       # Ин файл
```

---

## 🔒 Қоидаҳои Firestore Security

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    match /chats/{chatId} {
      allow read, write: if request.auth != null
        && request.auth.uid in resource.data.participants;
    }
    match /messages/{messageId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
        && request.auth.uid == request.resource.data.senderId;
    }
  }
}
```

---

## 🟡 Зангҳои овозӣ / видеоӣ

Сохтори зангҳо омода аст, аммо барои кор кардан ин қадамҳоро иҷро кунед:

### Agora SDK:
1. Дар `VoiceCallActivity.kt` ва `VideoCallActivity.kt` — қисмҳои TODO-ро кушоед
2. RtcEngine-ро инисиализатсия кунед
3. Канал ҳамроҳ шавед
4. Видеои локалӣ ва дурдастро насб кунед

```kotlin
// Намуна барои VoiceCallActivity:
val config = RtcEngineConfig().apply {
    mContext = this@VoiceCallActivity
    mAppId = Constants.AGORA_APP_ID
    mEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(...) {}
        override fun onUserOffline(...) { hangUp() }
    }
}
RtcEngine.create(config)
RtcEngine.joinChannel(null, channelName, null, 0)
```

---

## 🛠 Ҳалли мушкилот

| Мушкилот | Ҳал |
|-----------|------|
| Firebase auth кор намекунад | google-services.json-ро дуруст ҷойгир кунед |
| Чат кор намекунад | Firestore-ро дар Firebase Console созед |
| Занг кор намекунад | Agora App ID-ро дар Constants.kt иваз кунед |
| Build error | Gradle JDK 17-ро дар Android Studio интихоб кунед |

---

## 📄 Литсензия

MIT — Озодона истифода баред ва тағйир диҳед.

---

**Саволе доред?** Дар GitHub Issue кушоед ё ба ман нависед! 🚀
