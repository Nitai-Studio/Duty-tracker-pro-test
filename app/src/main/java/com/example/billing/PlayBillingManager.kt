package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayBillingManager(
    private val context: Context,
    private val billingScope: CoroutineScope,
    private val purchaseListener: (Purchase) -> Unit
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    
    private val _billingServiceReady = MutableStateFlow(false)
    val billingServiceReady: StateFlow<Boolean> = _billingServiceReady

    val premiumProducts = listOf("premium_monthly", "premium_yearly")
    
    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        startConnection()
    }

    private fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("PlayBilling", "Billing Client setup successful")
                    _billingServiceReady.value = true
                    queryPurchases()
                } else {
                    Log.e("PlayBilling", "Billing Setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("PlayBilling", "Billing service disconnected, reconnecting...")
                // retry connection (with exponential backoff in prod, simple retry for now)
                _billingServiceReady.value = false
                startConnection()
            }
        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("PlayBilling", "User cancelled purchase flow")
        } else {
            Log.e("PlayBilling", "Payment updated error: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase if it hasn't been acknowledged yet
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("PlayBilling", "Purchase successfully acknowledged")
                        // Activate premium on user profile
                        purchaseListener(purchase)
                    } else {
                        Log.e("PlayBilling", "Error acknowledging purchase: ${billingResult.debugMessage}")
                    }
                }
            } else {
                purchaseListener(purchase)
            }
        }
    }

    fun initiatePurchaseFlow(activity: Activity, productId: String) {
        if (!_billingServiceReady.value) {
            Log.e("PlayBilling", "Service not ready")
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                
                // Fetch valid offer tokens for subscription
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                billingClient?.launchBillingFlow(activity, billingFlowParams)
            } else {
                Log.e("PlayBilling", "Query product details failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun queryPurchases() {
        if (!_billingServiceReady.value) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    handlePurchase(purchase)
                }
            } else {
                Log.e("PlayBilling", "Error querying purchases: ${billingResult.debugMessage}")
            }
        }
    }
}
