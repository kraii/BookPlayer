package com.github.kraii.bookplayer

import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainPlayerActivity : AppCompatActivity() {
    private val mainUi = MainPlayerActivityUi()
    private val mediaPlayer: MediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainUi.setContentView(this)

        verifyPermissions(this)
        LibraryHolder.load(ctx)
        loadCurrentlySelectedBook()
        mediaPlayer.setOnErrorListener({ _, _, _ ->
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
            mainUi.currentBook?.text = "${selectedTitle.author} - ${selectedTitle.title}"
            if (selectedTitle.cover.exists()) {
                mainUi.bookCover?.setImageBitmap(BitmapFactory.decodeFile(selectedTitle.cover.path))
            } else {
                mainUi.bookCover?.imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.questionmark)
            }
        }
    }

    internal fun play() {
        mediaPlayer.start()
        showPause()
    }

    private fun showPause() {
        mainUi.play?.visibility = GONE
        mainUi.pause?.visibility = VISIBLE
    }

    internal fun pause() {
        mediaPlayer.pause()
        showPlay()
    }


    private fun showPlay() {
        mainUi.play?.visibility = VISIBLE
        mainUi.pause?.visibility = GONE
    }

    internal fun openLibrary() {
        if (mediaPlayer.isPlaying) {
            pause()
        }
        startActivity<LibraryActivity>()
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        LibraryHolder.save(ctx)
        super.onDestroy()
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
        if (mediaPlayer.isPlaying) {
            showPause()
        } else {
            showPlay()
        }
    }

    companion object {
        private val LOG_TAG = "MainActivity"
    }
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class MainPlayerActivityUi : AnkoComponent<MainPlayerActivity>, AnkoLogger {
    var bookCover: ImageView? = null
    var currentBook: TextView? = null
    var play: ImageButton? = null
    var pause: ImageButton? = null

    override fun createView(ui: AnkoContext<MainPlayerActivity>) = ui.apply {
        val screenHeight = resources.displayMetrics.heightPixels
        val screenWidth = resources.displayMetrics.widthPixels

        linearLayout {
            orientation = LinearLayout.VERTICAL
            padding = sp(10)
            backgroundColor = Color.parseColor("#0099cc")
            gravity = Gravity.CENTER

            play = imageButton(R.drawable.ic_play_circle_filled_black_48dp) {
                onClick { owner.play() }
            }.lparams(height = screenHeight / 3, width = screenWidth / 2)

            pause = imageButton(R.drawable.ic_pause_circle_filled_black_48dp) {
                onClick { owner.pause() }
            }.lparams(height = screenHeight / 3, width = screenWidth / 2)

            pause?.visibility = GONE

            bookCover = imageView {
                contentDescription = "Le book cover"
            }.lparams(height = dip(screenHeight / 3)) {
                topMargin = dip(10)
            }

            currentBook = textView("Unknown Book") {
                textSize = 30f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }.lparams(width = screenWidth / 2) {
                topMargin = dip(10)
                weight = 10f
            }

            button("Library") {
                textSize = 25f
                onClick { owner.openLibrary() }
            }.lparams {
                topMargin = dip(30)
            }
        }
    }.view
}

