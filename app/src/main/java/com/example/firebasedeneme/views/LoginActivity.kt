package com.example.firebasedeneme.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.firebasedeneme.databinding.ActivityLoginBinding
import com.example.firebasedeneme.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var firestore:FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        firestore = Firebase.firestore

        if(auth.currentUser != null){
            val intent = Intent(this@LoginActivity, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun girisYap(view:View){
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this@LoginActivity,"Enter email and password correctly",Toast.LENGTH_LONG).show()
        }else{
            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                val intent = Intent(this@LoginActivity, HomePageActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this@LoginActivity,it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun kayitOl(view: View){
        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this@LoginActivity,"Enter email and password correctly",Toast.LENGTH_LONG).show()
        }else{
            auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {result->
                result.user?.let{user->
                    val newUser = User(email)
                    newUser.userId = user.uid
                    CoroutineScope(Dispatchers.IO).launch {
                        firestore.collection("Users").document(newUser.userId).set(newUser).await()
                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@LoginActivity, HomePageActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            } .addOnFailureListener {
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
            }}

    }


}