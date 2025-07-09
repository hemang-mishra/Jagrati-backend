**Images Storage Design – Jagrati Web App**

**Overview:**
Images are used across the Jagrati system for three primary purposes: facial data (for both students and volunteers), classwork/homework references, and post attachments. Supabase Storage is chosen to securely and efficiently manage this image data.

---

**Storage Service:**
Supabase Storage (Free Tier)

* Total free storage: **5 GB**
* Storage limit: Approx **50,000 facial images** (assuming \~100 KB per image)
* File access: **Restricted** (no public access for sensitive data like facial recognition)
* Storage buckets are used to organize different image types.

---

**Image Categories & Usage:**

1. **Facial Data:**

   * Stored for each student and volunteer.
   * Each person has **3 images**.
   * Average image size: \~100 KB.
   * Used for secure facial authentication.
   * Stored in a dedicated, restricted-access Supabase bucket.
   * **No expiry**, but deletion is possible if a volunteer/student becomes inactive.

2. **Post Images:**

   * Volunteers can upload images with posts.
   * Image size restricted to **under 500 KB**.
   * Each post can have **multiple images**.
   * Stored with an optional expiry logic (e.g. **5 years**).
   * Uploaded to a public-facing bucket with limited access.

3. **Homework/Classwork Images:**

   * Volunteers may upload optional images for work assigned.
   * Stored **temporarily** with automatic backend expiry.
   * Expiry period: configurable (e.g. **7–30 days**).
   * Small size cap enforced (e.g. 200–300 KB max).

---

**Design Decisions:**

* **No profile pictures** are stored to save space. Instead, initials (e.g. “HR” in a circle) are generated dynamically for the UI.
* **Post count limitation** will be used to prevent unbounded storage usage.
* **Automatic expiry logic** for temporary images will be implemented at the backend (e.g. using scheduled cron jobs or Supabase functions).
* All uploads go through an access-controlled interface to ensure only authorized users can add media.
