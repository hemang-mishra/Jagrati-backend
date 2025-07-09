
**Screen Name:** Role Management Screen

**Purpose:** To display and manage a list of roles fetched from the backend.

**Components:**

1. **Roles List:**

    - Displays all roles fetched from the backend in a list format.

    - Each list item represents a specific role.

2. **Edit Role Bottom Sheet:**

    - Triggered when a user clicks on a specific role from the list.

    - Allows users to edit the details of the selected role.

    - Includes an option to delete the role.

3. **Delete Role Functionality:**

    - When the delete option is selected, a confirmation dialog box appears.

    - Instead of permanently deleting the role, the `isActive` parameter is set to `false`.

4. **Add Role Functionality:**

    - An "Add Role" button is available as a floating action button or at the top of the screen as a banner.

    - Clicking this button allows users to add a new role.

5. **State Management and Refresh:**

    - After any role is updated, added, or deleted, the screen should reload to reflect the latest data.

    - An API call is made to fetch the updated list of roles.

    - During the add, update, or delete operations, the respective button should show a loading state (e.g., become inactive or show a spinner) to indicate that the action is in progress.


---

**Screen Name:** Permission List Screen

**Purpose:** To display and manage a list of permissions fetched from the backend along with the roles assigned to each permission.

**Components:**

1. **Permissions List:**

    - Displays all permissions fetched from the backend in a list format.

    - Each list item represents a specific permission.

2. **Assigned Roles:**

    - For each permission listed, the active roles assigned to that permission are also fetched from the backend.

    - Assigned roles are displayed alongside each permission, showing which roles are currently active for that permission.

3. **Navigation to Permission Details:**

    - Clicking on a specific permission in the list redirects the user to a new screen called the Permission Details Screen.

    - This screen will provide more detailed information about the selected permission and its associated roles.

4. **State Management and Refresh:**

    - Similar to the Role Management Screen, any update or change in permissions or their assigned roles should trigger a refresh of the list to display the most current data.

    - The screen should show loading indicators during data fetch or updates to inform the user of ongoing actions.


---

**Screen Name:** Permission Details Screen

**Purpose:** To display detailed information about a specific permission and manage its assigned roles.

**Components:**

1. **Permission Information:**

    - Displays the details of the selected permission, including:

        - Permission Name

        - Permission Description

        - Date when the permission was added

        - Last updated date

2. **Assigned Roles Section:**

    - Shows all the roles currently assigned to this permission in a structured format.

    - Allows users to see which roles have access to this permission.

3. **Add/Remove Role Functionality:**

    - Provides an option to add a new role to this permission.

    - Provides an option to remove an existing role from this permission.

4. **State Management and Refresh:**

    - When adding or removing a role, the screen should refresh to display the updated list of assigned roles.

    - An API call is made to fetch the latest roles and update the permission details.

    - Loading indicators should be displayed during the API call to inform the user of ongoing actions.


---

**Screen Name:** Fetch All Users Screen

**Purpose:** To view all users, search them by name, and manage their assigned roles.

**Components:**

1. **User List:**

    - Fetches and displays all current users from the backend.

    - Each user item shows the user's name and their currently assigned roles.

2. **Search Bar:**

    - Allows searching users by their name.

    - Filters the user list in real-time based on the input.

3. **Role Display:**

    - Alongside each user, show a list of roles currently assigned to them.

    - This gives a quick overview of user-role mappings.

4. **Bottom Sheet – Role Management:**

    - Opens when a user taps on a particular user.

    - Provides two main actions:

        - Assign a new role to the user.

        - Remove an existing role from the user.

5. **State Management and Refresh:**

    - After assigning or removing a role, show a loading or disabled state while the API call is in progress.

    - Once the operation completes successfully, refresh the data by calling the fetch-all-users API again to update the UI.

    For each user displayed in the list, along with their name, we’ll also show the unique `PID` — a long string that helps identify the user uniquely. This helps in cases where multiple users have the same name. We can place the PID in a smaller font just below or next to the name so it's visible but not distracting.
---

**Screen Name:** Volunteer Requests Screen

**Purpose:** To display and manage all volunteer registration requests from the backend.

**Components:**

1. **Requests List:**

    - Displays all volunteer requests fetched from the backend.

    - Each request shows the volunteer's name and the current status of the request.

    - Status can be one of three values: Pending, Approved, or Rejected.

2. **Status Indicator:**

    - Each request clearly shows its status using either color coding, tags, or icons for better visual distinction.

3. **Bottom Sheet – Request Details:**

    - Opens when a specific request is tapped.

    - Displays full details of the volunteer’s request, including name and other information submitted during registration.

    - If the request status is **Pending**, two action buttons are shown at the bottom: **Approve** and **Reject**.

4. **Action Handling:**

    - On tapping Approve or Reject, an API call is made to update the request status.

    - Show loading or disabled state during the API call.

    - After successful update, refresh the request list to reflect the new status.
