package com.example.lab8

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "CarListFragment"

class CarListFragment : Fragment() {

    private lateinit var carRecyclerView: RecyclerView
    private var adapter: CarAdapter = CarAdapter(emptyList())
    private val carListViewModel: CarListViewModel by lazy {
        ViewModelProviders.of(this).get(CarListViewModel::class.java)
    }
    private var callbacks: Callbacks? = null

    interface Callbacks {
        fun onCarSelected(carId: UUID)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as? Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_car_list, container, false)

        carRecyclerView =
            view.findViewById(R.id.car_recycler_view) as RecyclerView
        carRecyclerView.layoutManager = LinearLayoutManager(context)
        carRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.supportActionBar?.setTitle(R.string.car_list)
    }

    override fun onStart() {
        super.onStart()

         carListViewModel.carListLiveData.observe(
            viewLifecycleOwner,
            Observer { cars ->
                cars?.let {
                    Log.i(TAG, "Got carLiveData ${cars.size}")
                    updateUI(cars)
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_car_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_car -> {
                val car = Car()
                carListViewModel.addCar(car)
                callbacks?.onCarSelected(car.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(cars: List<Car>) {
        adapter?.let {
            it.cars = cars
        } ?: run {
            adapter = CarAdapter(cars)
        }
        carRecyclerView.adapter = adapter
    }

    private inner class CarHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var car: Car

        private val car_mark: TextView = itemView.findViewById(R.id.car_mark)
        private val car_company: TextView = itemView.findViewById(R.id.car_company)
        private val car_price: TextView = itemView.findViewById(R.id.car_price)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(car: Car) {
            this.car = car
            car_mark.text = this.car.Mark
            car_company.text = this.car.Company
            car_price.text = this.car.Price.toString()
        }

        override fun onClick(v: View) {
            callbacks?.onCarSelected(car.id)
        }
    }

    private inner class CarAdapter(var cars: List<Car>)
        : RecyclerView.Adapter<CarHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : CarHolder {
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.list_item_car, parent, false)
            return CarHolder(view)
        }

        override fun onBindViewHolder(holder: CarHolder, position: Int) {
            val car = cars[position]
            holder.bind(car)
        }

        override fun getItemCount() = cars.size
    }

    companion object {
        fun newInstance(): CarListFragment {
            return CarListFragment()
        }
    }
}