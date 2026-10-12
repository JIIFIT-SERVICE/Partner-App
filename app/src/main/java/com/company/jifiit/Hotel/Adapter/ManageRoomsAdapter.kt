package com.company.jifiit.Hotel.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.Hotel.Model.HotelRoomItem
import com.company.jifiit.R

class ManageRoomsAdapter(
    private var roomsList: List<HotelRoomItem>,
    private val onBookClick: (HotelRoomItem) -> Unit,
    private val onRoomClick: (HotelRoomItem) -> Unit
) : RecyclerView.Adapter<ManageRoomsAdapter.RoomViewHolder>() {

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.cardRoomContainer)
        val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        val tvRoomType: TextView = itemView.findViewById(R.id.tvRoomType)
        val ivBed: ImageView = itemView.findViewById(R.id.ivBed)
        val btnBookSession: TextView = itemView.findViewById(R.id.btnBookSession)
        val tvActiveSessionBadge: TextView = itemView.findViewById(R.id.tvActiveSessionBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_room_card, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomsList[position]

        holder.tvRoomName.text = room.roomNumber
        holder.tvRoomType.text = room.roomType

        if (room.isBooked) {
            holder.container.setBackgroundResource(R.drawable.bg_room_card_active)
            holder.ivBed.setColorFilter(Color.parseColor("#0D9488"))
            holder.btnBookSession.visibility = View.GONE
            holder.tvActiveSessionBadge.visibility = View.VISIBLE
            holder.tvActiveSessionBadge.text = room.activeSessionText ?: "IN SESSION"
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_room_card_default)
            holder.ivBed.setColorFilter(Color.parseColor("#64748B"))
            holder.btnBookSession.visibility = View.VISIBLE
            holder.tvActiveSessionBadge.visibility = View.GONE
        }

        holder.btnBookSession.setOnClickListener {
            onBookClick(room)
        }

        holder.container.setOnClickListener {
            onRoomClick(room)
        }
    }

    override fun getItemCount(): Int = roomsList.size

    fun updateRooms(newRooms: List<HotelRoomItem>) {
        roomsList = newRooms
        notifyDataSetChanged()
    }
}
