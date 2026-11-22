"""Upload checks (architecture §11): size first, then the type from the file's own bytes, never its header."""

from app.api.errors import ApiError

MAX_AUDIO_BYTES = 8 * 1024 * 1024


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
