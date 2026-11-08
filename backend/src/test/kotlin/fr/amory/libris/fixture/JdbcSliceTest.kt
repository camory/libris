package fr.amory.libris.fixture

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JdbcTest(properties = ["spring.flyway.clean-disabled=false", "spring.datasource.hikari.maximum-pool-size=3"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
@Import(CleanSchema::class)
@ExtendWith(FreshSchema::class)
annotation class JdbcSliceTest
