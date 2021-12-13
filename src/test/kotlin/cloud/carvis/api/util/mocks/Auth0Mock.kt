package cloud.carvis.api.util.mocks

import cloud.carvis.api.config.SecurityConfig
import cloud.carvis.api.util.helpers.MockServerUtils
import com.auth0.client.mgmt.ManagementAPI
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.mockserver.verify.VerificationTimes
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtDecoder


@TestConfiguration
class Auth0Mock {

    companion object {
        private var mockServer = MockServerUtils.createMockServer()
    }

    @Bean
    fun managementApi(): ManagementAPI =
        ManagementAPI(this.getUrl(), "dummy")

    @Bean
    fun jwtDecoder(@Value("\${auth.audience}") audience: String): JwtDecoder {
        this.mockOidcConfigEndpoint()
        return SecurityConfig().jwtDecoder(audience, this.getUrl())
    }


    fun withUser(
        userId: String,
        username: String = "j+smith",
        name: String = "John Smith",
        email: String = "j+smith@example.com"
    ): Auth0Mock {
        this.mockApiCall(
            path = "/api/v2/users/$userId",
            body = """
                {
                    "userId":"$userId",
                    "username":"$username",
                    "name":"$name",
                    "email":"$email",
                    "createdAt":"2021-12-07T18:10:59.126Z"
                }
                """
        )
        return this
    }

    fun withRole(roleName: String, roleId: String = "rol_1283injasd"): Auth0Mock {
        this.mockApiCall(
            path = "/api/v2/roles",
            body = """
                [
                  {
                    "id": "$roleId",
                    "name": "$roleName",
                    "description": "$roleName"
                  }
                ]
                """
        )
        return this
    }

    fun withUserRoleAssignment(roleId: String, email: String): Auth0Mock {
        this.mockApiCall(
            path = "/api/v2/roles/$roleId/users",
            body = """
                [
                  {
                    "user_id": "auth0|ijasdjiasdai",
                    "email": "dummy@dummy.com",
                    "picture": "http://foo.bar",
                    "name": "Foo Bar"
                  }
                ]
                """
        )
        return this
    }

    fun verify(request: HttpRequest, times: VerificationTimes) {
        mockServer.verify(request, times)
    }

    fun reset() =
        mockServer.reset()

    private fun mockOidcConfigEndpoint() =
        this.mockApiCall(
            "/.well-known/openid-configuration",
            MockServerUtils.createOidcConfigJson(this.getUrl())
        )

    private fun getUrl(): String =
        "http://localhost:${mockServer.port}/"

    private fun mockApiCall(
        path: String,
        body: String = "",
        method: String = "GET",
        statusCode: Int = 200,
        contentType: MediaType = APPLICATION_JSON
    ): Auth0Mock {
        mockServer.`when`(
            request()
                .withMethod(method)
                .withPath(path)
        )
            .respond(
                response()
                    .withStatusCode(statusCode)
                    .withContentType(contentType)
                    .withBody(body.trimIndent())
            )
        return this
    }
}