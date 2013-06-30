#!/bin/bash
ant clean; ant debug
adb shell pm uninstall aaditya.myruns5
adb -d install bin/MyRuns5-debug.apk
