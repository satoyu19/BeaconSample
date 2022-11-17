package jp.ac.jec.cm0119.beaconsample.viewmodel

import android.Manifest
import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.beaconsample.adapters.BeaconState
import jp.ac.jec.cm0119.beaconsample.adapters.BeaconsAdapter
import jp.ac.jec.cm0119.beaconsample.databinding.ActivityMainBinding
import jp.ac.jec.cm0119.beaconsample.util.Constants
import org.altbeacon.beacon.*
import org.altbeacon.bluetooth.BluetoothMedic
import javax.inject.Inject

/**
 * LiveDataでListの監視をする際に、add等では通知されない。どうすれば良い？
 * (発火点はpostValue，setValueを実施した時)
 */
@HiltViewModel
class MainViewModel @Inject constructor(application: Application): AndroidViewModel(application), RangeNotifier, MonitorNotifier {

    //Beacon の電波が複数存在する場合に検知対象の Beacon を識別するためのもの
    private lateinit var mRegion: Region

    //取得したビーコンの情報群
    private var _beacons = MutableLiveData<MutableList<BeaconState>>(mutableListOf())
    val beacons : LiveData<MutableList<BeaconState>>
    get() = _beacons

    private var tempBeacons = mutableListOf<BeaconState>()

    //Beacon検知で利用
    lateinit var beaconManager: BeaconManager

    //権限リクエストの確認
    fun checkPermission(result: Map<String, Boolean>){
        //permission(権限名), isGrant(有効 or 無効)
        result.forEach { (permission, isGrant) ->
            val perm = when (permission) {
                Manifest.permission.ACCESS_FINE_LOCATION -> "位置情報の権限"
                Manifest.permission.CAMERA -> "カメラの権限"
                Manifest.permission.BLUETOOTH_SCAN -> "bluetoothの検出権限"
                Manifest.permission.BLUETOOTH_CONNECT -> "bluetoothの権限"
                else -> "その他の権限"
            }
            if (isGrant) {
                Toast.makeText(getApplication(), "${perm}が許可されました。", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getApplication(), "権限の許可を行なってください", Toast.LENGTH_SHORT).show()
            }

        }
    }

    //ビーコンのセットアップ
    fun setUpBeacon() {

        //uuidの例外処理
        val uuid = try {
            Identifier.parse(Constants.BEACON_UUID)
        } catch (e: Exception) {
            null
        }

        //リージョン(監視領域)を設定することで、設置してあるビーコンを検知することができる
        //インスタンス化の際に、UUID, Major, Minor の各々のフォーマットにマッチしないときは例外が吐かれるので対応が必要
        /** 第一引数は必ず識別しになる一意のな文字列を入れること。複数のRegionを設定しても全て同じRegionの扱いになってしまう?**/
        // TODO: nullの時の動作確認、学校のビーコンが多い状況下でも確認する →　Answer:学校等のビーコンが多い状況下では複数検出される
        mRegion = Region("iBeacon", uuid, null, null)   //uuid(16B(128b)?)

//        mRegion = Region("iBeacon", null, null, null)

        beaconManager = BeaconManager.getInstanceForApplication(getApplication())
        // デバッグを有効にすると、ライブラリからLogcatに多くの詳細なデバッグ情報が送信。トラブルシューティングに有効。
        // BeaconManager.setDebug(true)

        //bluetoothの問題を監視するコード
        // BluetoothMedic.getInstance().enablePowerCycleOnFailures(this)
        // BluetoothMedic.getInstance().enablePeriodicTests(this, BluetoothMedic.SCAN_TEST + BluetoothMedic.TRANSMIT_TEST)

        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(Constants.IBEACON_FORMAT)) // iBeaconのフォーマット指定
        /** 以下のコードでスキャン感覚を変えられる(Long型) **/
        beaconManager.foregroundBetweenScanPeriod = 5000
        beaconManager.backgroundBetweenScanPeriod = 5000
        beaconManager.foregroundScanPeriod = 1100

        //デフォルトでは、ライブラリはAndroid 4-7で5分ごとにバックグラウンドでスキャン。
        // Android 8+では、15分ごとにスケジュールされたスキャンジョブに制限される。
        // より頻繁にスキャンを行いたい場合 (Android 8+ ではフォアグラウンドサービスが必要)。
        //Android 8+ では、ライブラリ内蔵のフォアグラウンドサービスを使用して、この動作を解除することができます。
        //setupForegroundService()
        //beaconManager.setEnableScheduledScanJobs(false);
        //beaconManager.setBackgroundBetweenScanPeriod(0);
        //beaconManager.setBackgroundScanPeriod(1100);

        //ビーコンが検出されない場合、レンジングコールバックはドロップアウトします
        // beaconManager.setIntentScanningStrategyEnabled(true)


    }

    //開始ボタンの挙動
    fun actionStartBtn() {
        //BeaconService がある地域のビーコンを見たり、見なくなったりするたびに呼び出すべきクラスを指定
        // 登録の解除はremoveMonitoreNotifier
        beaconManager.addMonitorNotifier(this)  //引数→登録するMonitorNotifier

        //BeaconServiceがレンジングデータを取得するたびに呼び出されるクラスを指定
        //登録解除は、(@link #removeRangeNotifier)
        beaconManager.addRangeNotifier(this)    //引数→登録されるRangeNotifier

        //BeaconService に、渡された Region オブジェクトに一致するビーコンの探索を開始するように指示する
        beaconManager.startMonitoring(mRegion)
        beaconManager.startRangingBeacons(mRegion)

        beacons.value?.clear()
    }

    //停止ボタンの挙動
    fun actionStopBtn() {
        beaconManager.stopMonitoring(mRegion)
        beaconManager.stopRangingBeacons(mRegion)
    }

    //ビーコンの検知結果をbeaconsに追加する
    private fun detectionBeacon(beacons: MutableCollection<Beacon>?) {  //beacons　→　検知したすべてのビーコンを表す
        Log.i("Test", "実行中")
        //つまり、ビーコンのuuidを指定しているため、一秒に一回固定のビーコンの情報が取れる
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
//                if (this._beacons.value == null) {
//                    _beacons.value = mutableListOf(beaconState)
//                } else {
//                    _beacons.value!!.add(beaconState)
//                    Log.i("Test", _beacons.value!!.size.toString())
//                }
                tempBeacons.add(beaconState)
            }
            _beacons.value = tempBeacons
        }
    }

    //1秒に1回呼び出され、可視ビーコンまでのmDistanceの推定値を与える。
    // beacons - 過去1秒間に観測されたビーコンオブジェクトのコレクション。
    //region - 範囲指定されたビーコンの基準を定義する Region オブジェクト。
    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        detectionBeacon(beacons)
    }

    //リージョン内の少なくとも一つのビーコンが表示されている時に呼び出される(region → 探すべきビーコンの基準を定義するリージョン)
    //本番ではここで領域の入場を検知して、そのことをboolean等で記録しておく？
    override fun didEnterRegion(region: Region?) {
        //領域への入場を検知
        Log.d("iBeacon", "Enter Region ${region?.uniqueId}")
    }

    //リージョン内のビーコンが一つも表示されない時に呼び出される
    //本番ではdidEnterRegionの逆をする？

    override fun didExitRegion(region: Region?) {
        //領域からの退場を検知
        Log.d("iBeacon", "Exit Region ${region?.uniqueId}")
    }

    //didEnterRegionとdidExitRegionのコールバックと同じであり、stateがどちらかによって変わる感じ？
    //state → MonitorNotifier.INSIDE or MonitorNotifier.OUTSIDE
    override fun didDetermineStateForRegion(state: Int, region: Region?) {
        //領域への入退場のステータス変化を検知（INSIDE: 1, OUTSIDE: 0）
        Log.d("MainActivity", "Determine State: $state")
    }

    //fun setupForegroundService() {
    //        val builder = Notification.Builder(this, "BeaconReferenceApp")
    //        builder.setSmallIcon(R.drawable.ic_launcher_background)
    //        builder.setContentTitle("Scanning for Beacons")
    //        val intent = Intent(this, MainActivity::class.java)
    //        val pendingIntent = PendingIntent.getActivity(
    //                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
    //        )
    //        builder.setContentIntent(pendingIntent);
    //        val channel =  NotificationChannel("beacon-ref-notification-id",
    //            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
    //        channel.setDescription("My Notification Channel Description")
    //        val notificationManager =  getSystemService(
    //                Context.NOTIFICATION_SERVICE) as NotificationManager
    //        notificationManager.createNotificationChannel(channel);
    //        builder.setChannelId(channel.getId());
    //        BeaconManager.getInstanceForApplication(this).enableForegroundServiceScanning(builder.build(), 456);
    //    }
}