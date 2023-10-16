package com.example.firebasedeneme.util

import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.firebasedeneme.R
import com.example.firebasedeneme.model.Comment
import com.example.firebasedeneme.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun ImageView.downloadFromUrl(context: Context,url: String?, progressDrawable: CircularProgressDrawable = placeholderProgressBar(context)){

    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.mipmap.ic_launcher_round)

    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(url)
        .into(this)

}

fun placeholderProgressBar(context: Context) : CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 8f
        centerRadius = 40f
        start()
    }
}

suspend fun getUserById(userId: String): User? {
    val firestore = Firebase.firestore

    try {
        val documentSnapshot = firestore.collection("Users")
            .document(userId)
            .get()
            .await()

        if (documentSnapshot.exists()) {
            val email = documentSnapshot.getString("email")
            val userPhotoUrl = documentSnapshot.getString("userPhotoUrl")

            if (email != null) {
                val user = User(email, userPhotoUrl ?: "")
                user.userId = userId
                return user
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}


suspend fun getLikedUsers(postId: String): ArrayList<String> {
    val firestore = Firebase.firestore
    val postRef = firestore.collection("Posts").document(postId).get().await()
    val likedUsers = postRef.get("likedUsers") as? ArrayList<String> ?: arrayListOf()
    return likedUsers
}

suspend fun updateLikedUsers(postId: String, userId: String, liked: Boolean) {
    val firestore = Firebase.firestore
    val likedUsersRef = firestore.collection("Posts").document(postId)
        .collection("LikedUsers").document(userId)

    if (liked) {
        // Belge var mı kontrol et
        val docSnapshot = likedUsersRef.get().await()

        if (!docSnapshot.exists()) {
            // Belge yoksa ekle
            likedUsersRef.set(hashMapOf("userId" to userId)).await()
        }
    } else {
        // Silme işlemi
        likedUsersRef.delete().await()
    }
}

suspend fun addComment(context: Context,comment: Comment){
    val firestore = Firebase.firestore
    try {
        firestore.collection("Posts")
            .document(comment.postId)
            .collection("Comments")
            .document()
            .set(comment)
            .await()

        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Yorum yapıldı!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
     catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Hata! meydana geldi. Yorum yapılamadı!!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
