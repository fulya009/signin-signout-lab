package com.example.signinsignout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Landing screen shown after a successful sign-in or registration.
 *
 * <p>Loads the signed-in user's profile from Cloud Firestore, greets them by
 * name and offers a sign-out button that returns to {@link LoginActivity}.</p>
 */
public class HomeActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView emailText;
    private ImageView profileImage;
    private Button signOutButton;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    /**
     * Inflates the layout, wires up the views and loads the current profile.
     *
     * @param savedInstanceState the previously saved state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        welcomeText = findViewById(R.id.home_welcome);
        emailText = findViewById(R.id.home_email);
        profileImage = findViewById(R.id.home_profile_image);
        signOutButton = findViewById(R.id.home_sign_out);

        signOutButton.setOnClickListener(v -> signOut());

        loadProfile();
    }

    /**
     * Reads the signed-in user's profile document from Firestore and populates
     * the greeting, email and (where available) profile photo.
     */
    private void loadProfile() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            signOut();
            return;
        }

        emailText.setText(firebaseUser.getEmail());

        firestore.collection("users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(this::bindProfile)
                .addOnFailureListener(e ->
                        Toast.makeText(HomeActivity.this,
                                "Could not load profile: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /**
     * Binds a Firestore profile document to the on-screen views.
     *
     * @param snapshot the Firestore document for the current user
     */
    private void bindProfile(@NonNull DocumentSnapshot snapshot) {
        User user = snapshot.toObject(User.class);
        if (user != null) {
            welcomeText.setText(getString(R.string.welcome_user,
                    user.getFirstName(), user.getLastName()));
        }
    }

    /**
     * Signs the current user out of Firebase and returns to the login screen,
     * clearing the back stack so the home screen cannot be reopened with back.
     */
    private void signOut() {
        auth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
