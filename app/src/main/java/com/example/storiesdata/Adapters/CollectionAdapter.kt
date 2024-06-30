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
import com.google.android.material.imageview.ShapeableImageView
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
           context.startActivity(Intent(context,StoriesScreen::class.java).putExtra("storyCollection",obj.storyType))
       }

//        var  pos=position+1
////           Toast.makeText(context,collectionList.size,Toast.LENGTH_LONG).show()
////
//           if (pos==collectionList.size){
//               Toast.makeText(context,pos.toString(),Toast.LENGTH_LONG).show()
//               // Alternatively, if you want to set padding in dp, convert dp to pixels first
//               val paddingInDp = 30
//               val scale = context.resources.displayMetrics.density
//               val paddingInPx = (paddingInDp * scale + 0.5f).toInt()
//               holder.row.setPadding(0,0,0, paddingInPx)
//
//           }





    }



    inner  class MyViewHolder(val view:View):RecyclerView.ViewHolder(view){
        val collectionImage=view.findViewById<ShapeableImageView>(R.id.shimmerCollectionIamge)
        val collectionName=view.findViewById<TextView>(R.id.collectionName)
        val row=view.findViewById<ConstraintLayout>(R.id.mainRecyerRow)


    }

}