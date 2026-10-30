package ru.yandex.practicum.contacts.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import ru.yandex.practicum.contacts.R
import ru.yandex.practicum.contacts.presentation.country.CountryCodeBottomSheet
import ru.yandex.practicum.contacts.presentation.messengers.MessengersBottomSheet
import ru.yandex.practicum.contacts.presentation.sorting.SortBottomSheet
import ru.yandex.practicum.contacts.presentation.ui.components.ContactItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ContactsScreen(
    permissionState: PermissionState,
    viewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(LocalContext.current)
    )
) {
    var showSortBottomSheet by remember { mutableStateOf(false) }
    var showFilterBottomSheet by remember { mutableStateOf(false) }
    var showCountryCodeBottomSheet by remember { mutableStateOf(false) }
    
    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.onPermissionGranted()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showSortBottomSheet = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.menu_sort))
                    }
                    
                    IconButton(onClick = { showFilterBottomSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.menu_filter))
                    }
                    
                    IconButton(onClick = { showCountryCodeBottomSheet = true }) {
                        Icon(Icons.Default.Language, contentDescription = stringResource(R.string.menu_country))
                    }
                    
                    IconButton(onClick = { viewModel.toggleSearchVisibility() }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.menu_search))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                !permissionState.status.isGranted -> {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stringResource(R.string.permission_required_message),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { permissionState.launchPermissionRequest() }) {
                            Text(stringResource( R.string.ask_permission_button))
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (viewModel.isSearchVisible) {
                            OutlinedTextField(
                                value = viewModel.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                placeholder = { Text(stringResource(R.string.menu_search)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                            )
                        }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.getFilteredAndSortedContacts()) { contact ->
                                ContactItem(contact = contact)
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showSortBottomSheet) {
        SortBottomSheet(
            currentSortOrder = viewModel.sortOrder,
            onSortOrderSelected = { order ->
                viewModel.updateSortOrder(order)
                showSortBottomSheet = false
            },
            onDismiss = { showSortBottomSheet = false }
        )
    }
    
    if (showFilterBottomSheet) {
        MessengersBottomSheet(
            selectedApps = viewModel.selectedMessagingApps,
            onAppsSelected = { apps ->
                viewModel.updateSelectedMessagingApps(apps)
            },
            onDismiss = { showFilterBottomSheet = false }
        )
    }
    
    if (showCountryCodeBottomSheet) {
        CountryCodeBottomSheet(
            selectedCodes = viewModel.selectedCountryCodes,
            onCodesSelected = { codes ->
                viewModel.updateSelectedCountryCodes(codes)
            },
            onDismiss = { showCountryCodeBottomSheet = false }
        )
    }
}
