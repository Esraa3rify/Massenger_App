package com.example.massenger7

import android.content.ClipData
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.massenger7.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*
import java.text.FieldPosition

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)


        supportActionBar?.title = "Select User"

        //val Adapter=GroupAdapter<ViewHolder>()
//
//    adapter.add(UserItem())
//    adapter.add(UserItem())
//    adapter.add(UserItem())
//
//    recyclerview_newmessage.adapter = adapter

        fetchUsers()
    }

    companion object{
            //allow to pass username to next activity->chatLog
        val USER_KEY="USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }
                  //common way to pass objects between different activities 1
                adapter.setOnItemClickListener{item,view ->

                    val userItem=item as UserItem
                    //send the username value to the chatlog
                    val intent= Intent(view.context,chatLogActivity::class.java )
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()


                }

                recyclerViewNewMessage.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}


class UserItem(val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.username_textview_new_message.text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageview_new_message)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}

// this is super tedious

//class CustomAdapter: RecyclerView.Adapter<ViewHolder> {
//  override fun onBindViewHolder(p0:, p1: Int) {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//}

