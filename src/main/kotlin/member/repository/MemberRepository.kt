package com.example.member.repository

import member.domain.Member
import org.jooq.DSLContext
import org.jooq.generated.tables.JMember.MEMBER
import org.koin.core.annotation.Single

@Single
class MemberRepository(
    private val dslContext: DSLContext,
) {
    fun save(member: Member) {
        dslContext
            .insertInto(MEMBER, MEMBER.ID, MEMBER.NAME)
            .values(
                member.id,
                member.name,
            ).execute()
    }
}
