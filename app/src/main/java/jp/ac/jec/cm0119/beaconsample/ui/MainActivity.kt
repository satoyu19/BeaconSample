package jp.ac.jec.cm0119.beaconsample.ui

import android.Manifest
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.beaconsample.adapters.BeaconsAdapter
import jp.ac.jec.cm0119.beaconsample.databinding.ActivityMainBinding
import jp.ac.jec.cm0119.beaconsample.viewmodel.MainViewModel
import org.altbeacon.beacon.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    //呼び出し元の結果を受けて処理を行う
    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
        { result: Map<String, Boolean> ->
            //permission(権限名), isGrant(有効 or 無効)
           mainViewModel.checkPermission(result)
        }

    private val mAdapter by lazy { BeaconsAdapter() }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.beaconsList.adapter = mAdapter
        binding.beaconsList.layoutManager = LinearLayoutManager(this)
        binding.mainViewModel = mainViewModel

        // TODO: クラス実装でのビーコン検知の場合はコメントアウト
        //ビーコンデータのLiveDataオブザーバーを設定
        val regionViewModel = BeaconManager.getInstanceForApplication(this).getRegionViewModel(mainViewModel.mRegion)
        //オブザーバは、監視しているregionStateが変化するたびに呼び出される（insideとoutsideの2種類）。
        regionViewModel.regionState.observe(this, monitoringObserver)
        regionViewModel.rangedBeacons.observe(this, rangingObserver)

        val view = binding.root
        setContentView(view)

        //権限リクエスト
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {    //SDKバージョンが31以下の場合
            permissionResult.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            permissionResult.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }

        //ビーコン情報群の変化を監視、recyclerViewの更新
        mainViewModel.beacons.observe(this) {
            mAdapter.setItem(it)
        }

    val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mainViewModel.setUpBeacon()

        // TODO: フォアグラウンドサービスでビーコン開始をするとANRが発生する
        mainViewModel.setupForegroundService(manager)   //フォアグラウンドサービスの開始
    }

    var alertDialog: AlertDialog? = null

    private val monitoringObserver = Observer<Int> { state ->
        var dialogTitle = "ビーコン検出"
        var dialogMessage = "didEnterRegionEvent が発生しました。"
        var stateString = "inside"
        if (state == MonitorNotifier.OUTSIDE) { //ビーコン圏外の場合
            dialogTitle = "ビーコンが検出されない"
            dialogMessage = "didExitRegionEventが発生しました。"
            stateString = "outside"
        }
        else {
            Log.i("MainActivity", "ビーコン圏内")
        }
        Log.d("MainActivity", "monitoring state changed to : $stateString")
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(dialogTitle)
        builder.setMessage(dialogMessage)
        builder.setPositiveButton(android.R.string.ok, null)
        alertDialog?.dismiss()
        alertDialog = builder.create()
        alertDialog?.show()
    }

    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        if (mainViewModel.beaconManager.rangedRegions.isNotEmpty()) { //ビーコンが一つ以上検出されている場合
            Log.i("MainActivity","レンジング有効: ${beacons.count()} 個のビーコンが検出されました")
            mainViewModel.detectionBeacon(beacons as MutableCollection<Beacon>?)
        }
    }
}