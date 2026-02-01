package com.example.hydrasense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AvatarPickerActivity : AppCompatActivity() {

    private val avatars = listOf(
        R.drawable.ic_avatar_male_premium,
        R.drawable.ic_avatar_female_premium,
        R.drawable.ic_avatar_boy_premium,
        R.drawable.ic_avatar_girl_premium,
        R.drawable.ic_avatar_elderly_male_premium,
        R.drawable.ic_avatar_elderly_female_premium,
        R.drawable.prof // Default placeholder
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        setContentView(R.layout.activity_avatar_picker)
        supportActionBar?.hide()

        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        val rvAvatars = findViewById<RecyclerView>(R.id.rvAvatars)
        rvAvatars.layoutManager = GridLayoutManager(this, 3)
        rvAvatars.adapter = AvatarAdapter(avatars) { selectedAvatarRes ->
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_AVATAR", selectedAvatarRes)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    inner class AvatarAdapter(private val list: List<Int>, private val onClick: (Int) -> Unit) :
        RecyclerView.Adapter<AvatarAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val img = view.findViewById<ImageView>(R.id.imgItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_avatar, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.img.setImageResource(list[position])
            holder.itemView.setOnClickListener { onClick(list[position]) }
        }

        override fun getItemCount() = list.size
    }
}
