package com.example.firebasedeneme.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasedeneme.databinding.RecyclerCommentRowBinding
import com.example.firebasedeneme.model.Comment
import com.example.firebasedeneme.model.User
import com.example.firebasedeneme.util.downloadFromUrl
import com.example.firebasedeneme.util.getUserById
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentsAdapter(val commentList:ArrayList<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentVH>(){

    private var user:User? = null

    inner class CommentVH(val binding:RecyclerCommentRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentVH {
        val binding = RecyclerCommentRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CommentVH(binding)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentVH, position: Int) {
        val comment = commentList[position]
        CoroutineScope(Dispatchers.IO).launch {
            user = getUserById(comment.userId)
            withContext(Dispatchers.Main) {
                user?.let { us ->
                    holder.binding.postEmailText.text = us.email
                    holder.binding.profilePhotoImageView.downloadFromUrl(holder.itemView.context,us.userPhotoUrl)
                }
            }
        }
        holder.binding.yorumTextView.text = commentList[position].commentText
    }

    fun yorumlariGuncelle(newCommentList:ArrayList<Comment>){
        commentList.clear()
        commentList.addAll(newCommentList)
        notifyDataSetChanged()
    }

}