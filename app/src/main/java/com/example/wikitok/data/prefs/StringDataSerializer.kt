package com.example.wikitok.data.prefs

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

object StringDataSerializer : Serializer<String> {
    override val defaultValue: String = ""
    override suspend fun readFrom(input: InputStream): String =
        try {
            input.readBytes().toString(Charsets.UTF_8)
        } catch (t: Throwable) {
            throw CorruptionException("Cannot read", t)
        }

    override suspend fun writeTo(t: String, output: OutputStream) {
        output.write(t.toByteArray(Charsets.UTF_8))
    }
}
