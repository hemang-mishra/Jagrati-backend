package org.jagrati.jagratibackend.services

import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

@Service
class EmailService(
    private val mailSender: JavaMailSender,

    @Value("\${spring.mail.username}")
    private val fromEmail: String,

    @Value("\${app.base-url}")
    private val baseUrl: String,

    @Value("\${app.name:Jagrati}")
    private val appName: String
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun sendVerificationEmail(email: String, token: String, userName: String? = null) {
        val subject = "$appName: Verify Your Email - Join Our Mission to Educate Children"
        val verificationUrl = "$baseUrl/auth/verify-email?token=$token"

        val content = buildVerificationEmailContent(
            userName = userName ?: "Educator",
            verificationUrl = verificationUrl,
            appName = appName
        )

        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setTo(email)
            helper.setFrom(fromEmail, appName)
            helper.setSubject(subject)
            helper.setText(content, true) // true for HTML content
            mailSender.send(message)
            logger.info("Verification email sent to $email")
        } catch (e: Exception) {
            logger.error("Error while sending verification email to $email", e)
            throw e
        }
    }

    private fun buildVerificationEmailContent(
        userName: String,
        verificationUrl: String,
        appName: String
    ): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Email Verification - $appName</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #1a1a1a;
                        background: linear-gradient(135deg, #fff5f5 0%, #fed7cc 25%, #fb923c 50%, #ea580c 75%, #c2410c 100%);
                        min-height: 100vh;
                        padding: 20px 0;
                    }
                    
                    .email-container {
                        max-width: 650px;
                        margin: 0 auto;
                        background: rgba(255, 255, 255, 0.95);
                        backdrop-filter: blur(10px);
                        border-radius: 24px;
                        overflow: hidden;
                        box-shadow: 
                            0 25px 50px rgba(234, 88, 12, 0.15),
                            0 0 0 1px rgba(255, 255, 255, 0.1),
                            inset 0 1px 0 rgba(255, 255, 255, 0.6);
                        border: 1px solid rgba(251, 146, 60, 0.2);
                    }
                    
                    .header {
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 20%, #b91c1c 40%, #991b1b 60%, #7f1d1d 80%, #450a0a 100%);
                        padding: 60px 40px;
                        text-align: center;
                        color: white;
                        position: relative;
                        overflow: hidden;
                    }
                    
                    .header::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background: 
                            radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
                            radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
                            radial-gradient(circle at 40% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
                        animation: floating 6s ease-in-out infinite;
                    }
                    
                    @keyframes floating {
                        0%, 100% { 
                            transform: translateY(0px) rotate(0deg);
                            opacity: 0.7;
                        }
                        50% { 
                            transform: translateY(-10px) rotate(2deg);
                            opacity: 1;
                        }
                    }
                    
                    .header-content {
                        position: relative;
                        z-index: 2;
                    }
                    
                    .header-ornament {
                        font-size: 4rem;
                        margin-bottom: 20px;
                        text-shadow: 0 4px 8px rgba(0,0,0,0.3);
                        filter: drop-shadow(0 0 20px rgba(255,255,255,0.4));
                        animation: pulse 2s ease-in-out infinite;
                    }
                    
                    @keyframes pulse {
                        0%, 100% { transform: scale(1); }
                        50% { transform: scale(1.05); }
                    }
                    
                    .header h1 {
                        font-size: 36px;
                        font-weight: 800;
                        margin-bottom: 12px;
                        letter-spacing: -1px;
                        text-shadow: 0 2px 4px rgba(0,0,0,0.2);
                    }
                    
                    .header p {
                        font-size: 18px;
                        opacity: 0.95;
                        font-weight: 300;
                        letter-spacing: 0.5px;
                    }
                    
                    .content {
                        padding: 60px 50px;
                        background: linear-gradient(180deg, #ffffff 0%, #fff7ed 30%, #ffedd5 70%, #fed7cc 100%);
                        position: relative;
                    }
                    
                    .content::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        height: 3px;
                        background: linear-gradient(90deg, #ea580c 0%, #dc2626 25%, #b91c1c 50%, #991b1b 75%, #ea580c 100%);
                    }
                    
                    .greeting {
                        font-size: 28px;
                        font-weight: 700;
                        margin-bottom: 30px;
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 30%, #b91c1c 70%, #991b1b 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                        text-align: center;
                        letter-spacing: -0.5px;
                    }
                    
                    .message {
                        font-size: 17px;
                        line-height: 1.8;
                        margin-bottom: 40px;
                        color: #374151;
                        text-align: left;
                    }
                    
                    .message strong {
                        color: #dc2626;
                        font-weight: 600;
                    }
                    
                    .button-container {
                        text-align: center;
                        margin: 40px 0;
                        perspective: 1000px;
                    }
                    
                    .verify-button {
                        display: inline-block;
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 25%, #b91c1c 50%, #991b1b 75%, #7f1d1d 100%);
                        color: white;
                        padding: 20px 48px;
                        text-decoration: none;
                        border-radius: 16px;
                        font-weight: 700;
                        font-size: 18px;
                        text-align: center;
                        transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                        box-shadow: 
                            0 12px 28px rgba(234, 88, 12, 0.4),
                            0 4px 8px rgba(220, 38, 38, 0.3),
                            inset 0 1px 0 rgba(255, 255, 255, 0.2);
                        text-transform: none;
                        letter-spacing: 0.5px;
                        position: relative;
                        overflow: hidden;
                        border: 2px solid rgba(255, 255, 255, 0.1);
                        transform-style: preserve-3d;
                    }
                    
                    .verify-button::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: -100%;
                        width: 100%;
                        height: 100%;
                        background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
                        transition: left 0.6s ease;
                    }
                    
                    .verify-button::after {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, transparent 50%, rgba(255,255,255,0.1) 100%);
                        border-radius: 14px;
                        opacity: 0;
                        transition: opacity 0.3s ease;
                    }
                    
                    .verify-button:hover {
                        transform: translateY(-6px) rotateX(5deg);
                        box-shadow: 
                            0 20px 40px rgba(234, 88, 12, 0.5),
                            0 8px 16px rgba(220, 38, 38, 0.4),
                            inset 0 1px 0 rgba(255, 255, 255, 0.3);
                        background: linear-gradient(135deg, #dc2626 0%, #b91c1c 25%, #991b1b 50%, #7f1d1d 75%, #450a0a 100%);
                    }
                    
                    .verify-button:hover::before {
                        left: 100%;
                    }
                    
                    .verify-button:hover::after {
                        opacity: 1;
                    }
                    
                    .verify-button:active {
                        transform: translateY(-2px) rotateX(2deg);
                        transition: all 0.1s ease;
                    }
                    
                    .button-text {
                        position: relative;
                        z-index: 2;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        gap: 8px;
                    }
                    
                    .alternative-text {
                        font-size: 15px;
                        color: #6b7280;
                        margin-top: 35px;
                        padding: 25px;
                        background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
                        border-radius: 12px;
                        border-left: 4px solid #ea580c;
                        box-shadow: 0 2px 8px rgba(234, 88, 12, 0.1);
                    }
                    
                    .alternative-text strong {
                        color: #374151;
                        font-weight: 600;
                    }
                    
                    .url-text {
                        word-break: break-all;
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 50%, #b91c1c 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                        font-family: 'SF Mono', Consolas, 'Liberation Mono', Menlo, monospace;
                        background-color: #fff7ed;
                        padding: 16px 20px;
                        border-radius: 8px;
                        margin-top: 12px;
                        display: inline-block;
                        border: 2px solid #fed7cc;
                        font-size: 14px;
                        box-shadow: inset 0 2px 4px rgba(234, 88, 12, 0.1);
                    }
                    
                    .footer {
                        background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
                        padding: 35px 50px;
                        text-align: center;
                        border-top: 1px solid #e5e7eb;
                    }
                    
                    .footer p {
                        font-size: 14px;
                        color: #6b7280;
                        margin-bottom: 10px;
                        line-height: 1.5;
                    }
                    
                    .security-note {
                        background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
                        border: 2px solid #fed7cc;
                        border-radius: 16px;
                        padding: 30px;
                        margin: 35px 0;
                        box-shadow: 0 8px 20px rgba(234, 88, 12, 0.1);
                        position: relative;
                        overflow: hidden;
                    }
                    
                    .security-note::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        height: 3px;
                        background: linear-gradient(90deg, #ea580c 0%, #dc2626 50%, #b91c1c 100%);
                    }
                    
                    .security-note h3 {
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 50%, #b91c1c 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                        font-size: 20px;
                        font-weight: 700;
                        margin-bottom: 15px;
                        display: flex;
                        align-items: center;
                    }
                    
                    .security-note p {
                        color: #ea580c;
                        font-size: 16px;
                        line-height: 1.7;
                        font-weight: 500;
                    }
                    
                    .mission-highlight {
                        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
                        border: 2px solid #f59e0b;
                        border-radius: 16px;
                        padding: 30px;
                        margin: 35px 0;
                        box-shadow: 0 8px 20px rgba(245, 158, 11, 0.15);
                        position: relative;
                        overflow: hidden;
                    }
                    
                    .mission-highlight::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        height: 3px;
                        background: linear-gradient(90deg, #f59e0b 0%, #d97706 50%, #b45309 100%);
                    }
                    
                    .mission-highlight h3 {
                        color: #92400e;
                        font-size: 20px;
                        font-weight: 700;
                        margin-bottom: 15px;
                        display: flex;
                        align-items: center;
                    }
                    
                    .mission-highlight p {
                        color: #92400e;
                        font-size: 16px;
                        line-height: 1.7;
                        font-weight: 500;
                    }
                    
                    .icon {
                        margin-right: 10px;
                        font-size: 1.2em;
                    }
                    
                    @media (max-width: 650px) {
                        body {
                            padding: 10px 0;
                        }
                        
                        .email-container {
                            margin: 0 10px;
                            border-radius: 16px;
                        }
                        
                        .header {
                            padding: 40px 25px;
                        }
                        
                        .header h1 {
                            font-size: 28px;
                        }
                        
                        .header-ornament {
                            font-size: 3rem;
                        }
                        
                        .content {
                            padding: 40px 25px;
                        }
                        
                        .greeting {
                            font-size: 24px;
                        }
                        
                        .message {
                            font-size: 16px;
                        }
                        
                        .footer {
                            padding: 25px;
                        }
                        
                        .verify-button {
                            display: block;
                            width: 100%;
                            padding: 20px;
                            font-size: 16px;
                        }
                        
                        .security-note,
                        .mission-highlight {
                            padding: 20px;
                            margin: 25px 0;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        <div class="header-content">
                            <div class="header-ornament">📚</div>
                            <h1>$appName</h1>
                            <p>Empowering Children Through Education</p>
                        </div>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">Welcome $userName! 🌟</div>
                        
                        <div class="message">
                            Thank you for joining <strong>$appName</strong> - a social initiative dedicated to teaching and empowering young children through quality education!
                            <br><br>
                            We're thrilled to have you as part of our mission to make education accessible and joyful for every child. To complete your registration and start contributing to this noble cause, please verify your email address:
                        </div>
                        
                        <div class="button-container">
                            <a href="$verificationUrl" class="verify-button">
                                <span class="button-text">
                                    <span>✨</span>
                                    Verify & Join Our Mission
                                    <span>🚀</span>
                                </span>
                            </a>
                        </div>
                        
                        <div class="mission-highlight">
                            <h3><span class="icon">🎯</span>Our Mission</h3>
                            <p>
                                Together, we're building a brighter future by providing quality education to young children who need it most. 
                                Every verified member brings us one step closer to transforming lives through learning.
                            </p>
                        </div>
                        
                        <div class="security-note">
                            <h3><span class="icon">🔒</span>Account Security</h3>
                            <p>
                                This verification link will expire in 24 hours for your security. 
                                If you didn't create an account to support children's education with $appName, please ignore this email.
                            </p>
                        </div>
                        
                        <div class="alternative-text">
                            <strong>Having trouble with the button?</strong><br>
                            Copy and paste this link into your browser:
                            <div class="url-text">$verificationUrl</div>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>© ${java.time.Year.now().value} $appName - A Social Initiative for Children's Education. All rights reserved.</p>
                        <p>This email was sent to verify your account and welcome you to our educational mission.</p>
                        <p>Questions? Contact our team - we're here to help you make a difference!</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }


    // Additional method for password reset emails
    fun sendPasswordResetEmail(email: String, token: String, userName: String? = null) {
        val subject = "$appName: Secure Your Account - Continue Supporting Children's Education"
        val resetUrl = "$baseUrl/reset-password?token=$token"

        val content = buildPasswordResetEmailContent(
            userName = userName ?: "Educator",
            resetUrl = resetUrl,
            appName = appName
        )

        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setTo(email)
            helper.setFrom(fromEmail, appName)
            helper.setSubject(subject)
            helper.setText(content, true)
            mailSender.send(message)
            logger.info("Password reset email sent to $email")
        } catch (e: Exception) {
            logger.error("Error while sending password reset email to $email", e)
            throw e
        }
    }

    private fun buildPasswordResetEmailContent(
        userName: String,
        resetUrl: String,
        appName: String
    ): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset - $appName</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #1a1a1a; 
                        background: linear-gradient(135deg, #fff5f5 0%, #fed7cc 25%, #fb923c 50%, #ea580c 75%, #c2410c 100%);
                        min-height: 100vh;
                        padding: 20px 0;
                    }
                    .email-container { 
                        max-width: 650px; 
                        margin: 0 auto; 
                        background: rgba(255, 255, 255, 0.95);
                        backdrop-filter: blur(10px);
                        border-radius: 24px; 
                        overflow: hidden; 
                        box-shadow: 
                            0 25px 50px rgba(234, 88, 12, 0.15),
                            0 0 0 1px rgba(255, 255, 255, 0.1),
                            inset 0 1px 0 rgba(255, 255, 255, 0.6);
                        border: 1px solid rgba(251, 146, 60, 0.2);
                    }
                    .header { 
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 20%, #b91c1c 40%, #991b1b 60%, #7f1d1d 80%, #450a0a 100%); 
                        padding: 60px 40px; 
                        text-align: center; 
                        color: white; 
                        position: relative; 
                        overflow: hidden; 
                    }
                    .header-ornament {
                        font-size: 4rem;
                        margin-bottom: 20px;
                        text-shadow: 0 4px 8px rgba(0,0,0,0.3);
                        filter: drop-shadow(0 0 20px rgba(255,255,255,0.4));
                    }
                    .header h1 { 
                        font-size: 36px; 
                        font-weight: 800; 
                        margin-bottom: 12px; 
                        letter-spacing: -1px;
                        text-shadow: 0 2px 4px rgba(0,0,0,0.2);
                    }
                    .header p { 
                        font-size: 18px; 
                        opacity: 0.95; 
                        font-weight: 300;
                        letter-spacing: 0.5px;
                    }
                    .content { 
                        padding: 60px 50px; 
                        background: linear-gradient(180deg, #ffffff 0%, #fff7ed 30%, #ffedd5 70%, #fed7cc 100%);
                        position: relative;
                    }
                    .content::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        height: 3px;
                        background: linear-gradient(90deg, #ea580c 0%, #dc2626 25%, #b91c1c 50%, #991b1b 75%, #ea580c 100%);
                    }
                    .greeting { 
                        font-size: 28px; 
                        font-weight: 700; 
                        margin-bottom: 30px; 
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 30%, #b91c1c 70%, #991b1b 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                        text-align: center;
                        letter-spacing: -0.5px;
                    }
                    .message { 
                        font-size: 17px; 
                        line-height: 1.8; 
                        margin-bottom: 40px; 
                        color: #374151; 
                    }
                    .message strong {
                        color: #dc2626;
                        font-weight: 600;
                    }
                    .button-container {
                        text-align: center;
                        margin: 40px 0;
                        perspective: 1000px;
                    }
                    .verify-button { 
                        display: inline-block; 
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 25%, #b91c1c 50%, #991b1b 75%, #7f1d1d 100%); 
                        color: white; 
                        padding: 20px 48px; 
                        text-decoration: none; 
                        border-radius: 16px; 
                        font-weight: 700; 
                        font-size: 18px; 
                        text-align: center; 
                        transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); 
                        box-shadow: 
                            0 12px 28px rgba(234, 88, 12, 0.4),
                            0 4px 8px rgba(220, 38, 38, 0.3),
                            inset 0 1px 0 rgba(255, 255, 255, 0.2); 
                        text-transform: none; 
                        letter-spacing: 0.5px; 
                        position: relative; 
                        overflow: hidden;
                        border: 2px solid rgba(255, 255, 255, 0.1);
                        transform-style: preserve-3d;
                    }
                    .verify-button:hover {
                        transform: translateY(-6px) rotateX(5deg);
                        box-shadow: 
                            0 20px 40px rgba(234, 88, 12, 0.5),
                            0 8px 16px rgba(220, 38, 38, 0.4),
                            inset 0 1px 0 rgba(255, 255, 255, 0.3);
                        background: linear-gradient(135deg, #dc2626 0%, #b91c1c 25%, #991b1b 50%, #7f1d1d 75%, #450a0a 100%);
                    }
                    .button-text {
                        position: relative;
                        z-index: 2;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        gap: 8px;
                    }
                    .alternative-text { 
                        font-size: 15px; 
                        color: #6b7280; 
                        margin-top: 35px; 
                        padding: 25px; 
                        background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%); 
                        border-radius: 12px; 
                        border-left: 4px solid #ea580c;
                        box-shadow: 0 2px 8px rgba(234, 88, 12, 0.1);
                    }
                    .alternative-text strong {
                        color: #374151;
                        font-weight: 600;
                    }
                    .url-text { 
                        word-break: break-all; 
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 50%, #b91c1c 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                        font-family: 'SF Mono', Consolas, 'Liberation Mono', Menlo, monospace;
                        background-color: #fff7ed;
                        padding: 16px 20px;
                        border-radius: 8px;
                        margin-top: 12px;
                        display: inline-block;
                        border: 2px solid #fed7cc;
                        font-size: 14px;
                        box-shadow: inset 0 2px 4px rgba(234, 88, 12, 0.1);
                    }
                    .footer { 
                        background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%); 
                        padding: 35px 50px; 
                        text-align: center; 
                        border-top: 1px solid #e5e7eb; 
                    }
                    .footer p { 
                        font-size: 14px; 
                        color: #6b7280; 
                        margin-bottom: 10px;
                        line-height: 1.5;
                    }
                    .security-note { 
                        background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%); 
                        border: 2px solid #fed7cc; 
                        border-radius: 16px; 
                        padding: 30px; 
                        margin: 35px 0; 
                        box-shadow: 0 8px 20px rgba(234, 88, 12, 0.1);
                        position: relative;
                        overflow: hidden;
                    }
                    .security-note::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        height: 3px;
                        background: linear-gradient(90deg, #ea580c 0%, #dc2626 50%, #b91c1c 100%);
                    }
                    .security-note h3 { 
                        background: linear-gradient(135deg, #ea580c 0%, #dc2626 50%, #b91c1c 100%);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                        font-size: 20px; 
                        font-weight: 700;
                        margin-bottom: 15px; 
                        display: flex; 
                        align-items: center; 
                    }
                    .security-note p { 
                        color: #ea580c; 
                        font-size: 16px; 
                        line-height: 1.7; 
                        font-weight: 500; 
                    }
                    .icon { 
                        margin-right: 10px;
                        font-size: 1.2em;
                    }
                    @media (max-width: 650px) { 
                        body {
                            padding: 10px 0;
                        }
                        .email-container { 
                            margin: 0 10px; 
                            border-radius: 16px; 
                        }
                        .header { 
                            padding: 40px 25px; 
                        }
                        .header h1 { 
                            font-size: 28px; 
                        }
                        .header-ornament {
                            font-size: 3rem;
                        }
                        .content { 
                            padding: 40px 25px; 
                        }
                        .greeting {
                            font-size: 24px;
                        }
                        .message {
                            font-size: 16px;
                        }
                        .footer { 
                            padding: 25px; 
                        }
                        .verify-button { 
                            display: block; 
                            width: 100%; 
                            padding: 20px;
                            font-size: 16px;
                        }
                        .security-note {
                            padding: 20px;
                            margin: 25px 0;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="header">
                        <div class="header-content">
                            <div class="header-ornament">🔐</div>
                            <h1>$appName</h1>
                            <p>Secure Your Account</p>
                        </div>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">Hello $userName! 🔐</div>
                        
                        <div class="message">
                            We received a request to reset your password for your <strong>$appName</strong> account - your gateway to supporting children's education.
                            <br><br>
                            To continue your valuable contribution to our educational mission, please click below to create a new secure password:
                        </div>
                        
                        <div class="button-container">
                            <a href="$resetUrl" class="verify-button">
                                <span class="button-text">
                                    <span>🔑</span>
                                    Reset Password & Continue
                                    <span>🚀</span>
                                </span>
                            </a>
                        </div>
                        
                        <div class="security-note">
                            <h3><span class="icon">⚠️</span>Account Security Notice</h3>
                            <p>
                                This password reset link will expire in 1 hour for your security. 
                                If you didn't request this reset, please ignore this email - your account remains secure and your support for children's education continues uninterrupted.
                            </p>
                        </div>
                        
                        <div class="alternative-text">
                            <strong>Having trouble with the button?</strong><br>
                            Copy and paste this link into your browser:
                            <div class="url-text">$resetUrl</div>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>© ${java.time.Year.now().value} $appName - A Social Initiative for Children's Education. All rights reserved.</p>
                        <p>This email was sent to help secure your account and ensure continued access to our platform.</p>
                        <p>Questions about account security? Our support team is here to help!</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}