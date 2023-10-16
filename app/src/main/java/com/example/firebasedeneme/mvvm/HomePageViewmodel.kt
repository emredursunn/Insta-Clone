package com.example.firebasedeneme.mvvm

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.firebasedeneme.model.Post
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomePageViewmodel(application: Application) : AndroidViewModel(application) {
    val firestore = Firebase.firestore

    val postList = MutableLiveData<ArrayList<Post>>()
    val progressBar = MutableLiveData<Boolean>()
    val nullText = MutableLiveData<Boolean>()


    fun getData() {
        nullText.value = false
        progressBar.value = true
        CoroutineScope(Dispatchers.IO).launch{
            val newList = ArrayList<Post>()
            val postListSnapshot = firestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING)
                .get().await()

            withContext(Dispatchers.Main){
                if(!postListSnapshot.isEmpty && postListSnapshot != null) {
                    for (document in postListSnapshot.documents) {
                        val postId = document.id
                        val userId = document.get("userId") as? String
                        val imageUrl = document.get("imageUrl") as? String
                        val outline = document.get("outline") as? String
                        val post = Post(userId!!, imageUrl!!, outline!!)
                        post.postId = postId
                        newList.add(post)
                    }
                    postList.value = newList
                    progressBar.value = false

                }else {
                        nullText.value = true
                    }
                }
        }
    }
}



