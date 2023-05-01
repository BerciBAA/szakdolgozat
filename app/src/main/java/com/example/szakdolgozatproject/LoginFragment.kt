package com.example.szakdolgozatproject

import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.szakdolgozatproject.databinding.FragmentLoginBinding
import com.example.szakdolgozatproject.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_login,container,false)
        firebaseAuth = FirebaseAuth.getInstance()
        (activity as AppCompatActivity).supportActionBar?.title = "Login"
        binding.loginButton.setOnClickListener {
            login()
        }
        binding.registerPageButton.setOnClickListener{
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }
        return binding.root
    }

    private fun login(){
        val email = binding.emailPlainText.text.toString()
        val password = binding.passwordPlainText.text.toString()
        if(email.isNotEmpty() && password.isNotEmpty()){
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                if(it.isSuccessful){
                    val action = LoginFragmentDirections.actionLoginFragmentToProfileFragment()
                    Navigation.findNavController(binding.root).navigate(action)
                }else{
                    Toast.makeText(activity, "authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else{
            Toast.makeText(activity, "Empty Field are not allowed", Toast.LENGTH_SHORT).show()
        }
    }
}