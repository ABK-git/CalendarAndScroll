package com.example.theplanning1

import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.collections.ArrayList


class CalendarMaker() : Parcelable{
    //カレンダークラス
    private var calendar: Calendar? = null

    //現在の日付
    var nowYear = 0 //年
    var nowMonth = 0 //月
    var nowDay = 0 //日

    //表示するカレンダーの年月
    var displayYear = 0 //年　
    var displayMonth = 0 //月

    //カレンダーを表示するための必要情報
    var firstWeeks = 0 //月初めの曜日
    var lastDay = 0 //月の最終日

    //Parcelで保存してあった場合
    constructor(parcel: Parcel) : this() {
        displayYear = parcel.readInt()
        displayMonth = parcel.readInt()
        firstWeeks = parcel.readInt()
        lastDay = parcel.readInt()
    }

    //コンストラクタ
    init {
        //カレンダーインスタンスを取得する
        calendar = Calendar.getInstance()
        //今日の日付を取得
        nowYear = calendar?.get(Calendar.YEAR)!!
        nowMonth = calendar?.get(Calendar.MONTH)!!
        nowDay = calendar?.get(Calendar.DATE)!!
        //表示年月日をセットする
        displayYear = nowYear
        displayMonth = nowMonth
        //月のはじめの曜日を取得
        getLastDayAndFirstWeeks()
    }

    //日付情報を格納したListを生成して返すメソッド
    fun getList() : ArrayList<String>{
        val list = ArrayList<String>()
        //格納する日付
        var dateCount = 1
        //1週目を作成
        for(i in 1..7){
            if(i < firstWeeks){
                list.add("")
            }else{
                list.add(dateCount.toString())
                dateCount++
            }
        }
        //2週目以降を埋める
        for (j in 8..42){
            //最終日まで日付を格納していない場合
            if(dateCount <= lastDay){
                list.add(dateCount.toString())
                dateCount++
            }else{
                list.add("")
            }
        }
        return list
    }

    //月初めの曜日と月末の日付を取得する
    private fun getLastDayAndFirstWeeks(){
        getFirstWeek()
        getLastDay()
    }
    private fun getLastDay(){
        //月を一つ進める
        calendar?.add(Calendar.MONTH,1)
        calendar?.set(Calendar.DATE,1)
        //日付を一日戻す
        calendar?.add(Calendar.DATE,-1)
        //今月の最終日の日付を取得
        lastDay = calendar?.get(Calendar.DATE)!!
    }
    private fun getFirstWeek(){
        //今月の初日を取得
        calendar?.set(displayYear,displayMonth,1)
        //初日の曜日を取得
        firstWeeks = calendar?.get(Calendar.DAY_OF_WEEK)!!
    }
    //月を一つ進める
    fun nextMonth(){
        displayMonth++
        //年を超えた場合
        if (displayMonth > 11){
            //月を1月に設定
            displayMonth = 0
            //年を一つ進める
            displayYear++
        }
        //月初めの曜日と月末の日付を更新する
        getLastDayAndFirstWeeks()
    }
    //月を一つ前に戻す
    fun previous(){
        //月を戻して前年になってしまった場合
        displayMonth--
        if(displayMonth < 0){
            displayMonth = 11
            displayYear--
        }
        //月初めの曜日と月末の日付を更新する
        getLastDayAndFirstWeeks()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        //表示する年月の保存
        parcel.writeInt(displayYear)
        parcel.writeInt(displayMonth)
        //月の最終日と最初の曜日の保存
        parcel.writeInt(firstWeeks)
        parcel.writeInt(lastDay)
    }
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CalendarMaker> {
        override fun createFromParcel(parcel: Parcel): CalendarMaker {
            return CalendarMaker(parcel)
        }

        override fun newArray(size: Int): Array<CalendarMaker?> {
            return arrayOfNulls(size)
        }
    }
}

