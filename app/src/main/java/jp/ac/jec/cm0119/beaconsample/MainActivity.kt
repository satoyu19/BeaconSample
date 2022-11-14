package jp.ac.jec.cm0119.beaconsample

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import jp.ac.jec.cm0119.beaconsample.databinding.ActivityMainBinding
import org.altbeacon.beacon.*

class MainActivity : AppCompatActivity(), RangeNotifier, MonitorNotifier {

        //Beacon の電波が複数存在する場合に検知対象の Beacon を識別するためのもの
    private lateinit var mRegion: Region

    private lateinit var binding: ActivityMainBinding

        //Beacon検知で利用
    private lateinit var beaconManager: BeaconManager
    private val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"

    //呼び出し元の結果を受けて処理を行う
    private val permissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
        Log.d("permission result", result.toString())

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
                Toast.makeText(this, "${perm}が許可されました。", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "権限の許可を行なってください", Toast.LENGTH_SHORT).show()
            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

            //権限リクエスト
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {    //SDKバージョンが31以下の場合
            permissionResult.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        } else {
            permissionResult.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ))
        }

            //リージョン(監視領域)を設定することで、設置してあるビーコンを検知することができる
        //インスタンス化の際に、UUID, Major, Minor の各々のフォーマットにマッチしないときは例外が吐かれるので対応が必要
            /** 第一引数は必ず識別しになる一意のな文字列を入れること。複数のRegionを設定しても全て同じRegionの扱いになってしまう?**/
        // TODO: ビーコンのUUIDを入力する
        mRegion = Region("iBeacon", Identifier.parse("20011478-2000-0124-1111-003033637761"), null, null)
//        mRegion = Region("iBeacon", null, null, null)

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT)) // iBeaconのフォーマット指定
            //以下のコードでスキャン感覚を変えられる
//        beaconManager.foregroundBetweenScanPeriod = 1000;
//        beaconManager.backgroundBetweenScanPeriod = 1000;

            //モニタリング、リージングの開始
        binding.startBtn.setOnClickListener {
                //BeaconService がある地域のビーコンを見たり、見なくなったりするたびに呼び出すべきクラスを指定
                // 登録の解除はremoveMonitoreNotifier
            beaconManager.addMonitorNotifier(this)  //引数→登録するMonitorNotifier

                //BeaconServiceがレンジングデータを取得するたびに呼び出されるクラスを指定
                //登録解除は、(@link #removeRangeNotifier)
            beaconManager.addRangeNotifier(this)    //引数→登録されるRangeNotifier

                //BeaconService に、渡された Region オブジェクトに一致するビーコンの探索を開始するように指示する
            beaconManager.startMonitoring(mRegion)

            beaconManager.startRangingBeacons(mRegion)
        }

            //終了
        binding.stopBtn.setOnClickListener {
            beaconManager.stopMonitoring(mRegion)
            beaconManager.stopRangingBeacons(mRegion)
        }

    }

        //1秒に1回呼び出され、可視ビーコンまでのmDistanceの推定値を与える。
        // beacons - 過去1秒間に観測されたビーコンオブジェクトのコレクション。
        //region - 範囲指定されたビーコンの基準を定義する Region オブジェクト。
    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
            // 検知したBeaconの情報
            Log.d("MainActivity", "beacons.size ${beacons?.size}")
            beacons?.let {
                for (beacon in beacons) {
                    Log.d("MainActivity", "UUID: ${beacon.id1}, major: ${beacon.id2}, minor: ${beacon.id3}, RSSI: ${beacon.rssi}, TxPower: ${beacon.txPower}, Distance: ${beacon.distance}")
                }
            }
    }

        //リージョン内の少なくとも一つのビーコンが表示されている時に呼び出される(region → 探すべきビーコンの基準を定義するリージョン)
    override fun didEnterRegion(region: Region?) {
        //領域への入場を検知
            Log.d("iBeacon", "Enter Region ${region?.uniqueId}")
        }

        //リージョン内のビーコンが一つも表示されない時に呼び出される
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
}