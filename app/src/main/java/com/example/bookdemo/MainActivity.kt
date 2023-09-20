package com.example.bookdemo

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookdemo.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity() {

    private var currentWindow = 0
    private lateinit var binding: ActivityMainBinding
    private lateinit var progressBar: SeekBar
    private lateinit var currentPlayBackTime: TextView
    private lateinit var totalPlaybackTime: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var maximizePlayer: ImageView
    private var isFullscreen = false
    private lateinit var simpleExoplayer: SimpleExoPlayer
    private var playbackPosition: Long = 0
    private  val frames = arrayListOf<Frames>()
    private val mp4Url = "https://content.storymagic.co/2/83452/video_all.m3u8?loc=en_US&userId=218000820&ver=1.0&chapter=0&bookId=83452&dev=web"
       // "https://cdn-gcp.getepic.com/media/media_cat_ninja_book_low.m3u8"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        configPlayerPosition()
        updateSeekBar()
        handleSeekBar()
        playerCLickListeners()

    }

    private fun initUI() {
        progressBar = binding.idExoPlayerVIew.findViewById(R.id.seek_bar)
        currentPlayBackTime = binding.idExoPlayerVIew.findViewById(R.id.textCurrentTime)
        totalPlaybackTime = binding.idExoPlayerVIew.findViewById(R.id.textTotalTime)
        playPauseButton = binding.idExoPlayerVIew.findViewById(R.id.play_pause)
        prevButton = binding.idExoPlayerVIew.findViewById(R.id.previous)
        nextButton = binding.idExoPlayerVIew.findViewById(R.id.next)
        maximizePlayer = binding.idExoPlayerVIew.findViewById(R.id.iv_fullscreen)
    }

    private fun handleSeekBar() {
        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val newPosition: Long = simpleExoplayer.duration * progress / 100
                    simpleExoplayer.seekTo(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // No action needed here
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // No action needed here
            }
        })
    }

    private fun updateSeekBar() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val duration = simpleExoplayer.duration
            val currentPosition = simpleExoplayer.currentPosition

            if (duration > 0) {
                val progress = (currentPosition * 100 / duration).toInt()
                progressBar.progress = progress
            }
            // Update current time and total time labels
            "${formatTime(currentPosition)} / ".also { currentPlayBackTime.text = it }
            totalPlaybackTime.text = formatTime(duration)
            updateSeekBar()
        }, 1000) // Update the seek bar every 1 second
    }

    private fun formatTime(millis: Long): String? {
        var seconds = millis / 1000
        val minutes = seconds / 60
        seconds %= 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun configPlayerPosition() {
        frames.add(Frames(1,0.0,14.00))
        frames.add(Frames(2,15.00,17.00))
        frames.add(Frames(3,18.00,50.00))
        frames.add(Frames(4,51.0,62.00))
        frames.add(Frames(5,64.0,72.00))
        frames.add(Frames(6,73.00,94.00))
        frames.add(Frames(7,95.00,138.00))
        frames.add(Frames(8,139.00,157.00))
        frames.add(Frames(9,158.00,174.00))
        frames.add(Frames(10,175.00,191.00))
        frames.add(Frames(11,192.00,199.00))
        frames.add(Frames(12,200.10,207.00))
        frames.add(Frames(13,208.00,225.00))
        frames.add(Frames(14,226.00,235.00))
        frames.add(Frames(15,236.00,260.00))
        frames.add(Frames(16,261.00,282.00))
        frames.add(Frames(17,283.00,298.00))
        frames.add(Frames(18,299.00,319.00))
        frames.add(Frames(19,320.00,346.00))
        frames.add(Frames(20,347.00,390.00))
        frames.add(Frames(21,391.00,424.00))
        frames.add(Frames(22,425.00,450.00))
        frames.add(Frames(23,451.00,497.00))
        frames.add(Frames(24,498.00,511.00))
        frames.add(Frames(25,512.00,545.00))
        frames.add(Frames(26,547.00,558.00))
    }


    override fun onResume() {
        super.onResume()
        initializePlayer()
        if (playbackPosition.toInt() != 0)
            restorePlaybackState()
        else
            simpleExoplayer.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        savePlaybackState()
        releasePlayer()
    }

    private fun initializePlayer() {
        // Create a DefaultTrackSelector to enable adaptive track selection.
        val trackSelector = DefaultTrackSelector(this, AdaptiveTrackSelection.Factory())
        // Create a DefaultBandwidthMeter to measure network bandwidth.
        val bandwidthMeter = DefaultBandwidthMeter.Builder(this).build()
        val dataSourceFactory =  DefaultHttpDataSource.Factory().setUserAgent("demo")
        val sourceFactory = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(mp4Url))
        simpleExoplayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter)
            .build()
        simpleExoplayer.setMediaSource(sourceFactory)
        simpleExoplayer.prepare()
        binding.idExoPlayerVIew.player = simpleExoplayer


        // Add a listener to hide the loading spinner when the video starts playing
        simpleExoplayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(this@MainActivity, "An error occurred: " + error.message, Toast.LENGTH_SHORT).show()
                playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)

            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY ->
                        // Video is ready to play, hide the loading spinner
                        binding.loadingSpinner.visibility = ProgressBar.GONE

                    Player.STATE_BUFFERING -> {
                        binding.loadingSpinner.visibility = ProgressBar.VISIBLE
                    }

                    Player.STATE_ENDED ->
                        // Video is ready to play, hide the loading spinner
                        playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)
                }

            }
        })

    }



    private fun releasePlayer() {
        playbackPosition = simpleExoplayer.currentPosition
        simpleExoplayer.release()
    }

    private fun playerCLickListeners() {
        // Custom Play/Pause Button
        playPauseButton.setOnClickListener {
            if (simpleExoplayer.isPlaying) {
                simpleExoplayer.pause()
                playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                simpleExoplayer.play()
                playPauseButton.setImageResource(R.drawable.baseline_pause_24)
            }
        }

        // Custom Previous Button
        prevButton.setOnClickListener {
            val currentPositionMs = simpleExoplayer.currentPosition
            val currentPositionSeconds = currentPositionMs.toDouble() / 1000.0
            for (element in frames.reversed()) {
                if (element.startTime < currentPositionSeconds)
                {
                    val milliseconds = (element.startTime * 1000).toLong()
                    simpleExoplayer.seekTo(milliseconds)
                    break
                }
            }
        }

        // Custom Next Button
        nextButton.setOnClickListener {
            val currentPositionMs = simpleExoplayer.currentPosition
            val currentPositionSeconds = currentPositionMs.toDouble() / 1000.0
            for (element in frames) {
                if (element.endTime > currentPositionSeconds)
                {
                    val milliseconds = (element.endTime * 1000).toLong()
                    simpleExoplayer.seekTo(milliseconds)
                    Log.e("demo1",simpleExoplayer.currentPosition.toString())
                    break
                }
            }
            // Handle next track action here
        }

       maximizePlayer.setOnClickListener {
            isFullscreen = !isFullscreen
            toggleFullscreen(isFullscreen)
        }
    }

    private fun toggleFullscreen(fullscreen: Boolean) {
        if (fullscreen) {
            // Enter fullscreen mode
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            // Adjust layout params for fullscreen
            binding.idExoPlayerVIew.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT


        } else {
            // Exit fullscreen mode
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            // Restore original layout params
            binding.idExoPlayerVIew.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    // Save playback position when entering fullscreen
    private fun savePlaybackState() {
        playbackPosition = simpleExoplayer.currentPosition
        currentWindow = simpleExoplayer.currentWindowIndex
    }

    // Restore playback state when exiting fullscreen
    private fun restorePlaybackState() {
        simpleExoplayer.seekTo(currentWindow, playbackPosition)
        simpleExoplayer.pause()
        playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24)

    }


}
