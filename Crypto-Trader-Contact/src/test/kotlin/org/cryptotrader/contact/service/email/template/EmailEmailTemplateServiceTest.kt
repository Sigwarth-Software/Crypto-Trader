package org.cryptotrader.contact.service.email.template

import org.cryptotrader.contact.natives.normalized
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions.assertEquals

@SpringBootTest(properties = [
    "spring.autoconfigure.exclude=org.cryptotrader.docs.autoconfigure.CryptoTraderDocsAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    "docs.autoconfigure.enabled=false"
])
class EmailEmailTemplateServiceTest @Autowired constructor(
    private val emailTemplateService: EmailTemplateService
) : CryptoTraderTest() {

    val testEmailTemplate = EmailTemplate.WELCOME
    val testSubject: String = "Testing Crypto Trader"
    val testBody: String = "Hello from Spring Boot in Kotlin"
    val name: String = "User"
    val ctaLabel: String = "Open app"
    val year: String = "2026"

    @Nested
    @DisplayName("Process Template")
    inner class ProcessEmailTemplate {
        @Test
        @DisplayName("Should properly inject variables")
        fun processTemplate_InjectsVariables() {
            val expectedHtml: String = getExpectedWelcomeHtml(testSubject, testBody, name, ctaLabel, year).normalized()
            val actualHtml: String = emailTemplateService.process(
                testEmailTemplate,
                mapOf(
                    "subject" to testSubject,
                    "body" to testBody,
                    "name" to name,
                    "ctaLabel" to ctaLabel,
                    "year" to year
                )
            ).normalized()
            assertEquals(expectedHtml, actualHtml)
        }
    }
}

fun getExpectedWelcomeHtml(subject: String, body: String, name: String, ctaLabel: String, year: String): String {
    return """
            <!DOCTYPE html>
            <html lang="en" dir="ltr">
            <head>
                <meta charset="UTF-8" />
                <title>${subject}</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        padding: 20px;
                    }
                    .btn {
                        background:#0d6efd;
                        color:#fff;
                        padding:10px 16px;
                        text-decoration:none;
                        border-radius:6px;
                    }
                    .btn:hover {
                        cursor: pointer;
                        background:#0b5ed7;
                    }
                    .text-muted {
                        color: #6c757d;
                        font-size:12px;
                    }
                </style>
            </head>
            <body>
            <h1>Welcome, ${name}!</h1>
            <p>${body}</p>

            <p>
                <a href="" class="btn">${ctaLabel}</a>
            </p>

            <hr/>
            <img src="../../../Crypto-Trader-Assets/src/main/resources/assets/images/logos/crypto_trader/crypto_trader_logo_cropped_transparent.png" alt="Crypto Trader Logo" width="100" height="100"/>
            <p class="text-muted">
                Sent by Crypto Trader • <span>${year}</span>
            </p>
            </body>
            </html>
        """.trimIndent()
}
