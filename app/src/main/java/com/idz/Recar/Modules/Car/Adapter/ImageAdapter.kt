package com.idz.Recar.Modules.Car.Adapter

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.idz.Recar.R
import com.squareup.picasso.Picasso


class ImageAdapter(arrayList: ArrayList<String>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    var arrayList: ArrayList<String>
    var storage = Firebase.storage("gs://recar-46bcf.appspot.com")

    init {
        this.arrayList = arrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.carousel_item, parent, false)
        val circularProgressDrawable = CircularProgressDrawable(parent.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 60f
        circularProgressDrawable.setColorSchemeColors(Color.GREEN)
        circularProgressDrawable.start()
        return ViewHolder(view, parent.context, circularProgressDrawable)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var load = arrayList[position].let {

            storage.getReferenceFromUrl(it)

        }

        load.getDownloadUrl()
            .addOnSuccessListener(OnSuccessListener<Uri> { uri ->
                Picasso.get().load(uri.toString()).placeholder(holder.loader).into(holder.imageView)
            }).addOnFailureListener(OnFailureListener { })

//        Glide.with(holder.context).load(load).placeholder(holder.loader)
//            .into(holder.imageView)


    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ViewHolder(itemView: View, context: Context, loader: CircularProgressDrawable) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var context: Context
        var loader: CircularProgressDrawable

        init {
            imageView = itemView.findViewById(R.id.list_item_image)
            this.context = context
            this.loader = loader
        }
    }


}