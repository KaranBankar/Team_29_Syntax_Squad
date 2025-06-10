
package com.chaitany.agewell;
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ProgressBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.chaitany.agewell.R // Change with your package

class LoadingDialog(private val context: Context) {
    private var dialog: Dialog? = null

    fun showLoadingDialog() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_loading, null) // Custom progress layout

        dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(false) // Prevent dismissing by tapping outside
            .create()

        dialog?.show()
    }

    fun dismissLoadingDialog() {
        dialog?.dismiss()
    }
}
