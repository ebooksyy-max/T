package com.example.mybillbook.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.mybillbook.ads.AppLovinBanner
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.ui.bills.*
import com.example.mybillbook.ui.components.AppBottomNavigationBar
import com.example.mybillbook.ui.customers.*
import com.example.mybillbook.ui.home.*
import com.example.mybillbook.ui.inventory.*
import com.example.mybillbook.ui.khata.*
import com.example.mybillbook.ui.purchases.*
import com.example.mybillbook.ui.reports.ReportsScreen
import com.example.mybillbook.ui.settings.*

@Composable
fun AppNavigation(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        Routes.HOME,
        Routes.BILLS,
        Routes.KHATA,
        Routes.INVENTORY,
        Routes.REPORTS
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                androidx.compose.foundation.layout.Column {
                    AppLovinBanner(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    AppBottomNavigationBar(
                        currentRoute = currentRoute,
                        onNavigate = { targetRoute ->
                            navController.navigate(targetRoute) {
                                popUpTo(Routes.HOME) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            // 1. Home
            composable(Routes.HOME) {
                val homeVm = remember { HomeViewModel(container) }
                HomeScreen(
                    viewModel = homeVm,
                    onCreateBillClick = { navController.navigate(Routes.CREATE_BILL) },
                    onBillClick = { invId -> navController.navigate(Routes.billDetails(invId)) },
                    onViewAllBillsClick = { navController.navigate(Routes.BILLS) },
                    onRecordPaymentClick = { navController.navigate(Routes.recordPayment()) },
                    onAddProductClick = { navController.navigate(Routes.addEditProduct()) },
                    onAddCustomerClick = { navController.navigate(Routes.addEditCustomer()) },
                    onInventoryClick = { navController.navigate(Routes.INVENTORY) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                )
            }

            // 2. Bills List
            composable(Routes.BILLS) {
                val billsVm = remember { BillsViewModel(container) }
                BillsScreen(
                    viewModel = billsVm,
                    onCreateBillClick = { navController.navigate(Routes.CREATE_BILL) },
                    onBillClick = { invId -> navController.navigate(Routes.billDetails(invId)) }
                )
            }

            // 3. Create Bill
            composable(
                route = Routes.CREATE_BILL,
                arguments = listOf(navArgument("draftId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val activity = context as? android.app.Activity
                val createBillVm = remember { CreateBillViewModel(container) }
                CreateBillScreen(
                    viewModel = createBillVm,
                    onBackClick = { navController.popBackStack() },
                    onInvoiceSaved = { savedId ->
                        if (activity != null) {
                            com.example.mybillbook.ads.AppLovinManager.showInterstitial(activity) {
                                navController.popBackStack()
                                navController.navigate(Routes.billDetails(savedId))
                            }
                        } else {
                            navController.popBackStack()
                            navController.navigate(Routes.billDetails(savedId))
                        }
                    }
                )
            }

            // 4. Bill Details
            composable(
                route = Routes.BILL_DETAILS,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
                BillDetailsScreen(
                    invoiceId = invoiceId,
                    container = container,
                    onBackClick = { navController.popBackStack() },
                    onRecordPaymentClick = { invId, custId ->
                        navController.navigate(Routes.recordPayment("CUSTOMER", custId, invId))
                    },
                    onPreviewPdfClick = { invId ->
                        navController.navigate(Routes.billPreview(invId))
                    }
                )
            }

            // 5. Bill Preview
            composable(
                route = Routes.BILL_PREVIEW,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
                BillPreviewScreen(
                    invoiceId = invoiceId,
                    container = container,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 6. Customers
            composable(Routes.CUSTOMERS) {
                val customerVm = remember { CustomerViewModel(container) }
                CustomersScreen(
                    viewModel = customerVm,
                    onCustomerClick = { custId -> navController.navigate(Routes.customerDetails(custId)) },
                    onAddCustomerClick = { navController.navigate(Routes.addEditCustomer()) }
                )
            }

            // 7. Customer Details
            composable(
                route = Routes.CUSTOMER_DETAILS,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                CustomerDetailsScreen(
                    customerId = customerId,
                    container = container,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate(Routes.addEditCustomer(id)) },
                    onCreateBillClick = { id -> navController.navigate(Routes.CREATE_BILL) },
                    onRecordPaymentClick = { id -> navController.navigate(Routes.recordPayment("CUSTOMER", id)) },
                    onViewBillClick = { invId -> navController.navigate(Routes.billDetails(invId)) },
                    onViewKhataClick = { id -> navController.navigate(Routes.khataDetails(id)) }
                )
            }

            // 8. Add/Edit Customer
            composable(
                route = Routes.ADD_EDIT_CUSTOMER,
                arguments = listOf(navArgument("customerId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val customerIdStr = backStackEntry.arguments?.getString("customerId")
                val customerId = customerIdStr?.toLongOrNull()
                AddEditCustomerScreen(
                    customerId = customerId,
                    container = container,
                    onBackClick = { navController.popBackStack() },
                    onCustomerSaved = { navController.popBackStack() }
                )
            }

            // 9. Khata
            composable(Routes.KHATA) {
                KhataScreen(
                    container = container,
                    onCustomerKhataClick = { custId -> navController.navigate(Routes.khataDetails(custId)) },
                    onRecordPaymentClick = { navController.navigate(Routes.recordPayment()) }
                )
            }

            // 10. Khata Details
            composable(
                route = Routes.KHATA_DETAILS,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                KhataDetailsScreen(
                    customerId = customerId,
                    container = container,
                    onBackClick = { navController.popBackStack() },
                    onCreateBillClick = { id -> navController.navigate(Routes.CREATE_BILL) },
                    onRecordPaymentClick = { id -> navController.navigate(Routes.recordPayment("CUSTOMER", id)) }
                )
            }

            // 11. Record Payment
            composable(
                route = Routes.RECORD_PAYMENT,
                arguments = listOf(
                    navArgument("partyType") {
                        type = NavType.StringType
                        defaultValue = "CUSTOMER"
                    },
                    navArgument("partyId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("invoiceId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val partyType = backStackEntry.arguments?.getString("partyType") ?: "CUSTOMER"
                val partyId = backStackEntry.arguments?.getLong("partyId")
                val invoiceId = backStackEntry.arguments?.getLong("invoiceId")
                RecordPaymentScreen(
                    initialPartyType = partyType,
                    initialPartyId = partyId,
                    initialInvoiceId = invoiceId,
                    container = container,
                    onBackClick = { navController.popBackStack() },
                    onPaymentRecorded = { navController.popBackStack() }
                )
            }

            // 12. Inventory
            composable(Routes.INVENTORY) {
                val productVm = remember { ProductViewModel(container) }
                InventoryScreen(
                    viewModel = productVm,
                    onAddProductClick = { navController.navigate(Routes.addEditProduct()) },
                    onProductClick = { prodId -> navController.navigate(Routes.addEditProduct(prodId)) }
                )
            }

            // 13. Add/Edit Product
            composable(
                route = Routes.ADD_EDIT_PRODUCT,
                arguments = listOf(navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val productIdStr = backStackEntry.arguments?.getString("productId")
                val productId = productIdStr?.toLongOrNull()
                AddEditProductScreen(
                    productId = productId,
                    container = container,
                    onBackClick = { navController.popBackStack() },
                    onProductSaved = { navController.popBackStack() }
                )
            }

            // 14. Reports
            composable(Routes.REPORTS) {
                ReportsScreen(container = container)
            }

            // 15. Purchases
            composable(Routes.PURCHASES) {
                PurchasesScreen(
                    container = container,
                    onCreatePurchaseClick = { navController.navigate(Routes.CREATE_PURCHASE) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 16. Create Purchase
            composable(Routes.CREATE_PURCHASE) {
                CreatePurchaseScreen(
                    container = container,
                    onBackClick = { navController.popBackStack() },
                    onPurchaseSaved = { navController.popBackStack() }
                )
            }

            // 17. Settings
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    container = container,
                    onBackClick = { navController.popBackStack() },
                    onBusinessProfileClick = { navController.navigate(Routes.BUSINESS_PROFILE) },
                    onInvoiceSettingsClick = { navController.navigate(Routes.INVOICE_SETTINGS) },
                    onAdSettingsClick = { navController.navigate(Routes.AD_SETTINGS) }
                )
            }

            // 18. Ad Monetization Settings
            composable(Routes.AD_SETTINGS) {
                AdSettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 19. Business Profile
            composable(Routes.BUSINESS_PROFILE) {
                BusinessProfileScreen(
                    container = container,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 20. Invoice Settings
            composable(Routes.INVOICE_SETTINGS) {
                InvoiceSettingsScreen(
                    container = container,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
