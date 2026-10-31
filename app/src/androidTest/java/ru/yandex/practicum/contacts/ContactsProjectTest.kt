package ru.yandex.practicum.contacts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.yandex.practicum.contacts.data.models.Contact
import ru.yandex.practicum.contacts.data.models.CountryCode
import ru.yandex.practicum.contacts.data.models.MessagingApp
import ru.yandex.practicum.contacts.data.models.SortOrder
import ru.yandex.practicum.contacts.domain.repository.ContactsRepository
import ru.yandex.practicum.contacts.domain.usecase.GetContactsUseCase
import ru.yandex.practicum.contacts.presentation.country.CountryCodeBottomSheet
import ru.yandex.practicum.contacts.presentation.main.ContactsViewModel
import ru.yandex.practicum.contacts.presentation.messengers.MessengersBottomSheet
import ru.yandex.practicum.contacts.presentation.sorting.SortBottomSheet

@RunWith(AndroidJUnit4::class)
class ContactsProjectTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun bottomSheetsKeepMultipleSelectionAndSortWhenTappingRows() {
        var countries by mutableStateOf<Set<CountryCode>>(emptySet())
        var apps by mutableStateOf<Set<MessagingApp>>(emptySet())
        var sortOrder by mutableStateOf(SortOrder.NAME_ASC)
        var sheet by mutableStateOf("country")

        compose.setContent {
            when (sheet) {
                "country" -> CountryCodeBottomSheet(countries, { countries = it }, { sheet = "" })
                "messenger" -> MessengersBottomSheet(apps, { apps = it }, { sheet = "" })
                "sort" -> SortBottomSheet(sortOrder, { sortOrder = it; sheet = "" }, { sheet = "" })
            }
        }

        compose.onNodeWithText("+1").performClick()
        compose.onNodeWithText("+44").performClick()
        compose.runOnIdle { assertEquals(setOf(CountryCode.COMMON_CODES[0], CountryCode.COMMON_CODES[1]), countries) }
        compose.onNodeWithContentDescription("Закрыть").performClick()
        compose.runOnIdle { sheet = "country" }
        compose.onNodeWithText("+1").assertExists()
        compose.runOnIdle { assertEquals(2, countries.size) }

        compose.onNodeWithText("+1").performClick()
        compose.runOnIdle { assertEquals(setOf(CountryCode.COMMON_CODES[1]), countries) }
        repeat(10) { compose.onNodeWithText("+1").performClick() }
        compose.runOnIdle { assertEquals(setOf(CountryCode.COMMON_CODES[1]), countries) }
        compose.onNodeWithContentDescription("Закрыть").performClick()

        compose.runOnIdle { sheet = "messenger" }
        compose.onNodeWithText("TELEGRAM").performClick()
        compose.onNodeWithText("WHATS_APP").performClick()
        compose.runOnIdle { assertEquals(setOf(MessagingApp.TELEGRAM, MessagingApp.WHATS_APP), apps) }
        compose.onNodeWithContentDescription("Закрыть").performClick()
        compose.runOnIdle { sheet = "messenger" }
        compose.runOnIdle { assertEquals(2, apps.size) }
        compose.onNodeWithText("TELEGRAM").performClick()
        compose.runOnIdle { assertEquals(setOf(MessagingApp.WHATS_APP), apps) }
        compose.onNodeWithContentDescription("Закрыть").performClick()

        compose.runOnIdle { sheet = "sort" }
        compose.onNodeWithText(SortOrder.LAST_NAME_ASC.displayName).performClick()
        compose.runOnIdle { assertEquals(SortOrder.LAST_NAME_ASC, sortOrder) }
        compose.runOnIdle { sheet = "sort" }
        compose.onNodeWithText(SortOrder.NAME_DESC.displayName).performClick()
        compose.runOnIdle { assertEquals(SortOrder.NAME_DESC, sortOrder) }
        compose.runOnIdle { assertFalse(sheet == "sort") }
    }

    @Test
    fun countryAndMessengerFiltersNarrowContactsAndSortingKeepsFilters() {
        val source = listOf(
            Contact(1, "Alice", "Zephyr", "+15550001001", listOf(MessagingApp.TELEGRAM)),
            Contact(2, "Bob", "Young", "+445550001002", listOf(MessagingApp.WHATS_APP)),
            Contact(3, "Carol", "Adams", "+75550001003", listOf(MessagingApp.TELEGRAM, MessagingApp.WHATS_APP))
        )
        val repository = object : ContactsRepository {
            override fun getContacts() = flowOf(source)
        }
        val viewModel = ContactsViewModel(GetContactsUseCase(repository))

        compose.runOnIdle { viewModel.onPermissionGranted() }
        compose.waitUntil(5_000) { viewModel.contacts.size == 3 }
        compose.runOnIdle {
            assertEquals(listOf("Alice", "Bob", "Carol"), viewModel.getFilteredAndSortedContacts().map { it.firstName })

            viewModel.updateSelectedCountryCodes(setOf(CountryCode.COMMON_CODES[0], CountryCode.COMMON_CODES[1]))
            assertEquals(listOf("Alice", "Bob"), viewModel.getFilteredAndSortedContacts().map { it.firstName })

            viewModel.updateSelectedMessagingApps(setOf(MessagingApp.WHATS_APP))
            assertEquals(listOf("Bob"), viewModel.getFilteredAndSortedContacts().map { it.firstName })

            viewModel.updateSortOrder(SortOrder.LAST_NAME_DESC)
            assertEquals(listOf("Bob"), viewModel.getFilteredAndSortedContacts().map { it.firstName })
            assertEquals(2, viewModel.selectedCountryCodes.size)
            assertEquals(setOf(MessagingApp.WHATS_APP), viewModel.selectedMessagingApps)

            viewModel.updateSelectedCountryCodes(emptySet())
            viewModel.updateSelectedMessagingApps(emptySet())
            viewModel.updateSortOrder(SortOrder.LAST_NAME_ASC)
            assertEquals(listOf("Carol", "Bob", "Alice"), viewModel.getFilteredAndSortedContacts().map { it.firstName })
            viewModel.updateSortOrder(SortOrder.NAME_DESC)
            assertEquals(listOf("Carol", "Bob", "Alice"), viewModel.getFilteredAndSortedContacts().map { it.firstName })
            viewModel.updateSortOrder(SortOrder.NAME_ASC)
            assertEquals(listOf("Alice", "Bob", "Carol"), viewModel.getFilteredAndSortedContacts().map { it.firstName })
        }
    }
}
