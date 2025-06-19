
@Keep
@Entity
data class FaceInfo(
    @PrimaryKey
    val pid: String = "",
    val name: String,
    val width: Int = 0,
    val height: Int = 0,
    val faceWidth: Int = 0,
    val faceHeight: Int = 0,
    val top: Int = 0,
    val left: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
    val landmarks: List<FaceLandmark> = listOf(),
    val smilingProbability: Float = 0f,
    val leftEyeOpenProbability: Float = 0f,
    val rightEyeOpenProbability: Float = 0f,
    val timestamp: String = Utils.timestamp(),
    val time: Long = System.currentTimeMillis(),
) {
    val pattern get(): String = getPattern(pid)
    val faceFileName get(): String = getFaceFileName(pid)
    val imageFileName get(): String = getImageFileName(pid)
    val frameFileName get(): String = getFrameFileName(pid)
    fun faceBitmap(context: Context): Bitmap? = context.readBitmapFromFile(faceFileName).getOrNull()
    fun imageBitmap(context: Context): Bitmap? = context.readBitmapFromFile(imageFileName).getOrNull()
    fun frameBitmap(context: Context): Bitmap? = context.readBitmapFromFile(frameFileName).getOrNull()
    fun processedImage(context: Context): ProcessedImage {
        val image = imageBitmap(context)
        val frame = frameBitmap(context)
        val face = faceBitmap(context)
        return ProcessedImage(pid = pid, name = name, image = image, frame = frame, faceBitmap = face, landmarks = landmarks)
    }

    companion object{
        fun getPattern(pid: String) = "${pid}.png"
        fun getFaceFileName(pid: String) = "Face_${getPattern(pid)}"
        fun getImageFileName(pid:String) = "Image_${getPattern(pid)}"
        fun getFrameFileName(pid: String) = "Frame_${getPattern(pid)}"
    }
}


Here’s a structured summary of your facial authentication implementation, which you can use for documentation purposes:

---

### **Facial Authentication Implementation in the App**

**Overview:**
Facial authentication is integrated into the Android app, enabling secure and personalized user identification. This authentication process relies on facial recognition technology and involves storing various data elements on the device. The key challenge is ensuring real-time synchronization of the data across devices, especially if a web server is introduced in the future.

---

### **Data Stored for Facial Authentication:**

1. **Facial Data:**

   * **Three Images** are captured to store the user's facial features.
   * **Face Info:** Additional metadata about the face, such as measurements or landmarks, is stored for identification purposes.

2. **User Details:**

   * **Person ID:** A unique identifier assigned to both volunteers and students, enabling consistent management of their data within the system.
   * **Facial Data:** Both volunteer and student facial details are stored, linked to the person ID.

---

### **Current Implementation:**

* **Platform:** Android (Local Storage)
* **Data Storage:** Facial images and user details are stored locally on the Android device.
* **Authentication Process:** The app uses the stored facial data for face recognition to authenticate users when they access the app.

---

### **Challenges with Real-Time Data Sync:**

* **Real-Time Synchronization:** The app needs to ensure that facial authentication data (images and face info) is synchronized across multiple devices in real-time.
* **Data Syncing:** When implementing a web server, Firebase might be used to manage real-time data synchronization. This means the facial data must be fetched and updated in real-time from all devices to ensure consistency.
* **Local vs. Cloud Data Storage:** While the facial data is stored locally, future server integration will need to handle syncing this data across multiple platforms, requiring careful design to prevent data inconsistency.

---

### **Considerations for Future Implementation:**

* **Firebase Usage:** Firebase could be used to sync the facial data across devices and ensure real-time updates. This requires managing data flow carefully to avoid conflicts or latency in synchronization.
* **Security:** Ensuring that sensitive data like facial images is securely transmitted and stored, complying with privacy regulations.

---

**Suggested Title for This Section:**
**Facial Authentication and Data Management**

Let me know if you’d like to refine anything or add more details. This could serve as part of the documentation for your app’s authentication process!
