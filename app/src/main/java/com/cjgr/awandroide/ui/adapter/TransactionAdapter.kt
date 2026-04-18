package com.cjgr.awandroide.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cjgr.awandroide.R
import com.cjgr.awandroide.data.local.TransactionEntity

class TransactionAdapter : ListAdapter<TransactionEntity, TransactionAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgTipo: ImageView = view.findViewById(R.id.imgTipoTransaccion)
        val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcion)
        val txtFecha: TextView = view.findViewById(R.id.txtFecha)
        val txtMonto: TextView = view.findViewById(R.id.txtMonto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = getItem(position)
        holder.txtDescripcion.text = tx.descripcion
        holder.txtFecha.text = tx.fecha

        if (tx.monto >= 0) {
            holder.txtMonto.text = "+ ${"%.2f".format(tx.monto)}"
            holder.txtMonto.setTextColor(
                holder.itemView.context.getColor(R.color.colorVerde)
            )
            holder.imgTipo.setImageResource(R.drawable.ic_request)
        } else {
            holder.txtMonto.text = "- ${"%.2f".format(Math.abs(tx.monto))}"
            holder.txtMonto.setTextColor(
                holder.itemView.context.getColor(R.color.colorRojo)
            )
            holder.imgTipo.setImageResource(R.drawable.ic_send)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TransactionEntity>() {
        override fun areItemsTheSame(a: TransactionEntity, b: TransactionEntity) = a.id == b.id
        override fun areContentsTheSame(a: TransactionEntity, b: TransactionEntity) = a == b
    }
}