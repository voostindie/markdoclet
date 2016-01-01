package test.user;

/**
 * @md.common Entry point into the User API.
 *
 * ```java
 * final User user = (User) request.getUserPrincipal();
 * ```
 */
public interface User {

    /**
     * @md.common The globally unique ID of the user.
     * @md.anonymous Always `null`.
     * @md.secure Never `null`.
     */
    String getId();

    Customer getCustomer();

    UserType getUserType();

    /**
     * @md.common Denotes whether this user's registration has been verified
     * @md.unknown Nobody knows what happens here...
     */
    boolean isVerified();

    /**
     * @md.common Log the user out of the system.
     * @md.anonymous This operation is not supported.
     */
    void logout();

    /**
     * @md.common Login with a username and password.
     * @md.secure This operation is not supported.
     */
    boolean login(String username, String password);
}
