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
import com.google.firebase.auth.FirebaseAuth
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
        Toast.makeText(context,obj.isFav.toString(),Toast.LENGTH_LONG).show()
        if (obj.users.contains(FirebaseAuth.getInstance().uid.toString())){
            holder.favIcon.setImageResource(R.drawable.menu_fav_icon)


        }




        holder.favIcon.setOnClickListener {
            Toast.makeText(context, obj.storyID, Toast.LENGTH_LONG).show()
            val db = FirebaseFirestore.getInstance()
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid

                // Get the story document
                val storyRef = db.collection("stories").document(obj.storyID)

                // Fetch the current list of users
                storyRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val storyData = document.data
                            val users = storyData?.get("users") as? ArrayList<String> ?: arrayListOf()

                            // Add or remove the current user ID from the users list
                            if (users.contains(userId)) {
                                users.remove(userId)
                                obj.isFav = false // Update the isFav property
                            } else {
                                users.add(userId)
                                obj.isFav = true // Update the isFav property
                            }

                            // Update the story data
                            val updatedStoryData = hashMapOf(
                                "title" to obj.title,
                                "content" to obj.content,
                                "imageUrl" to obj.imageUrl,
                                "storyType" to obj.storyType,
                                "isFav" to obj.isFav,
                                "storyID" to obj.storyID,
                                "users" to users
                            )

                            // Update the story document in Firestore
                            storyRef.set(updatedStoryData)
                                .addOnSuccessListener {
                                    holder.favIcon.setImageResource(if (obj.isFav)
                                        R.drawable.menu_fav_icon else R.drawable.row_fav_false)
                                    Toast.makeText(context, if (obj.isFav) "Story added to favorites!" else "Story removed from favorites!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Failed to update story: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Story not found!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to fetch story: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // User is not logged in, handle this case if needed
                Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            }
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