package com.example.instagramfirebase

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class PostClass (private val userEmail : ArrayList<String>,
                 private val userImage : ArrayList<String>,
                 private val userComment : ArrayList<String>,
                 private val context:Activity
) : ArrayAdapter<String> ( context, R.layout.custom_view, userEmail){

//    private lateinit var binding: CustomViewBinding
    private class ViewHolder {
        lateinit var commentTextView: TextView
        lateinit var usernameTextView: TextView
        lateinit var profileImageView: ImageView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Khai báo biến view và holder
        var view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.custom_view, parent, false)
        val holder: ViewHolder

        // Nếu convertView là null, nghĩa là cần inflate và khởi tạo ViewHolder mới
        if (convertView == null) {
            holder = ViewHolder()

            // Gán các view vào ViewHolder
            holder.commentTextView = view.findViewById(R.id.customCommentText)
            holder.usernameTextView = view.findViewById(R.id.customUsername)
            holder.profileImageView = view.findViewById(R.id.CustomImageView)

            // Gắn ViewHolder vào view để tái sử dụng
            view.tag = holder
        } else {
            // Lấy ViewHolder đã được lưu trong tag của convertView
            holder = view.tag as ViewHolder
        }

        // Thiết lập giá trị cho các thành phần trong ViewHolder
        holder.commentTextView.text = userComment[position]
        holder.usernameTextView.text = userEmail[position]

        // Sử dụng Picasso để tải hình ảnh từ Firebase Storage và hiển thị trong ImageView
        Picasso.get()
            .load(userImage[position])   // Đường dẫn URL hình ảnh
            .placeholder(R.drawable.placeholder)  // Hình ảnh placeholder trong khi ảnh đang tải
            .error(R.drawable.error)  // Hình ảnh hiển thị nếu tải ảnh thất bại
            .into(holder.profileImageView)
        // Trả về view đã được tái sử dụng hoặc mới tạo
        return view
    }
}