package com.techtsunagu.wakatta.feature.update

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateViewModelTest {

    private class FakeInstaller(var allowed: Boolean = true) : ApkInstaller {
        var installed: File? = null
        override fun canRequestInstalls() = allowed
        override fun install(apk: File) {
            installed = apk
        }
    }

    private class ScriptedDownloader(private val result: DownloadResult) : ApkDownloader {
        var downloadCount = 0
        override suspend fun download(url: String, expectedSha256: String, onProgress: (Int) -> Unit): DownloadResult {
            downloadCount++
            onProgress(50)
            return result
        }
    }

    private class FixedRepo(private val offer: UpdateOffer) : AppConfigRepository {
        override suspend fun fetch() = AppResult.Success(offer)
    }

    private class ErrorRepo(private val error: AppError) : AppConfigRepository {
        override suspend fun fetch() = AppResult.Failure(error)
    }

    private val offer = UpdateOffer("1.3.0", "1.0.0", "https://x/a.apk", "ab", VersionGateResult.Soft)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(
        offer: UpdateOffer = this.offer,
        downloader: ApkDownloader = ScriptedDownloader(DownloadResult.Done(File("a.apk"))),
        installer: FakeInstaller = FakeInstaller(),
    ) = UpdateViewModel(UpdateVersionGate(FixedRepo(offer)), downloader, installer)

    @Test
    fun checkShowsAvailableHardAndUpToDate() {
        val soft = vm().state.value
        assertEquals(UpdatePhase.Available, soft.phase)
        assertTrue(soft.canDownload)
        assertFalse(soft.isHard)
        assertTrue(vm(offer.copy(level = VersionGateResult.Hard)).state.value.isHard)
        assertEquals(UpdatePhase.UpToDate, vm(offer.copy(level = VersionGateResult.UpToDate)).state.value.phase)
    }

    @Test
    fun upToDateWhenVersionsMatch() {
        val v = vm(offer.copy(level = VersionGateResult.UpToDate))
        assertEquals(UpdatePhase.UpToDate, v.state.value.phase)
        assertFalse(v.state.value.isHard)
    }

    @Test
    fun noChecksumMeansNoDownload() {
        val v = vm(offer.copy(sha256 = null))
        assertFalse(v.state.value.canDownload)
        v.onEvent(UpdateEvent.Download)
        assertEquals(UpdateError.NoDownload, v.state.value.error)
    }

    @Test
    fun downloadThenInstall() {
        val installer = FakeInstaller()
        val v = vm(installer = installer)
        v.onEvent(UpdateEvent.Download)
        assertEquals(UpdatePhase.Installing, v.state.value.phase)
        assertEquals(File("a.apk"), installer.installed)
    }

    @Test
    fun wrongChecksumFailsAndNothingIsInstalled() {
        val installer = FakeInstaller()
        val v = vm(downloader = ScriptedDownloader(DownloadResult.ChecksumMismatch), installer = installer)
        v.onEvent(UpdateEvent.Download)
        assertEquals(UpdateError.Checksum, v.state.value.error)
        assertNull(installer.installed)
    }

    @Test
    fun missingInstallPermissionHandsOffToSettingsThenResumes() = runTest {
        val installer = FakeInstaller(allowed = false)
        val v = vm(installer = installer)
        val effects = mutableListOf<UpdateEffect>()
        val job = launch(UnconfinedTestDispatcher()) {
            v.effects.collect { effects.add(it) }
        }
        v.onEvent(UpdateEvent.Download)
        assertTrue(v.state.value.needsInstallPermission)
        assertEquals(listOf(UpdateEffect.OpenInstallSettings), effects)
        assertNull(installer.installed)
        installer.allowed = true
        v.onEvent(UpdateEvent.Resume)
        assertFalse(v.state.value.needsInstallPermission)
        assertEquals(File("a.apk"), installer.installed)
        job.cancel()
    }

    @Test
    fun response426SetsHardInViewModel() {
        val v = UpdateViewModel(
            UpdateVersionGate(ErrorRepo(AppError.UpgradeRequired)),
            ScriptedDownloader(DownloadResult.Failed),
            FakeInstaller(),
        )
        assertTrue(v.state.value.isHard)
        assertEquals(UpdatePhase.Failed, v.state.value.phase)
    }

    @Test
    fun retryReAttemptsDownloadWhenOfferExists() {
        val downloader = ScriptedDownloader(DownloadResult.Failed)
        val v = vm(downloader = downloader)
        v.onEvent(UpdateEvent.Download)
        assertEquals(1, downloader.downloadCount)
        assertEquals(UpdatePhase.Failed, v.state.value.phase)

        v.onEvent(UpdateEvent.Retry)
        assertEquals(2, downloader.downloadCount)
    }
}
