package com.odenizturker.r2dbc.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Enumerator(val className: String)
