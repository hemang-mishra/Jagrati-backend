package org.jagrati.jagratibackend.entities.enums

enum class DeletionSource {
    SELF,
    ADMIN,

    /** Scrubbed by the pre-V3 delete path; original data is unrecoverable. */
    LEGACY
}
