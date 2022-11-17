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
        //リージョン(監視領域)を設定することで、設置してあるビーコンを検知することができる
        //インスタンス化の際に、UUID, Major, Minor の各々のフォーマットにマッチしないときは例外が吐かれるので対応が必要
        /** 第一引数は必ず識別しになる一意のな文字列を入れること。複数のRegionを設定しても全て同じRegionの扱いになってしまう?**/

        val uuid = try {
            Identifier.parse(Constants.BEACON_UUID)
        } catch (e: Exception) {
            null
        }

        // TODO: nullの時の動作確認、学校のビーコンが多い状況下でも確認する
        mRegion = Region("iBeacon", uuid, null, null)   //uuid(16B(128b)?)

//        mRegion = Region("iBeacon", null, null, null)

        beaconManager = BeaconManager.getInstanceForApplication(getApplication())
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(Constants.IBEACON_FORMAT)) // iBeaconのフォーマット指定
        /** 以下のコードでスキャン感覚を変えられる **/
        beaconManager.foregroundBetweenScanPeriod = 5000;
        beaconManager.backgroundBetweenScanPeriod = 5000;

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
        Log.i("beacon", _beacons.value?.size.toString())
    }

    //ビーコンの検知結果をbeaconsに追加する
    private fun detectionBeacon(beacons: MutableCollection<Beacon>?) {  //beacons　→　検知したすべてのビーコンを表す
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
}