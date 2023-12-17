package com.example.happyplaces.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel



open class HappyPlacesAdapter(
    private val context: Context,
    private val list: ArrayList<HappyPlaceModel>
): RecyclerView.Adapter<HappyPlacesAdapter.MyViewHolder>(){

   inner class MyViewHolder(val itemBinding: ItemHappyPlaceBinding): RecyclerView.ViewHolder(itemBinding.root){
       fun bindItem(happyPlace: HappyPlaceModel){
           itemBinding.ivPlaceImage.setImageURI(Uri.parse(happyPlace.image))
           itemBinding.tvTitle.text = happyPlace.title
           itemBinding.tvDescription.text = happyPlace.description
       }
   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context),parent,false))


    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
           holder.bindItem(model)
        }
    }


}