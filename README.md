<p align = "center">МИНИСТЕРСТВО НАУКИ И ВЫСШЕГО ОБРАЗОВАНИЯ
РОССИЙСКОЙ ФЕДЕРАЦИИ
ФЕДЕРАЛЬНОЕ ГОСУДАРСТВЕННОЕ БЮДЖЕТНОЕ
ОБРАЗОВАТЕЛЬНОЕ УЧРЕЖДЕНИЕ ВЫСШЕГО ОБРАЗОВАНИЯ
«САХАЛИНСКИЙ ГОСУДАРСТВЕННЫЙ УНИВЕРСИТЕТ»</p>
<br>
<p align = "center">Институт естественных наук и техносферной безопасности</p>
<p align = "center">Кафедра информатики</p>
<p align = "center">Пак Никита Витальевич</p>
<br>
<p align = "center">Лабораторная работа №8</p>
<p align = "center">«Вывод списков и RecyclerView»</p>
<p align = "center">01.03.02 Прикладная математика и информатика</p>
<br><br><br><br><br><br><br><br>
<p align = "right" >Научный руководитель</p>
<p align = "right" >Соболев Евгений Игоревич</p>
<p align = "center" >Южно-Сахалинск</p>
<p align = "center" >2023 г.</p>
<p align = "center" ><b>ВВЕДЕНИЕ</b></p>
<p>Kotlin (Ко́тлин) — статически типизированный, объектно-ориентированный язык программирования, работающий поверх Java Virtual Machine и разрабатываемый компанией JetBrains. Также компилируется в JavaScript и в исполняемый код ряда платформ через инфраструктуру LLVM. Язык назван в честь острова Котлин в Финском заливе, на котором расположен город Кронштадт</p>
<p>Авторы ставили целью создать язык более лаконичный и типобезопасный, чем Java, и более простой, чем Scala. Следствием упрощения по сравнению со Scala стали также более быстрая компиляция и лучшая поддержка языка в IDE. Язык полностью совместим с Java, что позволяет Java-разработчикам постепенно перейти к его использованию; в частности, язык также встраивается Android, что позволяет для существующего Android-приложения внедрять новые функции на Kotlin без переписывания приложения целиком.</p>
<p align = "center" >РЕШЕНИЕ ЗАДАЧ</p>

<p align = "center" >Упражнение. Приложение. Авто </p>

<p>Приложение, должно иметь следующие функции:
•	Отображение списка автомобилей с характеристиками (10-12 автомобилей, 3 производителя, 1-3 марки у каждого производителя)
•	Добавление нового автомобиля
•	Редактирование деталей автомобиля
Желательно:

•	Фильтрация по производителю и марке
•	Сортировка по цене
 </p>
 
<p align = "center" >MainActivity</p>

```kotlin
    
package com.example.lab8

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import java.util.UUID

class MainActivity : AppCompatActivity(), CarListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fm: FragmentManager = supportFragmentManager
        val currentFragment = fm.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            val fragment = CarListFragment.newInstance()
            fm.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onCarSelected(carId: UUID) {
        val fragment = CarFragment.newInstance(carId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

    
```

<p align = "center" >DatePickerFragment</p>

```kotlin

package com.example.lab8

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

private const val ARG_DATE = "date"

class DatePickerFragment : DialogFragment() {

    interface Callbacks {
        fun onDateSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dateListener = DatePickerDialog.OnDateSetListener {
                _: DatePicker, year: Int, month: Int, day: Int ->

            val resultDate = GregorianCalendar(year, month, day).time

            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }

        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDate = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDate
        )
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}

```

<p align = "center" >CarRepository</p>

```kotlin

package com.example.lab8

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.lab8.database.CarDatabase
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "car-database"

class CarRepository private constructor(context: Context) {

    private val database : CarDatabase = Room.databaseBuilder(
        context.applicationContext,
        CarDatabase::class.java,
        DATABASE_NAME
    ).build()
    private val carDao = database.carDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getCars(): LiveData<List<Car>> = carDao.getCars()

    fun getCar(id: UUID): LiveData<Car?> = carDao.getCar(id)

    fun updateCar(car: Car) {
        executor.execute {
            carDao.updateCar(car)
        }
    }

    fun addCar(car: Car) {
        executor.execute {
            carDao.addCar(car)
        }
    }

    companion object {
        private var INSTANCE: CarRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CarRepository(context)
            }
        }

        fun get(): CarRepository {
            return INSTANCE ?:
            throw IllegalStateException("CarRepository must be initialized")
        }
    }
}
   
```

<p align = "center" >CarListViewModel</p>

```kotlin

package com.example.lab8

import androidx.lifecycle.ViewModel

class CarListViewModel : ViewModel() {

    private val carRepository = CarRepository.get()
    val carListLiveData = carRepository.getCars()

    fun addCar(car: Car) {
        carRepository.addCar(car)
    }
}

```

<p align = "center" >CarListFragment</p>

```kotlin

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

```

<p align = "center" >CarIntentApplication</p>

```kotlin

package com.example.lab8

import android.app.Application
import com.example.lab8.CarRepository

class CarIntentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CarRepository.initialize(this)
    }
}

```

<p align = "center" >CarFragment</p>

```kotlin

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

```

<p align = "center" >CarDetailViewModel</p>

```kotlin

package com.example.lab8

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class CarDetailViewModel : ViewModel() {

    private val carRepository = CarRepository.get()
    private val carIdLiveData = MutableLiveData<UUID>()

    val carLiveData: LiveData<Car?> =
        Transformations.switchMap(carIdLiveData) { carId ->
            carRepository.getCar(carId)
        }

    fun loadCar(carId: UUID) {
        carIdLiveData.value = carId
    }

    fun saveCar(car: Car) {
        carRepository.updateCar(car)
    }
}

```

<p align = "center" >Car</p>

```kotlin

package com.example.lab8

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Car(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var Mark: String = "",
                 var Company: String = "",
                 var Price: Int = 0)

```

***
<p align = "center" >ВЫВОД</p>
<p>Подводя итог всему сказанному, могу сделать вывод, что, поработав c kotlin, я узнал многое и применил это на практике. Все задачи были выполнены.</p>
