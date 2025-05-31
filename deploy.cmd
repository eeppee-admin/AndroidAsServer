@REM adb 启动 https://blog.csdn.net/WUNEAL/article/details/140359304
.\gradlew.bat installDebug && adb shell am start -n  com.example.androidasserver/.ui.activity.MainActivity