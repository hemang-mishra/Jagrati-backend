Overview
Great start. Here's a clear and structured write-up of what you’ve said so far. Let me know if you want to tweak the names, tone, or structure later:

---

### 🔐 **Authentication & User Roles**

**Authentication:**
The app initially uses Firebase Authentication for login, but it's built with flexibility in mind—migrating to a more integrated backend with automated workflows is part of the roadmap.

---

### 🧑‍💼 **User Roles Overview**

There are **four distinct roles**, each with defined access levels:

1. **Guest**

   * **Definition:** Any random person who opens the app without logging in.
   * **Access Level:** Minimal.
   * **Permissions:**

     * Can view general content.
     * **Restricted from accessing any personal details** of students or volunteers.

2. **Volunteer**

   * **Definition:** A verified student from IIIT Naya Raipur affiliated with Jagrati.
   * **Access Level:** Moderate.
   * **Permissions:**

     * Can log in and view student and volunteer profiles.
     * Has **limited feature access** (detailed breakdown to follow).

3. **Admin**

   * **Definition:** A trusted, high-level user responsible for managing certain app operations.
   * **Access Level:** High.
   * **Permissions:**

     * Has access to most app features and data.
     * Can manage user entries and oversee volunteer activities.
     * **Cannot** modify user roles or system-wide settings.

4. **Super Admin**

   * **Definition:** The highest authority, usually one or two key people from the core Jagrati team.
   * **Access Level:** Full.
   * **Permissions:**

     * Can access and edit all data.
     * Can **modify user roles**, including promoting volunteers to admins, demoting users, or removing accounts.
     * Can control system-wide settings.

---

Whenever you're ready, we can move on to detailing the **admin and super admin permissions**, or define what specific modules each role can access (like case tracking, attendance, reports, etc.).

Want me to keep writing this down like a full doc as we go?


### Jagrati App User Roles and Access Levels

### User Roles:

- **Super-Admin**: Complete access and control over all features, including Admin management.
- **Admin**: Manage volunteer attendance, volunteer role assignments, syllabus, and student groups.
- **Volunteer**: Take homework assignments and manage attendance.
- **Guest**: Can view attendance, student profiles, volunteer profiles, and leaderboards.

### Role Change Process:

- **Anonymous User**: A user who hasn't signed in; can access limited features (view attendance, profiles, etc.).
- **Intermediate Stage**: A stage between a Guest and Volunteer can be called **"Applicant"** (when a new user applies to become a volunteer).
- **Volunteer**: Requires more detailed information upon signup, as they actively participate in tasks like homework and attendance.

### Powers and Permissions:

- **Guest**: View basic information (attendance, profiles, leaderboards).
- **Volunteer**: Take attendance, complete homework tasks.
- **Admin**: Manage volunteer attendance, role assignments, syllabus, and student groups.
- **Super-Admin**: Full management control, including Admin management.

### Data Collection:

- **Guest**: Only minimal data collected.
- **Volunteer**: Requires more detailed data for proper management.

### Visibility:

- The experience for anonymous users (not signed in) will be identical to that of Guests, with no distinction in access.

We can have multiple options here.

1. After login, we can redirect to app. And application option is in Profile section…
2. Apply for Volunteer option be given directly after account creation.
3. Maybe something else


So maybe we’ll use User.kt to also store the currnet role of user.

Maybe create an enum to store the user role.
So there are two types of admin dashboard o

one for super admin and other admin user
So once the user logs in and they are super admin or admin, show their relevant Dashboard. Keep it as a separate screen.

For volunteer details, use a realtime database of firebase to store all the details and fetcch them accordingly.

For guest user (logged in still), no need to do anything rn…
After login, in the dashboard, give an option to the guest user to upgrade to a volunteer.

Similarly if they are volunteer, they can request to upgrade to admin.

So while upgrading from guest to volunteer, take these details from the volunteer…

From guest users:

1. Name
2. Email (Verified)
3. Password (optional- not required for google login)

From Volunteers/Admins/Super-Admins:

The below details of volunteers are being stored in the database.

1. **Roll Number**
2. **First Name**
3. **Last Name**
4. **Gender (dropdown preferably)**
5. **Alternate Email**
6. **Batch (2019, 2020, etc…)**
7. **Programme( [B.Tech](http://B.Tech) or B.Des)**
8. **Street Address 1**
9. **Street Address 2**
10. **Pincode**
11. **City**
12. **State**
13. **Date of Birth (DOB)**
14. **Contact Number**
15. **Profile Image**

For profile image, give them an option to upload an image always. store it in data store. But reduce the size of image so that it occupies less space…

Maybe admin dashboard can be same, just different options for admin and super-admin.
The admin panel will have list of all users, and they can upgrade them or degrade them…