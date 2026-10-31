package ru.yandex.practicum.contacts.presentation.sorting

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.yandex.practicum.contacts.R
import ru.yandex.practicum.contacts.data.models.SortOrder
import ru.yandex.practicum.contacts.presentation.ui.components.CommonBottomSheet

@Composable
fun SortBottomSheet(
    currentSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    CommonBottomSheet(
        title = stringResource(R.string.sort_by_default),
        items = SortOrder.entries,
        selectedItems = setOf(currentSortOrder),
        onItemsSelected = { selected ->
            onSortOrderSelected(selected.firstOrNull { it != currentSortOrder } ?: currentSortOrder)
        },
        onDismiss = onDismiss
    ) { sortOrder, isSelected ->
        SortOrderOption(
            isSelected = isSelected,
            sortOrder = sortOrder
        )
    }
}

@Composable
private fun SortOrderOption(
    isSelected: Boolean,
    sortOrder: SortOrder
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            sortOrder.displayName
        )
    }
}
