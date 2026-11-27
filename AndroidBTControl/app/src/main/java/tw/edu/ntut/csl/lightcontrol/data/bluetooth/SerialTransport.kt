package tw.edu.ntut.csl.lightcontrol.data.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import tw.edu.ntut.csl.lightcontrol.model.BtDeviceUiModel

interface SerialSession {
    val incomingBytes: Flow<ByteArray>
    suspend fun write(bytes: ByteArray)
    fun close()
}

interface BluetoothDeviceController {
    val isHardwareSupported: Boolean
    val isAdapterEnabled: StateFlow<Boolean>
    val isScanning: StateFlow<Boolean>
    val pairedDevices: StateFlow<List<BtDeviceUiModel>>
    val discoveredDevices: StateFlow<List<BtDeviceUiModel>>

    fun hasConnectPermission(): Boolean
    fun hasScanPermission(): Boolean
    fun refreshAdapterAndDevices()
    fun startScan(): Boolean
    fun stopScan()
    fun initiatePairing(address: String): Boolean

    suspend fun openRfcommSession(
        device: BtDeviceUiModel,
        allowInsecureFallback: Boolean,
    ): SerialSession

    fun release()
}
