//package com.devhiv.paypaltest
//
//import androidx.fragment.app.FragmentActivity
//import com.paypal.android.cardpayments.ApproveOrderListener
//import com.paypal.android.cardpayments.CardClient
//import com.paypal.android.cardpayments.CardRequest
//import com.paypal.android.cardpayments.CardResult
//import com.paypal.android.corepayments.CoreConfig
//import com.paypal.android.corepayments.Environment
//import com.paypal.android.corepayments.PayPalSDKError
//
//class MyCardPaymentActivity: FragmentActivity(), ApproveOrderListener {
//    private val clientID =
//        "AZW7F7faibwCYNPJmEIGp2edjsVSqq0MjPc_fB5EXmeDm24BXWFDpIGlGAMVxs5dxPWY1fmXJPSJz0_C"
//    val config = CoreConfig(clientID, environment = Environment.SANDBOX)
//     var cardClient: CardClient = CardClient(this,config)
//
//    fun cardCheckoutTapped(cardRequest: CardRequest) {
//        val result = cardClient.approveOrder(this, cardRequest)
//    }
//    fun setupCardClient() {
//        cardClient.listener = this
//    }
//    override fun onApproveOrderSuccess(result: CardResult) {
//        // order was approved and is ready to be captured/authorized (see step 6)
//    }
//    override fun onApproveOrderFailure(error: PayPalSDKError) {
//        // inspect 'error' for more information
//    }
//    override fun onApproveOrderCanceled() {
//        // 3D Secure flow was canceled
//    }
//    override fun onApproveOrderThreeDSecureWillLaunch() {
//        // 3D Secure flow will launch
//    }
//    override fun onApproveOrderThreeDSecureDidFinish() {
//        // 3D Secure auth did finish successfully
//    }
//}