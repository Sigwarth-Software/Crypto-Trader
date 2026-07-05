package org.cryptotrader.contact.comm.email.request

import org.cryptotrader.contact.service.email.template.EmailTemplate

data class EmailRequest(
    val to: String,
    val subject: String,
    val body: String,
    val emailTemplate: EmailTemplate)