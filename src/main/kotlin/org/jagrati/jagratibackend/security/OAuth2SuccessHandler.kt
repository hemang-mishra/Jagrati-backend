package org.jagrati.jagratibackend.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jagrati.jagratibackend.entities.User
import org.jagrati.jagratibackend.services.UserService
import org.jagrati.jagratibackend.util.PidGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OAuth2SuccessHandler(
    private val jwtService: JWTService,
    private val authService: AuthService,
    @Value("\${app.base-url}") private val baseUrl: String,

): SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        if(authentication is OAuth2AuthenticationToken){
            val oauth2User = authentication.principal
            val attributes = oauth2User.attributes

            // Call the centralized method in AuthService
            val user = authService.processOAuth2User(
                email = attributes["email"] as String,
                firstName = attributes["given_name"] as String,
                lastName = attributes["family_name"] as String? ?: "",
                pictureUrl = attributes["picture"] as String?
            )

            //Generate tokens
            val accessToken = jwtService.generateAccessToken(user)
            val refreshToken = jwtService.generateRefreshToken(user)

            //Redirect user to frontend
            val redirectUrl = "$baseUrl/oauth2/redirect?token=$accessToken&refresh_token=$refreshToken"
            response?.sendRedirect(redirectUrl)
        }
    }


}