package com.example.firebasedeneme.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firebasedeneme.databinding.ActivityAddPostBinding
import com.example.firebasedeneme.model.Post
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class AddPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding
    private var selectedPicture: Uri? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>
    private lateinit var storage:FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerLaunchers()

        storage = Firebase.storage
        firestore = Firebase.firestore
        auth = Firebase.auth
    }

    fun fotoSec(view: View){
        //İZİN ALINMADIYSA
        if(ContextCompat.checkSelfPermission(this@AddPostActivity,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this@AddPostActivity,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Galeriye erişime izin vermelisin!",Snackbar.LENGTH_LONG).setAction("İzin Ver") {
                    permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()
            }else{
                permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        //İZİN ALINDIYSA
        else{
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

     fun paylas(view:View){

         view.isEnabled = false
         val reference = storage.reference
         val uuid = UUID.randomUUID()
         val name = "${uuid}.jpg"
         val imageReference = reference.child("images").child(name)

         selectedPicture?.let { resim ->
             // CoroutineScope'u Dispatchers.IO ile başlat
             CoroutineScope(Dispatchers.IO).launch {
                 try {
                     val uploadTask = imageReference.putFile(resim).await()
                     val imageUrl = uploadTask.storage.downloadUrl.await().toString()
                     val outline = binding.outlineText.text.toString()
                     val postId = UUID.randomUUID().toString()

                     val post = Post(auth.uid!!, imageUrl, outline)
                     post.date = Timestamp.now()
                     post.postId = postId
                     val hashmap = hashMapOf("date" to post.date,"imageUrl" to imageUrl,"outline" to outline,
                         "postId" to postId, "userId" to post.userId)

                     // Firestore'da yeni post dokümanı oluştur
                     firestore.collection("Posts").document(postId)
                         .set(hashmap).await()
                     withContext(Dispatchers.Main) {
                         val intent = Intent(this@AddPostActivity, HomePageActivity::class.java)
                         startActivity(intent)
                         finish()
                     }
                 }catch (e: Exception) {
                     withContext(Dispatchers.Main) {
                         Toast.makeText(this@AddPostActivity, "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                     }
                 } finally {
                     withContext(Dispatchers.Main) {
                         view.isEnabled = true
                     }
                 }
             }
         }
     }


    private fun registerLaunchers(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                intentFromResult?.let {intent->
                    selectedPicture = intent.data
                    selectedPicture?.let {uri->
                    }
                }
            }
        }
        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this@AddPostActivity,"İzin Verilmedi!",Toast.LENGTH_LONG).show()
            }
        }
    }
}