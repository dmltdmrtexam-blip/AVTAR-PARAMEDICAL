package com.example.data.model

enum class PaymentMethod {
    UPI,
    CARD,
    NETBANKING,
    OFFLINE_BANK_TRANSFER
}

enum class PaymentStatus {
    COMPLETED,
    PENDING_VERIFICATION,
    FAILED,
    REFUNDED
}

data class PaymentTransaction(
    val transactionId: String, // pay_xxx or UPI UTR
    val orderId: String,       // order_xxx
    val courseId: String,
    val courseTitle: String,
    val amount: Double,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val timestamp: String,
    val signature: String = "",
    val adminApprovalNote: String? = null
)
