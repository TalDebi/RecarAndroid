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

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

import com.idz.Recar.R
import com.idz.Recar.base.FireBaseStorage
import com.squareup.picasso.Picasso


class ImageAdapter(arrayList: List<String>, isLocal: Boolean = false) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    var arrayList: List<String>
    var storage = FireBaseStorage.getInstance().storage
    var isLocal = false

    init {
        this.arrayList = arrayList
        this.isLocal = isLocal
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
        if (isLocal) {
            Picasso.get().load(arrayList[position]).placeholder(holder.loader)
                .into(holder.imageView)

        } else {
            var load = arrayList[position].let {

                storage.getReferenceFromUrl(it)

            }

            load.getDownloadUrl()
                .addOnSuccessListener(OnSuccessListener<Uri> { uri ->
                    Picasso.get().load(uri.toString()).placeholder(holder.loader)
                        .into(holder.imageView)
                }).addOnFailureListener(OnFailureListener { })

        }


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