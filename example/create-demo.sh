cordova create demo com.qbox.QNRTCKitDemo QNRtcDemo
cd demo
cordova platform add ios android browser
cordova plugin add cordova-plugin-rtc-qiniu --variable APPID=dmqotunph --searchpath ../../
cp -r ../www .
cordova prepare