    package com.chaitany.agewell

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.material.chip.Chip
    import com.google.android.material.chip.ChipGroup

    class MedicineAdapter(
        private val medicines: List<Medicine>,
        private val onEditClick: (Medicine) -> Unit,
        private val onDeleteClick: (Medicine) -> Unit
    ) : RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

        class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvMedicineName: TextView = itemView.findViewById(R.id.tvMedicineName)
            val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)
            val tvStock: TextView = itemView.findViewById(R.id.tvStockValue)
            val tvMealTime: TextView = itemView.findViewById(R.id.tvMealTime)  // Added MealTime
            val scheduleChipGroup: ChipGroup = itemView.findViewById(R.id.chipGroupSchedule)
            val ivEdit: ImageView = itemView.findViewById(R.id.ivEdit)
            val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_medicine, parent, false)
            return MedicineViewHolder(view)
        }

        override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
            val medicine = medicines[position]

            holder.tvMedicineName.text = medicine.name
            holder.tvDosage.text = medicine.type
            holder.tvStock.text = "Stock: ${medicine.quantity}"
            holder.tvMealTime.text = "Meal Time: ${medicine.mealTime}" // Displaying MealTime

            // Populate Schedule ChipGroup
            holder.scheduleChipGroup.removeAllViews()
            medicine.schedule.forEach { scheduleTime ->
                val chip = Chip(holder.scheduleChipGroup.context)
                chip.text = scheduleTime
                chip.isCheckable = false
                holder.scheduleChipGroup.addView(chip)
            }


            // Edit and Delete Button Click Listeners
            holder.ivEdit.setOnClickListener { onEditClick(medicine) }
            holder.ivDelete.setOnClickListener { onDeleteClick(medicine) }
        }

        override fun getItemCount() = medicines.size
    }
