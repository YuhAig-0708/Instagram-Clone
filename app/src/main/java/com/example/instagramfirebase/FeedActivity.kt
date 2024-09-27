package com.example.instagramfirebase

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramfirebase.databinding.ActivityFeedBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    var userEmailsFromDB: ArrayList<String> = ArrayList()
    var userImagesFromDB: ArrayList<String> = ArrayList()
    var userCommentsFromDB: ArrayList<String> = ArrayList()
    var firebaseDatabase: FirebaseDatabase? = null
    var myRef: DatabaseReference? = null
    var adapter: PostClass? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_post, menu)
        return true // Trả về true để menu hiển thị
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_post) {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //khởi tạo binding với ViewBinding
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập Toolbar để sử dụng làm ActionBar
        setSupportActionBar(binding.toolbar)

        firebaseDatabase =
            FirebaseDatabase.getInstance("https://kotlinfirebaseinstagram-e54cd-default-rtdb.asia-southeast1.firebasedatabase.app")
        myRef = firebaseDatabase!!.getReference()

        adapter = PostClass(userEmailsFromDB, userImagesFromDB, userCommentsFromDB, this)

        binding.listView.adapter = adapter

        getDataFromFirebase()

    }

    private fun getDataFromFirebase() {
        val newReference = firebaseDatabase!!.getReference("Posts")

        // Sắp xếp các bài đăng theo timestamp từ cũ đến mới
        newReference.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adapter!!.clear()
                userEmailsFromDB.clear()
                userImagesFromDB.clear()
                userCommentsFromDB.clear()

                // Vòng lặp qua các bài đăng trong Firebase
                for (snap in snapshot.children) {
                    val hashMap = snap.value as? HashMap<String, Any>

                    if (hashMap != null && hashMap.size > 0) {
                        val email = hashMap["useremail"] as? String
                        val comment = hashMap["comment"] as? String
                        val image = hashMap["downloadurl"] as? String

                        // Lưu các giá trị vào các danh sách tương ứng
                        if (email != null) {
                            userEmailsFromDB.add(email)
                        }

                        if (comment != null) {
                            userCommentsFromDB.add(comment)
                        }

                        if (image != null) {
                            userImagesFromDB.add(image)
                        }
                    }
                }
                userEmailsFromDB.reverse()
                userCommentsFromDB.reverse()
                userImagesFromDB.reverse()
                // Cập nhật adapter sau khi đã thêm dữ liệu
                adapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi (nếu có)
            }
        })
    }
}
