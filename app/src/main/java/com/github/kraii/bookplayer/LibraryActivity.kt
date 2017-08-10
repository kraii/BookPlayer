@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.github.kraii.bookplayer

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
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

    fun libraryRefresh(ui: AnkoContext<LibraryActivity>) {
        info("checking permissions")
        verifyPermissions(this)
        ui.doAsync {
            info("refresh started")
            val newlyScanned: Library = buildLibrary()
            LibraryHolder.updateFrom(newlyScanned)
            LibraryHolder.save(ctx)
            val library = LibraryHolder.get()
            info("library built with ${library.books.size} books")
            activityUiThread {
                toast("Library Updated")
            }

            downloadCovers(library.books) { book ->
                val selectedTitle = LibraryHolder.get().selectedTitle()
                info("selected title = [$selectedTitle] book = [$book]")
                if (selectedTitle == book) {
                    info("Updating display as $book has new cover")
                    activityUiThread {
                        updateDisplay()
                    }
                }
            }
        }
    }

    fun updateDisplay() {
        val selectedTitle = LibraryHolder.get().selectedTitle()
        if (selectedTitle != null) {
            libraryActivityUi.currentBook?.text = "${selectedTitle.author} - ${selectedTitle.title}"
            val bitmap = BitmapFactory.decodeFile(selectedTitle.cover.path)
            if (bitmap != null) {
                libraryActivityUi.bookCover?.imageBitmap = bitmap
            } else {
                libraryActivityUi.bookCover?.imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.questionmark)
            }
            updateChapterDisplay()
        }
    }

    fun updateChapterDisplay() {
        val selectedTitle = LibraryHolder.get().selectedTitle()
        if (selectedTitle != null) {
            libraryActivityUi.currentChapter?.text = "Current Chapter ${selectedTitle.currentChapter + 1} / ${selectedTitle.totalChapters}"
        }
    }

    fun cycleNextBook() {
        LibraryHolder.get().selectNextTitle()
        info("selected title = ${LibraryHolder.get().selectedTitle()}")
        updateDisplay()
    }

    fun cyclePreviousBook() {
        LibraryHolder.get().selectPreviousTitle()
        info("selected title = ${LibraryHolder.get().selectedTitle()}")
        updateDisplay()
    }

    fun toStartOfSelectedBook() {
        val selectedTitle = LibraryHolder.get().selectedTitle()
        alert("Go to start of book", "Are you sure? Place in all chapters will be lost.") {
            yesButton {
                if (selectedTitle != null) {
                    selectedTitle.currentChapterTimestamp = 0
                    selectedTitle.currentChapter = 0
                    updateChapterDisplay()
                    toast("Book position reset")
                }
            }
            noButton {}
        }.show()
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

class LibraryActivityUi : AnkoComponent<LibraryActivity>, AnkoLogger {
    var bookCover: ImageView? = null
    var currentBook: TextView? = null
    var currentChapter: TextView? = null

    override fun createView(ui: AnkoContext<LibraryActivity>) = ui.apply {
        val screenHeight = resources.displayMetrics.heightPixels
        val screenWidth = resources.displayMetrics.widthPixels
        val ID_COVER = 1
        val ID_CURRENT_BOOK = 2
        val ID_CURRENT_CHAPTER = 3
        val ID_REFRESH = 4

        relativeLayout {
            padding = sp(10)
            backgroundColor = Color.parseColor("#0099cc")

            bookCover = imageView {
                id = ID_COVER
                contentDescription = "Le book cover"
            }.lparams(height = dip(screenHeight / 2)) {
                horizontalMargin = dip(5)
                topMargin = dip(10)
                alignParentTop()
            }

            currentBook = textView("Unknown Book") {
                id = ID_CURRENT_BOOK
                textSize = 30f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }.lparams(width = screenWidth / 2) {
                below(ID_COVER)
                centerHorizontally()
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            imageButton(R.drawable.ic_keyboard_arrow_left_black_36dp) {
                onClick { owner.cyclePreviousBook() }
            }.lparams {
                below(ID_COVER)
                leftOf(ID_CURRENT_BOOK)
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            imageButton(R.drawable.ic_keyboard_arrow_right_black_36dp) {
                onClick { owner.cycleNextBook() }
            }.lparams {
                below(ID_COVER)
                rightOf(ID_CURRENT_BOOK)
                horizontalMargin = dip(5)
                topMargin = dip(10)
            }

            currentChapter = textView("Current Chapter: - / -") {
                id = ID_CURRENT_CHAPTER
                textSize = 30f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }.lparams(width = screenWidth / 2) {
                below(ID_CURRENT_BOOK)
                centerHorizontally()
                horizontalMargin = dip(5)
                topMargin = dip(150)
            }

            imageButton(R.drawable.ic_keyboard_arrow_left_black_36dp) {
                onClick { owner.previousChapter() }
            }.lparams {
                below(ID_CURRENT_BOOK)
                leftOf(ID_CURRENT_CHAPTER)
                horizontalMargin = dip(5)
                topMargin = dip(150)
            }

            imageButton(R.drawable.ic_keyboard_arrow_right_black_36dp) {
                onClick { owner.nextChapter() }
            }.lparams {
                below(ID_CURRENT_BOOK)
                rightOf(ID_CURRENT_CHAPTER)
                horizontalMargin = dip(5)
                topMargin = dip(150)
            }

            button("Refresh Library") {
                onClick { owner.libraryRefresh(ui) }
                textSize = 30f
                id = ID_REFRESH
            }.lparams(width = wrapContent) {
                below(ID_CURRENT_CHAPTER)
                centerHorizontally()
                horizontalMargin = dip(5)
                topMargin = dip(80)
            }

            button("Back to start of book") {
                onClick { owner.toStartOfSelectedBook() }
            }.lparams(width = wrapContent) {
                below(ID_REFRESH)
                centerHorizontally()
                horizontalMargin = dip(5)
                topMargin = dip(20)
            }
        }
    }.view
}
