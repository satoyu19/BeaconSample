# BeaconSample

モニタリングとリージング：https://www.mokoblue.com/ja/a-detailed-guide-to-ibeacon/<br>
サンプル：https://qiita.com/kenmaeda51415/items/ac5a2d5a15783bbe9192, https://developers.cyberagent.co.jp/blog/archives/12901/, https://gaprot.jp/2015/11/09/altbeacon/, https://github.com/Bridouille/android-beacon-scanner<br>
ビーコンライブラリ：https://altbeacon.github.io/android-beacon-library/samples.html<br>
android-beacon-library：https://github.com/AltBeacon/android-beacon-library<br>
permission:https://qiita.com/kenmaeda51415/items/d28b714cba8f710fb5db<br>
権限リクエスト：https://developer.android.com/training/location/permissions?hl=ja<br>
フォアグラウンド・バックグラウンドでのスキャン：https://altbeacon.github.io/android-beacon-library/foreground-service.html, https://altbeacon.github.io/android-beacon-library/battery_manager.html<br>
フォアグラウンドサービス：https://altbeacon.github.io/android-beacon-library/foreground-service.html<br>
service: https://qiita.com/b150005/items/bc7054a520d4b858dc0f

## BLEAD Beacon 仕様
![スクリーンショット 2022-11-14 14 49 25](https://user-images.githubusercontent.com/96398365/201588351-3c8753c7-3811-492d-b93c-9f245508e9bb.png)
![スクリーンショット 2022-11-14 14 49 35](https://user-images.githubusercontent.com/96398365/201588664-a55b6d0f-4752-4558-835b-bd55be2e992f.png)
<br>

AltBeacon: Bluetoothを利用した機能、パーミッションが必要。(使っているビーコンのライブラリでは、デフォルトでAltBeaconのみを検出する)<br>
IBeacon: Bluetoos LEを利用した機能になっているため、それを使用できる端末のみでインストール可能にする。<br>

## 調査
実装の方法<br>
  1：RangeNotifier, MonitorNotifierを具象クラスで実装し、BeaconManagerのaddで登録する　← こっちの方が細かく処理できる?<br>
  2：regionViewModelのLiveDataにObserveを設定し、監視することで処理する。<br>
どちらも実装済み、機能は同一。実装によってTODOのコメントイン、コメントアウト要。
