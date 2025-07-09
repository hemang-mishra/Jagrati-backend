Got it! Here’s the updated version with those details added in:

---

### **📅 Attendance Feature**

**Overview:**
The app includes an attendance management system that enables authorized users to record and manage attendance for both students and volunteers. The system enforces clear role-based permissions and tracks attendance actions with proper timestamps.

---

### **🧾 Data Stored in Attendance Records:**

Each attendance entry includes:

* **Attendance ID:** A unique identifier for each attendance record.
* **Person ID:** Refers to the individual (student or volunteer) whose attendance is being recorded.
* **Taken By:** Person ID of the user who recorded the attendance.
* **Role Type of the Subject:** Indicates whether the attendance is for a **student** or **volunteer**.
* **Timestamps:**

  * **Created At:** The exact date and time when the attendance was taken.
  * **Updated At:** The last time this attendance entry was modified.
* **Optional:**

  * **Name of the Person:** Might be stored to avoid needing extra fetch operations when displaying attendance logs.

---

### **🎯 Attendance Rules & Permissions:**

* **Student Attendance:**

  * Only **volunteers** are authorized to take it.

* **Volunteer Attendance:**

  * Only **admins** or **super admins** are allowed to take it.

---

Let me know when you’re ready for the next one. You're building this really thoughtfully, by the way.
