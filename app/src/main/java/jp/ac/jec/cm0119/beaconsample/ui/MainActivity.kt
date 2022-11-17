package jp.ac.jec.cm0119.beaconsample.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.beaconsample.adapters.BeaconState
import jp.ac.jec.cm0119.beaconsample.adapters.BeaconsAdapter
import jp.ac.jec.cm0119.beaconsample.databinding.ActivityMainBinding
import jp.ac.jec.cm0119.beaconsample.util.Constants.Companion.BEACON_UUID
import jp.ac.jec.cm0119.beaconsample.util.Constants.Companion.IBEACON_FORMAT
import jp.ac.jec.cm0119.beaconsample.viewmodel.MainViewModel
import org.altbeacon.beacon.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /**
     *バックグラウンド処理のための追加
     */
    private val TAG: String = MainActivity::class.java.simpleName
    companion object {
        fun createIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        // TODO: 別の場所で更新する？
        binding.beaconsList.adapter = mAdapter
        binding.beaconsList.layoutManager = LinearLayoutManager(this)
        binding.mainViewModel = mainViewModel
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

        mainViewModel.setUpBeacon()
    }


}