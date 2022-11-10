package jp.ac.jec.cm0119.beaconsample

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import org.altbeacon.beacon.*

class MainActivity : AppCompatActivity(), RangeNotifier, MonitorNotifier {

        //Beacon の電波が複数存在する場合に検知対象の Beacon を識別するためのもの
    private lateinit var mRegion: Region

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
        setContentView(R.layout.activity_main)

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

        mRegion = Region("iBeacon", Identifier.parse("監視対象のUUID"), null, null)
    }

        //1秒に1回呼び出され、可視ビーコンまでのmDistanceの推定値を与える。
        // beacons - 過去1秒間に観測されたビーコンオブジェクトのコレクション。
        //region - 範囲指定されたビーコンの基準を定義する Region オブジェクト。
    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        TODO("Not yet implemented")
    }


    override fun didEnterRegion(region: Region?) {
        TODO("Not yet implemented")
    }

    override fun didExitRegion(region: Region?) {
        TODO("Not yet implemented")
    }

    override fun didDetermineStateForRegion(state: Int, region: Region?) {
        TODO("Not yet implemented")
    }
}