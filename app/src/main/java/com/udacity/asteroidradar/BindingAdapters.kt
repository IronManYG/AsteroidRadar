package com.udacity.asteroidradar

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.main.AsteroidListAdapter
import com.udacity.asteroidradar.main.NasaApiStatus

@BindingAdapter("statusIcon")
fun bindAsteroidStatusImage(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.setImageResource(R.drawable.ic_status_potentially_hazardous)
    } else {
        imageView.setImageResource(R.drawable.ic_status_normal)
    }
}

@BindingAdapter("asteroidStatusImage")
fun bindDetailsStatusImage(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.setImageResource(R.drawable.asteroid_hazardous)
    } else {
        imageView.setImageResource(R.drawable.asteroid_safe)
    }
}

@BindingAdapter("astronomicalUnitText")
fun bindTextViewToAstronomicalUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.astronomical_unit_format), number)
}

@BindingAdapter("kmUnitText")
fun bindTextViewToKmUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_unit_format), number)
}

@BindingAdapter("velocityText")
fun bindTextViewToDisplayVelocity(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_s_unit_format), number)
}

/**
 * When there is no Mars property data (data is null), hide the [RecyclerView], otherwise show it.
 */
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Asteroid>?) {
    val adapter = recyclerView.adapter as AsteroidListAdapter
    adapter.submitList(data)
}

/**
 * This binding adapter displays the [NasaApiStatus] of the network request in a ProgressBar.  When
 * the request is loading, it displays a loading_animation.  If the request has an error, it
 * displays a broken image to reflect the connection error.  When the request is finished, it
 * hides the image view.
 */
@BindingAdapter("nasaApiStatus")
fun bindStatus(statusProgressBar: ProgressBar, status: NasaApiStatus?) {
    when (status) {
        NasaApiStatus.LOADING -> {
            statusProgressBar.visibility = View.VISIBLE
        }
        NasaApiStatus.ERROR -> {
            statusProgressBar.visibility = View.GONE
        }
        NasaApiStatus.DONE -> {
            statusProgressBar.visibility = View.GONE
        }
    }
}

@BindingAdapter("imageOfTheDay")
fun loadImageWithUri(imageView: ImageView, urlPic: String?){
    urlPic?.let {
        val imgUri = urlPic.toUri().buildUpon().scheme("https").build()
        Picasso.get()
            .load(imgUri)
            .networkPolicy(NetworkPolicy.OFFLINE)
            .into(imageView, object : Callback {
                override fun onSuccess() {
                    //Offline Cache hit
                }
                override fun onError(e: Exception?) {
                    //Try again online if cache failed
                    Picasso.get()
                        .load(imgUri)
                        .into(imageView)
                }
            })
    }
}

@BindingAdapter("asteroidImageDescription")
fun bindAsteroidImageDescription(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.contentDescription = "Image of potentially hazardous asteroid"
    } else {
        imageView.contentDescription = "Image of happy, not hazardous asteroid"
    }
}
