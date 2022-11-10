package com.washathomes.apputils.helpers

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.modules.ChatUser
import com.washathomes.apputils.modules.User
import java.util.HashMap

class FirebaseHelper {

    companion object {

        fun sendTokenToDatabase(user: User, token: String, languageId: String) {
            AppDefs.chatUser = ChatUser(user.name!!, user.name, languageId, token)
            val database: DatabaseReference = FirebaseDatabase.getInstance().reference
            val userData = mapOf(
                "firstName" to user.name,
                "lastName" to user.name,
                "languageId" to languageId,
                "token" to token
            )

            val childUpdates = HashMap<String, Any?>()
            childUpdates["${AppDefs.USER_PATH}/${user.id}"] = userData
            database.updateChildren(childUpdates)
        }

        fun sendUserToDatabase(languageId: String) {
            sendTokenToDatabase(AppDefs.user.results!!, AppDefs.user.results!!.fcm_token!!, languageId)
        }

    }
}