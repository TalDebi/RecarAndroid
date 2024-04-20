package com.idz.Recar.Modules.Search

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import com.google.android.material.sidesheet.SideSheetBehavior
import com.google.android.material.sidesheet.SideSheetDialog
import com.idz.Recar.Model.Model
import com.idz.Recar.Model.Student
import com.idz.Recar.R
import java.lang.Exception

class SearchFragment : Fragment() {


    private var filtersButton: Button? = null
    private var filterSheet: SideSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    private fun setupUI(view: View) {
        try {


            filtersButton = view.findViewById(R.id.filterButton)
            filterSheet = SideSheetDialog(requireContext());
            filterSheet?.setContentView(R.layout.filters_layout)
            filterSheet?.setSheetEdge(Gravity.START)

            filtersButton?.setOnClickListener {
                filterSheet?.let {

                    it.show()

                }

            }
        } catch (e: Exception) {
            println(e)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.search, container, false)
        setupUI(view)

        return view
    }

}