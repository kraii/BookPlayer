package com.github.kraii.bookplayer

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class LibraryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LibraryActivityUi().setContentView(this)
    }

    fun libraryRefresh() {
        val newlyScanned : Library = buildLibrary()
        LibraryHolder.updateFrom(newlyScanned)
        LibraryHolder.save(ctx)
    }
}

class LibraryActivityUi : AnkoComponent<LibraryActivity> {
    override fun createView(ui: AnkoContext<LibraryActivity>) = ui.apply {
        verticalLayout {
            backgroundColor = Color.parseColor("#0099cc")

            button("Refresh Library") {
                onClick { owner.libraryRefresh() }
            }
        }
    }.view
}
