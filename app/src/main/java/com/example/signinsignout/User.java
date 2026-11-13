package com.example.signinsignout;

/**
 * Plain data model representing a registered user.
 *
 * <p>Instances of this class are serialized directly into Cloud Firestore,
 * therefore the class must expose a public no-argument constructor and public
 * getters/setters for every field that should be persisted.</p>
 */
public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String photoUrl;

    /**
     * Required empty constructor used by Firestore to deserialize documents.
     */
    public User() {
    }

    /**
     * Creates a fully populated user model.
     *
     * @param firstName the user's given name
     * @param lastName  the user's family name
     * @param email     the user's email address (also used as the login id)
     * @param photoUrl  a download URL pointing at the user's profile image in
     *                  Firebase Storage, or {@code null} if none was uploaded
     */
    public User(String firstName, String lastName, String email, String photoUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    /**
     * Returns the user's first name.
     *
     * @return the given name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     *
     * @param firstName the given name to store
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the user's last name.
     *
     * @return the family name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     *
     * @param lastName the family name to store
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email address to store
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the download URL of the user's profile image.
     *
     * @return the profile image URL, or {@code null} if none exists
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Sets the download URL of the user's profile image.
     *
     * @param photoUrl the profile image URL to store
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
