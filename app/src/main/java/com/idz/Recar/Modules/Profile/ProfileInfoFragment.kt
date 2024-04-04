package com.idz.Recar.Modules.Profile
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.Navigation
import com.idz.Recar.Model.Model
import com.idz.Recar.Model.Student
import com.idz.Recar.Modules.Students.StudentsFragmentDirections
import com.idz.Recar.R

class ProfileInfoFragment : Fragment() {

    private var nameTextField: EditText? = null
    private var idTextField: EditText? = null
    private var messageTextView: TextView? = null
    private var saveButton: Button? = null
    private var cancelButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_info, container, false)
        val editProfileButton: TextView = view.findViewById(R.id.name)
        val action = Navigation.createNavigateOnClickListener(ProfileInfoFragmentDirections.actionProfileInfoFragmentToProfileEditFragment())
        editProfileButton.setOnClickListener(action)
        setupUI(view)
        return view
    }

    private fun setupUI(view: View) {
//        nameTextField = view.findViewById(R.id.etAddStudentName)
//        idTextField = view.findViewById(R.id.etAddStudentID)
//        messageTextView = view.findViewById(R.id.tvAddStudentSaved)
//        saveButton = view.findViewById(R.id.btnAddStudentSave)
//        cancelButton = view.findViewById(R.id.btnAddStudentCancel)
//        messageTextView?.text = ""
//
//        cancelButton?.setOnClickListener {
//            Navigation.findNavController(it).popBackStack(R.id.studentsFragment, false)
//        }
//
//        saveButton?.setOnClickListener {
//            val name = nameTextField?.text.toString()
//            val id = idTextField?.text.toString()
//
//            val student = Student(name, id, "", false)
//            Model.instance.addStudent(student) {
//                Navigation.findNavController(it).popBackStack(R.id.studentsFragment, false)
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}