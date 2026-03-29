package com.kanyandula.nyasa.ui.main.blog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.kanyandula.nyasa.databinding.LayoutBlogListItemBinding
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.DateUtils

class BlogListAdapter(
    private val requestManager: RequestManager,
    private val interaction: Interaction? = null
) : PagingDataAdapter<BlogPost, BlogListAdapter.BlogViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val binding = LayoutBlogListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BlogViewHolder(binding, requestManager, interaction)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class BlogViewHolder(
        private val binding: LayoutBlogListItemBinding,
        private val requestManager: RequestManager,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlogPost) {
            binding.root.setOnClickListener {
                val position = this@BlogViewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    interaction?.onItemSelected(position, item)
                }
            }

            binding.apply {
                requestManager
                    .load(item.image)
                    .transition(withCrossFade())
                    .into(blogImage)
                blogTitle.text = item.title
                blogAuthor.text = item.username
                blogUpdateDate.text = DateUtils.convertLongToStringDate(item.date_updated)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: BlogPost)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BlogPost>() {
            override fun areItemsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
                return oldItem.pk == newItem.pk
            }

            override fun areContentsTheSame(oldItem: BlogPost, newItem: BlogPost): Boolean {
                return oldItem == newItem
            }
        }
    }
}
