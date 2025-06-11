package com.gribansky.opentracker.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.gribansky.opentracker.data.Bill
import com.gribansky.opentracker.data.UserData
import com.gribansky.opentracker.ui.components.PacketRow
import com.gribansky.opentracker.ui.components.StatementBody
import java.util.Date


@Composable
fun SettingsScreen(
    bills: List<Bill> = remember { UserData.bills }
) {
    StatementBody(
        modifier = Modifier.clearAndSetSemantics { contentDescription = "Bills" },
        items = bills,
        amounts = { bill -> bill.amount },
        colors = { bill -> bill.color },
        amountsTotal = bills.map { bill -> bill.amount }.sum(),
        circleLabel = "????????",
        rows = { bill ->
            PacketRow(
                onDate = Date().time,
                color = Color.Green
            )
        }
    )
}
