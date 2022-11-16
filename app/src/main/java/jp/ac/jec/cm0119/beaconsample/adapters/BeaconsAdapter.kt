package jp.ac.jec.cm0119.beaconsample.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jp.ac.jec.cm0119.beaconsample.databinding.BeaconsStateRowBinding
import org.altbeacon.beacon.Beacon

class BeaconsAdapter(private val beacons: List<BeaconState>): RecyclerView.Adapter<BeaconsAdapter.MyViewHolder>() {

    var beacons = ArrayList<Beacon>()

    class MyViewHolder(private val binding: BeaconsStateRowBinding): RecyclerView.ViewHolder(binding.root) {
        //レイアウトとバインド
        fun bind(beacon: BeaconState) {
            binding.beacon = beacon
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = BeaconsStateRowBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentBeacon = beacons[position]
        holder.bind(currentBeacon)
    }

    override fun getItemCount(): Int = beacons.size


}

data class BeaconState(
    val uuid: String,
    val major: String,
    val minor: String,
    val rssi: String,
    val txPower: String,
    val distance: String
)