package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersDescriptionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    val primaryViewModel: RemindersDescriptionViewModel by viewModel()

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_reminder_description
        )

        val reminderDataItem = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

        val format = DecimalFormat("#.####")
        format.roundingMode = RoundingMode.CEILING

        binding.titleTextview.text = reminderDataItem.title
        binding.descriptionTextview.text = reminderDataItem.description
        binding.locationTextview.text = reminderDataItem.location

        binding.deleteButton.setOnClickListener {

            primaryViewModel.deleteReminder(reminderDataItem.id) {
                finish()
            }

        }
    }
}
