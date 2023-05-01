package com.example.szakdolgozatproject

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ViewPagerAdapter(private var uriList: List<Uri>) : RecyclerView.Adapter<ViewPagerAdapter.Pager2ViewHolder>() {
    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val itemPic: ImageView = itemView.findViewById(R.id.profilePictureMainPage)
    }
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int) : ViewPagerAdapter.Pager2ViewHolder{
        return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.firend_page_picture,parent,false))
    }
    override fun getItemCount(): Int {
        return uriList.size
    }
    override fun onBindViewHolder(holder: ViewPagerAdapter.Pager2ViewHolder, position: Int){
        val currentItem = uriList[position]
        Picasso.get().load(currentItem).into(holder.itemPic)
    }
}