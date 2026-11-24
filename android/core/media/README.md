# :core:media

Media utilities, constraints, QR parsing/encoding, and media interfaces for Wakatta.

## Responsibilities
- QR code payload parsing (`QrPayload`): supports Machine Sticker (`https://[HOST]/m/{machineId}?s={shopCode}`) and My QR (`wk:<opaque token>`), strictly rejecting any foreign codes or user IDs.
- QR encoding (`ZxingQrEncoder`): ZXing core 3.5.4 based encoder returning a bitmap-free `QrMatrix` (`BooleanArray` data).
- Photo downscale math (`PhotoDownscale`): Max 2048px, JPEG format, max 2MB file limit calculation.
- Recording limits (`RecordingLimits`): 60 seconds / 8MB limit constants and validations.
- Media contract interfaces (`AudioRecorder`, `PhotoCapture`, `QrScanner`, `QrEncoder`, `Speaker`, `ShareFile`).

CameraX preview and capture implementations will be wired in future work packages.
