package com.washathomes.apputils.modules

data class ReviewObj(val user_type: String, val review_start: String, val feedback: String)

data class Review(val order_id: String, val review: ArrayList<ReviewObj>)

data class WasheeReviewInfo(val id: String, val washer_info: UserInfo = UserInfo(), val courier_info: UserInfo?)

data class WasheeReviewData(val status: Status, val results: WasheeReviewInfo)

data class UserInfo(val id: String = "", val name: String = "", val image: String = "")

data class WasherReviewInfo(val id: String, val washee_info: UserInfo = UserInfo(), val courier_info: UserInfo?)

data class WasherReview(val status: Status, val results: WasherReviewInfo)

data class CourierReviewInfo(val id: String, val washee_info: UserInfo = UserInfo(), val washer_info: UserInfo = UserInfo())

data class CourierReview(val status: Status, val results: CourierReviewInfo)
