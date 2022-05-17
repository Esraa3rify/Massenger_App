package com.views

import com.example.massenger7.R
import com.example.massenger7.models.ChatMessage
import com.example.massenger7.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item

class LatestMesssageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){

    var chatPartnerUser: User? = null

    //make the view of Recycler responsive: allow showing data on the view components
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_textview_latest_message.text=chatMessage.text

        //to determine the right user/ owner to the chat
        val chatPartnerId:String
        if(chatMessage.fromId== FirebaseAuth.getInstance().uid){

            chatPartnerId=chatMessage.toId
        }else{

            chatPartnerId=chatMessage.fromId
        }

        val ref= FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser=snapshot.getValue(User::class.java)
                viewHolder.itemView.username_textview_latest_message.text=user?.username

                //fetch the view
                val targetImageView=viewHolder.itemView.imageview_latest_message
                //load the pic into the fetched view
                Picasso.get().load(user?.profileImageUrl).into(targetImageView)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })




    }

    //return the view of row to the recyclerview
    override fun getLayout(): Int {
        return R.layout.latest_msg_row
    }
}