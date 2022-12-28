package com.washathomes.apputils.modules

data class Version(val status: Status, val results: VersionResult)

data class VersionResult(val version_android: String, val ios: String)
