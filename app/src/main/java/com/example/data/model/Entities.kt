package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
  @PrimaryKey
  val date: String, // format: "yyyy-MM-dd"
  val initialCash: Long = 0L,
  val isClosed: Boolean = false,
  val closedAt: Long? = null,
  val lastSyncAt: Long? = null,
  val storeName: String = "Toko Pak Kadi Cash System"
)

@Entity(
  tableName = "transactions",
  indices = [Index(value = ["date"]), Index(value = ["timestamp"])]
)
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val date: String, // format: "yyyy-MM-dd"
  val receiptNumber: String,
  val timestamp: Long = System.currentTimeMillis(),
  val totalAmount: Long,
  val cashPaid: Long,
  val changeAmount: Long,
  val paymentMethod: String = "Tunai"
)

@Entity(
  tableName = "transaction_items",
  indices = [Index(value = ["transactionId"]), Index(value = ["date"])]
)
data class TransactionItemEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val transactionId: Long,
  val date: String,
  val itemName: String,
  val qty: Int,
  val unitPrice: Long,
  val subtotal: Long
)

@Entity(
  tableName = "expenses",
  indices = [Index(value = ["date"]), Index(value = ["timestamp"])]
)
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val date: String, // format: "yyyy-MM-dd"
  val timestamp: Long = System.currentTimeMillis(),
  val description: String,
  val category: String = "Operasional",
  val amount: Long
)

data class TransactionWithItems(
  @Embedded
  val transaction: TransactionEntity,
  @Relation(
    parentColumn = "id",
    entityColumn = "transactionId"
  )
  val items: List<TransactionItemEntity>
)

// In-memory model for current active cart in Kasir screen
data class CartItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val itemName: String,
  val qty: Int,
  val unitPrice: Long
) {
  val subtotal: Long get() = qty.toLong() * unitPrice
}
