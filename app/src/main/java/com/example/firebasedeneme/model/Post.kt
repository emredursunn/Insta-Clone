package com.example.firebasedeneme.model

import com.google.firebase.Timestamp
import java.io.Serializable


class Post(val userId:String, val imageUrl:String, val outline:String) : Serializable {
    var postId :String = ""
    var likedUsers = ArrayList<String>()
    var date : Timestamp? = null
}


