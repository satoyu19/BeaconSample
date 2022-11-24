package jp.ac.jec.cm0119.beaconsample.util

class Constants {
    companion object {
        // TODO: 家様のビーコンUUID要確認
        const val BEACON_UUID_HOME = "20000124-0124-1478-1111-003033637761"
        const val BEACON_UUID_SCHOOL = "20011478-2000-0124-1111-003033637761"
        //iBeaconを検知できる様にするため必要
        const val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
    }
}