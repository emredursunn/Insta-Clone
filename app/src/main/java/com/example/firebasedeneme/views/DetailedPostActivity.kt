package com.example.firebasedeneme.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasedeneme.mvvm.DetailedPostViewmodel
import com.example.firebasedeneme.R
import com.example.firebasedeneme.adapter.CommentsAdapter
import com.example.firebasedeneme.databinding.ActivityDetailedPostBinding
import com.example.firebasedeneme.model.Comment
import com.example.firebasedeneme.model.Post
import com.example.firebasedeneme.util.downloadFromUrl
import com.example.firebasedeneme.util.getLikedUsers
import com.example.firebasedeneme.util.getUserById
import com.example.firebasedeneme.util.updateLikedUsers
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DetailedPostActivity : AppCompatActivity() {

    val auth = Firebase.auth
    private lateinit var binding: ActivityDetailedPostBinding
    private lateinit var viewmodel: DetailedPostViewmodel
    lateinit var post: Post
    private lateinit var adapter: CommentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedPostBinding.inflate(layoutInflater)


        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this@DetailedPostActivity)
        adapter = CommentsAdapter(arrayListOf())
        binding.commentRecyclerView.adapter = adapter

        post = intent.getSerializableExtra("post") as Post

        viewmodel = ViewModelProvider(this).get(DetailedPostViewmodel::class.java)
        viewmodel.getData(post.postId)

        observeLiveData()

    }

    fun observeLiveData() {
        viewmodel.postLiveData.observe(this@DetailedPostActivity, Observer { postdata ->
            bind(postdata)
        })

        viewmodel.commentsLiveData.observe(this@DetailedPostActivity, Observer { commentList ->
            if (!commentList.isEmpty()) {
                binding.commentRecyclerView.adapter = adapter
                adapter.yorumlariGuncelle(commentList)
                binding.commentRecyclerView.visibility = View.VISIBLE
                binding.nullCommentText.visibility = View.GONE
            } else {
                binding.commentRecyclerView.visibility = View.GONE
                binding.nullCommentText.visibility = View.VISIBLE
            }

        })

        viewmodel.nullLiveData.observe(this@DetailedPostActivity, Observer { bool ->
            if (bool) {
                binding.nullCommentText.visibility = View.VISIBLE
                binding.commentRecyclerView.visibility = View.GONE
            } else {
                binding.nullCommentText.visibility = View.GONE
            }
        })
    }

    fun like(view: View) {
        val currentUserId = auth.uid
        currentUserId?.let { uid ->
            CoroutineScope(Dispatchers.IO).launch {
                post.likedUsers = getLikedUsers(post.postId)
                if (uid in post.likedUsers) {
                    updateLikedUsers(post.postId, uid, false)
                    withContext(Dispatchers.Main) {
                        binding.likeBtn.text = "LIKED!"
                    }
                } else {
                    updateLikedUsers(post.postId, uid, true)
                    withContext(Dispatchers.Main) {
                        binding.likeBtn.text = "LIKE!"
                    }
                }
            }
        }
    }

    fun send(view: View) {
        val commentText = binding.commentText.text.toString()
        binding.commentText.text.clear()
        if (!commentText.isEmpty()) {
            viewmodel.updateCommentList(post.postId, commentText)
        } else {
            Toast.makeText(this, "YORUM YAZINIZ!", Toast.LENGTH_LONG).show()
        }
    }

    private fun bind(post: Post) {
        binding.apply {
            likeCountDetail.text = post.likedUsers.size.toString()
            postOutlineText.text = post.outline

            CoroutineScope(Dispatchers.IO).launch {
                val user = getUserById(post.userId)
                user?.let { us ->
                    withContext(Dispatchers.Main) {
                        postImageView.downloadFromUrl(this@DetailedPostActivity, post.imageUrl)
                        postEmailText.text = us.email
                        profilePhotoImageView.downloadFromUrl(this@DetailedPostActivity, us.userPhotoUrl)
                    }
                }
            }
        }
    }

}

/*binding.sendBtn.setOnClickListener {
            val commentText = binding.commentText.text.toString()
            binding.commentText.text.clear()
            if(!commentText.isEmpty()){
                viewmodel.updateCommentList(post.postId,commentText)
            }else{
                Toast.makeText(this,"YORUM YAZINIZ!",Toast.LENGTH_LONG).show()
            }
        }
         */

/*binding.likeBtn.setOnClickListener {
    val currentUserId = auth.uid
    currentUserId?.let {uid->
        CoroutineScope(Dispatchers.IO).launch {
            post.likedUsers = getLikedUsers(post.postId)
            if (uid in post.likedUsers) {
                updateLikedUsers(post.postId, uid, false)
                withContext(Dispatchers.Main) {
                    binding.likeBtn.text = "LIKED!"
                }
            }else{
                updateLikedUsers(post.postId, uid, true)
                withContext(Dispatchers.Main) {
                    binding.likeBtn.text = "LIKE!"
                }
            }
        }
    }
}

 */
