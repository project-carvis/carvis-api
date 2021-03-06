package cloud.carvis.api.common.auth.converters

import cloud.carvis.api.common.auth.model.AudienceType
import cloud.carvis.api.common.properties.AuthProperties
import cloud.carvis.api.users.model.UserRole
import cloud.carvis.api.users.model.UserRole.SYSTEM
import com.nimbusds.jose.shaded.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class CustomAuth0RoleConverter(
    @Value("\${auth.custom-role-claim-key}") private val claimKey: String,
    authProperties: AuthProperties
) : Converter<Jwt, JwtAuthenticationToken> {

    val systemAudience = authProperties.audiences[AudienceType.SYSTEM] ?: throw RuntimeException("SYSTEM audience not defined")

    override fun convert(source: Jwt): JwtAuthenticationToken {
        val customRoles = source.claims[claimKey]
        if (customRoles != null && customRoles is JSONArray) {
            val authorities = customRoles
                .map { it.toString() }
                .mapNotNull { UserRole.from(it) }
                .map { it.toGrantedAuthority() }

            return JwtAuthenticationToken(source, authorities)
        }

        val audience = source.claims["aud"]
        if (isSystemUser(audience)) {
            return JwtAuthenticationToken(source, listOf(SYSTEM.toGrantedAuthority()))
        }
        return JwtAuthenticationToken(source)
    }

    private fun isSystemUser(audiences: Any?): Boolean {
        return audiences is ArrayList<*> && audiences.contains(systemAudience)
    }
}
