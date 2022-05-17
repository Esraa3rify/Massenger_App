package com.example.massenger7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.massenger7.messages.LatestMessageActivity
import com.example.massenger7.messages.LatestMessageActivity.Companion.currentUser
import com.example.massenger7.models.ChatMessage
import com.example.massenger7.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.example.massenger7.views.ChatFromItem
import com.example.massenger7.views.ChatToItem
import com.xwray.groupie.GroupAdapter
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*

class chatLogActivity : AppCompatActivity() {

    companion object {
        val TAG="ChatLog"
    }

    val adapter=GroupAdapter<ViewHolder>()
    var toUser: User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        //  supportActionBar?.title="Chat Log"
        RVchatLog.adapter=adapter

        //common way to pass objects between different activities 2

        //receive the username value from the previous activity<-newmassege
        //val username=intent.getStringExtra(NewMessageActivity.USER_KEY)
        val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        if (toUser != null) {
            supportActionBar?.title = toUser.username
        }

        //setupDummyData()
        listenForMessage()
        sendBtn.setOnClickListener {
          Log.d("Attempt to send message!")
            performSendMessage()
        }

    }

    private fun listenForMessage() {
        //
        val fromId=FirebaseAuth.getInstance().uid
        val toId=toUser?.uid
        val ref=FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val chatMessage=snapshot.getValue(ChatMessage::class.java)

                if (chatMessage!=null) {

                    Log.d(TAG, chatMessage.text)

                   if (chatMessage.fromId==FirebaseAuth.getInstance().uid) {
                       val CurrentUser=LatestMessageActivity.currentUser?:return
                       adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
                   }else {
                       val toUser= intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

                       adapter.add(ChatToItem(chatMessage.text, toUser!!))
                   }
                }
                RVchatLog.scrollToPosition(adapter.itemCount -1)


            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

        })
    }




    private fun performSendMessage() {
        //how we actually send message to firebase
        //how to create a node called message in FB

        val text=enterMessageET.text.toString()
        //implement the feature which refer to the user to user messages
        val fromId=FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        val toId= user?.uid
        if (fromId==null)return
       // val reference=FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference=FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference=FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val chatMessage=ChatMessage(reference.key!!,text, fromId,toId,System.currentTimeMillis()/1000)

            reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "saved our chat message:${reference.key}")

                    enterMessageET.text.clear()
                    //allow the latest message to be at the bottom of recyclerview
                    RVchatLog.scrollToPosition(adapter.itemCount -1)
                }
        toReference.setValue(chatMessage)

        //create a node and pass the values of the model class into it
         val LatestMsgRef=FirebaseDatabase.getInstance().getReference("/latest_messages/$fromId/$toId")
        //chatMessage is the model class
        LatestMsgRef.setValue(chatMessage)

        //create a node in the nmain node and pass the values of the model class into it
        val LatestMsgToRef=FirebaseDatabase.getInstance().getReference("/latest_messages/$toId/$fromId")
        //chatMessage is the model class
        LatestMsgRef.setValue(chatMessage)
    }

//    private fun setupDummyData() {
//
//            val adapter = GroupAdapter<ViewHolder>()
//            adapter.add(ChatFromItem("from message"))
//            adapter.add(ChatToItem("to message"))
//            adapter.add(ChatFromItem("from message "))
//            adapter.add(ChatToItem(" to message"))
//
//            RVchatLog.adapter = adapter
//        }

    }


