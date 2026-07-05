package org.cryptotrader.contact.service.email.template

import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailTemplateService (
    private val templateEngine: TemplateEngine
) {
    fun process(emailTemplate: EmailTemplate, data: Map<String, String>): String {
        return when (emailTemplate) {
            EmailTemplate.WELCOME -> this.processWelcome(data)
        }
    }

    fun processWelcome(data: Map<String, String>): String {
        val context = Context().apply { 
            data.forEach {
                setVariable(it.key, it.value)
            }
        }
        return this.templateEngine.process("welcome", context)
    }
}