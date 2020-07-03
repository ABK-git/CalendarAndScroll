package com.example.theplanning1

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class RealmScroll : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    //登録日時
    var dateTime: Date = Date()
    //メニュー
    var menu: String? = null
    //メモ
    var memo: String? = null
    //金額(会計限定)
    var money: Int = 0
}