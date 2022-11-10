package com.washathomes.views.main.washee.chats

import android.annotation.SuppressLint
import android.view.View

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.washathomes.R
import com.washathomes.apputils.appdefs.AppDefs
import com.washathomes.apputils.modules.chatmodel.ChatRoom
import com.washathomes.base.ui.BaseAdapter


import com.washathomes.base.ui.BaseViewHolder
import com.washathomes.base.util.DateUtil

import com.washathomes.databinding.ItemInboxBinding


import org.apache.xmlbeans.UserType


class WasheeInboxListAdapter : BaseAdapter<ItemInboxBinding, ChatRoom>(R.layout.item_inbox) {

    override fun bindView(binding: ItemInboxBinding, item: ChatRoom?) {
        binding.chatRoom = item
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseViewHolder<ItemInboxBinding, ChatRoom>, position: Int) {
        super.onBindViewHolder(holder, position)

//        holder.binding.inboxSender.text = "Order #" + holder.binding.chatRoom?.orderId
        if (!holder.binding.chatRoom?.messages.isNullOrEmpty()) {

//            holder.binding.inboxTime.text = holder.binding.chatRoom?.messages?.last()?.createTime?.substring(11, 16)
            holder.itemView.visibility = View.VISIBLE

            holder.binding.chatRoom?.let {
                holder.binding.inboxText.text = it.messages.last().message
                holder.binding.inboxTime.text = DateUtil.convertLongToTimeString(it.messages.last().createTime)
                if (it.order?.driver_user_id == 0) {
                    //TODO CHECK YESR
                   if (AppDefs.user.results!!.sigup_type == "2") {
                        setImage(holder.binding.inboxProfile, it.order?.buyer_profile_picture)
                    } else {
                        setImage(holder.binding.inboxProfile, it.order?.seller_profile_picture)
                    }
                } else
                    holder.binding.inboxProfile.setImageResource(R.drawable.ic_chatroom)
            }
        }
        else{
            holder.itemView.visibility = View.GONE
        }
    }

    private fun setImage(view: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty())
            view.setImageResource(R.drawable.ic_chat_anonymous)
        else
            Glide.with(view.context)
                .load(imageUrl)
                .priority(Priority.IMMEDIATE)
                .error(R.drawable.ic_chat_anonymous)
                .apply(RequestOptions.circleCropTransform())
                .into(view)
    }
}