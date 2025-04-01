import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.orphans.OrphanReport
import com.example.orphans.R

class OrphanReportsAdapter : RecyclerView.Adapter<OrphanReportsAdapter.ReportViewHolder>() {

    private var reports = listOf<OrphanReport>()

    fun submitList(newReports: List<OrphanReport>) {
        reports = newReports
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_orphan_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount() = reports.size

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val townTextView: TextView = view.findViewById(R.id.text_town)
        private val photoImageView: ImageView = view.findViewById(R.id.image_photo)

        fun bind(report: OrphanReport) {
            townTextView.text = report.town

            if (!report.photoUrl.isNullOrEmpty()) {
                Glide.with(photoImageView.context)
                    .load(report.photoUrl)
                    .into(photoImageView)
            } else {
                photoImageView.setImageResource(R.drawable.placeholder_image) // Placeholder image
            }
        }
    }
}
