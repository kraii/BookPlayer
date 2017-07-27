package com.github.kraii.bookplayer

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_main_player.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.startActivity

class MainPlayerActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        fullscreen_content!!.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
        fullscreen_content_controls!!.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyStoragePermissions(this)
        setContentView(R.layout.activity_main_player)
        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreen_content!!.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        dummy_button.setOnTouchListener(mDelayHideTouchListener)

        play.setOnClickListener(this::play)
        pause.setOnClickListener(this::pause)
        reset.setOnClickListener(this::reset)
        forwardChapter.setOnClickListener(this::forwardChapter)
        browseLibrary.setOnClickListener(this::openLibrary)
        LibraryHolder.load(ctx)
        loadCurrentlySelectedBook()
        mediaPlayer.setOnErrorListener({_,_,_ ->
            Log.i(LOG_TAG, "Media player error received, resetting")
            mediaPlayer.reset()
            loadCurrentlySelectedBook()
            showPlay()
            true
        })
    }

    private fun loadCurrentlySelectedBook() {
        val selectedTitle = LibraryHolder.get().selectedTitle()
        val selectedChapter = selectedTitle?.currentChapter()
        Log.i(LOG_TAG, "Loading $selectedChapter")

        if (selectedChapter != null && selectedChapter.file.exists()) {
            val uri = Uri.parse(selectedChapter.file.path)
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, uri)
            mediaPlayer.prepare()
            mediaPlayer.seekTo(selectedChapter.currentTimestamp)

            if(selectedTitle.cover?.exists() ?: false) {
                bookCover.setImageBitmap(BitmapFactory.decodeFile(selectedTitle.cover?.path))
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun play(view: View) {
        mediaPlayer.start()
        showPause()
    }

    private fun showPause() {
        play.visibility = GONE
        pause.visibility = VISIBLE
    }

    @Suppress("UNUSED_PARAMETER")
    private fun pause(unused: View) {
        mediaPlayer.pause()
        showPlay()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun reset(view: View) {
        mediaPlayer.seekTo(0)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun forwardChapter(view: View) {
        val newChapter = LibraryHolder.get().selectNextChapter()
        if (newChapter != null) {
            showPlay()
            loadCurrentlySelectedBook()
        }
    }

    private fun showPlay() {
        play.visibility = VISIBLE
        pause.visibility = GONE
    }

    private fun openLibrary(view: View) {
        if(mediaPlayer.isPlaying) {
            pause(view)
        }
        startActivity<LibraryActivity>()
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        LibraryHolder.save(ctx)
        super.onDestroy()
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        fullscreen_content_controls!!.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @SuppressLint("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreen_content!!.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    override fun onPause() {
        super.onPause()
        LibraryHolder.get().updateTimestamp(mediaPlayer.currentPosition)
        LibraryHolder.save(ctx)
    }

    override fun onRestart() {
        super.onRestart()
        LibraryHolder.load(ctx)
        loadCurrentlySelectedBook()
        if(mediaPlayer.isPlaying) {
            showPause()
        } else {
            showPlay()
        }
    }

    /**
     * Schedules a call to hide() in [delayMillis] milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [.AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [.AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300

        private val LOG_TAG = "MainActivity"
    }
}

