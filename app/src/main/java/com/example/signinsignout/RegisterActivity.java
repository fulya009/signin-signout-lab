package com.example.signinsignout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Sign-up screen.
 *
 * <p>Collects a first name, last name, email, password and password
 * confirmation, lets the user optionally pick a profile photo, and then:</p>
 * <ol>
 *   <li>creates the account with Firebase Authentication,</li>
 *   <li>uploads the selected photo to Firebase Storage (if any),</li>
 *   <li>stores the user profile document in Cloud Firestore.</li>
 * </ol>
 * Empty fields or mismatched passwords are reported with a toast message.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private ImageView profileImage;
    private Button pickImageButton;
    private Button registerButton;
    private TextView goToLogin;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    /** Uri of the profile photo chosen from the device gallery, or {@code null}. */
    private Uri selectedImageUri;

    /** Launcher that opens the system image picker and receives the result. */
    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    this::onImageSelected);

    /**
     * Inflates the layout, obtains the Firebase handles, wires up the views and
     * installs the click listeners.
     *
     * @param savedInstanceState the previously saved state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        firstNameInput = findViewById(R.id.register_first_name);
        lastNameInput = findViewById(R.id.register_last_name);
        emailInput = findViewById(R.id.register_email);
        passwordInput = findViewById(R.id.register_password);
        confirmPasswordInput = findViewById(R.id.register_confirm_password);
        profileImage = findViewById(R.id.register_profile_image);
        pickImageButton = findViewById(R.id.register_pick_image);
        registerButton = findViewById(R.id.register_button);
        goToLogin = findViewById(R.id.register_go_to_login);
        progressBar = findViewById(R.id.register_progress);

        pickImageButton.setOnClickListener(v -> imagePicker.launch("image/*"));
        registerButton.setOnClickListener(v -> attemptRegister());
        goToLogin.setOnClickListener(v -> finish());
    }

    /**
     * Callback invoked by the image picker once the user selects (or cancels)
     * a photo. On success the chosen image is previewed in the circle view.
     *
     * @param uri the content Uri of the chosen image, or {@code null} if the
     *            user cancelled
     */
    private void onImageSelected(@Nullable Uri uri) {
        if (uri != null) {
            selectedImageUri = uri;
            profileImage.setImageURI(uri);
        }
    }

    /**
     * Validates every field. If all fields are filled and the two passwords
     * match, begins the account-creation flow; otherwise shows a toast that
     * tells the user what is missing.
     */
    private void attemptRegister() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        handleAccountCreated(firstName, lastName, email);
                    } else {
                        showLoading(false);
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";
                        Toast.makeText(RegisterActivity.this, message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Called once the Firebase account exists. Uploads the profile image when
     * one was chosen, otherwise saves the profile straight away.
     *
     * @param firstName the user's given name
     * @param lastName  the user's family name
     * @param email     the user's email address
     */
    private void handleAccountCreated(String firstName, String lastName, String email) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        String uid = firebaseUser.getUid();

        if (selectedImageUri != null) {
            uploadProfileImage(uid, firstName, lastName, email);
        } else {
            saveUserProfile(uid, firstName, lastName, email, null);
        }
    }

    /**
     * Uploads the selected profile image to Firebase Storage and, on success,
     * forwards the resulting download URL to {@link #saveUserProfile}.
     *
     * @param uid       the authenticated user's unique id
     * @param firstName the user's given name
     * @param lastName  the user's family name
     * @param email     the user's email address
     */
    private void uploadProfileImage(String uid, String firstName,
                                    String lastName, String email) {
        StorageReference ref = storage.getReference()
                .child("profile_images")
                .child(uid + ".jpg");

        ref.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri ->
                        saveUserProfile(uid, firstName, lastName, email,
                                downloadUri.toString()))
                .addOnFailureListener(e -> {
                    // Still save the profile even if the image upload fails.
                    Toast.makeText(RegisterActivity.this,
                            "Image upload failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    saveUserProfile(uid, firstName, lastName, email, null);
                });
    }

    /**
     * Writes the user profile document to Cloud Firestore under the
     * {@code users/{uid}} path and, on success, moves the user to the home
     * screen.
     *
     * @param uid       the authenticated user's unique id
     * @param firstName the user's given name
     * @param lastName  the user's family name
     * @param email     the user's email address
     * @param photoUrl  the profile image download URL, or {@code null}
     */
    private void saveUserProfile(String uid, String firstName, String lastName,
                                 String email, @Nullable String photoUrl) {
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("email", email);
        data.put("photoUrl", photoUrl);

        firestore.collection("users").document(uid).set(data)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful", Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Could not save profile";
                        Toast.makeText(RegisterActivity.this, message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Navigates to {@link HomeActivity} and clears the back stack.
     */
    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Toggles the progress spinner and the enabled state of the register button.
     *
     * @param loading {@code true} to show the spinner and disable the button,
     *                {@code false} to restore the idle state
     */
    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
    }
}
