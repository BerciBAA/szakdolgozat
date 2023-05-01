package com.example.szakdolgozatproject

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.szakdolgozatproject.databinding.FragmentProfileBinding
import com.example.szakdolgozatproject.models.Games
import com.example.szakdolgozatproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.util.*


class ProfileFragment : Fragment(), MenuProvider {

    private lateinit var binding : FragmentProfileBinding
    private var selectedPhotosUri: ArrayList<Uri>? = null
    private lateinit var user : User
    private var newSelectedPhotosUri: ArrayList<Uri>? = null
    private var adapter: GroupieAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false)
        verifyUserLoggedIn()
        (activity as AppCompatActivity).supportActionBar?.title = "Profile"

        selectedPhotosUri = ArrayList<Uri>()
        newSelectedPhotosUri = ArrayList<Uri>()
        binding.profileImageRecycleView.layoutManager = LinearLayoutManager(context)
        adapter = GroupieAdapter()
        getUserInformation()
        binding.profileImageRecycleView.adapter = adapter


        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                if (it != null) {
                    selectedPhotosUri?.add(it)
                    newSelectedPhotosUri?.add(it)
                    adapter!!.add(ImgItem(it,adapter!!))
                }
            })

        binding.uploadImagesButton.setOnClickListener {
            if(selectedPhotosUri!!.size < 5){
                getImage.launch("image/*")
            }
        }

        binding.SaveButton.setOnClickListener{
            uploadImageToFirebaseStorage()
        }

        binding.profileFragmentToMessagesFragment.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToMessagesFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }

        binding.profileFragemntButton.setOnClickListener{
            val action = ProfileFragmentDirections.actionProfileFragmentSelf()
            Navigation.findNavController(binding.root).navigate(action)
        }
        binding.profileFragmentToMainFragment.setOnClickListener{
            val action = ProfileFragmentDirections.actionProfileFragmentToMainFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }

        binding.leagueOfLegendsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            isItCheck(isChecked,"leagueoflegends")

        }
        binding.dotaSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            isItCheck(isChecked,"dota")
        }

        binding.counterStrikeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            isItCheck(isChecked,"counterstrikeglobaloffensive")

        }

        return binding.root
    }

    private fun isItCheck(checked: Boolean, game : String) {
        val uid = FirebaseAuth.getInstance().uid
        if(checked){
            val ref = FirebaseDatabase.getInstance().getReference("games/${game}/${uid}")
            ref.setValue(Games(uid.toString()))
        }else{
            val ref = FirebaseDatabase.getInstance().getReference("games/${game}/${uid}")
            ref.removeValue()
        }
    }

    private fun getUserInformation() {
        val uid = FirebaseAuth.getInstance().uid
        val userRef = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val imagesRef = FirebaseStorage.getInstance().getReference("/images/$uid")
        imagesRef.listAll().addOnSuccessListener {
            it.items.forEach{ item ->
                item.downloadUrl.addOnSuccessListener { photoUri ->
                    selectedPhotosUri?.add(photoUri)
                    adapter?.add(ImgItem(photoUri, adapter!!))
                }
            }
        }
        userRef.get().addOnSuccessListener {
            user = it.getValue(User::class.java)!!
            binding.usernameText.setText(user.username)
            binding.descriptionText.setText(user.description)
        }
    }

    private fun uploadImageToFirebaseStorage(){
        if(selectedPhotosUri?.isEmpty() == true){
            Toast.makeText(activity,"Please upload at least one photo",Toast.LENGTH_SHORT).show()
            return
        }
        if(binding.usernameText.text.isEmpty() || binding.descriptionText.text.isEmpty()){
            Toast.makeText(activity,"Please fill in the username field",Toast.LENGTH_SHORT).show()
            return
        }
        val uid = FirebaseAuth.getInstance().uid
        newSelectedPhotosUri?.forEach{ photoUri ->
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$uid/$filename")
            ref.putFile(photoUri).addOnSuccessListener {
                Toast.makeText(activity,"Upload picture done",Toast.LENGTH_SHORT).show()
                user.photoNumber++;
            }
        }
        saveUserToFirebaseDatabase(selectedPhotosUri?.get(0).toString(),user.photoNumber)
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String, photoNumber: Int){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/${uid}")
        val username = binding.usernameText.text.toString()
        val description = binding.descriptionText.text.toString()
        ref.setValue(User(uid, profileImageUrl,username,description,photoNumber)).addOnSuccessListener {
            Toast.makeText(activity,"Saved user data",Toast.LENGTH_SHORT ).show()
            newSelectedPhotosUri?.clear()
        }
    }

    private fun verifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val action = ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.application_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem) : Boolean {
        when(menuItem.itemId){
            R.id.menuSignOut->{
                FirebaseAuth.getInstance().signOut()
                val action = ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
                Navigation.findNavController(binding.root).navigate(action)
            }

        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this,viewLifecycleOwner)
    }


}


class ImgItem( private val uri: Uri, private val adapter : GroupieAdapter): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val img : ImageView = viewHolder.itemView.findViewById(R.id.userPicturesImageView)
        val deleteBtn : Button = viewHolder.itemView.findViewById(R.id.deleteItemButton)
        deleteBtn.setOnClickListener {
            val ref = FirebaseStorage.getInstance()
                .getReferenceFromUrl(uri.toString())
            ref.delete().addOnSuccessListener {
                Log.d("URL","image deleted")
            }
            adapter.remove(viewHolder.item)
        }

        Picasso.get().load(uri).into(img)
    }
    override fun getLayout(): Int {
        return R.layout.user_pictures
    }
}

