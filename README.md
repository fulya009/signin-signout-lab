# Sign In / Sign Up App for Android with Firebase

An Android (Java) application for user authentication and registration, backed by
Firebase. Users can register with their name, email, password and an optional
profile photo, sign in, and view a personalized home screen. Credentials are
handled by **Firebase Auth**, profile data is stored in **Cloud Firestore**, and
profile images are stored in **Firebase Storage**.

**Primary color:** `#D83D68` (from the supplied value `FFD83D68`).

---

## Features (mapped to the rubric)

| Rubric item | Where it lives |
|---|---|
| **Sign In view** (email + password, link to register) | `LoginActivity.java`, `activity_login.xml` |
| **Sign Up view** (first/last name, email, password + confirm, photo upload, toast validation, link back to sign in) | `RegisterActivity.java`, `activity_register.xml` |
| **Firebase integration** (Auth + Firestore + Storage) | `LoginActivity`, `RegisterActivity`, `HomeActivity` |
| **App icon** (custom, brand-colored) | `res/mipmap-*/ic_launcher*`, `res/drawable/ic_launcher_*` |
| **Javadoc comments** | Every method in every `.java` file |

---

## Project structure

```
SignInSignOut/
├── app/
│   ├── build.gradle                  # app dependencies (Firebase BoM, etc.)
│   ├── google-services.json          # <-- YOU add this (see below)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/signinsignout/
│       │   ├── LoginActivity.java    # Sign in
│       │   ├── RegisterActivity.java # Sign up + photo upload
│       │   ├── HomeActivity.java     # Post-login screen
│       │   └── User.java             # Firestore data model
│       └── res/
│           ├── layout/               # activity_login / register / home .xml
│           ├── values/               # colors (#D83D68), strings, theme
│           ├── drawable/             # field/button/circle backgrounds + icon vectors
│           └── mipmap-*/             # launcher icons (all densities)
├── build.gradle                      # project-level
├── settings.gradle
└── gradle.properties
```

---

## Setup — do this before running (required)

The repo builds, but it needs **your** Firebase project to actually authenticate.

### 1. Create the Firebase project
1. Go to <https://console.firebase.google.com> → **Add project**.
2. Inside the project, click the **Android** icon to add an app.
3. For **Android package name**, enter exactly:
   ```
   com.example.signinsignout
   ```
4. Download **`google-services.json`** and drop it into the **`app/`** folder.
   (Delete `app/google-services.json.PLACEHOLDER`.)

### 2. Turn on the Firebase services
In the Firebase console:
- **Build → Authentication → Sign-in method →** enable **Email/Password**.
- **Build → Firestore Database →** Create database (Start in **test mode** for the lab).
- **Build → Storage →** Get started (Start in **test mode** for the lab).

> Test mode makes the screenshots easy. For anything real, tighten the security
> rules so only authenticated users can read/write their own data.

### 3. Open & run
1. Open the `SignInSignOut` folder in **Android Studio** (Giraffe or newer).
2. Let Gradle sync (it downloads the Firebase + AndroidX libraries).
3. Pick an emulator (e.g. Pixel 6, API 34) → **Run ▶**.

---

## Submission checklist (from the assignment)

You still need to produce these yourself — the app is built to make each easy:

- [ ] **GitHub link** — push this folder to a public GitHub repo (see below).
- [ ] **App-in-simulator screenshot** — showing the custom icon / graphics.
- [ ] **Sign Up screenshot** — the register form filled with *your* name.
- [ ] **Toast screenshot** — submit the Sign Up form with a field empty to trigger
      the "Please fill in all fields" toast.
- [ ] **Firebase console screenshot** — Firestore `users` collection showing your
      stored record after registering.
- [ ] **Successful sign-in screenshot** — the "Sign in successful" toast.
- [ ] **Demo video / Show & Tell** — upload to the repo.

### Push to GitHub
```bash
cd SignInSignOut
git init
git add .
git commit -m "Lab 4: Sign In / Sign Up app with Firebase"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```
> `.gitignore` is set up to keep build output out of the repo. The real
> `google-services.json` for this lab can be committed (it's needed to run), but
> for a real app you'd keep it private.

---

## Notes
- **Min SDK 21**, **Target SDK 34**, **Java 17**.
- Profile image upload uses the modern `ActivityResultContracts.GetContent()` API
  (no deprecated `onActivityResult`).
- If Gradle sync fails on first open, use **File → Sync Project with Gradle Files**,
  and make sure `google-services.json` is in `app/`.
