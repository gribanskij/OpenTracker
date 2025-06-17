package com.gribansky.opentracker.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gribansky.opentracker.R
import com.gribansky.opentracker.ui.theme.TrackerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



@Composable
fun LogRow(
    modifier: Modifier = Modifier,
    message: String = "Сообщение",
    onDate: String,
    color: Color

) {

    BaseRow(
        modifier = modifier,
        color = color,
        title = stringResource(R.string.log),
        message = message,
        onDate = onDate

    )

}

@Composable
fun GpsRow(
    modifier: Modifier = Modifier,
    message: String = "Определено",
    onDate: String,
    color: Color
) {
    BaseRow(
        modifier = modifier,
        color = color,
        title = stringResource(R.string.gps),
        message = message,
        onDate = onDate

    )
}

@Composable
fun GSMRow(
    modifier: Modifier = Modifier,
    message: String = "Определено",
    onDate: String,
    color: Color
) {
    BaseRow(
        modifier = modifier,
        color = color,
        title = stringResource(R.string.gsm),
        message = message,
        onDate = onDate

    )
}


@Composable
fun PacketRow(
    modifier: Modifier = Modifier,
    message: String = "Отправлено",
    onDate: String,
    color: Color
) {
    BaseRow(
        modifier = modifier,
        color = color,
        title = stringResource(R.string.packets),
        message = message,
        onDate = onDate
    )
}

@Composable
private fun BaseRow(
    modifier: Modifier = Modifier,
    color: Color,
    title: String,
    message: String,
    onDate: String
) {
    Row(
        modifier = modifier
            .height(68.dp)
            .clearAndSetSemantics {
                contentDescription =
                    "$title $message $onDate"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val typography = MaterialTheme.typography
        ItemIndicator(
            color = color,
            modifier = Modifier
        )
        Spacer(Modifier.width(12.dp))

        Text(
            modifier = Modifier.width(72.dp),
            text = title,
            style = typography.body1,
            color = MaterialTheme.colors.onBackground
        )

        Spacer(Modifier.weight(0.5f))

        Column(
            modifier=Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = message,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                style = typography.body2,
                color = MaterialTheme.colors.onBackground
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = onDate,
                    style = typography.subtitle1,
                    color = MaterialTheme.colors.onBackground
                )
            }
        }
        Spacer(Modifier.width(16.dp))

    }
    TrackerDivider()
}


@Composable
private fun ItemIndicator(color: Color, modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .size(4.dp, 36.dp)
            .background(color = color)
    )
}

@Composable
fun TrackerDivider(modifier: Modifier = Modifier) {
    Divider(color = MaterialTheme.colors.background, thickness = 1.dp, modifier = modifier)
}


/**
 * Форматирует дату и время в формате "17:06 10 июн. 25"
 */
fun formatDateTime(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm dd MMM yy", Locale.getDefault())
    return formatter.format(date).lowercase()
}



fun <E> List<E>.extractProportions(selector: (E) -> Float): List<Float> {
    val total = this.sumOf { selector(it).toDouble() }
    return this.map { (selector(it) / total).toFloat() }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RowPreview() {
    TrackerTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            GpsRow(color = Color.Green, onDate = "16:47 10 июн. 25")
            GSMRow(color = Color.Yellow, onDate = "16:47 10 июн. 25")
            PacketRow(color = Color.Blue, onDate = "16:47 10 июн. 25")
        }
    }
}
