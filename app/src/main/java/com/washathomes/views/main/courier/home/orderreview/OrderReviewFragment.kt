package com.washathomes.views.main.courier.home.orderreview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.appdefs.Urls
import com.washathomes.apputils.modules.*
import com.washathomes.apputils.remote.RetrofitAPIs
import com.washathomes.R
import com.washathomes.views.main.courier.CourierMainActivity
import com.washathomes.databinding.FragmentOrderReview3Binding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
@AndroidEntryPoint
class OrderReviewFragment : Fragment() {

    lateinit var binding: FragmentOrderReview3Binding
    lateinit var courierMainActivity: CourierMainActivity
    lateinit var navController: NavController
    lateinit var review: CourierReviewInfo
    var rate1 : Float = 0.0f
    var rate2 : Float = 0.0f
    lateinit var review2: ReviewObj

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_order_review3, container, false)
        binding = FragmentOrderReview3Binding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CourierMainActivity) {
            courierMainActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        onClick()
        getReview()
    }

    private fun initViews(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun onClick(){
        binding.toolbarBackIcon.setOnClickListener { navController.popBackStack() }
        binding.ratingBarOne.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                rate1 = rating
                if (rate1 <= 3.0f) {
                    binding.feedbackOne.visibility = View.VISIBLE
                } else {
                    binding.feedbackOne.visibility = View.GONE
                }
            }

        binding.ratingBarTwo.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                rate2 = rating
                if (rate2 <= 3.0f && review.washer_info.id.isNotEmpty()) {
                    binding.feedbackTwo.visibility = View.VISIBLE
                } else {
                    binding.feedbackTwo.visibility = View.GONE
                }
            }
        binding.submitReview.setOnClickListener { submitReview() }

    }

    private fun setData(){
        binding.orderNo.text = resources.getString(R.string.order) + "#" + AppDefs.activeOrder.id
        binding.orderInfo.text = resources.getString(R.string.order_confirmed_on)+" "+ AppDefs.activeOrder.time+" "+ AppDefs.activeOrder.date
        if (review.washee_info.image.isNotEmpty()){
            Glide.with(courierMainActivity).load(review.washee_info.image).into(binding.imageOne)
        }
        binding.questionOne.text = resources.getString(R.string.how_was)+" "+review.washee_info.name

        if (review.washer_info.id.isNotEmpty()){
            if (review.washer_info.image.isNotEmpty()){
                Glide.with(courierMainActivity).load(review.washer_info.image).into(binding.imageTwo)
            }
            binding.questionTwo.text = resources.getString(R.string.how_was)+" "+review.washer_info.name
            binding.cardSecond.visibility = View.VISIBLE
        }else{
            binding.cardSecond.visibility = View.GONE
        }

        rate1 = binding.ratingBarOne.rating
        rate2 = binding.ratingBarTwo.rating
    }

    private fun getReview(){
        binding.progressBar.visibility = View.VISIBLE
        val orderId = OrderObj(AppDefs.activeOrder.id)
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val categoriesCall: Call<CourierReview> =
            retrofit.create(RetrofitAPIs::class.java).getCourierReview(orderId)
        categoriesCall.enqueue(object : Callback<CourierReview> {
            override fun onResponse(call: Call<CourierReview>, response: Response<CourierReview>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    review = response.body()!!.results
                    setData()
                    onClick()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CourierReview>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun submitReview(){
        binding.progressBar.visibility = View.VISIBLE
        val reviews: ArrayList<ReviewObj> = ArrayList()
        reviews.add(ReviewObj("1", rate1.toString(), binding.feedbackOne.text.toString()))
        review2 = ReviewObj("1", rate2.toString(), binding.feedbackTwo.text.toString())
        reviews.add(review2)
        val review = Review(review.id, reviews)
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val categoriesCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).courierOrderReview(review)
        categoriesCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    Toast.makeText(courierMainActivity, resources.getString(R.string.reviews_success), Toast.LENGTH_SHORT).show()
                    val intent = Intent(courierMainActivity, CourierMainActivity:: class.java)
                    startActivity(intent)
                    courierMainActivity.finish()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<ErrorResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<ErrorResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(courierMainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(courierMainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

}