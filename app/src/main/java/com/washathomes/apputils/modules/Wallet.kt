package com.washathomes.apputils.modules

data class WalletObj(val status: Status, val results: Wallet)

data class Wallet(val total_all: String, val total_paid: String, val paymnt_next_total: String, val next_payment: String, val trans_list: ArrayList<TransList>)

data class TransList(val id: String, val user_id: String, val order_id: String, val status: String, val user_type: String, val date_created: String, val date: String, val amount: String)
