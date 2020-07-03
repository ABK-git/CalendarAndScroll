package com.example.theplanning1

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmResults

class CustomRecyclerViewAdapter(realmResults: RealmResults<RealmScroll>) :
        RecyclerView.Adapter<ViewHolder>() {

    //データベースを取得
    private val rResults: RealmResults<RealmScroll> = realmResults

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //scroll_rowレイアウトを取得する
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.scroll_row,parent,false)
        //ViewHolderを生成
        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return rResults.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //データを取得する
        val realmScroll = rResults[position]
        //データが存在した場合
        if (realmScroll != null) {
            //メモを記入
            holder.memo?.text = realmScroll.memo.toString()
            //時刻を記入
            val time = "%02d時%02d分"
            holder.time?.text = time.format(realmScroll.dateTime.hours,realmScroll.dateTime.minutes)
            //会計メニュー以外だった場合
            if (realmScroll.money == 0){
                //会計を表示するレイアウトを削除する
                holder.setVisible(false)
            }else{
                //会計情報を記入する
                val str = "￥"+"%,d".format(realmScroll.money)+"円"
                holder.accounting_money?.text = str
            }
        }
        //ClickListenerを設定する
        holder.itemView.setOnClickListener {

            if (realmScroll != null) {
                val intent = Intent(it.context,AddScrollRowActivity::class.java)
                //idを送る
                intent.putExtra("id",realmScroll.id)
                //登録日時
                intent.putExtra("year",realmScroll.dateTime.year)
                intent.putExtra("month",realmScroll.dateTime.month)
                intent.putExtra("day",realmScroll.dateTime.date)
                intent.putExtra("hour",realmScroll.dateTime.hours)
                intent.putExtra("minutes",realmScroll.dateTime.minutes)
                //メモ
                intent.putExtra("memo",realmScroll.memo)
                //メニュー
                intent.putExtra("menu",realmScroll.menu)
                //金額
                intent.putExtra("money",realmScroll.money)
                //activityを移動
                it.context.startActivity(intent)
            }
        }
    }

}