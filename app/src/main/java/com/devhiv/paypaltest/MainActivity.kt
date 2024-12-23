package com.devhiv.paypaltest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast

import com. paypal. android. corepayments. Address
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.devhiv.paypaltest.databinding.ActivityMainBinding
import com.paypal.android.cardpayments.ApproveOrderListener
import com.paypal.android.cardpayments.Card
import com.paypal.android.cardpayments.CardClient
import com. paypal. android. cardpayments. CardRequest
import com.paypal.android.cardpayments.CardResult
import com.paypal.android.cardpayments.threedsecure.SCA
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val clientID =
        "AZW7F7faibwCYNPJmEIGp2edjsVSqq0MjPc_fB5EXmeDm24BXWFDpIGlGAMVxs5dxPWY1fmXJPSJz0_C"
    private val secretID =
        "EGtnEFG5AGEsC0c0CmHWO3b7hz4vdqrisulBhi8Sh5BAaHxOuXppUP-P2_82-rCC2U9pFccx3b4Wekm2"
    private val returnUrl = "com.ramiro.paypaltest://demoapp"
    var accessToken = ""
    private lateinit var uniqueId: String
    private var orderid = ""
    private lateinit var cardClient: CardClient
    val config = CoreConfig(clientID, environment = Environment.SANDBOX)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        AndroidNetworking.initialize(applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startOrderBtn.visibility = View.GONE

        fetchAccessToken()


        binding.startOrderBtn.setOnClickListener {
            startOrder()
        }

        binding.cardOrderBtn.setOnClickListener{
            startCardOrder()
        }
    }


    private fun startCardOrder() {
        uniqueId = UUID.randomUUID().toString()

        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
                        put("value", "5.00")
                    })
                })
            })
        }
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("PayPal-Request-Id", uniqueId)
            .addJSONObjectBody(orderRequestJson)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Order Response : " + response.toString())
                    handlerCardOrderID(response.getString("id"))
                }

                override fun onError(error: ANError) {
                    Log.d(
                        TAG,
                        "Order Error : ${error.message} || ${error.errorBody} || ${error.response}"
                    )
                }
            })
    }

    private fun handlerCardOrderID(orderID: String) {
        var cardRequest:CardRequest  = com.paypal.android.cardpayments.CardRequest(
            orderID,
            card,
             returnUrl, // custom URL scheme needs to be configured in AndroidManifest.xml
            SCA.SCA_ALWAYS // default value is SCA.SCA_WHEN_REQUIRED
        )
        cardClient= CardClient(this@MainActivity,config)
        cardClient.approveOrder(this@MainActivity, cardRequest)
        cardClient.approveOrderListener = object: ApproveOrderListener {
            override fun onApproveOrderCanceled() {
                Log.d(TAG, "onApproveOrderCanceled")
            }

            override fun onApproveOrderFailure(error: PayPalSDKError) {
                Log.d(TAG, "onApproveOrderFailure: ${error}")
            }

            override fun onApproveOrderSuccess(result: CardResult) {
                Log.d(TAG, "onApproveOrderSuccess: ${result}")
                orderid = orderID
                captureOrder(orderID)
            }

            override fun onApproveOrderThreeDSecureDidFinish() {
                Log.d(TAG, "onApproveOrderThreeDSecureDidFinish")
            }

            override fun onApproveOrderThreeDSecureWillLaunch() {
                Log.d(TAG, "onApproveOrderThreeDSecureWillLaunch")
                captureOrder(orderID)
            }

        }


//        orderid = orderID
//        val payPalWebCheckoutRequest =
//            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
//        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)

    }

    private fun handlerOrderID(orderID: String) {
        val payPalWebCheckoutClient = PayPalWebCheckoutClient(this@MainActivity, config, returnUrl)
        payPalWebCheckoutClient.listener = object : PayPalWebCheckoutListener {
            override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
                Log.d(TAG, "onPayPalWebSuccess: $result")
            }

            override fun onPayPalWebFailure(error: PayPalSDKError) {
                Log.d(TAG, "onPayPalWebFailure: $error")
            }

            override fun onPayPalWebCanceled() {
                Log.d(TAG, "onPayPalWebCanceled: ")
            }
        }

        orderid = orderID
        val payPalWebCheckoutRequest =
            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)

    }

    private fun startOrder() {
        uniqueId = UUID.randomUUID().toString()

        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
                        put("value", "5.00")
                    })
                })
            })
            put("payment_source", JSONObject().apply {
                put("paypal", JSONObject().apply {
                    put("experience_context", JSONObject().apply {
                        put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED")
                        put("brand_name", "SH Developer")
                        put("locale", "en-US")
                        put("landing_page", "LOGIN")
                        put("shipping_preference", "NO_SHIPPING")
                        put("user_action", "PAY_NOW")
                        put("return_url", returnUrl)
                        put("cancel_url", "https://example.com/cancelUrl")
                    })
                })
            })
        }

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("PayPal-Request-Id", uniqueId)
            .addJSONObjectBody(orderRequestJson)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Order Response : " + response.toString())
                    handlerOrderID(response.getString("id"))
                }

                override fun onError(error: ANError) {
                    Log.d(
                        TAG,
                        "Order Error : ${error.message} || ${error.errorBody} || ${error.response}"
                    )
                }
            })
    }

    private fun fetchAccessToken() {
        val authString = "$clientID:$secretID"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v1/oauth2/token")
            .addHeaders("Authorization", "Basic $encodedAuthString")
            .addHeaders("Content-Type", "application/x-www-form-urlencoded")
            .addBodyParameter("grant_type", "client_credentials")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    accessToken = response.getString("access_token")
                    Log.d(TAG, accessToken)

                    Toast.makeText(this@MainActivity, "Access Token Fetched!", Toast.LENGTH_SHORT)
                        .show()

                    binding.startOrderBtn.visibility = View.VISIBLE
                }

                override fun onError(error: ANError) {
                    Log.d(TAG, error.errorBody)
                    println(error.errorBody.toString())
                    Toast.makeText(this@MainActivity, "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: $intent")
        println(intent?.data)
        if (intent?.data!!.getQueryParameter("opType") == "payment") {
            captureOrder(orderid)
        } else if (intent?.data!!.getQueryParameter("opType") == "cancel") {
            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureOrder(orderID: String) {
        println("HITTING CAPTURE ORDER")
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderID/capture")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(JSONObject()) // Empty body
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Capture Response : " + response.toString())
                    Toast.makeText(this@MainActivity, "Payment Successful", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: ANError) {
                    // Handle the error
                    Log.e(TAG, "Capture Error : " + error.errorDetail)
                }
            })





    }

    val card = Card(
        number = "4032035231121567",
        expirationMonth = "01",
        expirationYear = "2027",
        securityCode = "123",
        billingAddress = Address(
            streetAddress = "123 Main St.",
            extendedAddress = "Apt. 1A",
            locality = "Anytown",
            region = "CA",
            postalCode = "12345",
            countryCode = "US"
        )
    )




    fun cardCheckoutTapped(cardRequest: CardRequest) {
        cardClient.approveOrder(this, cardRequest)
    }


    companion object {
        const val TAG = "MyTag"
    }
}
