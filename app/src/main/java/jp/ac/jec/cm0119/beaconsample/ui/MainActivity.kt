package jp.ac.jec.cm0119.beaconsample.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.beaconsample.R
import jp.ac.jec.cm0119.beaconsample.adapters.BeaconsAdapter
import jp.ac.jec.cm0119.beaconsample.databinding.ActivityMainBinding
import jp.ac.jec.cm0119.beaconsample.util.Constants
import org.altbeacon.beacon.*

//@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }
    //    private lateinit var binding: ActivityMainBinding
//
//    private val mainViewModel: MainViewModel by viewModels()
//    private lateinit var beaconManager: BeaconManager
////
////    //呼び出し元の結果を受けて処理を行う
//    private val permissionResult =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
//        { result: Map<String, Boolean> ->
//            //permission(権限名), isGrant(有効 or 無効)
//           mainViewModel.checkPermission(result)
//        }
////
//    private val mAdapter by lazy { BeaconsAdapter() }
////
////    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//
//        binding.beaconsList.adapter = mAdapter
//        binding.beaconsList.layoutManager = LinearLayoutManager(this)
//        binding.mainViewModel = mainViewModel
//
//        // TODO: クラス実装でのビーコン検知の場合はコメントアウト
//        //ビーコンデータのLiveDataオブザーバーを設定
//        val regionViewModel = BeaconManager.getInstanceForApplication(this).getRegionViewModel(mainViewModel.mRegion)
////        オブザーバは、監視しているregionStateが変化するたびに呼び出される（insideとoutsideの2種類）。
//        regionViewModel.regionState.observe(this, monitoringObserver)
//        regionViewModel.rangedBeacons.observe(this, rangingObserver)
//
//        val view = binding.root
//        setContentView(view)
//
//        //権限リクエスト
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {    //SDKバージョンが31以下の場合
//            permissionResult.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//            )
//        } else {
//            permissionResult.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                )
//            )
//        }
////
//        //ビーコン情報群の変化を監視、recyclerViewの更新
//        mainViewModel.beacons.observe(this) {
//            mAdapter.setItem(it)
//        }
//
//        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        setupForegroundService(manager)   //フォアグラウンドサービスの開始
//
////        mainViewModel.setUpBeacon()
//    }
//
//    var alertDialog: AlertDialog? = null
////
////    // TODO:　アプリを閉じるとOUTSIDEになる？
//    private val monitoringObserver = Observer<Int> { state ->
//        var dialogTitle = "ビーコン検出"
//        var dialogMessage = "didEnterRegionEvent が発生しました。"
//        var stateString = "inside"
//        if (state == MonitorNotifier.OUTSIDE) { //ビーコン圏外の場合
//            dialogTitle = "ビーコンが検出されない"
//            dialogMessage = "didExitRegionEventが発生しました。"
//            stateString = "outside"
//        }
//        else {
//            Log.i("MainActivity", "ビーコン圏内")
//        }
//        val builder =
//            AlertDialog.Builder(this)
//        builder.setTitle(dialogTitle)
//        builder.setMessage(dialogMessage)
//        builder.setPositiveButton(android.R.string.ok, null)
//        alertDialog?.dismiss()
//        alertDialog = builder.create()
//        alertDialog?.show()
//    }
////
//    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->
//        if (mainViewModel.beaconManager.rangedRegions.isNotEmpty()) { //ビーコンが一つ以上検出されている場合
//            mainViewModel.detectionBeacon(beacons as MutableCollection<Beacon>?)
//        }
//    }
//    fun setupForegroundService(manager: NotificationManager) {
//        beaconManager = BeaconManager.getInstanceForApplication(applicationContext)
//        if (!beaconManager.isAnyConsumerBound) {    //フォアグラウンドでの検知が開始済みだった場合にアプリが落ちるのを防ぐ
//            //(フォアグラウンドで処理を行っていることをユーザーに認識させる)通知を作成
//            val channelId = "0"
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(
//                    channelId,
//                    "Beacon service",
//                    NotificationManager.IMPORTANCE_HIGH
//                )
//                manager.createNotificationChannel(channel)
//            }
//
//            val builder = NotificationCompat.Builder(applicationContext, channelId)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("Beacon検知中")
//                .setContentText("領域監視を実行しています")
//
//            //PendingIntentを作成
//            val intent = Intent(applicationContext, MainActivity::class.java)
//            val pendingIntent = PendingIntent.getActivity(getApplication(), 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//            builder.setContentIntent(pendingIntent) //通知のクリック時の遷移
//
//            beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(Constants.IBEACON_FORMAT)) // iBeaconのフォーマット指定
//            beaconManager.foregroundBetweenScanPeriod = 5000
//            beaconManager.foregroundScanPeriod = 1100
//            beaconManager.backgroundBetweenScanPeriod = 5000
//            beaconManager.backgroundScanPeriod = 1100
//
//            /**
//             * enableForegroundServiceScanning → ビーコンスキャンにフォアグラウンドサービスを使用するようライブラリーを設定
//             * setEnableScheduledScanJobs(true or false)　→ スキャンを実行する際に、長時間稼働する `BeaconService` を使用するのではなく、
//             * `JobScheduler` で実行する `ScanJob` を使用するように設定します。(以下のコードでは無効にしている、)
//             * Android12以降だとアプリがバックグラウンドにある場合、フォアグラウンド サービスを開始することは一般的に禁止されている。
//             * BeaconManagerのbindInternal()でstartForegroundService呼び出している
//             */
//            beaconManager.enableForegroundServiceScanning(builder.build(), 1)   //foreground公式よりnotificationIdは0にしてはいけない
//            beaconManager.setEnableScheduledScanJobs(false)
//
//        }
//    }
}