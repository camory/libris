package fr.amory.libris.library.fixture

import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionOperations

data class Transaction<S>(
    val before: S,
    val after: S,
)

class TransactionsObserving<S>(private val observe: () -> S) : TransactionOperations {
    private val transactions = mutableListOf<Transaction<S>>()

    val recorded: List<Transaction<S>> get() = transactions.toList()

    override fun <T> execute(action: TransactionCallback<T>): T {
        val before = observe()
        val result = action.doInTransaction(status())
        transactions += Transaction(before, observe())
        return result
    }

    private fun status(): TransactionStatus = SimpleTransactionStatus()
}
