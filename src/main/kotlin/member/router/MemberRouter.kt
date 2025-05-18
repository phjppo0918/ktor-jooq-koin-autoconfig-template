package com.example.member.router

import com.example.member.service.MemberService
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import member.domain.Member

fun Route.memberRouter(memberService: MemberService) {
    get("/") {
        memberService.save(Member(1, "4"))
    }
}
