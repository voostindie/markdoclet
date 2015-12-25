/*
 * Copyright 2015 Vincent Oostindie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License
 *
 */

package test.user;

/**
 * @md.common Entry point into the User API.
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
