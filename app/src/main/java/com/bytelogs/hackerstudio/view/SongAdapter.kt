package com.bytelogs.hackerstudio.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bytelogs.hackerstudio.AppContants
import com.bytelogs.hackerstudio.R
import com.bytelogs.hackerstudio.model.SongModel
import kotlinx.android.synthetic.main.item_song.view.*


class SongAdapter(var songList: MutableList<SongModel>): RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    public lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapter.SongViewHolder {
        context = parent.context
        val view= LayoutInflater.from(context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongAdapter.SongViewHolder, position: Int) {
        val songModel = songList[position]
        holder.tvName.text = songModel.song
        holder.tvArtist.text = songModel.artists
        Glide.with(context).load(songModel.cover_image).into(holder.ivCover)
        holder.llMain.setOnClickListener(View.OnClickListener {
            val activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity,holder.ivCover,"Cover")
            val intent= Intent(context,PlayerActivity::class.java)
            var bundle = Bundle()
            bundle.putSerializable(AppContants.SONGMODEL,songModel)
            intent.putExtras(bundle)
            context.startActivity(intent,activityOptionsCompat.toBundle())
        })

    }

    override fun getItemCount(): Int {
        return  songList.size
    }

    fun updatePostList(apodList:MutableList<SongModel>){
        this.songList = apodList
        notifyDataSetChanged()
    }

    class SongViewHolder(view: View):RecyclerView.ViewHolder(view){
        var tvName = view.tvName
        var  tvArtist = view.tvArtist
        var  ivCover = view.ivCover
        var  llMain = view.llMain
    }
}