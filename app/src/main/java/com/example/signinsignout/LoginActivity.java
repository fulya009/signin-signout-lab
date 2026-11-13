package com.example.signinsignout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Sign-in screen.
 *
 * <p>Lets an existing user authenticate with their email and password through
 * Firebase Authentication. A text button navigates to {@link RegisterActivity}
 * for users who still need to create an account.</p>
 */
public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView goToRegister;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    /**
     * Inflates the layout, wires up the views and installs the click listeners.
     *
     * @param savedInstanceState the previously saved state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.login_email);
        passwordInput = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        goToRegister = findViewById(R.id.login_go_to_register);
        progressBar = findViewById(R.id.login_progress);

        loginButton.setOnClickListener(v -> attemptLogin());
        goToRegister.setOnClickListener(v -> openRegisterScreen());
    }

    /**
     * If a user is already signed in, skips the login screen and goes straight
     * to the home screen.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            goToHome();
        }
    }

    /**
     * Validates the input fields and, when valid, signs the user in through
     * Firebase Authentication. A toast reports either success or the failure
     * reason returned by Firebase.
     */
    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Sign in successful", Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Authentication failed";
                        Toast.makeText(LoginActivity.this, message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Opens the registration screen.
     */
    private void openRegisterScreen() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    /**
     * Navigates to {@link HomeActivity} and clears the back stack so the user
     * cannot return to the login screen with the back button.
     */
    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Toggles the progress spinner and the enabled state of the login button.
     *
     * @param loading {@code true} to show the spinner and disable the button,
     *                {@code false} to restore the idle state
     */
    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
    }
}
