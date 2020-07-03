package com.example.theplanning1

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.calendar.*
import kotlinx.android.synthetic.main.plan_do.*
import java.util.*

class MainActivity : AppCompatActivity() {
    //CalendarMaker
    lateinit var calendarMaker: CalendarMaker

    //Realmデータベース
    private lateinit var realm:Realm
    //Realmの検索結果
    var realmResults: RealmResults<RealmScroll>? = null
    //RecyclerViewの表示用
    private lateinit var adapter: CustomRecyclerViewAdapter
    private lateinit var layoutManager:RecyclerView.LayoutManager

    //TextViewのIDが入った配列(6行7列)
    private lateinit var dateViews: MutableList<MutableList<TextView>>

    //今日の年月
    var year = 0
    var month = 0

    //今日の日付
    var today = 0

    //選択されているTextView
    var chooseTextView: TextView? = null
    //activityを破棄したときに日付を記録するString文
    var chooseDay : String? = null

    //選択されているメニュー
    var menu: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Realmデータベースの取得
        realm = Realm.getDefaultInstance()

        //CalendarMakerの取得
        calendarMaker = CalendarMaker()
        //年月日を取得する
        year = calendarMaker.nowYear
        month = calendarMaker.nowMonth
        today = calendarMaker.nowDay

        //TableLayoutのTextViewを取得する
        dateViews = mutableListOf(
            mutableListOf(one_one, one_two, one_three, one_four, one_five, one_six, one_seven),
            mutableListOf(two_one, two_two, two_three, two_four, two_five, two_six, two_seven),
            mutableListOf(three_one, three_two, three_three, three_four, three_five, three_six, three_seven),
            mutableListOf(four_one, four_two, four_three, four_four, four_five, four_six, four_seven),
            mutableListOf(five_one, five_two, five_three, five_four, five_five, five_six, five_seven),
            mutableListOf(six_one, six_two, six_three, six_four, six_five, six_six, six_seven)
        )
        //最初は今日の日付のカレンダーを表示する
        year_month.text = getString(R.string.year_month).format(year, month + 1)

        //区切り専用オブジェクトを生成
        val decorator = DividerItemDecoration(applicationContext,DividerItemDecoration.VERTICAL)
        recycler_scroll.addItemDecoration(decorator)
        recycler_scroll.setHasFixedSize(true)
    }

    //onResumeはRestoreInstanceの後に処理される
    override fun onResume() {
        super.onResume()
        //日付TextViewに日付とClickListenerをセットする
        updateTextView()
        //予定が表示されていなかった場合
        if (menu == null){
            //最初は予定メニューを選択
            onMenuButtonClick(planning)
        }
        //日付の選択があった場合
        updateRecyclerView()
    }

    //日付TextViewの更新
    private fun updateTextView() {
        //表示年月の変更
        year_month.text = getString(R.string.year_month).format(calendarMaker.displayYear, calendarMaker.displayMonth + 1)
        //表示月の日付が格納されたListを取得する
        val dateList = calendarMaker.getList()
        //dateListから何番目の文字列を取り出すか
        var dateCount = 0
        //日付TextViewにClickListenerを設定する
        for (row in dateViews) {
            for (date in row) {
                //背景をリセットする
                date.setBackgroundColor(Color.WHITE)

                date.text = dateList[dateCount]
                //日付が入らなかった場合
                if (date.text.toString() == "") {
                    date.isClickable = false
                    //6行目の日曜日が空欄だった場合
                    if (date == six_one) {
                        //6行目を不可視化
                        lastWeek.visibility = View.GONE
                        //ループ終了
                        break
                    } else {
                        //6行目を可視化する
                        lastWeek.visibility = View.VISIBLE
                    }
                }//日付入りの場合
                else {
                    //ClickListenerを登録する
                    date.setOnClickListener {
                        //日付の選択がされていた場合
                        if (chooseTextView != null) {
                            //選択されていたのが今日の日付の場合
                            if (todayOrElse(chooseTextView!!)) {
                                //今日の日付用の背景に戻す
                                chooseTextView!!.setBackgroundResource(R.drawable.today)
                            } else {
                                //今日以外の日付用の背景に戻す
                                chooseTextView!!.setBackgroundColor(Color.WHITE)
                            }
                        }
                        //選択した日付を記録する
                        chooseTextView = it as TextView //変更した背景をもとに戻すのに必要
                        chooseDay = chooseTextView!!.text.toString() //画面の再構築時に必要

                        //今日の日付がクリックされた場合
                        if (todayOrElse(chooseTextView!!)) {
                            //背景を変更する
                            it.setBackgroundResource(R.drawable.today_and_choose_day)
                        } else {
                            //背景を変更する
                            it.setBackgroundResource(R.drawable.choose_day)
                        }
                        //RecyclerViewを作り直す
                        updateRecyclerView()
                    }//end ClickListener
                    //今日の日付の場合(背景を今日の日付用のものに変える)
                    if (todayOrElse(date)) date.setBackgroundResource(R.drawable.today)
                    //データの破棄、再構築時に選択した日付の記録があった場合
                    if (chooseDay != null && date.text.toString() == chooseDay){
                        //dateを選択TextViewに
                        chooseTextView = date
                        //今日の日付が選択されていた場合
                        if (todayOrElse(date)){
                            date.setBackgroundResource(R.drawable.today_and_choose_day)
                        }else{
                            //ほかの日付が選択されていた場合
                            date.setBackgroundResource(R.drawable.choose_day)
                        }
                    }
                }
                //カウントを1追加
                dateCount++
            }
        }
    }
    //RecyclerViewを更新するメソッド
    private fun updateRecyclerView(){
        if(chooseDay != null && chooseTextView != null){
            //選択した日付の0時0分のDateを生成
            val firstDate = Date(calendarMaker.displayYear,calendarMaker.displayMonth+1,
                    chooseDay!!.toInt(),0,0)
            //選択した日付の23時59分を作成
            val lastDate = Date(calendarMaker.displayYear,calendarMaker.displayMonth+1,
                    chooseDay!!.toInt(),23,59)
            //Realmから選択した日付のデータを取得する
            val realmQuery = realm.where(RealmScroll::class.java)
                    .between("dateTime",firstDate,lastDate)
                    .equalTo("menu",menu)
                    .sort("dateTime",Sort.ASCENDING)
            //条件に合ったデータをすべて取得して、記録する
            realmResults = realmQuery.findAll()
            //adapterを設定
            adapter = CustomRecyclerViewAdapter(realmQuery.findAll())
            recycler_scroll.adapter = adapter
            //LayoutManagerを設定
            layoutManager = LinearLayoutManager(this)
            recycler_scroll.layoutManager = layoutManager

            //RecyclerViewのスワイプ処理を作成
            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.
            SimpleCallback(ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT, ItemTouchHelper.RIGHT){

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    viewHolder.let {
                        //スワイプしたViewのIDを取得する
                        val realmDate = realmResults?.get(viewHolder.adapterPosition)
                        val id = realmDate?.id
                        realm.executeTransaction {
                            //データベースからデータを消去する処理
                            if (id != null) {
                                realm.where<RealmScroll>()
                                        .equalTo("id",id.toLong())
                                        ?.findFirst()
                                        ?.deleteFromRealm()
                            }
                        }
                    }
                    //adapterからスワイプしたデータを削除する
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                }
            })
            //スワイプ処理をRecyclerViewに設定する
            itemTouchHelper.attachToRecyclerView(recycler_scroll)
        }
    }
    //FloatingActionButtonを押したとき
    fun onAddScrollRow(view:View){
        //日付が選択されている場合
        if(chooseDay != null && menu != null) {
            val intent = Intent(this,AddScrollRowActivity::class.java)
            //メニューを送る
            intent.putExtra("menu",menu)
            //年月日を送る
            intent.putExtra("year",calendarMaker.displayYear)
            intent.putExtra("month",calendarMaker.displayMonth+1)
            intent.putExtra("day", chooseDay!!.toInt())
            //画面移動
            startActivity(intent)
        }
    }

    //メニューボタンを押したとき
    fun onMenuButtonClick(view: View){
        //初回起動時ではない場合
        menu?.let {
            //クリック不可になっていたButtonをクリック可能に戻す
            when(menu){
                //日報を押していた場合
                getString(R.string.do_planning) -> do_planning.isEnabled = true
                //予定を押していた場合
                getString(R.string.planning) -> planning.isEnabled = true
                //会計を押していた場合
                getString(R.string.accounting) -> accounting.isEnabled = true
            }
        }
        //ViewをButtonに変更
        val button = view as Button
        //予定メニューを選択
        menu = button.text.toString()
        when(menu){
            //日報を押した場合
            getString(R.string.do_planning) ->{
                //FloatingActionButtonの背景色を変更
                floating_action_button.backgroundTintList = ColorStateList.valueOf(R.drawable.do_planning_back)
                //RecyclerViewの背景色を変更
                recycler_scroll.setBackgroundResource(R.drawable.do_planning_back)
                //日報ボタンをクリックできないようにする
                do_planning.isEnabled = false
            }
            //予定を押した場合
            getString(R.string.planning) ->{
                //FloatingActionButtonの背景色を変更
                floating_action_button.backgroundTintList = ColorStateList.valueOf(R.drawable.planning_back)
                //RecyclerViewの背景色を変更
                recycler_scroll.setBackgroundResource(R.drawable.planning_back)
                //予定ボタンをクリックできないようにする
                planning.isEnabled = false
            }
            //会計を押した場合
            getString(R.string.accounting) ->{
                //FloatingActionButtonの背景色を変更
                floating_action_button.backgroundTintList = ColorStateList.valueOf(R.drawable.accounting_back)
                //RecyclerViewの背景色を変更
                recycler_scroll.setBackgroundResource(R.drawable.accounting_back)
                //会計ボタンをクリックできないようにする
                accounting.isEnabled = false
            }
        }
        updateRecyclerView()
    }

    //表示月を変更する
    fun createNewCalendar(view: View){
        //選択日をリセット
        chooseTextView = null
        val button = view as Button
        when(button.text.toString()){
            //前の月へ移動の場合
            getString(R.string.to_previous) -> calendarMaker.previous()
            //次の月に移動の場合
            getString(R.string.to_next) -> calendarMaker.nextMonth()
        }
        //選択していた日付が次の月にはない場合
        chooseDay?.let {
            if (chooseDay!!.toInt() > calendarMaker.lastDay){
                chooseDay = null
            }
        }
        //TextViewの更新
        updateTextView()
        //RecyclerViewの更新
        updateRecyclerView()
    }
    //今日の日付のTextViewかどうか
    private fun todayOrElse(date: TextView): Boolean {
        //今日の年月
        val chooseYearMonth = getString(R.string.year_month).format(year, month + 1)
        //表示されている年月日が選択されているTextViewの年月日と等しいかどうか
        if (date.text.toString() == today.toString() &&
            year_month.text.toString() == chooseYearMonth
        ) {
            return true
        }
        return false
    }
    //画面を破棄したときのデータ保存処理
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //CalendarMakerを保存する
        outState.putParcelable("CalendarMaker",calendarMaker)
        //選択していたメニューを保存する
        outState.putString("menu",menu)
        if (chooseDay != null){
            //日付を保存する
            outState.putString("day",chooseDay)
        }
    }
    //画面を再構築したときのデータ取得処理
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState.run {
            //CalendarMakerを取得
            calendarMaker = getParcelable("CalendarMaker")!!
            //Menuを取得
            menu = getString("menu")!!
            //日付を取得
            chooseDay = getString("day")
        }
        //TextViewの更新
        updateTextView()
        //Scrollのメニュー変更
        when(menu){
            getString(R.string.planning) -> onMenuButtonClick(planning)
            getString(R.string.do_planning) -> onMenuButtonClick(do_planning)
            getString(R.string.accounting) -> onMenuButtonClick(accounting)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}