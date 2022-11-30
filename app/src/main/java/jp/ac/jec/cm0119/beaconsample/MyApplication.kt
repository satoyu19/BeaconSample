package jp.ac.jec.cm0119.beaconsample

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import dagger.hilt.android.HiltAndroidApp
import jp.ac.jec.cm0119.beaconsample.adapters.BeaconState
import jp.ac.jec.cm0119.beaconsample.ui.MainActivity
import jp.ac.jec.cm0119.beaconsample.util.Constants
import org.altbeacon.beacon.*

// TODO: シングルトンでBeaconmanagerやチャンネルを管理しとき、アクティビティで呼び出す感じにする 
//@HiltAndroidApp
class MyApplication : Application(), MonitorNotifier, RangeNotifier {
//    class MyApplication : Application() {
//
    //アクティビティーから呼び出して実行させる？
    companion object {
        const val TAG = "MyApplication"
        val mRegion = Region("beacon", Identifier.parse(Constants. BEACON_UUID_SCHOOL), null, null)
        var insideRegion = false
        var stateBeacons = mutableListOf<BeaconState>()
    }

    
    override fun onCreate() {
        super.onCreate()
        val beaconManager = BeaconManager.getInstanceForApplication(this)

        if (!beaconManager.isAnyConsumerBound) {    //フォアグラウンドでの検知が開始済みだった場合にアプリが落ちるのを防ぐ
            //(フォアグラウンドで処理を行っていることをユーザーに認識させる)通知を作成
            val channelId = "0"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Beacon service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Beacon検知中")
                .setContentText("領域監視を実行しています")

            //PendingIntentを作成
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            builder.setContentIntent(pendingIntent) //通知のクリック時の遷移

            beaconManager.enableForegroundServiceScanning(builder.build(), 456);

            beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(Constants.IBEACON_FORMAT)) // iBeaconのフォーマット指定
            beaconManager.setEnableScheduledScanJobs(false)
            beaconManager.foregroundBetweenScanPeriod = 5000
            beaconManager.backgroundBetweenScanPeriod = 5000
            beaconManager.backgroundScanPeriod = 1100

            Log.d(TAG, "setting up background monitoring in app onCreate")
            beaconManager.addMonitorNotifier(this)
            beaconManager.addRangeNotifier(this)

            for (region in beaconManager.monitoredRegions) {
                beaconManager.stopMonitoring(region!!)
            }

            beaconManager.startMonitoring(mRegion)
            beaconManager.startRangingBeacons(mRegion)

        }
    }

    override fun didEnterRegion(region: Region?) {
        Log.d(TAG, "did enter region.")
        insideRegion = true

    }

    override fun didExitRegion(region: Region?) {
        Log.d(TAG, "did exit region.")
        insideRegion = false
    }

    override fun didDetermineStateForRegion(state: Int, region: Region?) {
    }

    //本番ではここでサーバーに記録する
    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        beacons?.let {
            for (beacon in beacons) {   //ここは、常に一つだけと言うことになる
                val beaconState = BeaconState(
                    beacon.id1.toString(),
                    beacon.id2.toString(),
                    beacon.id3.toString(),
                    beacon.rssi.toString(),
                    beacon.txPower.toString(),
                    beacon.distance.toString()
                )
                stateBeacons.add(beaconState)
                Log.d(TAG, stateBeacons.size.toString())
            }
        }
    }
}