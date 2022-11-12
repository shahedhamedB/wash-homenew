package com.washathomes.views.introduction

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.apputils.modules.UserData
import com.washathomes.apputils.modules.UserLogin
import com.washathomes.databinding.FragmentVerificationBinding
//import com.twilio.Twilio
//import com.twilio.rest.verify.v2.service.Verification
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import com.onesignal.OneSignal
import com.washathomes.R
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
@AndroidEntryPoint
class VerificationFragment : Fragment() {

    lateinit var binding: FragmentVerificationBinding
    lateinit var introductionActivity: IntroductionActivity
    lateinit var navController: NavController
    val args: VerificationFragmentArgs by navArgs()
    var phoneNum = ""
    val ONESIGNAL_APP_ID = "e8dd2851-f756-4c29-9972-48916b7aca3e"
    val ACCOUNT_SID = "ACdd2756a8d9e25aaa7dbf398b38fec8c9"
    val AUTH_TOKEN = "65099ece136055f2d68291c47915ad55"
    val SERVICE_SID = "VA827dbc60c5cca9e69ffbb983d3b1fbc4"
    var code = ""
    var codeBySystem: String = ""
    var token = ""
    lateinit var mAuth: FirebaseAuth
    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_verification, container, false)
        binding = FragmentVerificationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IntroductionActivity) {
            introductionActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirebaseApp.initializeApp(introductionActivity)
        initViews(view)
        onClick()
        sendVerification()
        getToken()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
        phoneNum = args.phoneNum
        mAuth = FirebaseAuth.getInstance()
        
        Glide.with(introductionActivity).load(AppDefs.whiteLogo).into(binding.logo)
        Glide.with(introductionActivity).load(AppDefs.background).into(binding.background)


        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                codeBySystem = s
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    binding.verificationCodeEdt.setText(code)
                    verifyCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        sendVerificationCodeToUser(phoneNum)

//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
//
//        // OneSignal Initialization
//        OneSignal.initWithContext(introductionActivity)
//        OneSignal.setAppId(ONESIGNAL_APP_ID)
//        OneSignal.setSMSNumber(phoneNum)
    }

    private fun sendOneSignalVerification(){
        val smsNumber = phoneNum
        val smsAuthHash = "Zjg5OTg0NGMtY2U2Zi00MWE2LTk0YWEtYzVkYmRlZDU0Yjgx" // SMS auth hash generated from your server

        OneSignal.setSMSNumber(smsNumber, smsAuthHash, object : OneSignal.OSSMSUpdateHandler {
            override fun onSuccess(result: JSONObject) {
                // SMS successfully synced with OneSignal
                activity!!.runOnUiThread {
                    Toast.makeText(introductionActivity,  "Success", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(error: OneSignal.OSSMSUpdateError) {
                // Error syncing SMS, check error.getType() and error.getMessage() for details
                Toast.makeText(introductionActivity,  "Fail", Toast.LENGTH_SHORT).show()
            }
        })
//        Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
//        val verification: Verification = Verification.creator(
//            SERVICE_SID,
//            phoneNum,
//            "sms"
//        ).create()
//
//        Log.d("otpCode", verification.getSid())
    }

    private fun onClick(){
        binding.verifyBtn.setOnClickListener {
            if (binding.verificationCodeEdt.text.toString().isNotEmpty()){
                code = binding.verificationCodeEdt.text.toString()
                verifyCode(code)
            }
        }
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    Log.w(
                        "FAILED",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@addOnCompleteListener
                }
                token = task.result!!
            }
    }

    private fun sendVerification(){
//        Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
//        val verification: Verification = Verification.creator(
//            SERVICE_SID,
//            phoneNum,
//            "sms"
//        ).create()
//
//        Log.d("otpCode", verification.getSid())
    }

    private fun login(){
        val userParams = UserLogin(phoneNum, AppDefs.lang, token, "1")
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val loginCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).login(userParams)
        loginCall.enqueue(object : Callback<UserData>{
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    AppDefs.user = response.body()!!
                    navController.navigate(VerificationFragmentDirections.actionVerificationFragmentToAccountTypesFragment())
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun verifyCode(code: String) {
        binding.progressBar.visibility = View.VISIBLE
        val credential = PhoneAuthProvider.getCredential(codeBySystem, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(
            introductionActivity
        ) { task: Task<AuthResult?> ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Verification completed", Toast.LENGTH_LONG).show()
                AppDefs.firebaseUser = task.result!!.user!!
                Log.d("uId",AppDefs.firebaseUser.uid)
                Log.d("uPhone", AppDefs.firebaseUser.phoneNumber!!)
                login()
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                        context,
                        "Verification not completed! Try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun sendVerificationCodeToUser(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(2L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(introductionActivity) // Activity (for callback binding)
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


}