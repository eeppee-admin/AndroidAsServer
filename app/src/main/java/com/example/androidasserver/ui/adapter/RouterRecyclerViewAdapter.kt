package com.example.androidasserver.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidasserver.R
import com.example.androidasserver.data.local.RouterItem

//
///**
// * 路由Recycler View 适配器
// * 【x】https://blog.csdn.net/ERP_LXKUN_JAK/article/details/135295918
// * 刘强东
// * [v] https://github.com/liangjingkanji/BRV/blob/HEAD/sample/src/main/java/com/drake/brv/sample/ui/fragment/SimpleFragment.kt
// */
//class RouterRecyclerViewAdapter : RecyclerView.Adapter<RouterRecyclerViewAdapter.ViewHolder>() {
//
//    private var items: List<RouterItem> = emptyList()
//
//    fun submitList(items: List<RouterItem>) {
//        this.items = items
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_router, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = items[position]
//        holder.bind(item)
//    }
//
//    override fun getItemCount(): Int = items.size
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val urlTv: TextView = itemView.findViewById(R.id.router_url_tv)
//        private val descTv: TextView = itemView.findViewById(R.id.router_desc_tv)
//
//        fun bind(item: RouterItem) {
//            urlTv.text = item.url
//            descTv.text = item.desc
//        }
//    }
//}