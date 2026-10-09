package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CountdownStatus
import com.example.util.DateUtils
import com.example.util.LiveCountdown
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Real-time composable countdown timer that calculates the time remaining until a user-defined
 * exam date and updates second-by-second in real-time.
 */
@Composable
fun RealtimeCountdownTimer(
    examDateMillis: Long,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val liveCountdown by produceState(
        initialValue = DateUtils.calculateLiveCountdown(examDateMillis),
        key1 = examDateMillis
    ) {
        while (isActive) {
            value = DateUtils.calculateLiveCountdown(examDateMillis)
            delay(1000L)
        }
    }

    if (liveCountdown.isPassed) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = modifier
                .fillMaxWidth()
                .testTag("countdown_passed")
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exam Completed",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .testTag("realtime_countdown_timer"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Urgency / Status Indicator
            if (liveCountdown.isToday) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDC2626),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "EXAM TODAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            } else if (liveCountdown.status == CountdownStatus.URGENT) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEA580C).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEA580C).copy(alpha = 0.5f)),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "FINAL COUNTDOWN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFEA580C),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            // 4 Digital Units (Days, Hours, Minutes, Seconds)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountdownUnitBox(
                    value = liveCountdown.days,
                    label = "DAYS",
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )

                CountdownColon(accentColor = accentColor)

                CountdownUnitBox(
                    value = liveCountdown.hours,
                    label = "HOURS",
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )

                CountdownColon(accentColor = accentColor)

                CountdownUnitBox(
                    value = liveCountdown.minutes,
                    label = "MINS",
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )

                CountdownColon(accentColor = accentColor)

                CountdownUnitBox(
                    value = liveCountdown.seconds,
                    label = "SECS",
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CountdownUnitBox(
    value: Long,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    slideInVertically { height -> height } togetherWith
                        slideOutVertically { height -> -height }
                },
                label = "countdown_unit_$label"
            ) { targetValue ->
                Text(
                    text = String.format("%02d", targetValue),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = accentColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CountdownColon(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = ":",
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.padding(horizontal = 4.dp)
    )
}

/**
 * Compact real-time ticker string (e.g. "12d 04h 32m 15s") updating every second.
 */
@Composable
fun CompactRealtimeCountdown(
    examDateMillis: Long,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val liveCountdown by produceState(
        initialValue = DateUtils.calculateLiveCountdown(examDateMillis),
        key1 = examDateMillis
    ) {
        while (isActive) {
            value = DateUtils.calculateLiveCountdown(examDateMillis)
            delay(1000L)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.testTag("compact_realtime_countdown")
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = liveCountdown.formattedCompact,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = textColor
        )
    }
}
