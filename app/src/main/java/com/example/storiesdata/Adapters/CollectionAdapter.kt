package com.example.storiesdata.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.storiesdata.Models.MainCollectionModel
import com.example.storiesdata.R
import com.example.storiesdata.StoriesScreen
import com.squareup.picasso.Picasso

class CollectionAdapter(val context:Context,val collectionList:ArrayList<MainCollectionModel>):RecyclerView.Adapter<CollectionAdapter.MyViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.main_recycler,parent,false))

    }

    override fun getItemCount(): Int {
      return  collectionList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val obj=collectionList[position]
        holder.collectionName.text=obj.storyType
        Picasso.get()
            .load(obj.imageUrl)
            .into(holder.collectionImage)
       holder.row.setOnClickListener {
           Toast.makeText(context,obj.storyType.toString(),Toast.LENGTH_LONG).show()
           context.startActivity(Intent(context,StoriesScreen::class.java).putExtra("storyCollection",obj.storyType))

       }




    }



    inner  class MyViewHolder(val view:View):RecyclerView.ViewHolder(view){
        val collectionImage=view.findViewById<ImageView>(R.id.collectionImage)
        val collectionName=view.findViewById<TextView>(R.id.collectionName)
        val row=view.findViewById<ConstraintLayout>(R.id.mainRecyerRow)


    }

}