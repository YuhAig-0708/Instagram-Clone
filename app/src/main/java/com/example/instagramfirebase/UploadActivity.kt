package com.example.instagramfirebase

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.instagramfirebase.databinding.ActivityUploadBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class UploadActivity : AppCompatActivity() {

    var selected : Uri? = null
    private lateinit var binding: ActivityUploadBinding
    var mAuth : FirebaseAuth? = null
    var mAuthListener : FirebaseAuth.AuthStateListener? = null
    var firebaseDatabase : FirebaseDatabase? = null
    var myRef : DatabaseReference? = null
    var mStorageRef : StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Khởi tạo ViewBinding
        binding = ActivityUploadBinding.inflate(layoutInflater)
        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance("https://kotlinfirebaseinstagram-e54cd-default-rtdb.asia-southeast1.firebasedatabase.app")
        myRef = firebaseDatabase!!.reference
        mStorageRef = FirebaseStorage.getInstance().reference


        setContentView(binding.root)

        // Thiết lập nút "X" để đóng Activity
        binding.closeButton.setOnClickListener {
            finish()
        }

    }
    fun upload(view: View) {
        if (selected != null) {
            val uuid = UUID.randomUUID()
            val imageName = "image/$uuid.jpg"

            // Tham chiếu đến Firebase Storage
            val storageReference = mStorageRef!!.child(imageName)

            // Tải file lên Firebase Storage
            storageReference.putFile(selected!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Lấy URL download của ảnh sau khi upload thành công
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val downloadURL = uri.toString()
                        println(downloadURL)  // In ra URL của ảnh

                        val user = mAuth?.currentUser
                        val userEmail = user?.email.toString()
                        val userComment = binding.commentText.text.toString()

                        // Kiểm tra user comment có rỗng không
                        if (userComment.isEmpty()) {
                            Toast.makeText(applicationContext, "Please enter a comment", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }


                        val uuidString = UUID.randomUUID().toString()
                        val timestamp = System.currentTimeMillis()

                        Log.d("UploadActivity", "Database reference: ${myRef.toString()}")
                        // Lưu dữ liệu vào Realtime Database
                        myRef?.child("Posts")?.child(uuidString)?.apply {
                            child("useremail").setValue(userEmail)
                                .addOnSuccessListener {
                                    Log.d("UploadActivity", "User email saved successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("UploadActivity", "Failed to save user email: ${e.message}")
                                }

                            child("comment").setValue(userComment)
                                .addOnSuccessListener {
                                    Log.d("UploadActivity", "Comment saved successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("UploadActivity", "Failed to save comment: ${e.message}")
                                }

                            child("downloadurl").setValue(downloadURL)
                                .addOnSuccessListener {
                                    Log.d("UploadActivity", "Download URL saved successfully.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("UploadActivity", "Failed to save download URL: ${e.message}")
                                }

                            child("timestamp").setValue(timestamp)
                        }
                    }.addOnFailureListener { exception ->
                        // Xử lý lỗi khi không thể lấy URL
                        println("Error fetching download URL: ${exception.localizedMessage}")
                        Toast.makeText(applicationContext, "Error fetching download URL", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Xử lý lỗi khi upload thất bại
                    Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
                .addOnCompleteListener { task ->
                    if (task.isComplete) {
                        Toast.makeText(applicationContext, "Post Shared", Toast.LENGTH_LONG).show()
                        // intent hoặc các thao tác sau khi hoàn thành upload
                        val intent = Intent(applicationContext, FeedActivity::class.java)
                        startActivity(intent)
                    }
                }
        } else {
            Toast.makeText(applicationContext, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun selectImage(view: View) {
        // Yêu cầu quyền truy cập ảnh và video trên Android 13+
        if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {

            // Nếu chưa được cấp quyền, yêu cầu cả hai quyền
            requestPermissions(arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            ), 1)
        } else {
            // Nếu quyền đã được cấp, mở trình chọn ảnh và video
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 2)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            val allPermissionsGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allPermissionsGranted) {
                Log.d("UploadActivity", "All permissions granted, opening image picker.")
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 2)
            } else {
                Log.d("UploadActivity", "Permission denied.")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    @Deprecated("Sử dụng registerForActivityResult thay thế")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("UploadActivity", "Image selected.")
            selected = data.data

            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selected)
                binding.imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("UploadActivity", "Error loading image: ${e.message}")
            }
        } else {
            Log.d("UploadActivity", "No image selected or result canceled.")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
