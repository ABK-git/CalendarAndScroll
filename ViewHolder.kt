package com.example.theplanning1

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.plan_do.*
import kotlinx.android.synthetic.main.scroll_row.view.*

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    //メモ
    var memo:TextView? = null
    //登録時刻
    var time:TextView? = null
    //金額表示View
    var accounting_money:TextView? = null

    init {
        //ビューホルダーのプロパティとレイアウトのViewの対応
        memo = itemView.memo
        time = itemView.time
        accounting_money = itemView.accounting_money
    }
    //会計の時以外は金額表示するLinearLayoutを削除する
    fun setVisible(boolean: Boolean){
        if(!boolean){
            itemView.accounting_layout.visibility = View.GONE
        }
    }
}