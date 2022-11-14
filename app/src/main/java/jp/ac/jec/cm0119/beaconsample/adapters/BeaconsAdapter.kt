package jp.ac.jec.cm0119.beaconsample.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jp.ac.jec.cm0119.beaconsample.BeaconState
import jp.ac.jec.cm0119.beaconsample.databinding.BeaconsStateRowBinding

class BeaconsAdapter: RecyclerView.Adapter<BeaconsAdapter.MyViewHolder>() {

    private var beacons = emptyList<BeaconState>()

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