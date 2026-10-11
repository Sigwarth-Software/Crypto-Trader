package org.cryptotrader.api.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(

) {
    @PostMapping("/init")
    fun initChat() {

    }

    @PostMapping("/{id}/send")
    fun sendMessage(@PathVariable("id") id: String) {

    }
}