package com.michaelmagdy.themoviesapp.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.michaelmagdy.themoviesapp.R
import com.michaelmagdy.themoviesapp.model.webservice.POSTER_BASE_URL
import com.michaelmagdy.themoviesapp.model.webservice.Result
import com.michaelmagdy.themoviesapp.util.NetworkState
import kotlinx.android.synthetic.main.movie_list_item.view.*
import kotlinx.android.synthetic.main.network_state_item.view.*

class MoviePagedListAdapter (public val context: Context) : PagedListAdapter<Result, RecyclerView.ViewHolder>(MovieDiffCallback())  {

    val MOVIE_VIEW_TYPE = 1
    val NETWORK_VIEW_TYPE = 2
    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View

        if (viewType == MOVIE_VIEW_TYPE) {
            view = layoutInflater.inflate(R.layout.movie_list_item, parent, false)
            return MovieItemViewHolder(view)
        } else {
            view = layoutInflater.inflate(R.layout.network_state_item, parent, false)
            return NetworkStateItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (getItemViewType(position) == MOVIE_VIEW_TYPE) {
            (holder as MovieItemViewHolder).bind(getItem(position),context)
        }
        else {
            (holder as NetworkStateItemViewHolder).bind(networkState)
        }
    }


    class MovieDiffCallback : DiffUtil.ItemCallback<Result>() {
        override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean {
            return oldItem == newItem
        }

    }

    class MovieItemViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        fun bind(movie: Result?, context: Context) {
            with(itemView) {

                itemView.cv_movie_title.text = movie?.title
                itemView.cv_movie_release_date.text =  movie?.releaseDate


                val moviePosterURL = POSTER_BASE_URL + movie?.posterPath
                Glide.with(itemView.context)
                    .load(moviePosterURL)
                    .into(itemView.cv_iv_movie_poster);

                itemView.setOnClickListener{

                    showAlertDialog(itemView.context, movie)
                }
            }

        }

        fun showAlertDialog(context: Context, movie: Result?) {
            val dialogBuilder =
                AlertDialog.Builder(context)
            //val layoutView: View = getLayoutInflater().inflate(layout, null)
            val layoutView: View = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)
            val dialogButton =
                layoutView.findViewById<Button>(R.id.btnDialog)
            val imageView = layoutView.findViewById<ImageView>(R.id.imageView)
            val titleTxt = layoutView.findViewById<TextView>(R.id.textView_title)
            val detailsTxt = layoutView.findViewById<TextView>(R.id.textView_details)
            titleTxt.text = movie?.title
            detailsTxt.text = movie?.overview + " " + movie?.originalLanguage + " " + movie?.releaseDate
            val moviePosterURL = POSTER_BASE_URL + movie?.posterPath
            Glide.with(context)
                .load(moviePosterURL)
                .into(imageView)
            dialogBuilder.setView(layoutView)
            val alertDialog = dialogBuilder.create()
            alertDialog.window
                ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
            alertDialog.show()
            dialogButton.setOnClickListener { alertDialog.dismiss() }
        }

    }

    class NetworkStateItemViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        fun bind(networkState: NetworkState?) {
            if (networkState != null && networkState == NetworkState.LOADING) {
                itemView.progress_bar_item.visibility = View.VISIBLE;
            }
            else  {
                itemView.progress_bar_item.visibility = View.GONE;
            }

            if (networkState != null && networkState == NetworkState.ERROR) {
                itemView.error_msg_item.visibility = View.VISIBLE;
                itemView.error_msg_item.text = networkState.msg;
            }
            else if (networkState != null && networkState == NetworkState.ENDOFLIST) {
                itemView.error_msg_item.visibility = View.VISIBLE;
                itemView.error_msg_item.text = networkState.msg;
            }
            else {
                itemView.error_msg_item.visibility = View.GONE;
            }
        }
    }

    fun setNetworkState(newNetworkState: NetworkState) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()

        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {                             //hadExtraRow is true and hasExtraRow false
                notifyItemRemoved(super.getItemCount())    //remove the progressbar at the end
            } else {                                       //hasExtraRow is true and hadExtraRow false
                notifyItemInserted(super.getItemCount())   //add the progressbar at the end
            }
        } else if (hasExtraRow && previousState != newNetworkState) { //hasExtraRow is true and hadExtraRow true and (NetworkState.ERROR or NetworkState.ENDOFLIST)
            notifyItemChanged(itemCount - 1)       //add the network message at the end
        }

    }

    private fun hasExtraRow(): Boolean {
        return networkState != null && networkState != NetworkState.LOADED
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            NETWORK_VIEW_TYPE
        } else {
            MOVIE_VIEW_TYPE
        }
    }

}