package org.jagrati.jagratibackend.utils

enum class NotificationContent(
    val title: String,
    val description: String
) {
    APPRECIATION_FOR_VOLUNTEERING(
        "Thanks for volunteering!!",
        "Your time brings light into someone’s day."
    );

    companion object {
        fun getNewVolunteeringRequestContent(
            requesterFirstName: String,
            requesterLastName: String,
            requesterRollNo: String?
        ): Pair<String, String> {
            val title = "New Volunteering Request"
            val description = "$requesterFirstName $requesterLastName ($requesterRollNo) wants to join as volunteer!!."
            return Pair(title, description)
        }

        fun getVolunteeringRequestAcceptedContent(approverName: String): Pair<String, String> {
            val title = "Congratulations, you are now a volunteer!!"
            val description = "Your volunteer request has been approved by ${approverName}."
            return Pair(title, description)
        }

        fun getVolunteeringRequestRejectedContent(reason: String): Pair<String, String> {
            val title = "Volunteer Request Rejected"
            val description = "Your volunteer request has been rejected. Reason: $reason"
            return Pair(title, description)
        }
    }
}