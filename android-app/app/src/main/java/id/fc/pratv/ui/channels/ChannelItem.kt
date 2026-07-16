package id.fc.pratv.ui.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.KeyEvent
import coil.compose.AsyncImage
import id.fc.pratv.data.model.Channel
import id.fc.pratv.data.model.isLocal
import id.fc.pratv.ui.theme.VSCodeColors
import id.fc.pratv.ui.theme.rememberResponsive

@Composable
fun ChannelItem(
    channel: Channel,
    onActivate: () -> Unit,
    onFocus: (Channel) -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val r = rememberResponsive()
    val logoSize = r.channelItem * 0.4f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = r.spacingSmall * 0.4f, horizontal = r.spacingSmall * 0.5f)
            .onFocusChanged { state ->
                focused = state.isFocused
                if (state.isFocused) onFocus(channel)
            }
            .focusable()
            .onKeyEvent { event ->
                val native = event.nativeKeyEvent
                if (native.action == KeyEvent.ACTION_DOWN &&
                    (native.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        native.keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    onActivate()
                    true
                } else {
                    false
                }
            }
            .clickable { onActivate() }
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = VSCodeColors.accent,
                shape = RoundedCornerShape(6.dp)
            )
            .background(
                color = if (focused) VSCodeColors.accent.copy(alpha = 0.18f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = r.spacingSmall * 0.6f, vertical = r.spacingSmall * 0.5f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (channel.logoUrl != null) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    modifier = Modifier
                        .size(logoSize, logoSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(logoSize, logoSize)
                        .clip(CircleShape)
                        .background(Color(0xFF3c3c3c)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.name.firstOrNull()?.uppercase() ?: "?",
                        color = VSCodeColors.textPrimary,
                        fontSize = (logoSize.value * 0.5f).sp
                    )
                }
            }
        }
        Spacer(Modifier.width(r.spacingSmall))
        Text(
            text = channel.name,
            color = VSCodeColors.textPrimary,
            fontSize = (13 * r.scale).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (channel.isLocal()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(VSCodeColors.accent)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(text = "Lokal", color = Color.White, fontSize = (9 * r.scale).sp)
            }
        }
        if (channel.name.contains("(V+)", ignoreCase = true)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE94560))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(text = "V+", color = Color.White, fontSize = (9 * r.scale).sp)
            }
        }
    }
}
