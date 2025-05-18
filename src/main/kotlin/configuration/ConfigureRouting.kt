package com.example.configuration

import com.example.member.router.memberRouter
import com.example.member.service.MemberService
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val memberService by inject<MemberService>()

    routing {
        memberRouter(memberService)
    }
}
