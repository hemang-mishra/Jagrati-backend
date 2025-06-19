The collected student data rn is:
package com.hexagraph.jagrati_android.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.hexagraph.jagrati_android.util.FileUtility.readBitmapFromFile

@Entity(tableName = "student_details")
data class StudentDetails(
    @PrimaryKey
    val pid: String = "",

    @ColumnInfo(name = "first_name")
    val firstName: String = "",

    @ColumnInfo(name = "last_name")
    val lastName: String = "",

    @ColumnInfo(name = "age")
    val age: Int = -1,

    @ColumnInfo(name = "gender")
    val gender: Gender = Gender.MALE,

    @ColumnInfo(name = "current_group_id")
    val currentGroupId: JagratiGroups = JagratiGroups.UNASSIGNED,

    @ColumnInfo(name = "profile_pic")
    val profilePic: String = "",

    @ColumnInfo(name = "school_class")
    val schoolClass: String = "",

    @ColumnInfo(name = "village")
    val village: Village = Village.KAKARTALA,

    @ColumnInfo(name = "primary_contact_no")
    val primaryContactNo: String = "",

    @ColumnInfo(name = "secondary_contact_no")
    val secondaryContactNo: String = "",

    @ColumnInfo(name = "guardian_name")
    val guardianName: String = "",

    @ColumnInfo(name = "last_updated")
    val lastUpdated: String = "",

    @ColumnInfo(name = "remarks")
    val remarks: String? = ""
){
    val pattern get(): String = "${pid}.png"
    val faceFileName get(): String = "Face_${pattern}"
    val imageFileName get(): String = "Image_${pattern}"
    val frameFileName get(): String = "Frame_${pattern}"
    fun faceBitmap(context: Context): Bitmap? = context.readBitmapFromFile(faceFileName).getOrNull()
    fun imageBitmap(context: Context): Bitmap? = context.readBitmapFromFile(imageFileName).getOrNull()
    fun frameBitmap(context: Context): Bitmap? = context.readBitmapFromFile(frameFileName).getOrNull()
}

enum class Gender {
    MALE, FEMALE, OTHER
}

enum class Village(val title: String) {
    KAKARTALA(title = "Kakartala"), GADHERI(title = "Gadheri"), MEHAGWAN(title = "Mehagwan"), CHANDITOLA(title = "Chanditola"), AMANALA(title = "Amanala"), SUARKOL(title = "Suarkol")
}

