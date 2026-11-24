package com.techtsunagu.wakatta.core.media

sealed interface QrPayload {
    data class MachineSticker(
        val host: String,
        val machineId: String,
        val shopCode: String
    ) : QrPayload

    data class MyQr(
        val token: String
    ) : QrPayload

    companion object {
        fun parse(raw: String): QrPayload? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return null

            // My QR form: wk:<opaque token>
            // No user id ever; strictly opaque token
            if (trimmed.startsWith("wk:")) {
                val token = trimmed.substring(3).trim()
                return if (token.isNotEmpty() && !token.contains(' ') && !token.contains('/') && !token.contains(':')) {
                    MyQr(token)
                } else {
                    null
                }
            }

            // Machine sticker form: https://[HOST]/m/{machineId}?s={shopCode}
            if (trimmed.startsWith("https://")) {
                val withoutScheme = trimmed.substring(8)
                val slashIndex = withoutScheme.indexOf('/')
                if (slashIndex <= 0) return null
                val host = withoutScheme.substring(0, slashIndex)
                if (host.isEmpty() || host.contains(' ')) return null

                val remainder = withoutScheme.substring(slashIndex)
                val questionIndex = remainder.indexOf('?')
                if (questionIndex <= 0) return null

                val path = remainder.substring(0, questionIndex)
                val query = remainder.substring(questionIndex + 1)

                if (!path.startsWith("/m/")) return null
                val machineId = path.substring(3)
                if (machineId.isEmpty() || machineId.contains('/') || machineId.contains(' ')) return null

                val params = query.split('&').associate { param ->
                    val parts = param.split('=', limit = 2)
                    if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
                }

                // Strictly require query param 's' and reject any user id or foreign params
                val shopCode = params["s"]
                if (params.size == 1 && !shopCode.isNullOrEmpty() && !shopCode.contains(' ')) {
                    return MachineSticker(host = host, machineId = machineId, shopCode = shopCode)
                }
            }

            return null
        }
    }
}
