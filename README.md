# BeaconSample

モニタリングとリージング：(https://www.mokoblue.com/ja/a-detailed-guide-to-ibeacon/)<br>
サンプル：(https://qiita.com/kenmaeda51415/items/ac5a2d5a15783bbe9192, https://developers.cyberagent.co.jp/blog/archives/12901/, https://gaprot.jp/2015/11/09/altbeacon/, https://github.com/Bridouille/android-beacon-scanner, https://github.com/AltBeacon/android-beacon-library-reference)<br>
ビーコンライブラリ：(https://altbeacon.github.io/android-beacon-library/samples.html)<br>
android-beacon-library：(https://github.com/AltBeacon/android-beacon-library)<br>
permission:(https://qiita.com/kenmaeda51415/items/d28b714cba8f710fb5db)<br>
権限リクエスト：(https://developer.android.com/training/location/permissions?hl=ja)<br>
フォアグラウンド・バックグラウンドでのスキャン：(https://altbeacon.github.io/android-beacon-library/foreground-service.html, https://altbeacon.github.io/android-beacon-library/battery_manager.html, http://www.davidgyoungtech.com/2022/06/25/the-rise-and-fall-of-the-foreground-service)<br> 
フォアグラウンドサービス：(https://qiita.com/kenmaeda51415/items/aa0c6e2f09f6e390a0ea, https://altbeacon.github.io/android-beacon-library/foreground-service.html, https://qiita.com/kenmaeda51415/items/d2f720494c10f39fd985, 制限：https://developer.android.com/about/versions/12/foreground-services?hl=ja)<br>
service:(https://qiita.com/b150005/items/bc7054a520d4b858dc0f, https://qiita.com/kenmaeda51415/items/c80065a48bd11d26df84)<br>
通知作成：(https://developer.android.com/training/notify-user/build-notification?hl=ja)

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
