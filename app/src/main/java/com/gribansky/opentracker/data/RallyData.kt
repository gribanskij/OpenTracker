package com.gribansky.opentracker.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color


@Immutable
data class Account(
    val name: String,
    val number: Int,
    val balance: Float,
    val color: Color
)

@Immutable
data class Bill(
    val name: String,
    val due: String,
    val amount: Float,
    val color: Color
)


object UserData {
    val accounts: List<Account> = listOf(
        Account(
            "Checking",
            1234,
            2215.13f,
            Color(0xFF004940)
        ),
        Account(
            "Home Savings",
            5678,
            8676.88f,
            Color(0xFF005D57)
        ),
        Account(
            "Car Savings",
            9012,
            987.48f,
            Color(0xFF04B97F)
        ),
        Account(
            "Vacation",
            3456,
            253f,
            Color(0xFF37EFBA)
        )
    )
    val bills: List<Bill> = listOf(
        Bill(
            "RedPay Credit",
            "Jan 29",
            45.36f,
            Color(0xFFFFDC78)
        ),
        Bill(
            "Rent",
            "Feb 9",
            1200f,
            Color(0xFFFF6951)
        ),
        Bill(
            "TabFine Credit",
            "Feb 22",
            87.33f,
            Color(0xFFFFD7D0)
        ),
        Bill(
            "ABC Loans",
            "Feb 29",
            400f,
            Color(0xFFFFAC12)
        ),
        Bill(
            "ABC Loans 2",
            "Feb 29",
            77.4f,
            Color(0xFFFFAC12)
        )
    )

    fun getAccount(accountName: String?): Account {
        return accounts.first { it.name == accountName }
    }
}
