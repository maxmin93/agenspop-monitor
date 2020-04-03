package net.bitnine.ag3.agensalert.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

// data models
@Table("users")
data class User(
        @Id val id: Long? = null,
        @Column("name") val name: String,
        @Column("login") val login: String,
        @Column("email") val email: String,
        @Column("avatar") val avatar: String? = null
)


// rest models
data class ErrorMessage(val message: String)

data class UserDTO(
        val name: String,
        val login: String,
        val email: String,
        val avatar: String? = null
)

fun UserDTO.toModel(withId: Long? = null) = User(withId, this.name, this.login, this.email, this.avatar)