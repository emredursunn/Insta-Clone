package com.example.firebasedeneme.model

import com.google.firebase.Timestamp
import java.io.Serializable

class Comment(val userId:String,val postId:String,val commentText:String) : Serializable {
    var date : Timestamp? = null
}