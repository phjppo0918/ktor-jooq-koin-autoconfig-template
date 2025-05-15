package com.example.member.service

import com.example.member.repository.MemberRepository
import member.domain.Member
import org.koin.core.annotation.Single

@Single
class MemberService(
    private val memberRepository: MemberRepository,
) {
    fun save(member: Member) {
        memberRepository.save(member)
    }
}
