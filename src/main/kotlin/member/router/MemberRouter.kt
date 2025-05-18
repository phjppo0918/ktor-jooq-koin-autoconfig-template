package com.example.member.router

import com.example.member.service.MemberService
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import member.domain.Member
import org.koin.ktor.ext.inject

fun Route.memberRouter() {
    val memberService by inject<MemberService>()

    get("/member") {
        memberService.save(Member(1, "4"))
    }
}
