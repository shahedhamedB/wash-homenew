package com.washathomes.apputils.modules

data class AccountTypeIntros(val results: ArrayList<AccountTypeIntro>)
data class AccountTypeIntro(val id: String?, val title:String?, val description: String?, val icons: String?, val image: String?)
