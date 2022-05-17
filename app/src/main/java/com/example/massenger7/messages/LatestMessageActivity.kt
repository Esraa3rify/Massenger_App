package com.example.massenger7.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.massenger7.NewMessageActivity
import com.example.massenger7.R
import com.example.massenger7.chatLogActivity
import com.example.massenger7.logReg.RegisterActivity
import com.example.massenger7.models.ChatMessage
import com.example.massenger7.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.massenger7.views.LatestMesssageRow
import com.xwray.groupie.GroupAdapter
import kotlinx.android.synthetic.main.activity_latest_message.*

class LatestMessageActivity : AppCompatActivity() {
    companion object{
       var currentUser: User? =null
        val TAG = "LatestMessages"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)

        RvLatestMsg.adapter=adapter

        //create vertical line between the chats
        RvLatestMsg.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))


        // set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG, "123")
            val intent = Intent(this, chatLogActivity::class.java)

            // we are missing the chat partner user

            val row = item as LatestMesssageRow

            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

      //  setupDummyRows()
        listenToTheLatestMsg()

        fetchCurrentUser()

         verifyUserIsLoggedIn()

    }





    val latestMsgMap=HashMap<String,ChatMessage>()

    private fun refreshRecycleViewMsgs() {
        adapter.clear()
        latestMsgMap.values.forEach {
            adapter.add(LatestMesssageRow(it))
        }
    }

    private fun listenToTheLatestMsg() {
       val fromId=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/latest_messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{

            //to show the new messages
            //after storing it in the FB, get the value, pass it to the vars in the model class
            //fetch the the values and vars of model class an put it in the class latest msg row
            //add it to the adapter

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val chatMessage= snapshot.getValue(ChatMessage::class.java)?:return

                latestMsgMap[snapshot.key!!]=chatMessage

                refreshRecycleViewMsgs()
                adapter.add(LatestMesssageRow(chatMessage))
            }

              //allow to show the changes of the msg and update it
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage= snapshot.getValue(ChatMessage::class.java)?:return
                adapter.add(LatestMesssageRow(chatMessage))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    val adapter=GroupAdapter<ViewHolder>()


//    private fun setupDummyRows(){
//
//        //call the class to show the rows
//        adapter.add(LatestMesssageRow())
//
//        RvLatestMsg.adapter=adapter
//    }
      //to load the user (from) image
    private fun fetchCurrentUser() {

          val uid=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
          ref.addListenerForSingleValueEvent(object :ValueEventListener{

              override fun onDataChange(snapshot: DataSnapshot) {
                currentUser=snapshot.getValue(User::class.java)
                  Log.d("LatestMessage","currentUser ${currentUser?.profileImageUrl}")
              }

              override fun onCancelled(error: DatabaseError) {
                  TODO("Not yet implemented")
              }
          })


    }

    private fun verifyUserIsLoggedIn(){
        val uid=FirebaseAuth.getInstance().uid
        if(uid==null){
            val intent= Intent(this, RegisterActivity::class.java)
            intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
       R.id.menu_new_message -> {
           val intent= Intent(this, NewMessageActivity::class.java)
           startActivity(intent)

       }
       R.id.menu_sign_out ->{
           FirebaseAuth.getInstance().signOut()
           val intent= Intent(this, RegisterActivity::class.java)
           intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
           startActivity(intent)

       }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}