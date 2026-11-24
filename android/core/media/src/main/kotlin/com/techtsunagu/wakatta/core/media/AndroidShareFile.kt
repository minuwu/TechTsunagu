package com.techtsunagu.wakatta.core.media

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.techtsunagu.wakatta.core.model.ShareFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/** System share sheet. The file must be under the app cache or files directory (`media_file_paths.xml`). */
class AndroidShareFile @Inject constructor(@ApplicationContext private val context: Context) : ShareFile {
    override fun share(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.media.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND)
            .setType(mimeType)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        send.clipData = ClipData.newRawUri(file.name, uri)
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
