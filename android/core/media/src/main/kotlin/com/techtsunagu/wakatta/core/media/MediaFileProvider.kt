package com.techtsunagu.wakatta.core.media

import androidx.core.content.FileProvider

/** A class of its own, because the manifest merger allows each provider class once and `:feature:update` has one too. */
class MediaFileProvider : FileProvider()
