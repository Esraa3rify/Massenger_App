package com.example.massenger7

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        register_button.setOnClickListener {
            performRegister()
        }

        already_have_account_text_view.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")

            // launch the login activity somehow
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        SelectPicBtn.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && requestCode == Activity.RESULT_OK && data != null) {
            //proceed and check what the selected image was.
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)

            SelectPicBtn.alpha = 0f


            // val bitmapDrawable = BitmapDrawable(bitmap)
           // SelectPicBtn.setBackgroundDrawable(bitmapDrawable)


        }
    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MainActivity", "Email is: " + email)
        Log.d("MainActivity", "Password: $password")

        // Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(this, "Successfully created user with uid", Toast.LENGTH_SHORT)
                    .show()

                uploadPhotoToFirebaseStorage()
                // else if successful
                Log.d("Main", "Successfully created user with uid")
            }
            .addOnFailureListener {
                Log.d("Main", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun uploadPhotoToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$fileName")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register", "Successfully uploaded images: ${it.metadata?.path}")



                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Register", "File location:$it")

                    saveUseToFirebaseDatabase(it.toString())
                }
            }.addOnFailureListener{
             //do some logging here
                Log.d("RegisterActivity", "Failed to upload image to storage: ${it.message}")

            }
    }



    private fun saveUseToFirebaseDatabase(profileImageUri: String) {

        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(),profileImageUri)
          ref.setValue(user)
              .addOnSuccessListener {

                  Log.d("RegisterActivity","Finally we save the user to the firebase database")


              } .addOnFailureListener {
                  Log.d("RegisterActivity", "Failed to set value to database: ${it.message}")
              }

    }

}
class User(val uid: String,val username:String,val profileImageUri:String)
