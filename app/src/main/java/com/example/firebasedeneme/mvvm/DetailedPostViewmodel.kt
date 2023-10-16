package com.example.firebasedeneme.mvvm

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.firebasedeneme.model.Comment
import com.example.firebasedeneme.model.Post
import com.example.firebasedeneme.util.addComment
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DetailedPostViewmodel(application: Application) : AndroidViewModel(application) {
    val auth = Firebase.auth
    val firestore = Firebase.firestore

    val postLiveData = MutableLiveData<Post>()
    val commentsLiveData = MutableLiveData<ArrayList<Comment>>()
    val nullLiveData = MutableLiveData<Boolean>()

    val commentList = ArrayList<Comment>()


    fun getData(postId: String) {
        nullLiveData.value = false
        CoroutineScope(Dispatchers.IO).launch {
            getPost(postId)
            getComments(postId)
        }
    }

    private suspend fun getPost(postId: String) {
        //******İLGİLİ POSTU ALIYORUZ**********
        // Firestore'dan belgeyi al
        try {
            val snapshot = firestore.collection("Posts").document(postId).get().await()

            if (snapshot.exists()) {
                // Belge varsa, verileri al ve Post nesnesine dönüştür
                val userId = snapshot.getString("userId") ?: ""
                val imageUrl = snapshot.getString("imageUrl") ?: ""
                val outline = snapshot.getString("outline") ?: ""

                // Post nesnesini oluştur
                val post = Post(userId, imageUrl, outline)

                // PostLiveData'ya atama yap
                withContext(Dispatchers.Main) {
                    postLiveData.value = post
                }
            }
            else{
                withContext(Dispatchers.Main){
                    nullLiveData.value = true
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    private suspend fun getComments(postId: String) {
        try {
            // Yorumları al ve LiveData'ya ata
            val snapshot = firestore.collection("Posts").document(postId)
                .collection("Comments").orderBy("date", Query.Direction.DESCENDING)
                .get().await()

            withContext(Dispatchers.Main) {
                for (document in snapshot) {
                    val userId = document.getString("userId") ?: ""
                    val commentText = document.getString("commentText") ?: ""
                    val date = document.getTimestamp("date")

                    val comment = Comment(userId, postId, commentText)
                    comment.date = date

                    commentList.add(comment)
                }


                commentsLiveData.value = commentList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCommentList(postId: String,commentText: String){
        val comment = Comment(auth.uid!!,postId,commentText)
        comment.date = Timestamp.now()
        CoroutineScope(Dispatchers.IO).launch {
            addComment(getApplication(), comment)
            withContext(Dispatchers.Main){
                commentList.add(comment)
                commentsLiveData.value = commentList
            }
        }
    }
}