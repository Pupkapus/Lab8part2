package com.example.lab8

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.util.*


private const val TAG = "CarFragment"
private const val ARG_CAR_ID = "car_id"

class CarFragment : Fragment() {

    private lateinit var car: Car
    private lateinit var car_mark: Spinner
    private lateinit var car_company: Spinner
    private lateinit var car_price: EditText
    private lateinit var adapterMark: ArrayAdapter<String>
    private lateinit var adapterCompany: ArrayAdapter<String>
    private val carDetailViewModel: CarDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CarDetailViewModel::class.java)
    }
    private val markList = arrayListOf("Impreza", "Forester", "Outback", "Camry", "Corolla", "Highlander", "3 Series", "5 Series", "X5", "Chiron", "Veyron", "Divo")
    private val companyList = arrayListOf("Subaru", "Toyota", "BMW", "Bugatti")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        car = Car()
        val carId: UUID = arguments?.getSerializable(ARG_CAR_ID) as UUID
        carDetailViewModel.loadCar(carId)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_car, container, false)

        car_mark = view.findViewById(R.id.car_mark_spinner) as Spinner
        car_company = view.findViewById(R.id.car_company_spinner) as Spinner
        car_price = view.findViewById(R.id.car_price) as EditText

        adapterMark = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, markList)
        adapterCompany = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, companyList)
        adapterMark.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterCompany.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        car_mark.setAdapter(adapterMark);
        car_company.setAdapter(adapterCompany);

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val carId = arguments?.getSerializable(ARG_CAR_ID) as UUID
        carDetailViewModel.loadCar(carId)
        carDetailViewModel.carLiveData.observe(
            viewLifecycleOwner,
            Observer { car ->
                car?.let {
                    this.car = car
                    updateUI()
                }
            })

        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.supportActionBar?.setTitle(R.string.new_car)
    }

    override fun onStart() {
        super.onStart()

        car_mark.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMark = car_mark.selectedItem as String
                car.Mark = selectedMark
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Действия при отсутствии выбранной марки автомобиля
            }
        }

        car_company.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCompany = car_company.selectedItem as String
                car.Company = selectedCompany
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Действия при отсутствии выбранной компании автомобиля
            }
        }

        car_price.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && s.matches(Regex("[0-9]+"))) {
                    car.Price = s.toString().toInt()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    override fun onStop() {
        super.onStop()
        carDetailViewModel.saveCar(car)
    }


    private fun updateUI() {
        var position = adapterMark.getPosition(car.Mark)
        car_mark.setSelection(position);
        position = adapterCompany.getPosition(car.Company)
        car_company.setSelection(position);
        car_price.setText(car.Price.toString())
    }

    companion object {

        fun newInstance(carId: UUID): CarFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CAR_ID, carId)
            }
            return CarFragment().apply {
                arguments = args
            }
        }
    }
}