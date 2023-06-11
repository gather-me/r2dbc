package com.odenizturker.r2dbc.config

import com.odenizturker.r2dbc.annotation.Enumerator
import com.odenizturker.r2dbc.config.converter.EnumConverter
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.postgresql.extension.CodecRegistrar
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import java.lang.reflect.Field

@Configuration
@EnableR2dbcRepositories
@EnableConfigurationProperties(R2dbcProperties::class)
class DatabaseAutoConfiguration(
    private val prop: R2dbcProperties,
    private val context: ApplicationContext
) : AbstractR2dbcConfiguration() {
    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val options = ConnectionFactoryOptions.parse(prop.url)
        val host = options.getValue(Option.valueOf<String>("host")) as String
        val port = options.getValue(Option.valueOf<String>("port")) as Int
        val database = options.getValue(Option.valueOf<String>("database")) as String

        val builder = PostgresqlConnectionConfiguration.builder()
            .database(database)
            .username(prop.username)
            .password(prop.password)
            .host(host)
            .port(port)
            .preparedStatementCacheQueries(0)
        getEnumeratorClasses().forEach { (field, enum) ->
            @Suppress("UNCHECKED_CAST")
            builder.codecRegistrar(buildCodecRegistrar(enum.className, field.type as Class<out Enum<*>>))
        }

        val postgresConnection = PostgresqlConnectionFactory(builder.build())

        return if (prop.pool.isEnabled) {
            val poolConfig = ConnectionPoolConfiguration.builder()
                .connectionFactory(postgresConnection)
                .initialSize(prop.pool.initialSize)
                .maxSize(prop.pool.maxSize)
                .maxAcquireTime(prop.pool.maxAcquireTime)
                .maxIdleTime(prop.pool.maxIdleTime)
                .maxLifeTime(prop.pool.maxLifeTime)
                .maxCreateConnectionTime(prop.pool.maxCreateConnectionTime)
                .validationDepth(prop.pool.validationDepth)
                .build()
            ConnectionPool(poolConfig)
        } else {
            postgresConnection
        }
    }

    override fun getCustomConverters(): List<GenericConverter> {
        return getEnumeratorClasses().map { (field, _) ->
            EnumConverter(field.type)
        }
    }

    override fun getMappingBasePackages(): MutableCollection<String> {
        val candidates = context.getBeansWithAnnotation(SpringBootApplication::class.java)
        return candidates.map { it.value::class.java.packageName }.toMutableList()
    }

    private fun getEnumeratorClasses(): List<Pair<Field, Enumerator>> {
        return r2dbcManagedTypes().toList().flatMap {
            it.declaredFields.mapNotNull { field ->
                val annotation = field.getDeclaredAnnotation(Enumerator::class.java) ?: return@mapNotNull null
                field to annotation
            }
        }
    }

    fun buildCodecRegistrar(name: String, javaClass: Class<out Enum<*>>): CodecRegistrar {
        return EnumCodec.builder().withEnum(name, javaClass).build()
    }
}
