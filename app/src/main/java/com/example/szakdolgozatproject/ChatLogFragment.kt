package com.example.szakdolgozatproject

import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.szakdolgozatproject.databinding.FragmentChatLogBinding
import com.example.szakdolgozatproject.databinding.FragmentMessagesBinding
import com.example.szakdolgozatproject.models.ChatMessage
import com.example.szakdolgozatproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.GroupieViewHolder



class ChatLogFragment : Fragment(), MenuProvider {

    lateinit var binding : FragmentChatLogBinding
    private val args by navArgs<ChatLogFragmentArgs>()
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_chat_log,container,false)
        binding.recycleViewChatLog.adapter = adapter
        binding.recycleViewChatLog.layoutManager = LinearLayoutManager(context)
        (activity as AppCompatActivity).supportActionBar?.title = args.user.username

        listenForMessages()

        binding.sendButton.setOnClickListener{
            sendMessageButton()
        }

        binding.recycleViewChatLog.adapter = adapter
        return binding.root
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = args.user.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {

                    if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
                        Log.d("MESSAGE",chatMessage.text)
                        adapter.add(ChatFromItem(chatMessage.text, args.user))
                    }
                    else{
                        Log.d("MESSAGE",chatMessage.text)
                        adapter.add(ChatToItem(chatMessage.text,args.user))
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }

    private fun sendMessageButton() {

        val currentMessage = binding.enterMessagePlainText.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = args.user.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        ref.setValue(ChatMessage(ref.key!!,currentMessage,fromId.toString(),toId.toString(),System.currentTimeMillis()/1000)).addOnSuccessListener {
            Log.d("MESSAGE","Saved our massege: ${ref.key}")
        }
        toRef.setValue(ChatMessage(ref.key!!,currentMessage,fromId.toString(),toId.toString(),System.currentTimeMillis()/1000))
        binding.enterMessagePlainText.text.clear()
        binding.recycleViewChatLog.scrollToPosition(adapter.itemCount-1)
    }

    class ChatFromItem(private val messages: String, private val user: User): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val message: TextView = viewHolder.itemView.findViewById(R.id.rightTextView)
            val img: ImageView = viewHolder.itemView.findViewById(R.id.rightImgView)
            Picasso.get().load(user.profileImageUrl).into(img)
            message.text = messages
        }

        override fun getLayout(): Int {
            return R.layout.chat_row_right
        }
    }

    class ChatToItem(private val messages: String, private val user: User): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val message: TextView = viewHolder.itemView.findViewById(R.id.leftTextView)
            val img: ImageView = viewHolder.itemView.findViewById(R.id.leftChatImg)
            Picasso.get().load(user.profileImageUrl).into(img)
            message.text = messages
        }

        override fun getLayout(): Int {
            return R.layout.chat_row_left
        }

    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.userchat_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
            R.id.menuSignOut->{
                FirebaseAuth.getInstance().signOut()
                val action = ChatLogFragmentDirections.actionChatLogFragmentToLoginFragment()
                Navigation.findNavController(binding.root).navigate(action)
            }
            R.id.unfirend->{
                val uid = args.user.uid;
                val ref = FirebaseStorage.getInstance().getReference("/matches/$uid")
                ref.delete()
                val action = ChatLogFragmentDirections.actionChatLogFragmentToMessagesFragment()
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