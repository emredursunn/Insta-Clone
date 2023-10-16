package com.example.firebasedeneme.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasedeneme.R
import com.example.firebasedeneme.databinding.RecyclerRowBinding
import com.example.firebasedeneme.model.Comment
import com.example.firebasedeneme.model.Post
import com.example.firebasedeneme.model.User
import com.example.firebasedeneme.util.addComment
import com.example.firebasedeneme.util.downloadFromUrl
import com.example.firebasedeneme.util.getLikedUsers
import com.example.firebasedeneme.util.getUserById
import com.example.firebasedeneme.util.updateLikedUsers
import com.example.firebasedeneme.views.DetailedPostActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeAdapter(val postList: ArrayList<Post>) : RecyclerView.Adapter<HomeAdapter.PostVH>() {

    val auth = Firebase.auth
    val firestore = Firebase.firestore
    var user: User? = null

    inner class PostVH(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostVH {
        val binding = DataBindingUtil.inflate<RecyclerRowBinding>(
            LayoutInflater.from(parent.context),
            R.layout.recycler_row, parent, false
        )
        return PostVH(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostVH, position: Int) {
        val post = postList[position]
        CoroutineScope(Dispatchers.IO).launch {
            user = getUserById(post.userId)
            post.likedUsers = getLikedUsers(post.postId)
            withContext(Dispatchers.Main){
                user?.let {us->
                    holder.binding.profilePhotoImageView.downloadFromUrl(holder.itemView.context,us.userPhotoUrl)
                    holder.binding.postEmailText.text = us.email
                }
                holder.binding.likeCountDetail.text = "${post.likedUsers.size} kişi beğendi!"
            }
        }
        holder.binding.postOutlineText.text = post.outline
        holder.binding.postImageView.downloadFromUrl(holder.itemView.context,post.imageUrl)

        holder.binding.likeBtn.setOnClickListener {
            val currentUserId = auth.uid
            currentUserId?.let {uid->
                if (uid in post.likedUsers) {
                    CoroutineScope(Dispatchers.IO).launch {
                        updateLikedUsers(post.postId, uid, false)
                        withContext(Dispatchers.Main) {
                            holder.binding.likeBtn.text = "LIKED!"
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        updateLikedUsers(post.postId, uid, true)
                        withContext(Dispatchers.Main) {
                            holder.binding.likeBtn.text = "LIKE!"
                        }
                    }
                }
                notifyItemChanged(position)
            }
        }



        holder.binding.sendBtn.setOnClickListener {
            val commentText = holder.binding.commentText.text.toString()
            holder.binding.commentText.text.clear()
            user?.let { us ->
                val comment = Comment(us.userId, post.postId, commentText)
                CoroutineScope(Dispatchers.IO).launch {
                    addComment(holder.itemView.context,comment)
                }
            }
        }



        holder.binding.seeComments.setOnClickListener {
            val intent = Intent(holder.itemView.context,DetailedPostActivity::class.java)
            intent.putExtra("post",post)
            holder.itemView.context.startActivity(intent)
        }

    }

    fun listeyiGuncelle(newPostList: ArrayList<Post>) {
        postList.clear()
        postList.addAll(newPostList)
        notifyDataSetChanged()
    }
}