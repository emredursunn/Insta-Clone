package com.example.firebasedeneme.views

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasedeneme.adapter.HomeAdapter
import com.example.firebasedeneme.mvvm.HomePageViewmodel
import com.example.firebasedeneme.R
import com.example.firebasedeneme.databinding.ActivityHomePageBinding
import com.example.firebasedeneme.model.Post
import com.example.firebasedeneme.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: HomeAdapter
    private lateinit var viewmodel: HomePageViewmodel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = Firebase.firestore


        val postList = arrayListOf<Post>()
        adapter = HomeAdapter(postList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this@HomePageActivity)

        viewmodel = ViewModelProvider(this@HomePageActivity).get(HomePageViewmodel::class.java)
        viewmodel.getData()
        observeLiveData()


        //SAYFA YENİLEMESİ
        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing=false
            binding.recyclerView.visibility = View.GONE
            binding.nullTextview.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            viewmodel.getData()
        }

    }


    fun observeLiveData(){

        viewmodel.postList.observe(this@HomePageActivity) {newPostList ->
            adapter.listeyiGuncelle(newPostList)
            binding.recyclerView.visibility = View.VISIBLE
            binding.nullTextview.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }

        viewmodel.nullText.observe(this@HomePageActivity) {bool->
            if(bool){
                binding.nullTextview.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
            else{
                binding.nullTextview.visibility = View.GONE
            }
        }

        viewmodel.progressBar.observe(this@HomePageActivity) {bool ->
            if(bool){
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }else{
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_add_photo -> {
                val intent = Intent(this@HomePageActivity, AddPostActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_logout -> {
                auth.signOut()
                val intent = Intent(this@HomePageActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
