package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DataBaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : ComponentActivity() {
    var fabAddHappyPlace: FloatingActionButton? = null
    private var rv_happy_places_list: RecyclerView? = null
    private var tv_no_records_available:TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddHappyPlace = findViewById(R.id.fabAddHappyPlace)
        rv_happy_places_list = findViewById(R.id.rv_happy_places_list)
        tv_no_records_available = findViewById(R.id.tv_no_records_available)

        fabAddHappyPlace?.setOnClickListener{
            val intent = Intent(this, AddHappyPlacesActivity::class.java)
            startActivityForResult(intent,ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getHappyPlacesListFromLocalDB()


    }

    private fun setUpHappyPlacesRecyclerView(happyPlacesList: ArrayList<HappyPlaceModel>){
        rv_happy_places_list?.layoutManager = LinearLayoutManager(this)
        rv_happy_places_list?.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        rv_happy_places_list?.adapter = placesAdapter
    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DataBaseHandler(this)
        val getHappyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if(getHappyPlaceList.size >0){
           rv_happy_places_list?.visibility = View.VISIBLE
            tv_no_records_available?.visibility = View.GONE
            setUpHappyPlacesRecyclerView(getHappyPlaceList)
        }else{
            rv_happy_places_list?.visibility = View.GONE
            tv_no_records_available?.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Activity","Cancelled or Back pressed ")
            }
        }
    }

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
    }

}

