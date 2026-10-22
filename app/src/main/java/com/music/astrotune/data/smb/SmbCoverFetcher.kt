package com.music.astrotune.data.smb

import coil.ImageLoader
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceFetchResult
import coil.request.Options
import okio.FileSystem
import okio.buffer
import okio.source

/**
 * Covers off the share for Coil. Reads the whole picture — covers are
 * kilobytes, and listings already cap what can be one — then hands Coil
 * bytes it decodes and caches exactly like any network image.
 *
 * Registered in [BitChordApplication][com.music.astrotune.BitChordApplication]
 * alongside the defaults: anything that isn't `smb://` falls straight
 * through to them.
 */
class SmbCoverFetcher private constructor(private val url: String) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val bytes = SmbClient.readFully(url)
        val mime = when (url.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> null
        }
        return SourceFetchResult(
            source = ImageSource(bytes.inputStream().source().buffer(), FileSystem.SYSTEM),
            mimeType = mime,
            dataSource = coil.decode.DataSource.NETWORK,
        )
    }

    class Factory : Fetcher.Factory<String> {
        override fun create(data: String, options: Options, imageLoader: ImageLoader): Fetcher? =
            if (SmbConfig.isSmbUrl(data)) SmbCoverFetcher(data) else null
    }
}
