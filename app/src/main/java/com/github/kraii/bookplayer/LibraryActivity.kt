@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.github.kraii.bookplayer

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class LibraryActivity : AppCompatActivity(), AnkoLogger {

    private val libraryActivityUi = LibraryActivityUi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryActivityUi.setContentView(this)
        updateDisplay()
    }

    fun libraryRefresh() {
        info("checking permissions")
        verifyPermissions(this)
        info("refresh started")
        val newlyScanned : Library = buildLibrary()
        LibraryHolder.updateFrom(newlyScanned)
        LibraryHolder.save(ctx)
        val library = LibraryHolder.get()
        info("library built $library")
        downloadCovers(library.books) { book ->
            if(library.selectedTitle() == book) {
                info("Updating display as $book has new cover")
                updateDisplay()
            }
        }
    }

    fun updateDisplay() {
        val selectedTitle = LibraryHolder.get().selectedTitle()
        if(selectedTitle != null) {
            libraryActivityUi.bookCover?.imageBitmap = BitmapFactory.decodeFile(selectedTitle.cover?.path)
            updateChapterDisplay()
        }
    }

    fun updateChapterDisplay() {
        val selectedTitle = LibraryHolder.get().selectedTitle()
        if(selectedTitle != null) {
            libraryActivityUi.currentChapter?.text = "Current Chapter ${selectedTitle.currentChapter + 1} / ${selectedTitle.totalChapters}"
        }
    }

    fun cycleNextBook() {
        LibraryHolder.get().selectNextTitle()
        updateDisplay()
    }

    fun cyclePreviousBook() {
        LibraryHolder.get().selectPreviousTitle()
        updateDisplay()
    }

    fun toStartOfSelectedBook() {
        val selectedTitle = LibraryHolder.get().selectedTitle()
        if(selectedTitle != null) {
            selectedTitle.currentChapterTimestamp = 0
            selectedTitle.currentChapter = 0
            updateChapterDisplay()
        }
    }

    fun nextChapter() {
        LibraryHolder.get().selectNextChapter()
        updateChapterDisplay()
    }

    fun previousChapter() {
        LibraryHolder.get().selectPreviousChapter()
        updateChapterDisplay()
    }
}

class LibraryActivityUi : AnkoComponent<LibraryActivity> {
    var bookCover : ImageView? = null
    var currentChapter : TextView? = null

    override fun createView(ui: AnkoContext<LibraryActivity>) = ui.apply {
        verticalLayout {
            backgroundColor = Color.parseColor("#0099cc")

            button("Refresh Library") {
                onClick { owner.libraryRefresh() }
                textSize = 30f
            }.lparams(width = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            button("Next Book") {
                onClick { owner.cycleNextBook() }
                textSize = 30f
            }.lparams(width = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            button("Previous Book") {
                onClick { owner.cyclePreviousBook() }
                textSize = 30f
            }.lparams(width = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            button("Next Chapter") {
                onClick { owner.nextChapter() }
                textSize = 30f
            }.lparams(width = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            button("Previous Chapter") {
                onClick { owner.previousChapter() }
                textSize = 30f
            }.lparams(width = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            button("Back to start of book") {
                onClick { owner.toStartOfSelectedBook() }
            }.lparams(width = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            bookCover = imageView {
                contentDescription = "Le book cover"
            }.lparams(width = wrapContent, height = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            currentChapter = textView("Current Chapter: - / -"){
                textSize = 30f
            }.lparams(width = wrapContent, height = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }
        }
    }.view
}
