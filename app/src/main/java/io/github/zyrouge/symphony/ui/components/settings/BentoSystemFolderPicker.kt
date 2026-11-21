package io.github.zyrouge.symphony.ui.components.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.ActivityUtils

@Composable
fun BentoSystemFolderPicker(
    context: ViewContext,
    values: Set<Uri>,
    onChange: (Set<Uri>) -> Unit,
) {
    val colors = LocalBentoColors.current
    val folders = remember(values) { mutableStateListOf<Uri>().apply { addAll(values) } }
    val addLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let {
            ActivityUtils.makePersistableReadableUri(context.symphony.applicationContext, it)
            folders.add(it)
            onChange(folders.toSet())
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(folders) { index, uri ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(BentoRadii.ROW.dp))
                        .background(colors.raised)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        Uri.decode(uri.path ?: uri.toString()),
                        style = BentoTypography.secondary,
                        color = colors.fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Filled.Close,
                        context.symphony.t.Cancel,
                        tint = colors.sub,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                folders.removeAt(index)
                                onChange(folders.toSet())
                            },
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BentoRadii.ROW.dp))
                .background(colors.accent)
                .clickable { addLauncher.launch(null) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Add, null, tint = colors.onAccent)
            Text(
                context.symphony.t.AddFolder,
                style = BentoTypography.rowTitle,
                color = colors.onAccent,
            )
        }
    }
}
