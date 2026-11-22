"""Upload checks (architecture §11): size first, then the type from the file's own bytes, never its header."""

from app.api.errors import ApiError
from app.models.platform import MAX_BLOB_BYTES

MAX_AUDIO_BYTES = 8 * 1024 * 1024
MAX_PHOTO_BYTES = MAX_BLOB_BYTES


def audio_mime(data: bytes) -> str:
    """webm/opus, ogg/opus or mp4/m4a; anything else is refused."""
    if len(data) > MAX_AUDIO_BYTES:
        raise ApiError(413, "PAYLOAD_TOO_LARGE", "Recording is too long (max about 60 seconds)")
    if data.startswith(b"\x1a\x45\xdf\xa3"):
        return "audio/webm"
    if data.startswith(b"OggS"):
        return "audio/ogg"
    if data[4:8] == b"ftyp":
        return "audio/mp4"
    raise ApiError(415, "UNSUPPORTED_MEDIA_TYPE", "Recording format not supported (use webm, ogg or m4a)")


def jpeg_without_exif(data: bytes) -> bytes:
    """A flyer photo as JPEG with its APP1 segments (EXIF, XMP: GPS, device) removed (architecture §12 APPI).
    shortcut: JPEG only, as the frontend re-encodes every photo to JPEG on a canvas; add PNG if a client needs it."""
    if len(data) > MAX_PHOTO_BYTES:
        raise ApiError(413, "PAYLOAD_TOO_LARGE", "Photo is too large (max 2 MB)")
    unsupported = ApiError(415, "UNSUPPORTED_MEDIA_TYPE", "Photo format not supported (use JPEG)")
    if not data.startswith(b"\xff\xd8"):
        raise unsupported
    out, i = bytearray(b"\xff\xd8"), 2
    while i + 4 <= len(data) and data[i] == 0xFF:
        marker = data[i + 1]
        if marker == 0xDA:  # start of scan: the image data follows, no more metadata segments
            break
        end = i + 2 + int.from_bytes(data[i + 2 : i + 4])
        if end > len(data):
            raise unsupported
        if marker != 0xE1:
            out += data[i:end]
        i = end
    if i + 2 > len(data) or data[i : i + 2] != b"\xff\xda":
        raise unsupported
    return bytes(out + data[i:])
