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
import com.example.storiesdata.Models.StoriesDataModel
import com.example.storiesdata.R
import com.example.storiesdata.StoriesScreen
import com.example.storiesdata.StoryReadScreen
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class StoriesAdapter(val context:Context,val collectionList:ArrayList<StoriesDataModel>,
   val isFromFavContext:Boolean):RecyclerView.Adapter<StoriesAdapter.MyViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.story_row,parent,false))

    }

    override fun getItemCount(): Int {
        return  collectionList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val obj=collectionList[position]
        holder.title.text=obj.title
        holder.storyNumber.text=(position+1).toString()
        if (isFromFavContext){
            holder.favIcon.setImageResource(R.drawable.menu_fav_icon)
        }
        holder.favIcon.setOnClickListener {

            val db = FirebaseFirestore.getInstance() // Get Firestore instance

            val docRef = db.collection("stories")

            obj.isFav=false
            Toast.makeText(context,"removed",Toast.LENGTH_LONG).show()
        }


        holder.storyRow.setOnClickListener {
            context.startActivity(Intent(context, StoryReadScreen::class.java).putExtra("myObject", obj))

        }

    }



    inner  class MyViewHolder(val view:View):RecyclerView.ViewHolder(view){
        val title=view.findViewById<TextView>(R.id.storyTtitle)
        val storyNumber=view.findViewById<TextView>(R.id.storyNumber)
        val storyRow=view.findViewById<ConstraintLayout>(R.id.storyRow)
        val favIcon=view.findViewById<ImageView>(R.id.storyFavIcons)
    }

}