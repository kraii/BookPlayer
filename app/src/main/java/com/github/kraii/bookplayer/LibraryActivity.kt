package com.github.kraii.bookplayer

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class LibraryActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LibraryActivityUi().setContentView(this)
    }

    fun libraryRefresh() {
        info("refresh started")
        val newlyScanned : Library = buildLibrary()
        LibraryHolder.updateFrom(newlyScanned)
        LibraryHolder.save(ctx)
        info("library built ${LibraryHolder.get()}")
    }
}

class LibraryActivityUi : AnkoComponent<LibraryActivity> {
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
        }
    }.view
}
