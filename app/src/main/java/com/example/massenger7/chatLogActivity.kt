package com.example.massenger7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.massenger7.models.ChatMessage
import com.example.massenger7.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*

class chatLogActivity : AppCompatActivity() {

    companion object {
        val TAG="ChatLog"
    }

    val adapter=GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        //  supportActionBar?.title="Chat Log"
        RVchatLog.adapter=adapter

        //common way to pass objects between different activities 2

        //receive the username value from the previous activity<-newmassege
        //val username=intent.getStringExtra(NewMessageActivity.USER_KEY)
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        if (user != null) {
            supportActionBar?.title = user.username
        }

        //setupDummyData()
        listenForMessage()
        sendBtn.setOnClickListener {
          Log.d("Attempt to send message!")
            performSendMessage()
        }

    }

    private fun listenForMessage() {
        val ref=FirebaseDatabase.getInstance().getReference("messages/")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val chatMessage=snapshot.getValue(ChatMessage::class.java)

                if (chatMessage!=null) {

                    Log.d(TAG, chatMessage.text)

                   if (chatMessage.fromId==FirebaseAuth.getInstance().uid) {
                       adapter.add(ChatFromItem(chatMessage.text))
                   }else {

                       adapter.add(ChatToItem(chatMessage.text))
                   }
                }


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

        val fromId=FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        val toId= user.uid
        if (fromId==null)return
        val reference=FirebaseDatabase.getInstance().getReference("/messages").push()

        val chatMessage=ChatMessage(reference.key!!,text, fromId,toId,System.currentTimeMillis()/1000)

            reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG,"saved our chat message:${reference.key}")
                }
    }

    private fun setupDummyData() {

            val adapter = GroupAdapter<ViewHolder>()
            adapter.add(ChatFromItem("from message"))
            adapter.add(ChatToItem("to message"))
            adapter.add(ChatFromItem("from message "))
            adapter.add(ChatToItem(" to message"))

            RVchatLog.adapter = adapter
        }

    }
    class ChatFromItem(val text: String) : Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            ViewHolder.itemView.textView_from_row.text=text

        }

        override fun getLayout(): Int {
            return R.layout.chat_from_row
        }
    }


    class ChatToItem(val text: String) : Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            ViewHolder.itemView.textView_to_row.text=text

        }

        override fun getLayout(): Int {
            return R.layout.chat_to_row
        }
    }

}