import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chaitany.agewell.ChatMessage
import com.chaitany.agewell.R
import com.chaitany.agewell.com.chaitany.agewell.DataStorer
import kotlin.random.Random

class ChatAdapter(private val chatList: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userTextView: TextView = view.findViewById(R.id.userTextView)
        val messageTextView: TextView = view.findViewById(R.id.messageTextView)
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val userImageView: ImageView = view.findViewById(R.id.img_look)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        // Retrieve user details from SharedPreferences

        holder.userTextView.text = chat.mobile
        holder.messageTextView.text = chat.message
        holder.timeTextView.text = chat.time
        holder.dateTextView.text = chat.date

        // Array of drawable resource IDs
        val images = arrayOf(
            R.drawable.ani1, R.drawable.ani2, R.drawable.ani3,
            R.drawable.ani4, R.drawable.ani5, R.drawable.ani6,
            R.drawable.ani7, R.drawable.ani8, R.drawable.ani9
        )

        // Pick a random image
        val randomIndex = Random.nextInt(images.size)
        holder.userImageView.setImageResource(images[randomIndex])
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}
