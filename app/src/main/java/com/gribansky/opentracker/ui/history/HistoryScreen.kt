package com.gribansky.opentracker.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gribansky.opentracker.data.UserData
import com.gribansky.opentracker.ui.ServiceViewModel
import com.gribansky.opentracker.ui.components.AccountRow
import com.gribansky.opentracker.ui.components.StatementBody

/**
 * The Accounts screen.
 */
@Composable
fun HistoryScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    onAccountClick: (String) -> Unit = {},
) {
    val viewModel: ServiceViewModel = viewModel(viewModelStoreOwner)
    val amountsTotal = remember { UserData.accounts.map { account -> account.balance }.sum() }
    StatementBody(
        modifier = Modifier.semantics { contentDescription = "Accounts Screen" },
        items = UserData.accounts,
        amounts = { account -> account.balance },
        colors = { account -> account.color },
        amountsTotal = amountsTotal,
        circleLabel = "???????",
        rows = { account ->
            AccountRow(
                modifier = Modifier.clickable {
                    onAccountClick(account.name)
                },
                name = account.name,
                number = account.number,
                amount = account.balance,
                color = account.color
            )
        }
    )
}

/**
 * Detail screen for a single account.
 */
@Composable
fun SingleAccountScreen(
    accountType: String? = UserData.accounts.first().name
) {
    val account = remember(accountType) { UserData.getAccount(accountType) }
    StatementBody(
        items = listOf(account),
        colors = { account.color },
        amounts = { account.balance },
        amountsTotal = account.balance,
        circleLabel = account.name,
    ) { row ->
        AccountRow(
            name = row.name,
            number = row.number,
            amount = row.balance,
            color = row.color
        )
    }
}
