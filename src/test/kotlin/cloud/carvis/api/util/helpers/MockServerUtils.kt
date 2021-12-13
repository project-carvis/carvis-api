package cloud.carvis.api.util.helpers

import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import java.net.ServerSocket

class MockServerUtils {

    companion object {

        fun createMockServer(): ClientAndServer {
            val unusedPort = findUnusedPort()
            return startClientAndServer(unusedPort)
        }

        private fun findUnusedPort(): Int {
            val socket = ServerSocket(0)
            return socket.localPort
                .also { socket.close() }
        }

        fun createOidcConfigJson(issuer: String) =
            """
            {
              "issuer": "$issuer",
              "authorization_endpoint": "https://carvis.eu.auth0.com/authorize",
              "token_endpoint": "https://carvis.eu.auth0.com/oauth/token",
              "device_authorization_endpoint": "https://carvis.eu.auth0.com/oauth/device/code",
              "userinfo_endpoint": "https://carvis.eu.auth0.com/userinfo",
              "mfa_challenge_endpoint": "https://carvis.eu.auth0.com/mfa/challenge",
              "jwks_uri": "https://carvis.eu.auth0.com/.well-known/jwks.json",
              "registration_endpoint": "https://carvis.eu.auth0.com/oidc/register",
              "revocation_endpoint": "https://carvis.eu.auth0.com/oauth/revoke",
              "scopes_supported": [
                "openid",
                "profile",
                "offline_access",
                "name",
                "given_name",
                "family_name",
                "nickname",
                "email",
                "email_verified",
                "picture",
                "created_at",
                "identities",
                "phone",
                "address"
              ],
              "response_types_supported": [
                "code",
                "token",
                "id_token",
                "code token",
                "code id_token",
                "token id_token",
                "code token id_token"
              ],
              "code_challenge_methods_supported": [
                "S256",
                "plain"
              ],
              "response_modes_supported": [
                "query",
                "fragment",
                "form_post"
              ],
              "subject_types_supported": [
                "public"
              ],
              "id_token_signing_alg_values_supported": [
                "HS256",
                "RS256"
              ],
              "token_endpoint_auth_methods_supported": [
                "client_secret_basic",
                "client_secret_post"
              ],
              "claims_supported": [
                "aud",
                "auth_time",
                "created_at",
                "email",
                "email_verified",
                "exp",
                "family_name",
                "given_name",
                "iat",
                "identities",
                "iss",
                "name",
                "nickname",
                "phone_number",
                "picture",
                "sub"
              ],
              "request_uri_parameter_supported": false
            }
            """
    }
}