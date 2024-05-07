package com.idz.Recar.Modules.Car

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idz.Recar.Model.Model
import com.idz.Recar.Modules.Search.Adapter.CarResultRecyclerAdapter
import com.idz.Recar.Modules.Search.Adapter.OnItemClickListener
import com.idz.Recar.Modules.Search.ResultsViewModel
import com.idz.Recar.Utils.SharedPreferencesHelper
import com.idz.Recar.databinding.FragmentMyCarsBinding


class MyCarsFragment : Fragment() {


    var resultsRecyclerView: RecyclerView? = null
    var adapter: CarResultRecyclerAdapter? = null
    var progressBar: ProgressBar? = null
    var addCarButton: ImageButton? = null
    var userId: String? = null
    private lateinit var viewModel: ResultsViewModel


    private var _binding: FragmentMyCarsBinding? = null
    private val binding get() = _binding!!


    private fun setupUI() {


        progressBar = binding.progressBar
        addCarButton = binding.btnAddCar
        val action =
            Navigation.createNavigateOnClickListener(MyCarsFragmentDirections.actionGlobalCarFormFragment())
        addCarButton?.setOnClickListener(action)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMyCarsBinding.inflate(inflater, container, false)
        val view = binding.root
        // Inflate the layout for this fragment
        setupUI()
        userId = SharedPreferencesHelper.getUserId(requireContext()) ?: ""
        setResultList(view)

        return view
    }

    fun setResultList(view: View) {


        viewModel = ViewModelProvider(this)[ResultsViewModel::class.java]


        progressBar?.visibility = View.VISIBLE

        viewModel.results = Model.instance.getAllCars(
            owner = userId
        )

        resultsRecyclerView = binding.rvCars
        resultsRecyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        resultsRecyclerView?.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            resultsRecyclerView?.context,
            layoutManager.orientation
        )
        adapter = CarResultRecyclerAdapter(viewModel.results?.value)
        adapter?.listener = object : OnItemClickListener {

            override fun onItemClick(position: Int) {
                Log.i("TAG", "Cars Recycle adapter: Position clicked $position")
                val result = viewModel.results?.value?.get(position)
                result?.let {
                    val action =
                        MyCarsFragmentDirections.actionMyCarFragmentToCarFragment(result.id)
                    view.findNavController().navigate(action)
                }
            }


        }

        resultsRecyclerView?.adapter = adapter



        viewModel.results?.observe(viewLifecycleOwner) {
            adapter?.results = it
            adapter?.notifyDataSetChanged()
            progressBar?.visibility = View.GONE
        }

        binding.pullToRefresh.setOnRefreshListener {
            reloadData()
        }

        Model.instance.resultsLoadingState.observe(viewLifecycleOwner) { state ->
            binding.pullToRefresh.isRefreshing = state == Model.LoadingState.LOADING
        }


    }

    override fun onResume() {
        super.onResume()
        reloadData()
    }

    private fun reloadData() {
        progressBar?.visibility = View.VISIBLE
        Model.instance.refreshAllCars(
            owner = userId
        )
        progressBar?.visibility = View.GONE
        binding.pullToRefresh.isRefreshing = false
    }

}