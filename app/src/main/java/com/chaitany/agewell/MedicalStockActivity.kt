package com.chaitany.agewell

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaitany.agewell.databinding.ActivityMedicalStockBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

class MedicalStockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedicalStockBinding
    private lateinit var adapter: MedicineAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private val medicines = mutableListOf<Medicine>()
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalStockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences and Database
        sharedPreferences = getSharedPreferences("UserLogin", Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference

        // Initialize Progress Dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading medicines...")
        progressDialog.setCancelable(false)

        setupRecyclerView()
        loadMedicines()

        binding.btnAddMedicine.setOnClickListener {
            startActivity(Intent(this, AddMedicineActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = MedicineAdapter(
            medicines,
            onEditClick = { medicine ->
                // TODO: Implement edit functionality
                Toast.makeText(this, "Editing  ${medicine.name}", Toast.LENGTH_SHORT).show()
                showEditMedicineDialog(medicine)
            },
            onDeleteClick = { medicine ->
                deleteMedicine(medicine)
            }
        )

        binding.rvMedicines.apply {
            layoutManager = LinearLayoutManager(this@MedicalStockActivity)
            adapter = this@MedicalStockActivity.adapter
        }
    }

    private fun loadMedicines() {
        val userPhone = sharedPreferences.getString("mobile", null) // Retrieve from SharedPreferences

        if (userPhone.isNullOrEmpty()) {
            Toast.makeText(this, "User  phone number not found", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("User  Phone", "Fetching medicines for user: $userPhone") // Log the user phone

        progressDialog.show()  // Show loading

        database.child("users")
            .child(userPhone)
            .child("medicines")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    medicines.clear()
                    if (snapshot.exists()) {
                        Log.d("FirebaseData", "Medicines found for user: $userPhone")
                        snapshot.children.forEach {
                            val medicine = it.getValue(Medicine::class.java)
                            medicine?.let { medicines.add(it) }
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.d("FirebaseData", "No medicines found for user: $userPhone")
                        Toast.makeText(this@MedicalStockActivity, "No medicines found.", Toast.LENGTH_SHORT).show()
                    }
                    progressDialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error loading medicines: ${error.message}")
                    Toast.makeText(this@MedicalStockActivity,
                        "Error loading medicines: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            })
    }

    private fun deleteMedicine(medicine: Medicine) {
        val userPhone = sharedPreferences.getString("mobile", null) // Retrieve from SharedPreferences
        val medicineId = medicine.id // This ID is the Firebase generated unique key

        if (medicineId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid medicine ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase reference to delete the medicine
        val medicineRef = database.child("users")
            .child(userPhone!!)
            .child("medicines")
            .child(medicineId)  // Use the unique 'id' as the key

        medicineRef.removeValue()
            .addOnSuccessListener {
                // After successfully deleting the medicine, remove the associated task
                deleteTask(medicineId)  // Call deleteTask with the medicineId
                Toast.makeText(this, "Medicine deleted", Toast.LENGTH_SHORT).show()

                // Remove the medicine from the local list and notify the adapter
                medicines.remove(medicine)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete medicine", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to delete the task associated with the medicine
    private fun deleteTask(medicineId: String) {
        val userPhone = sharedPreferences.getString("mobile", null) // Retrieve from SharedPreferences

        if (medicineId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid medicine ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase reference to the tasks
        val taskRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userPhone!!)
            .child("tasks")

        // Query to find and delete the task associated with the medicine
        taskRef.orderByChild("medicineId").equalTo(medicineId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Loop through the snapshot to get all tasks with this medicineId
                    for (taskSnapshot in snapshot.children) {
                        // Deleting the task by reference
                        taskSnapshot.ref.removeValue()
                    }
                    Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No associated task found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete task: ${e.message}", Toast.LENGTH_SHORT).show()
                e.message?.let { Log.e("Here", it) }
            }
    }

    private fun showEditMedicineDialog(medicine: Medicine) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_medicine, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Get UI elements
        val etMedicineName = dialogView.findViewById<TextInputEditText>(R.id.etMedicineName)
        val etDosage = dialogView.findViewById<TextInputEditText>(R.id.etDosage)
        val etStock = dialogView.findViewById<TextInputEditText>(R.id.etStock)
        val etMealTime = dialogView.findViewById<TextInputEditText>(R.id.etMealTime)
        val btnSaveChanges = dialogView.findViewById<MaterialButton>(R.id.btnSaveChanges)


        // Populate fields with existing data
        etMedicineName.setText(medicine.name)
        etDosage.setText(medicine.type)
        etStock.setText(medicine.quantity.toString())
        etMealTime.setText(medicine.mealTime)

        // Handle save button click
        btnSaveChanges.setOnClickListener {
            val updatedMedicine = medicine.copy(
                name = etMedicineName.text.toString(),
                type = etDosage.text.toString(),
                quantity = etStock.text.toString().toIntOrNull() ?: medicine.quantity,
                mealTime = etMealTime.text.toString()
            )

            updateMedicineInFirebase(updatedMedicine)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateMedicineInFirebase(medicine: Medicine) {
        val userPhone = sharedPreferences.getString("mobile", null)

        if (userPhone.isNullOrEmpty() || medicine.id.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Data", Toast.LENGTH_SHORT).show()
            return
        }

        val medicineRef = database.child("users")
            .child(userPhone)
            .child("medicines")
            .child(medicine.id)

        medicineRef.setValue(medicine)
            .addOnSuccessListener {
                Toast.makeText(this, "Medicine updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
            }
    }





}