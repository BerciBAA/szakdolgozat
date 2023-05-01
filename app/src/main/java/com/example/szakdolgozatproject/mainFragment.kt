package com.example.szakdolgozatproject

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.example.szakdolgozatproject.databinding.FragmentMainBinding
import com.example.szakdolgozatproject.models.Games
import com.example.szakdolgozatproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class mainFragment : Fragment() {

    private lateinit var uriList : ArrayList<Uri>;
    lateinit var binding: FragmentMainBinding;
    var game: String? = null
    var gamesIds:ArrayList<Games>?=null
    var randomUserId: String=""
    var currUid: String = "";
    var matchArray:ArrayList<String>?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_main,container,false)
        (activity as AppCompatActivity).supportActionBar?.title = "Main"
        uriList = ArrayList()
        gamesIds = ArrayList();
        matchArray = ArrayList();
        binding.mainFragmentToProfileFragment.setOnClickListener {
            val action = mainFragmentDirections.actionMainFragmentToProfileFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }

        binding.mainFragmentToMessagesFragment.setOnClickListener {
            val action = mainFragmentDirections.actionMainFragmentToMessagesFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }
        binding.matchButton.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid

            val ref = FirebaseDatabase.getInstance().getReference("/matches/$uid")
            ref.setValue(uid.toString())

            getFriendUser()
        }
        binding.notMatchButton.setOnClickListener {
            val uid = FirebaseAuth.getInstance().uid

            val ref = FirebaseDatabase.getInstance().getReference("/matches/$uid")
            ref.setValue(uid.toString())
            getFriendUser()
        }
        val items = listOf("Dota 2","League Of Legends", "Counter-Strike: Global Offensive")
        val adapterItem = ArrayAdapter(requireContext(), R.layout.game_list_item,items)

        binding.selector.setAdapter(adapterItem)
        binding.selector.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val itemSelected = parent.getItemAtPosition(position)
            Toast.makeText(context,"$itemSelected",Toast.LENGTH_SHORT).show()
            when(itemSelected){
                "Dota 2" ->  game = "dota"
                "League Of Legends" ->  game = "leagueoflegends"
                "Counter-Strike: Global Offensive" ->  game = "counterstrikeglobaloffensive"
            }
            getFriendUser()
        }

        return binding.root
    }

    private fun getFriendUser() {
        val ref = FirebaseDatabase.getInstance().getReference("/matches/${FirebaseAuth.getInstance().uid}/${currUid}")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val gameId = it.getValue(Games::class.java)
                    if (gameId != null) {
                        matchArray?.add(gameId.uid)
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        )
        val gamesRefUid = FirebaseDatabase.getInstance().getReference("/games/$game")
        gamesRefUid.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                gamesIds!!.clear()
                snapshot.children.forEach {
                    val gameId = it.getValue(Games::class.java)
                    if (gameId != null) {
                        gamesIds?.add(gameId)
                    }

                }
                var randomNum: Int;
                do {
                    randomNum = (0 until gamesIds!!.size).random()
                    randomUserId = gamesIds!![randomNum].uid
                } while (matchArray?.contains(randomUserId) == true)
                addUser()
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun addUser(){
        val userRef = FirebaseDatabase.getInstance().getReference("/users/$randomUserId")
        val imagesRef = FirebaseStorage.getInstance().getReference("/images/$randomUserId")
        imagesRef.listAll().addOnSuccessListener {
            it.items.forEach{ item ->
                item.downloadUrl.addOnSuccessListener { photoUri ->
                    uriList.add(photoUri)
                    binding.pictureViewerRecycleView.adapter = ViewPagerAdapter(uriList)
                    binding.pictureViewerRecycleView.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                }
            }
        }
        userRef.get().addOnSuccessListener {
            val user = it.getValue(User::class.java)!!
            binding.usernameFriendTextView.text = user.username
            binding.descriptionTextView.text = user.description

        }
    }

}