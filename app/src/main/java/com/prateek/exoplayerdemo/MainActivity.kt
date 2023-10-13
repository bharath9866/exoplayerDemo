package com.prateek.exoplayerdemo

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.TracksInfo
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverrides
import com.google.android.exoplayer2.upstream.AssetDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Player.Listener {
    private var qualityPopUp: PopupMenu?=null
    private var player: ExoPlayer? = null
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private var playWhenReady = true
    private lateinit var trackSelector:DefaultTrackSelector
    private var qualityList = ArrayList<Pair<String, TrackSelectionOverrides.Builder>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListener()
    }

    private fun initListener() {
        exo_quality.setOnClickListener {
            qualityPopUp?.show()
        }
    }


    private fun initPlayer() {

        // Create a TrackSelector
        trackSelector = DefaultTrackSelector(this)

        // Set the preferred quality to low
        val parameters = trackSelector.buildUponParameters()
        parameters.setMaxVideoBitrate(10)
        trackSelector.setParameters(parameters)

        // Create an ExoPlayer instance
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()


        player?.playWhenReady = true
        player_exo.player = player

        val mediaItemMP4 = MediaItem.fromUri(getString(R.string.mp4_url_two))

        player?.setMediaItems(listOf(mediaItemMP4), mediaItemIndex, playbackPosition)

        player?.seekTo(playbackPosition)
        player?.playWhenReady = playWhenReady
        player?.addListener(this)

        player?.prepare()

    }

    private fun setUpQualityList() {
        qualityPopUp = PopupMenu(this, exo_quality)
        qualityList.let {
            for ((i, videoQuality) in it.withIndex()) {
                qualityPopUp?.menu?.add(0, i, 0, videoQuality.first)
            }
        }
        qualityPopUp?.setOnMenuItemClickListener { menuItem ->
            qualityList[menuItem.itemId].let {
                trackSelector!!.parameters = trackSelector!!.parameters
                    .buildUpon()
                    .setTrackSelectionOverrides(it.second.build())
                    .setTunnelingEnabled(true)
                    .build()
            }
            true
        }
    }

    override fun onTracksInfoChanged(tracksInfo: TracksInfo) {
        Log.d("TRACK_CHANGED", Gson().toJson(tracksInfo.trackGroupInfos))
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState==Player.STATE_READY){
             trackSelector?.generateQualityList()?.let {
                 qualityList = it
                 setUpQualityList()
             }
        }
    }

    private fun releasePlayer() {
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            it.release()
            player = null
        }
    }


    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24) {
            initPlayer()
        }
    }
    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun hideSystemUI() {
        // Hide the system UI for a full-screen experience (for landscape mode).
        val decorView = window.decorView
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    private fun showSystemUI() {
        // Show the system UI for portrait mode.
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}

const val WithTokenURL = "https://il-cms-assets.s3.ap-south-1.amazonaws.com/media/1643292596381.mp4?X-Amz-Security-Token=IQoJb3JpZ2luX2VjEA0aCmFwLXNvdXRoLTEiRzBFAiEA1VJKEld7dPTWzZc1dS2ZVvDfG9xwTv5gSsaq5NxvlrYCIEgUro8NisJA3PeGZZzrveF66CtgLEpEOV2vD4Twe6igKr4FCCYQARoMNzI1NDU2NzgwMjc0IgyDhRep4O3dk3QkfRAqmwUX8wwamnkjM3vtXSFrfDumBFzIlay%2BEW%2BJBcGdNx93CMgjlmh4%2B0TX9xb7FNnMsJu3z8ruKL9%2Fap9bJtcQxrxumvLIj9wM4bVkHUKVBum3oZeR3RnniGlbTsD5%2BIZIVzLWMV9VEya2i2%2Feau9IXs99ykWwY0jhhJNBdXNpYTN64uSokPFY4NZJkv%2FzhOvZYyzb6VHf%2FbsHXgQulHE%2FBHDht8m4cod6dUUcg%2Bp835wrSL7g6v1USE21%2BfQeS2UQ8DeJ7ivfNpY0xkfVsh3CBH3%2BuuF4bwNlRTEDLek7Suy1obuwKARFKrPHj5bljsyyPFZrEBEfGgdYohkkCKDtfy8J9%2FvJR5KV2K4UT%2B4qBqIivpgGt1VU%2FYCNUKvshDtUf9SCRAFFSNn9PPBQRaOoZcNfs088bj6yqgKxLtktmciwrdYGg9pdbYqUNa8nxU7zGjiCBG0OPjDDKlMiROQCeE9nyqbrYD%2FV6Bd2L%2FdFADaKdBDUH2rOyep1ztQdAjupk6EqL%2FqvndjY5fTEfMWywAthEfzc75l27OJilCF70q805DCupdhBsZd6lkn%2Fn%2BlBvXAiav2DTLQ2Msd3Udn0bK6CavFMPSyX56Fgwsn4u3ojN%2FdYLYrF3rGPcRFFLnWKEaUwQfCwdRGwNjG7ebCr4ESqgm35ZMDqvigZ%2BYH1oCJNZfczfURhx8RUFTn3VzZ8qx%2FGAUJDfxkvzFC%2FJWliZiPM9%2BgxduYcYQFKCoIIKTwbpGhq7FAmu5K8CA88aFcImE9wg765KEBUbcdTSr%2FdICyiCokCYs93anJ%2BJ11qFZlO2%2FKpXdjSdii3wuo0HZ7S8wJSDIU%2FmKCrsAPB6ETQaHHHkaJhX9AMLuZCGfCUR4ha46y4v9tkzPNQX4T6MNmco6kGOrEBevLW27idmwgRry%2FNyUb5JkJmqPtmxACfYDhNaaLhRBzkFQPTvaKntppxXvC%2BUzikJbWtw5Zzn9q8PIffWBqCC42PMbnNORVflt5xGC4l3sF6uQZZzhH%2FAIbhTjKuTcLvHmYn6%2BVjivao5ug%2BJdGUn5jMMxkT3P%2BVEnh6eB0EYL2WOjc6Q43g5orliH6TjFXctyLayq%2Fg41fK%2B6O9NGKeoGIi5tL%2BIMCqDUXDdqPIefhf&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20231013T052736Z&X-Amz-SignedHeaders=host&X-Amz-Expires=3600&X-Amz-Credential=ASIA2R2EX7PZBLQXKYCX%2F20231013%2Fap-south-1%2Fs3%2Faws4_request&X-Amz-Signature=dfaa68532ebbfe144e6a540f749d73cf048e33237d22addf9a561cababeb0e87"