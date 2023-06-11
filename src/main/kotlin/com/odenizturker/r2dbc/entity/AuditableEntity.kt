package com.odenizturker.r2dbc.entity

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

abstract class AuditableEntity(
    @Id
    val id: Long? = null,
    @CreatedDate
    val createdDate: Instant? = null,
    @CreatedBy
    val createdBy: String? = null,
    @LastModifiedDate
    val updatedDate: Instant? = null,
    @LastModifiedBy
    val updatedBy: String? = null
)
