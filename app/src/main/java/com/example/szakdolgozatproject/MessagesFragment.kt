package com.example.szakdolgozatproject

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.szakdolgozatproject.databinding.FirendsItemBinding
import com.example.szakdolgozatproject.databinding.FragmentMessagesBinding
import com.example.szakdolgozatproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class MessagesFragment : Fragment(), MenuProvider{

    private lateinit var binding : FragmentMessagesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_messages,container,false)
        verifyUserLoggedIn()
        (activity as AppCompatActivity).supportActionBar?.title = "Messages"

        binding.messageFragmentToProfileFragment.setOnClickListener {
            val action = MessagesFragmentDirections.actionMessagesFragmentToProfileFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }
        binding.messageFragmentToMainFragment.setOnClickListener{
            val action = MessagesFragmentDirections.actionMessagesFragmentToMainFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }

        binding.yourFriendsRecycleView.layoutManager = LinearLayoutManager(context)
        fetchUser()

        return binding.root

    }
    private fun fetchUser() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupieAdapter()
                snapshot.children.forEach {
                    Log.d("URL",it.toString())
                    val user = it.getValue(User::class.java)
                    if(user != null && user.uid != FirebaseAuth.getInstance().uid){
                        adapter.add(FriendItem(user))
                    }
                }
                adapter.setOnItemClickListener{ item, view ->
                    val friendItem = item as FriendItem
                    val action = MessagesFragmentDirections.actionMessagesFragmentToChatLogFragment(friendItem.user)
                    Navigation.findNavController(binding.root).navigate(action)

                }
                binding.yourFriendsRecycleView.adapter = adapter
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    class FriendItem( val user: User): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.findViewById<TextView>(R.id.lastMeassageTextView).text = user.description
            viewHolder.itemView.findViewById<TextView>(R.id.usernameTextView).text = user.username
            val img: ImageView = viewHolder.itemView.findViewById(R.id.userPictureImageView)
            Picasso.get().load(user.profileImageUrl).into(img)

        }

        override fun getLayout(): Int {
            return R.layout.firends_item
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this,viewLifecycleOwner)
    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.application_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem) : Boolean {
        when(menuItem.itemId){
            R.id.menuSignOut->{
                FirebaseAuth.getInstance().signOut()
                val action = MessagesFragmentDirections.actionMessagesFragmentToLoginFragment()
                Navigation.findNavController(binding.root).navigate(action)
            }
        }
        return false
    }

    private fun verifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val action = MessagesFragmentDirections.actionMessagesFragmentToRegisterFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }
    }


}