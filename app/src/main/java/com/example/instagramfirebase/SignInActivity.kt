package com.example.instagramfirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.instagramfirebase.databinding.ActivitySignInBinding



class SignInActivity : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //khởi tạo binding với ViewBinding
        binding = ActivitySignInBinding.inflate(layoutInflater)

        //thiết lập giao diên
        setContentView(binding.root)

        //khởi tạo firebaseAuth
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { }

    }

    fun signIn(view: View) {
        mAuth!!.signInWithEmailAndPassword(
            binding.emailText.text.toString(),
            binding.passwordText.text.toString()
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //intent
                val intent = Intent(applicationContext, FeedActivity::class.java)
                startActivity(intent)
            }
        }.addOnFailureListener { exception ->
            // Xử lý khi có lỗi xảy ra
                Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()

            }
        }

    fun signUp(view: View) {

        mAuth!!.createUserWithEmailAndPassword(
            binding.emailText.text.toString(),
            binding.passwordText.text.toString()
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Đăng ký thành công
                Toast.makeText(this,"Tạo tài khoản thành công",Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            // Xử lý khi có lỗi xảy ra
            if (exception != null){
                Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}
