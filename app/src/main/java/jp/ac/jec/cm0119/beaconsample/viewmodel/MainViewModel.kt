package jp.ac.jec.cm0119.beaconsample.viewmodel

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import jp.ac.jec.cm0119.beaconsample.R
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.beaconsample.adapters.BeaconState
import jp.ac.jec.cm0119.beaconsample.ui.MainActivity
import jp.ac.jec.cm0119.beaconsample.util.Constants
import org.altbeacon.beacon.*
import javax.inject.Inject

/**
 * LiveDataでListの監視をする際に、add等では通知されない。どうすれば良い？
 * (発火点はpostValue，setValueを実施した時)
 */
@HiltViewModel
class MainViewModel @Inject constructor(application: Application): AndroidViewModel(application), RangeNotifier, MonitorNotifier {  //モニター：範囲の出入り、 レンジ：動きの検知

    //Beacon の電波が複数存在する場合に検知対象の Beacon を識別するためのもの
    val mRegion by lazy {
        //uuidの例外処理
        val uuid = try {
            Identifier.parse(Constants.BEACON_UUID_HOME)
        } catch (e: Exception) {
            null
        }
        //リージョン(監視領域)を設定することで、設置してあるビーコンを検知することができる
        //インスタンス化の際に、UUID, Major, Minor の各々のフォーマットにマッチしないときは例外が吐かれるので対応が必要
        /** 第一引数は必ず識別しになる一意のな文字列を入れること。複数のRegionを設定しても全て同じRegionの扱いになってしまう?**/
        Region("iBeacon", uuid, null, null)   //uuid(16B(128b)?)
    }

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
//        beaconManager.foregroundScanPeriod = 1100     スキャン時間?

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
        // TODO: ビーコンの実装方法によってコメントイン、コメントアウト
        //BeaconService がある地域のビーコンを見たり、見なくなったりするたびに呼び出すべきクラスを指定
        // 登録の解除はremoveMonitoreNotifier
//        beaconManager.addMonitorNotifier(this)  //引数→登録するMonitorNotifier

        //BeaconServiceがレンジングデータを取得するたびに呼び出されるクラスを指定
        //登録解除は、(@link #removeRangeNotifier)
//        beaconManager.addRangeNotifier(this)    //引数→登録されるRangeNotifier

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
    fun detectionBeacon(beacons: MutableCollection<Beacon>?) {  //beacons　→　検知したすべてのビーコンを表す
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

    /**
     * Android 8.0 以上で通知を配信するには、
     * NotificationChannel のインスタンスを createNotificationChannel() に渡すことにより、アプリの通知チャネルをシステムに登録しておく必要がある
     * (そうしないと通知が表示されない)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setupForegroundService(manager: NotificationManager) {
            if (!beaconManager.isAnyConsumerBound) {    //フォアグラウンドでの検知が開始済みだった場合にアプリが落ちるのを防ぐ
                //(フォアグラウンドで処理を行っていることをユーザーに認識させる)通知を作成
                val channelId = "0"
                val channel = NotificationChannel(channelId, "Beacon service", NotificationManager.IMPORTANCE_HIGH)
                manager.createNotificationChannel(channel)

                val builder = NotificationCompat.Builder(getApplication(), channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Beacon検知中")
                    .setContentText("領域監視を実行しています")

                //PendingIntentを作成
                val intent = Intent(getApplication(), MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(getApplication(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

                builder.setContentIntent(pendingIntent) //通知のクリック時の遷移

                /**
                 * enableForegroundServiceScanning → ビーコンスキャンにフォアグラウンドサービスを使用するようライブラリーを設定
                 * setEnableScheduledScanJobs(true or false)　→ スキャンを実行する際に、長時間稼働する `BeaconService` を使用するのではなく、
                 * `JobScheduler` で実行する `ScanJob` を使用するように設定します。(以下のコードでは無効にしている、)
                 * Android12以降だとアプリがバックグラウンドにある場合、フォアグラウンド サービスを開始することは一般的に禁止されている。
                 * BeaconManagerのbindInternal()でstartForegroundService呼び出している
                 */

                beaconManager.enableForegroundServiceScanning(builder.build(), 3)   //foreground公式よりnotificationIdは0にしてはいけない
                beaconManager.setEnableScheduledScanJobs(false)
//                beaconManager.foregroundBetweenScanPeriod = 5000
//                beaconManager.backgroundBetweenScanPeriod = 5000    //(デフォルト値、3000000(300秒))
//                beaconManager.foregroundScanPeriod = 1100 (1100がデフォルト値)

            }
        }
}