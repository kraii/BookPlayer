package com.github.kraii.bookplayer

import android.content.Context
import com.squareup.moshi.*
import java.io.File

internal class FileJsonAdapter : JsonAdapter<File>() {
    override fun toJson(writer: JsonWriter, value: File?) {
        writer.value(value?.path)
    }

    override fun fromJson(reader: JsonReader): File? {
        val path = reader.nextString()
        if (path == null) {
            return null
        } else {
            return File(path)
        }
    }

}

class LibraryRepository {
    val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(File::class.java, FileJsonAdapter())
            .build()
    val libraryAdapter: JsonAdapter<Library> = moshi.adapter(Library::class.java)

    internal fun toJson(library: Library): String {
        return libraryAdapter.toJson(library)
    }

    internal fun fromJson(json: String): Library {
        return libraryAdapter.fromJson(json) ?: emptyLibrary()
    }

    fun save(context: Context, library: Library) {
        libraryFile(context).writeText(toJson(library))
    }

    private fun libraryFile(context: Context) = File(context.filesDir, "library.json")

    fun load(context: Context): Library {
        val libraryFile = libraryFile(context)
        if (libraryFile.exists()) {
            return fromJson(libraryFile.readText())
        } else {
            return emptyLibrary()
        }
    }
}