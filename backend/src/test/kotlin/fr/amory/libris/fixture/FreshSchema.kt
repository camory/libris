package fr.amory.libris.fixture

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext

class FreshSchema : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        val flyway = getApplicationContext(context).getBean(Flyway::class.java)
        flyway.clean()
        flyway.migrate()
    }
}
