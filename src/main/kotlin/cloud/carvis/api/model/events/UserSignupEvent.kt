package cloud.carvis.api.model.events

import java.time.Instant

data class UserSignupEvent(
    val userId: String,
    val email: String,
    val name: String? = null,
    val createdAt: Instant? = null
)