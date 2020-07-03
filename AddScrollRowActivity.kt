package com.example.theplanning1

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_add_scroll_row.*
import java.util.*

class AddScrollRowActivity : AppCompatActivity() {
    //登録年月日
    var year = 0
    var month = 0
    var day = 0

    //ID
    var id :Long = 0L

    //メニュー
    private lateinit var menu:String
    //Realm
    lateinit var realm:Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_scroll_row)

        //Realmを取得する
        realm = Realm.getDefaultInstance()

        //送られて来たインテントを取得
        val intent = intent

        //年月日を取得する
        year = intent.getIntExtra("year",0)
        month = intent.getIntExtra("month",0)
        day = intent.getIntExtra("day",0)

        //年月日を生成
        val year_month_day = getString(R.string.year_month_day).format(year,month,day)
        //年月日をTextViewに代入
        display_year_month_day.text = year_month_day

        //メニューを取得
        menu = intent.getStringExtra("menu").toString()

        //IDを取得する
        id = intent.getLongExtra("id",0)
        //メニューで分岐
        when(menu){
            //日報の場合
            getString(R.string.do_planning) -> {
                add_scroll_title.text = getString(R.string.add_memo_do_planning)
                add_scroll_title.background = getDrawable(R.drawable.do_planning_back)
                //金額入力欄を不可視化
                linear_money.visibility = View.GONE
            }
            //予定の場合
            getString(R.string.planning) -> {
                add_scroll_title.text = getString(R.string.add_memo_planning)
                add_scroll_title.background = getDrawable(R.drawable.planning_back)
                //金額入力欄を不可視化
                linear_money.visibility = View.GONE
            }
            //会計の場合
            getString(R.string.accounting) -> {
                add_scroll_title.text = getString(R.string.add_memo_accounting)
                add_scroll_title.background = getDrawable(R.drawable.accounting_back)
                //編集の場合
                if(id > 0 ){
                    //金額を取得
                    val money = intent.getIntExtra("money",0)
                    //初期値としてViewに渡す
                    money_edit.setText(money.toString())
                }
            }
        }
        //編集の場合
        if(id > 0){
            //初期時刻の設定
            val hour = intent.getIntExtra("hour",0)
            if(hour != 0){
                timer_picker.hour = hour
            }
            val minutes = intent.getIntExtra("minutes",0)
            if(minutes != 0){
                timer_picker.minute = minutes
            }
            //メモの初期値を設定
            val memo = intent.getStringExtra("memo")
            edit_memo.setText(memo)
        }
    }
    //背景をクリックしたとき、ソフトキーボードを消す
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(add_scroll_layout.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
        return false
    }

    //Cancelボタンが押された場合
    fun onCancel(view: View){
        //入力されたメモを削除する
        edit_memo.setText("")
        //入力された金額を削除する
        money_edit.setText("")
    }
    //戻るボタンを押された場合
    fun onReturn(view: View){
        //Activityを破棄する
        finish()
    }
    //登録ボタンを押された場合
    fun onRegister(view: View){
        //会計メニューで金額が入力されていなかった場合
        if (menu == getString(R.string.accounting) && money_edit.text.isNullOrEmpty()){
            //Toastの作成、表示
            val toast = Toast.makeText(this,getString(R.string.empty_money),Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER,0,0)
            toast.show()
            //メソッドを終了
            return
        }
        //メモが入力されていなかった場合
        else if(edit_memo.text.isNullOrEmpty()){
            //Toastの作成、表示
            val toast = Toast.makeText(this,getString(R.string.empty_memo),Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER,0,0)
            toast.show()
            //メソッドを終了
            return
        }
        else {
            //登録時間の取得
            val hour = timer_picker.hour
            //登録分の取得
            val minute = timer_picker.minute

             //登録年月日、時刻からDate型を作成
             val date = Date(year,month,day,hour,minute)
             //メモの内容を取得
             val memo = edit_memo.text.toString()
            //新規登録の場合
            if (id == 0L){
                //データベースに登録する
                realm.executeTransaction {
                    //データベースに登録されている最大のIDを取得
                    val maxId = realm.where<RealmScroll>().max("id")
                    //登録するIDを生成(maxIdがnullのときは0を返して、1を足す)
                    val nextId = (maxId?.toLong() ?: 0L) +1L
                    //RealmScrollモデルのインスタンスの作成
                    val realmScroll = realm.createObject<RealmScroll>(nextId)
                    //日時の登録
                    realmScroll.dateTime = date
                    //メニューの登録
                    realmScroll.menu = menu
                    //メモの登録
                    realmScroll.memo = memo

                    //会計メニューの場合
                    if (menu == getString(R.string.accounting)){
                        //金額の登録
                        realmScroll.money = money_edit.text.toString().toInt()
                    }else{ //日報、予定メニューだった場合
                        //金額の登録
                        realmScroll.money = 0
                    }
                }
            }else{//編集の場合
                realm.executeTransaction {
                    //選択されているデータを取得
                    val realmScroll = realm.where<RealmScroll>().equalTo("id",id).findFirst()
                    //データの取得に成功した場合
                    if (realmScroll != null) {
                        //日時登録
                        realmScroll.dateTime = date
                        //メニューの登録
                        realmScroll.menu = menu
                        //メモの登録
                        realmScroll.memo = memo
                        //会計メニューの場合
                        if (menu == getString(R.string.accounting)){
                            //金額の登録
                            realmScroll.money = money_edit.text.toString().toInt()
                        }
                    }
                }
            }

            //Toastの作成、表示
            val toast = Toast.makeText(this,getString(R.string.successful_register),Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER,0,0)
            toast.show()
             //アクティビティーの破棄
             finish()
        }
    }
    //アクティビティーの破棄
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}