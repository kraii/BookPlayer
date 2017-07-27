package com.github.kraii.bookplayer

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class LibraryActivity : AppCompatActivity(), AnkoLogger {

    private val libraryActivityUi = LibraryActivityUi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryActivityUi.setContentView(this)
        updateCover()
    }

    fun libraryRefresh() {
        info("refresh started")
        val newlyScanned : Library = buildLibrary()
        LibraryHolder.updateFrom(newlyScanned)
        LibraryHolder.save(ctx)
        info("library built ${LibraryHolder.get()}")
    }


    fun updateCover() {
        val selectedTitle = LibraryHolder.get().selectedTitle()
        if(selectedTitle?.cover?.exists() ?: false) {
            libraryActivityUi.bookCover?.setImageBitmap(BitmapFactory.decodeFile(selectedTitle?.cover?.path))
        }
    }

    fun cycleNextBook() {
        LibraryHolder.get().selectNextTitle()
        updateCover()
    }
}

class LibraryActivityUi : AnkoComponent<LibraryActivity> {

    var bookCover : ImageView? = null

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
            button("Next") {
                onClick { owner.cycleNextBook() }
                textSize = 30f
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
        }
    }.view
}
