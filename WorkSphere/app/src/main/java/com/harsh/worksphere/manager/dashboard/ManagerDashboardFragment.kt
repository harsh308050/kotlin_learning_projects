package com.harsh.worksphere.manager.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.harsh.worksphere.R
class ManagerDashboardFragment : Fragment(R.layout.manager_dashboard_fragment) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dateFilter(view)
    }

    private fun dateFilter(view: View){
        val dates  = listOf("This Week", "This Month", "This Year")
        val dateFilter = view.findViewById<Spinner>(R.id.manager_dashboard_date_filter)
        val dateFilterAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dates
        )
        dateFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dateFilter.adapter = dateFilterAdapter
        dateFilter.setSelection(0)

    }
}